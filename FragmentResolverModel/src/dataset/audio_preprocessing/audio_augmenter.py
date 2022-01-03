import tensorflow as tf


class AudioDataShiftAugmenterLayer(tf.keras.layers.Layer):
    def __init__(self, sample_rate, max_shift_sec, seed=None, random_generator_state=None, **kwargs):
        super().__init__(**kwargs)
        self.sample_rate = sample_rate
        self.max_shift_sec = max_shift_sec
        self.seed = seed
        if random_generator_state:
            self._random_generator = tf.random.Generator\
                .from_state(*random_generator_state)
        elif seed:
            self._random_generator = tf.random.Generator.from_seed(seed)
        else:
            self._random_generator = tf.random.Generator.from_non_deterministic_state()

    @tf.function
    def call(self, ragged_samples):
        augmented_ragged_samples = tf.map_fn(
            self.augment_samples,
            ragged_samples,
            fn_output_signature=tf.RaggedTensorSpec(
                shape=[None, 1], dtype=ragged_samples.dtype, ragged_rank=0
            )
        )

        return augmented_ragged_samples

    @tf.function
    def augment_samples(self, samples, fragments=None):
        start_sample_int32 = self._random_generator.uniform(
            (1,), 0, int(self.sample_rate * self.max_shift_sec), tf.int32
        )[0]

        new_samples = samples[start_sample_int32:, ...]

        if fragments is not None:
            start_sample_floatx = tf.cast(
                start_sample_int32, dtype=fragments.dtype
            )
            new_fragment_bounds = tf.math.maximum(
                fragments[..., :2] - start_sample_floatx,
                0
            )
            new_fragments = tf.concat([
                new_fragment_bounds,
                fragments[..., 2:]
            ], axis=-1)
            filtered_fragments = new_fragments[
                new_fragments[..., 1] - new_fragments[..., 0] > 0
            ]

            new_fragments = filtered_fragments
        else:
            new_fragments = None

        return self._prepare_output(new_samples, new_fragments)

    @tf.function
    def _prepare_output(self, new_samples, new_fragments):
        result = []
        result.append(new_samples)

        if new_fragments is not None:
            result.append(new_fragments)

        return tuple(result) if len(result) > 1 else result[0]

    def get_config(self):
        config = super().get_config()
        config.update({
            'sample_rate': self.sample_rate,
            'max_shift_sec': self.max_shift_sec,
            'random_generator_state': [self._random_generator.state.numpy(), self._random_generator.algorithm]
        })
        return config
