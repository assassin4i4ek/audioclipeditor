import tensorflow as tf
import tensorflow_io as tfio


class AudioDecoder:
    def __init__(self, target_sample_rate):
        self.target_sample_rate = target_sample_rate

    @tf.function(experimental_relax_shapes=True)
    def decode(self, audio_filepath):
        audio = tfio.audio.AudioIOTensor(
            audio_filepath, dtype=tf.float32
        )
        channels = audio.shape[1]
        samples = audio.to_tensor()
        # Use only channel 1
        if channels > 1:
            samples = samples[:, 0]
        # Resample to target sample rate
        if audio.rate != self.target_sample_rate:
            samples = tfio.audio.resample(
                samples,
                rate_in=tf.cast(audio.rate, tf.int64),
                rate_out=self.target_sample_rate
            )
            samples = tf.expand_dims(samples, axis=-1)

        samples_floatx = tf.cast(samples, dtype=tf.keras.backend.floatx())

        return samples_floatx
