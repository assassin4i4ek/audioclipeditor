import tensorflow as tf


class YoloOutputDecoderLayer(tf.keras.layers.Layer):
    def __init__(self, input_length, confidence_threshold, encoding_type, fragments_dtype, **kwargs):
        super().__init__(**kwargs)
        self.input_length = input_length
        self.confidence_threshold = confidence_threshold
        self.encoding_type = encoding_type
        self.fragments_dtype = fragments_dtype

    @tf.function
    def call(self, yolo_outputs_batch):
        decoded_fragments_batch = tf.map_fn(
            self.decode_fragments,
            yolo_outputs_batch,
            fn_output_signature=tf.RaggedTensorSpec(
                shape=[None, yolo_outputs_batch.shape[-1] - 1],
                dtype=yolo_outputs_batch.dtype,
                ragged_rank=0
            )
        )
        return decoded_fragments_batch

    @tf.function
    def decode_fragments(self, yolo_output):
        num_grid_cells = tf.shape(yolo_output)[0]

        yolo_output = tf.cast(yolo_output, dtype=self.fragments_dtype)

        grid_cells = tf.expand_dims(
            # tf.range(num_grid_cells, dtype=yolo_output.dtype),
            tf.cast(tf.range(num_grid_cells), dtype=yolo_output.dtype),
            axis=-1
        )
        fragments_mask = yolo_output[..., 0] >= self.confidence_threshold
        cell_binded_feature_coef = tf.cast(
            self.input_length / num_grid_cells, dtype=yolo_output.dtype
        )
        cell_unbinded_feature_coef = tf.cast(
            self.input_length, dtype=yolo_output.dtype

        )
        decoded_cell_binded_feature = tf.boolean_mask(
            (grid_cells + yolo_output[..., 1:2]) * cell_binded_feature_coef,
            fragments_mask
        )
        decoded_cell_unbinded_feature = tf.boolean_mask(
            yolo_output[..., 2:3] * cell_unbinded_feature_coef,
            fragments_mask
        )

        fragment_starts, fragment_ends = self._convert_features_to_fragment_bounds(
            decoded_cell_binded_feature, decoded_cell_unbinded_feature
        )
        fragment_classes = tf.boolean_mask(
            yolo_output[..., 3:],
            fragments_mask
        )
        fragments = tf.concat([
            fragment_starts,
            fragment_ends,
            fragment_classes
        ], axis=-1)

        return fragments

    @tf.function
    def _convert_features_to_fragment_bounds(
            self, decoded_cell_binded_feature,
            decoded_cell_unbinded_feature):
        if self.encoding_type == 'START_DURATION':
            # decoded_cell_binded_feature is fragment start itself
            fragment_starts = decoded_cell_binded_feature
            # decoded_cell_unbinded_feature is fragment duration
            fragment_duration = decoded_cell_unbinded_feature
            fragment_ends = fragment_starts + fragment_duration
        elif self.encoding_type == 'CENTER_DURATION':
            # decoded_cell_binded_feature is fragment center
            fragment_centers = decoded_cell_binded_feature
            # decoded_cell_unbinded_feature is fragment duration
            fragment_duration = decoded_cell_unbinded_feature
            fragment_starts = fragment_centers - fragment_duration / 2
            fragment_ends = fragment_centers + fragment_duration / 2
        else:
            raise ValueError('Unsupported value for self.encoding_type')

        fragment_starts = tf.round(fragment_starts)
        fragment_ends = tf.round(fragment_ends)

        return fragment_starts, fragment_ends

    def get_config(self):
        config = super().get_config()
        config.update({
            'input_length': self.input_length,
            'confidence_threshold': self.confidence_threshold,
            'encoding_type': self.encoding_type,
            'fragments_dtype': self.fragments_dtype
        })
        return config


class YoloOutputBatchDecoderLayer(YoloOutputDecoderLayer):
    @tf.function
    def call(self, yolo_output_frames_batch, frame_offsets_samples_batch):
        adjusted_fragments_batch = tf.map_fn(
            self._decode_yolo_output_frames_map_fn_alias,
            (yolo_output_frames_batch, frame_offsets_samples_batch),
            fn_output_signature=tf.RaggedTensorSpec(
                shape=[None, None, yolo_output_frames_batch.shape[-1] - 1],
                dtype=yolo_output_frames_batch.dtype, ragged_rank=1
            )
        )
        return adjusted_fragments_batch

    @tf.function
    def _decode_yolo_output_frames_map_fn_alias(self, yolo_output_frames_with_frame_offsets):
        yolo_output_frames, frame_offsets_samples = yolo_output_frames_with_frame_offsets
        return self.decode_yolo_output_frames(yolo_output_frames, frame_offsets_samples)

    @tf.function
    def decode_yolo_output_frames(self, yolo_output_frames, frame_offsets_samples):
        decoded_fragments_frames = super().call(yolo_output_frames)
        frame_offsets_samples_floatx = tf.cast(
            frame_offsets_samples,
            dtype=decoded_fragments_frames.dtype
        )
        adjusted_fragments_frames = tf.map_fn(
            self._shift_by_offset,
            (decoded_fragments_frames, frame_offsets_samples_floatx),
            fn_output_signature=tf.RaggedTensorSpec(
                shape=[None, decoded_fragments_frames.shape[-1]],
                dtype=decoded_fragments_frames.dtype,
                ragged_rank=0
            )
        )
        return adjusted_fragments_frames

    @tf.function
    def _shift_by_offset(self, fragments_with_offset):
        fragments, offset = fragments_with_offset
        new_fragments = tf.concat([
            fragments[..., :2] + offset, fragments[..., 2:]
        ], axis=-1)
        return new_fragments

    def get_config(self):
        config = super().get_config()
        return config
