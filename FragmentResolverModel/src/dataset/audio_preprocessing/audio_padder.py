import tensorflow as tf


class AudioDataPadderLayer(tf.keras.layers.Layer):
    def __init__(self, sample_rate, target_duration_sec,
                 last_gap_threshold_sec, default_padding_transformer=None, **kwargs):
        super().__init__(**kwargs)
        self.sample_rate = sample_rate
        self.target_duration_sec = target_duration_sec
        self.last_gap_threshold_sec = last_gap_threshold_sec

        self.target_duration_samples_int64 = tf.cast(
            tf.math.round(
                target_duration_sec * self.sample_rate
            ),
            dtype=tf.int64
        )

        self.last_gap_threshold_samples_int64 = tf.cast(
            tf.math.round(
                last_gap_threshold_sec * self.sample_rate
            ),
            dtype=tf.int64
        )
        self.default_padding_transformer = default_padding_transformer

    @tf.function
    def call(self, ragged_samples):
        ragged_padded_samples = tf.map_fn(
            self.pad_samples,
            ragged_samples,
            fn_output_signature=tf.RaggedTensorSpec(
                shape=[None, 1], dtype=ragged_samples.dtype, ragged_rank=0
            )
        )

        return ragged_padded_samples

    @tf.function
    def pad_samples(self, samples, fragments=None):
        total_duration_samples_int64 = tf.shape(samples, out_type=tf.int64)[0]
        padding_duration_samples_int64 = self.target_duration_samples_int64 - \
            total_duration_samples_int64

        if padding_duration_samples_int64 > 0:
            padded_samples = tf.pad(
                samples,
                [[0, padding_duration_samples_int64], [0, 0]]
            )
        else:
            padded_samples = samples

        if fragments is not None and (padding_duration_samples_int64 > 0):
            new_fragments = self._update_fragments_after_padding(
                fragments, total_duration_samples_int64
            )
        else:
            new_fragments = fragments

        return self._prepare_output(padded_samples, new_fragments)

    @tf.function
    def _update_fragments_after_padding(self, fragments, total_duration_samples_int64):
        num_fragments = tf.shape(fragments)[0]
        target_duration_samples_floatx = tf.cast(
            self.target_duration_samples_int64,
            dtype=fragments.dtype
        )

        if num_fragments > 0:
            last_fragment_end_samples_int64 = tf.cast(
                fragments[-1, 1],
                dtype=tf.int64
            )
            last_gap_duration_samples_int64 = total_duration_samples_int64 - \
                last_fragment_end_samples_int64
        else:
            # the fragments are absent, a single fragment for padded area must be appended
            last_gap_duration_samples_int64 = self.last_gap_threshold_samples_int64

        if last_gap_duration_samples_int64 < self.last_gap_threshold_samples_int64:
            # update last fragment's end to match new padded duration
            last_fragment_start = fragments[-1, 0]
            new_last_fragment_end = target_duration_samples_floatx
            updated_last_fragment_bounds = [
                last_fragment_start, new_last_fragment_end
            ]
            updated_last_fragment = tf.concat([
                updated_last_fragment_bounds,
                fragments[-1, 2:]
            ], axis=0)
            new_fragments = tf.concat([
                fragments[:-1], [updated_last_fragment]
            ], axis=0)
        else:
            # create new fragment for the padded area
            total_duration_samples_floatx = tf.cast(
                total_duration_samples_int64, dtype=tf.keras.backend.floatx()
            )
            new_last_fragment = tf.concat([
                [total_duration_samples_floatx, target_duration_samples_floatx],
                self.default_padding_transformer
            ], axis=-1)
            new_fragments = tf.concat([
                fragments, [new_last_fragment]
            ], axis=0)

        return new_fragments

    @tf.function
    def _prepare_output(self, padded_samples, new_fragments):
        result = []
        result.append(padded_samples)

        if new_fragments is not None:
            result.append(new_fragments)

        return tuple(result) if len(result) > 1 else result[0]

    def get_config(self):
        config = super().get_config()
        config.update({
            'sample_rate': self.sample_rate,
            'target_duration_sec': self.target_duration_sec,
            'last_gap_threshold_sec': self.last_gap_threshold_sec
        })
        return config
