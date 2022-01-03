import tensorflow as tf
from .utils import ProtoFragmentTransformerCodecLayer


class ProtoFragmentBatchEncoderLayer(ProtoFragmentTransformerCodecLayer):
    def __init__(self, sample_rate, transformer_params_dict, descriptor, out_dtype, **kwargs):
        super().__init__(sample_rate, transformer_params_dict, descriptor, **kwargs)
        self.out_dtype = out_dtype
        self.output_length = 2 + self.transformer_output_length

    @tf.function
    def call(self, proto_audio_fragments_batch):
        ragged_encoded_fragments_batch = tf.map_fn(
            self.encode_fragments_to_tensor,
            proto_audio_fragments_batch,
            fn_output_signature=tf.TensorSpec(
                shape=[None, self.output_length], dtype=self.out_type
            )
        )
        return ragged_encoded_fragments_batch

    @tf.function
    def encode_fragments_to_tensor(self, proto_audio_fragments):
        _, audio_fragments_list = tf.io.decode_proto(
            proto_audio_fragments, 'FragmentResolverModelResponse',
            ['fragments'], [tf.string],
            descriptor_source=self._binary_descriptor
        )
        audio_fragments = audio_fragments_list[0]

        _, fragment_bounds_list = tf.io.decode_proto(
            audio_fragments, 'ResolvedFragment',
            ['startUs', 'endUs'],
            [tf.int64, tf.int64],
            descriptor_source=self._binary_descriptor
        )
        fragment_starts_us, fragment_ends_us = fragment_bounds_list
        fragment_starts_samples = tf.cast(
            fragment_starts_us, dtype=tf.float32
        ) / (1e6 / self.sample_rate)
        fragment_ends_samples = tf.cast(
            fragment_ends_us, dtype=tf.float32
        ) / (1e6 / self.sample_rate)

        _, fragment_transformers_list = tf.io.decode_proto(
            audio_fragments, 'ResolvedFragment',
            ['transformer'],
            [tf.string],
            descriptor_source=self._binary_descriptor
        )
        fragment_transformers = fragment_transformers_list[0]
        encoded_fragment_transformers = self.encode_transformers_to_tensor(
            fragment_transformers
        )

        encoded_fragments = tf.concat([
            tf.cast(fragment_starts_samples, dtype=self.out_dtype),
            tf.cast(fragment_ends_samples, dtype=self.out_dtype),
            tf.cast(encoded_fragment_transformers, dtype=self.out_dtype)
        ], axis=-1)

        return encoded_fragments

    @tf.function
    def encode_transformers_to_tensor(self, proto_transformers):
        _, transformers = tf.io.decode_proto(
            proto_transformers, 'ResolvedTransformer',
            self._proto_field_names,
            [normalizer.in_dtype for normalizer in self._normalizers_for_transformer] + [tf.int32],
            descriptor_source=self._binary_descriptor
        )

        transformers_norm_params = tf.stack([
            param_normalizer.normalize(tf.reshape(transformers_param, [-1]))
            for transformers_param, param_normalizer in
            zip(transformers[:-1], self._normalizers_for_transformer)
        ], axis=-1)

        types = tf.reshape(transformers[-1], [-1, 1])
        types_indices = tf.where(
            tf.equal(
                tf.tile(types, [1, len(self._types_list)]),
                self._types_list
            )
        )[..., -1]
        encoded_types = tf.one_hot(types_indices, len(self._types_list))

        unsorted_output = tf.concat([
            transformers_norm_params, encoded_types
        ], axis=-1)
        output_indices = tf.argsort(
            tf.concat([
                self._params_indices_in_transformer,
                self._type_indices_in_transformer,
            ], axis=-1)
        )
        sorted_output = tf.gather(
            unsorted_output, output_indices, axis=-1
        )

        return sorted_output

    def get_config(self):
        config = super().get_config()
        config.update({
            'out_dtype': self.out_dtype,
        })
        return config
