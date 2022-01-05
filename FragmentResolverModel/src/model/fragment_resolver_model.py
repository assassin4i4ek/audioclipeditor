import tensorflow as tf


class FragmentResolverModel(tf.keras.Model):
    def __init__(self, audio_requests, response, config, **kwargs):
        super().__init__(inputs=audio_requests, outputs=response, **kwargs)
        self.serialized_config = tf.constant(config.SerializeToString(), dtype=tf.string)

    @tf.function(input_signature=[tf.TensorSpec(shape=(None, 1), dtype=tf.string)])
    def resolve(self, fragment_resolver_model_requests):
        return {'fragment_resolver_model_responses': self(fragment_resolver_model_requests)}

    def __call__(self, *args, **kwargs):
        return super().__call__(*args, **kwargs)

    @tf.function(input_signature=[])
    def config(self):
        return {'config': self.serialized_config}
