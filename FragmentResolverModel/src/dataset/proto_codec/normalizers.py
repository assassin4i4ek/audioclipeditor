import tensorflow as tf


class LinearTransformerNormalizerLayer(tf.keras.layers.Layer):
    def __init__(self, division_coef, in_dtype=tf.float32.name, out_dtype=tf.float32.name, **kwargs):
        super().__init__(**kwargs)
        self.division_coef = division_coef
        self._casted_division_coef = tf.cast(division_coef, dtype=out_dtype)
        self.in_dtype = in_dtype
        self.out_dtype = out_dtype

    @tf.function
    def normalize(self, value):
        casted_value = tf.cast(value, self.out_dtype)
        return casted_value / self._casted_division_coef

    @tf.function
    def restore(self, norm_value):
        casted_norm_value = tf.cast(norm_value, dtype=self.out_dtype)
        restored_value = casted_norm_value * self._casted_division_coef
        return tf.cast(restored_value, dtype=self.in_dtype)

    def get_config(self):
        config = super().get_config()
        config.update({
            'division_coef': self.division_coef,
            'in_dtype': self.in_dtype,
            'out_dtype': self.out_dtype
        })
        return config
