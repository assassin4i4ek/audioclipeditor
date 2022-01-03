import tensorflow as tf


class YoloOutputValidatorLayer(tf.keras.layers.Layer):
    def call(self, yolo_outputs_batch):
        is_valid = tf.map_fn(
            self.is_valid,
            yolo_outputs_batch,
            fn_output_signature=tf.RaggedTensorSpec(
                shape=[1], dtype=tf.bool, ragged_rank=0
            )
        )
        return is_valid

    def is_valid(self, yolo_output):
        presence_indicators = yolo_output[..., 0]
        is_valid = tf.reduce_all(
            (presence_indicators == 0) | (presence_indicators == 1)
        )
        return is_valid
