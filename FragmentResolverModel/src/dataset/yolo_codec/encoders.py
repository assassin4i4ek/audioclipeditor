import tensorflow as tf


class YoloOutputEncoderLayer(tf.keras.layers.Layer):
    def __init__(self, num_grid_cells, input_length, encoding_type, validate, **kwargs):
        super().__init__(**kwargs)
        self.num_grid_cells = num_grid_cells
        self.input_length = input_length
        self.encoding_type = encoding_type
        self.validate = validate

    def call(self, fragments_batch):
        yolo_outputs_batch = tf.map_fn(
            self.encode,
            fragments_batch,
            fn_output_signature=tf.RaggedTensorSpec(
                shape=[None, self.num_grid_cells, fragments_batch.shape[-1]],
                dtype=tf.keras.backend.floatx(),
                ragged_rank=0
            )
        )
        return yolo_outputs_batch

    @tf.function
    def encode(self, fragments):
        norm_fragment_starts = fragments[..., 0:1] / self.input_length
        norm_fragment_ends = fragments[..., 1:2] / self.input_length

        norm_cell_binded_feature, norm_cell_unbinded_feature = self\
            ._convert_fragment_bounds_to_features(
                norm_fragment_starts, norm_fragment_ends
            )

        rel_cell_binded_feature = self.num_grid_cells * norm_cell_binded_feature
        # grid cell must be in range [0, self.num_grid_cells)
        cell_binded_feature_grid_cell = tf.clip_by_value(
            tf.floor(rel_cell_binded_feature),
            0.0, self.num_grid_cells - 1.0
        )
        encoded_cell_binded_feature = rel_cell_binded_feature - \
            cell_binded_feature_grid_cell

        cell_binded_feature_grid_cell_int32 = tf.cast(
            cell_binded_feature_grid_cell, dtype=tf.int32
        )
        fragment_probabilities = tf.ones(
            (tf.shape(fragments)[0], 1), dtype=fragments.dtype
        )
        encoded_fragments = tf.concat([
            fragment_probabilities,
            encoded_cell_binded_feature,
            norm_cell_unbinded_feature,
            fragments[..., 2:]
        ], axis=-1)
        yolo_output = tf.scatter_nd(
            cell_binded_feature_grid_cell_int32,
            encoded_fragments,
            (self.num_grid_cells, tf.shape(encoded_fragments)[-1])
        )

        tf.debugging.Assert(
            not self.validate or tf.reduce_all(yolo_output[:, 0] <= 1),
            [
                'grid cells of fragments might intersect:',
                cell_binded_feature_grid_cell, 'resulting output:',
                yolo_output
            ],
            summarize=-1
        )

        casted_yolo_output = tf.cast(
            yolo_output, dtype=tf.keras.backend.floatx()
        )

        return casted_yolo_output

    @tf.function
    def _convert_fragment_bounds_to_features(self, norm_fragment_starts, norm_fragment_ends):
        norm_fragment_duration = norm_fragment_ends - norm_fragment_starts

        if self.encoding_type == 'START_DURATION':
            norm_cell_binded_feature = norm_fragment_starts
            norm_cell_unbinded_feature = norm_fragment_duration
        elif self.encoding_type == 'CENTER_DURATION':
            norm_cell_binded_feature = (
                norm_fragment_starts + norm_fragment_ends
            ) / 2
            norm_cell_unbinded_feature = norm_fragment_duration
        else:
            raise ValueError('Unsupported value for self.encoding_type')

        return norm_cell_binded_feature, norm_cell_unbinded_feature

    def get_config(self):
        config = super().get_config()
        config.update({
            'num_grid_cells': self.num_grid_cells,
            'input_length': self.input_length,
            'encoding_type': self.encoding_type,
            'validate': self.validate
        })
        return config
