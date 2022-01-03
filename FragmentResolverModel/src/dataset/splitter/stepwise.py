import tensorflow as tf


class AudioDataStepwiseSplitterLayer(tf.keras.layers.Layer):
    def __init__(
            self, sample_rate, frame_duration_sec,
            frame_step_sec, return_num_frames, return_steps, **kwargs):
        super().__init__(**kwargs)
        self.sample_rate = sample_rate
        self.frame_duration_sec = frame_duration_sec
        self.frame_step_sec = frame_step_sec
        self.return_num_frames = return_num_frames
        self.return_steps = return_steps
        self.samples_in_frame_int32 = tf.cast(
            frame_duration_sec * sample_rate, dtype=tf.int32)
        self.samples_in_step_int32 = tf.cast(
            frame_step_sec * sample_rate, dtype=tf.int32)

    @tf.function
    def call(self, ragged_samples):
        samples_batch_size = ragged_samples.nrows(out_type=tf.int32)
        frame_length_samples_batch = tf.tile(
            [self.samples_in_frame_int32],
            [samples_batch_size]
        )
        step_length_samples_batch = tf.tile(
            [self.samples_in_step_int32],
            [samples_batch_size]
        )
        ragged_frames_of_samples = tf.map_fn(
            self._split_into_frames_map_fn_alias,
            (ragged_samples, frame_length_samples_batch, step_length_samples_batch),
            fn_output_signature=tf.RaggedTensorSpec(
                shape=[None, None, 1], dtype=ragged_samples.dtype, ragged_rank=0)
        )
        return ragged_frames_of_samples

    @tf.function
    def _split_into_frames_map_fn_alias(self, samples_with_frame_length_and_step):
        samples, frame_length_samples, step_length_samples = samples_with_frame_length_and_step
        return self.split_into_frames(
            samples,
            step_length_samples=frame_length_samples,
            sframe_length_samples=step_length_samples
        )

    @tf.function
    def split_into_frames(self, samples, fragments=None, step_length_samples=None, frame_length_samples=None):
        input_length_int64 = tf.shape(samples, out_type=tf.int64)[0]
        if step_length_samples is None:
            step_length_samples = self.samples_in_step_int32
        if frame_length_samples is None:
            frame_length_samples = self.samples_in_frame_int32

        frames_of_samples = tf.signal.frame(
            samples, frame_length_samples, step_length_samples, axis=0
        )

        if self.return_num_frames:
            num_frames = tf.shape(frames_of_samples, out_type=tf.int64)[0]
        else:
            num_frames = None

        if self.return_steps or fragments is not None:
            _frame_steps_samples_for_adjustment = tf.range(
                0, input_length_int64 -
                tf.cast(frame_length_samples, dtype=tf.int64) + 1,
                tf.cast(step_length_samples, dtype=tf.int64), dtype=tf.int64
            )
        else:
            _frame_steps_samples_for_adjustment = None

        if fragments is not None:
            adjusted_fragments = self._split_fragments_into_frames(
                fragments, _frame_steps_samples_for_adjustment, frame_length_samples
            )
        else:
            adjusted_fragments = None

        if self.return_steps:
            frame_steps_samples = _frame_steps_samples_for_adjustment
        else:
            frame_steps_samples = None

        return self._prepare_output(frames_of_samples, adjusted_fragments, num_frames, frame_steps_samples)

    @tf.function
    def _split_fragments_into_frames(self, fragments, frame_steps_samples, frame_length_samples):
        frame_starts_samples = tf.cast(
            frame_steps_samples, dtype=fragments.dtype
        )
        frame_ends_samples = frame_starts_samples + tf.cast(
            frame_length_samples, dtype=fragments.dtype
        )

        new_fragments = tf.map_fn(
            lambda bounds: self._get_adjusted_fragments_in_bounds(
                bounds[0], bounds[1], fragments
            ),
            (frame_starts_samples, frame_ends_samples),
            fn_output_signature=tf.RaggedTensorSpec(
                shape=[None, fragments.shape[-1]], dtype=fragments.dtype, ragged_rank=0
            )
        )

        return new_fragments

    @tf.function
    def _get_adjusted_fragments_in_bounds(self, frame_start, frame_end, fragments):
        fragments_bounds = fragments[:, :2]
        # any of the fragment bounds lies inside [frame_start, frame_and]
        frame_fragments_mask = tf.reduce_any(
            (fragments_bounds >= frame_start) & (fragments_bounds <= frame_end), axis=-1
        )
        fragments_in_frame = fragments[frame_fragments_mask]

        # if tf.math.count_nonzero(upper_fragments) % 2 == 1:
        #     frame_fragments = tf.concat(
        #         ([frame_start], frame_fragments), axis=0)
        # if tf.math.count_nonzero(lower_fragments) % 2 == 1:
        #     frame_fragments = tf.concat((frame_fragments, [frame_end]), axis=0)

        # Substract initial point of each frame from correspondent fragment
        adjusted_fragments_in_frame = tf.concat([
            fragments_in_frame[..., :2] - frame_start,
            fragments_in_frame[..., 2:]
        ], axis=-1)

        return adjusted_fragments_in_frame

    @tf.function
    def _prepare_output(self, frames_of_samples, adjusted_fragments, num_frames, frame_steps_samples):
        result = []

        if adjusted_fragments is not None:
            frames_of_samples = tf.data.Dataset.zip((
                tf.data.Dataset.from_tensor_slices(frames_of_samples),
                tf.data.Dataset.from_tensor_slices(adjusted_fragments)
            ))

        result.append(frames_of_samples)

        if num_frames is not None:
            result.append(num_frames)

        if frame_steps_samples is not None:
            if self.return_steps == 'SAMPLE':
                frame_steps = frame_steps_samples
            elif self.return_steps == 'SEC':
                frame_steps = tf.cast(
                    frame_steps_samples,
                    dtype=tf.float64
                ) / self.sample_rate
            else:
                raise ValueError('Unknown value of self.return_steps')
            result.append(frame_steps)

        return tuple(result) if len(result) > 1 else result[0]

    def get_config(self):
        config = super().get_config()
        config.update({
            'sample_rate': self.sample_rate,
            'frame_duration_sec': self.frame_duration_sec,
            'frame_step_sec': self.frame_step_sec,
            'return_num_frames': self.return_num_frames
        })
        return config
