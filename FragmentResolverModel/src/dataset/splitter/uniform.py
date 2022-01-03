import tensorflow as tf
from .stepwise import AudioDataStepwiseSplitterLayer


class AudioDataUniformSplitterLayer(AudioDataStepwiseSplitterLayer):
    def __init__(
            self, sample_rate,
            frame_duration_sec, min_overlap_duration_sec,
            max_ignored_gap_duration_sec, return_steps, **kwargs):
        super().__init__(
            sample_rate, frame_duration_sec,
            frame_duration_sec - min_overlap_duration_sec,
            True, return_steps, **kwargs
        )
        self.samples_in_frame_int64 = tf.cast(
            self.samples_in_frame_int32, dtype=tf.int64)

        self.max_step_duration_sec = self.frame_step_sec

        self.min_overlap_duration_sec = min_overlap_duration_sec
        self.max_ignored_gap_duration_sec = max_ignored_gap_duration_sec

        self.min_samples_in_overlap_int64 = tf.cast(
            min_overlap_duration_sec * sample_rate, dtype=tf.int64)
        self.max_samples_in_ignored_gap_int64 = tf.cast(
            max_ignored_gap_duration_sec * sample_rate, dtype=tf.int64)
        self.max_samples_in_step_int64 = self.samples_in_frame_int64 - \
            self.min_samples_in_overlap_int64

    @tf.function
    def call(self, ragged_samples):
        split_into_frames_output_signature = tf.RaggedTensorSpec(
            shape=[None, None, 1], dtype=ragged_samples.dtype, ragged_rank=0)
        if self.return_steps:
            if self.return_steps == 'SAMPLE':
                split_into_frames_output_signature = (
                    split_into_frames_output_signature,
                    tf.RaggedTensorSpec(
                        shape=[None], dtype=tf.int64, ragged_rank=0)
                )
            elif self.return_steps == 'SEC':
                split_into_frames_output_signature = (
                    split_into_frames_output_signature,
                    tf.RaggedTensorSpec(
                        shape=[None], dtype=tf.float64, ragged_rank=0)
                )
            else:
                raise ValueError('Unknown value of self.return_steps')

        ragged_frames_of_samples = tf.map_fn(
            self.split_into_frames,
            ragged_samples,
            fn_output_signature=split_into_frames_output_signature
        )

        if self.return_steps:
            ragged_frames_of_samples, frame_offsets = ragged_frames_of_samples
            return ragged_frames_of_samples, frame_offsets
        else:
            return ragged_frames_of_samples

    @tf.function
    def split_into_frames(self, samples, fragments=None):
        input_length_int64 = tf.shape(samples, out_type=tf.int64)[0]
        num_fully_placed_frames = 1 + (
            input_length_int64 - self.samples_in_frame_int64
        ) // self.max_samples_in_step_int64
        samples_in_free_gap = input_length_int64 - (
            (num_fully_placed_frames - 1) * self.max_samples_in_step_int64 +
            self.samples_in_frame_int64
        )
        is_invalid_gap = samples_in_free_gap > self.max_samples_in_ignored_gap_int64

        if is_invalid_gap:
            step_length_samples = tf.cast(
                tf.math.floor(
                    (input_length_int64 - self.samples_in_frame_int64) /
                    num_fully_placed_frames
                ),
                dtype=tf.int32
            )
            target_num_frames = num_fully_placed_frames + 1
        else:
            step_length_samples = tf.cast(
                self.max_samples_in_step_int64, dtype=tf.int32
            )
            target_num_frames = num_fully_placed_frames

        if self.return_steps:
            frames_of_samples, num_frames, frame_steps = super().split_into_frames(
                samples, fragments=fragments, step_length_samples=step_length_samples
            )
        else:
            frames_of_samples, num_frames = super().split_into_frames(
                samples, fragments=fragments, step_length_samples=step_length_samples
            )
            frame_steps = None

        tf.debugging.assert_equal(
            num_frames, target_num_frames,
            'stepwise splitter return unexpected number of frames'
        )

        return self._prepare_uniform_output(frames_of_samples, frame_steps)

    @tf.function
    def _prepare_uniform_output(self, frames_of_samples, frame_steps):
        result = []
        result.append(frames_of_samples)
        if self.return_steps:
            result.append(frame_steps)

        return tuple(result) if len(result) > 1 else result[0]

    def get_config(self):
        config = super(AudioDataStepwiseSplitterLayer, self).get_config()
        config.update({
            'sample_rate': self.sample_rate,
            'frame_duration_sec': self.frame_duration_sec,
            'min_overlap_duration_sec': self.min_overlap_duration_sec,
            'max_ignored_gap_duration_sec': self.max_ignored_gap_duration_sec,
            'return_steps': self.return_steps,
        })
        return config
