import tensorflow as tf
from .utils import ProtoDecoderLayer, ProtoFragmentTransformerCodecLayer


class ProtoFragmentBatchDecoderLayer(ProtoFragmentTransformerCodecLayer):
    @tf.function
    def call(self, ragged_fragments_batch):
        ragged_proto_fragments = tf.map_fn(
            self.decode_fragments_to_proto,
            ragged_fragments_batch,
            fn_output_signature=tf.TensorSpec(
                shape=[1], dtype=tf.string
            )
        )
        return ragged_proto_fragments

    @tf.function
    def decode_fragments_to_proto(self, encoded_fragments):
        decoded_transformer_protos = self.decode_transformers_to_proto(
            encoded_fragments[..., 2:]
        )
        fragment_starts_samples = encoded_fragments[..., 0:1]
        fragment_ends_samples = encoded_fragments[..., 1:2]
        fragment_starts_us = tf.cast(
            fragment_starts_samples, dtype=tf.float32
        ) * (1e6 / self.sample_rate)
        fragment_ends_us = tf.cast(
            fragment_ends_samples, dtype=tf.float32
        ) * (1e6 / self.sample_rate)

        fragment_sizes = tf.ones(
            (tf.shape(encoded_fragments)[0], 3), dtype=tf.int32
        )
        fragment_values = [
            tf.cast(fragment_starts_us, dtype=tf.int64),
            tf.cast(fragment_ends_us, dtype=tf.int64),
            tf.expand_dims(decoded_transformer_protos, axis=-1)
        ]
        decoded_fragment_protos = tf.io.encode_proto(
            fragment_sizes, fragment_values,
            ['startUs', 'endUs', 'transformer'],
            'ResolvedFragment', self._binary_descriptor
        )
        # expand shape of encoded_fragments_shape to make it
        # look like a single batch of fragments
        audio_sizes = tf.reshape(len(encoded_fragments), (1, -1))
        audio_values = [tf.expand_dims(decoded_fragment_protos, axis=0)]
        encoded_audio_proto = tf.io.encode_proto(
            audio_sizes, audio_values, ['fragments'],
            'FragmentResolverModelResponse', self._binary_descriptor
        )
        return encoded_audio_proto

    @tf.function
    def decode_transformers_to_proto(self, encoded_transformers):
        all_type_confidences = tf.gather(
            encoded_transformers, self._type_indices_in_transformer, axis=-1
        )
        max_confidence_type_indices = tf.argmax(
            all_type_confidences, output_type=tf.int32, axis=-1
        )
        transformer_type = tf.gather(
            self._types_list, max_confidence_type_indices
        )
        max_confidence_params_start_indices = 1 + tf.gather(
            self._type_indices_in_transformer, max_confidence_type_indices
        )
        max_confidence_params_end_indices = tf.where(
            max_confidence_type_indices +
            1 < len(self._type_indices_in_transformer),
            tf.gather(
                self._type_indices_in_transformer,
                tf.minimum(
                    max_confidence_type_indices + 1,
                    len(self._type_indices_in_transformer) - 1
                )
            ) - 1,
            tf.tile(
                [self._params_indices_in_transformer[-1]],
                [len(encoded_transformers)]
            )
        )

        proto_values = [None] * len(
            self._normalizers_for_transformer
        ) + [tf.expand_dims(transformer_type, axis=-1)]

        for i_transformer_param, normalizer in enumerate(self._normalizers_for_transformer):
            i_transformers_params = self._params_indices_in_transformer[i_transformer_param]
            transformers_params = encoded_transformers[
                ..., i_transformers_params
            ]
            proto_values[i_transformer_param] = tf.expand_dims(
                tf.where(
                    (
                        (
                            max_confidence_params_start_indices <= i_transformers_params
                        ) & (
                            i_transformers_params <= max_confidence_params_end_indices
                        )
                    ),
                    normalizer.restore(
                        transformers_params
                    ),
                    tf.zeros_like(
                        transformers_params, dtype=normalizer.in_dtype
                    )
                ),
                axis=-1
            )

        proto_sizes = tf.ones(
            [len(encoded_transformers), len(proto_values)],
            dtype=tf.int32
        )
        proto_transformer = tf.io.encode_proto(
            proto_sizes, proto_values, self._proto_field_names,
            'ResolvedTransformer', descriptor_source=self._binary_descriptor
        )

        return proto_transformer


class AudioProcessRequestDecoderLayer(ProtoDecoderLayer):
    @tf.function
    def call(self, audio_process_requests_batch):
        _, audio_requests_list = tf.io.decode_proto(
            audio_process_requests_batch, 'FragmentResolverModelRequest',
            ['sampleRate', 'audioSamplesChannel1'], [tf.int32, tf.string],
            descriptor_source=self._binary_descriptor
        )
        sample_rates = tf.reshape(audio_requests_list[0], [-1])
        audio_samples_raw = tf.reshape(audio_requests_list[1], [-1])
        audio_samples = tf.map_fn(
            self._decode_samples_from_raw,
            (audio_samples_raw, sample_rates),
            fn_output_signature=tf.RaggedTensorSpec(
                shape=[None, 1], dtype=tf.float32, ragged_rank=0
            )
        )
        return audio_samples

    @tf.function
    def _decode_samples_from_raw(self, raw_samples_with_sample_rate):
        raw_samples, sample_rate = raw_samples_with_sample_rate
        audio_samples = tf.io.decode_raw(raw_samples, tf.float32)
        return tf.expand_dims(audio_samples, -1)
