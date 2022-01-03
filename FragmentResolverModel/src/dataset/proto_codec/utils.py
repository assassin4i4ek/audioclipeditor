import tensorflow as tf
import base64
from itertools import accumulate
from tensorflow.python.training.tracking.data_structures import NoDependency


class ProtoDecoderLayer(tf.keras.layers.Layer):
    def __init__(self, descriptor, **kwargs):
        super().__init__(**kwargs)
        # init proto descriptor
        if type(descriptor) is bytes:
            descriptor_bytes = descriptor
            serialized_descriptor = base64.b64encode(descriptor)\
                .decode('ascii')
        elif type(descriptor) is str:
            # must be base64 encoded
            descriptor_bytes = base64.b64decode(descriptor)
            serialized_descriptor = descriptor
        else:
            raise ValueError('Invalid serialized_descriptor value')

        self.serialized_descriptor = serialized_descriptor
        self._binary_descriptor = b'bytes://' + descriptor_bytes

    def get_config(self):
        config = super().get_config()
        config.update({
            'descriptor': self.serialized_descriptor,
        })
        return config


class ProtoFragmentTransformerCodecLayer(ProtoDecoderLayer):
    def __init__(self, sample_rate, transformer_params_dict, descriptor, **kwargs):
        super().__init__(descriptor, **kwargs)
        self.sample_rate = sample_rate
        # init transformer codec
        self.transformer_params_dict = NoDependency(transformer_params_dict)
        self._types_list = list(transformer_params_dict.keys())
        self._proto_field_names = [
            tr_params
            for tr_type in self._types_list
            for tr_params in transformer_params_dict[tr_type].keys()
        ] + ['type']
        self.transformer_output_length = len(self._types_list) \
            + len(self._proto_field_names) - 1

        self._type_indices_in_transformer = list(accumulate(
            [0] + [
                1 + len(transformer_params_dict[tr_type])
                for tr_type in self._types_list[:-1]
            ]
        ))
        self._params_indices_in_transformer = [
            self._type_indices_in_transformer[i_tr_type] +
            1 + i_tr_param
            for i_tr_type, tr_type in enumerate(self._types_list)
            for i_tr_param in range(len(transformer_params_dict[tr_type]))
        ]

        self._normalizers_for_transformer = [
            normalizer for tr_type in self._types_list
            for normalizer in transformer_params_dict[tr_type].values()
        ]

        self._types_list = tf.constant(self._types_list)
        self._type_indices_in_transformer = tf.constant(
            self._type_indices_in_transformer
        )
        self._params_indices_in_transformer = tf.constant(
            self._params_indices_in_transformer
        )

    def get_config(self):
        config = super().get_config()
        config.update({
            'sample_rate': self.sample_rate,
            'transformer_params_dict': self.transformer_params_dict
        })
        return config

    @classmethod
    def from_config(cls, config):
        config['transformer_params_dict'] = {
            int(tr_type): {
                tr_param: tf.keras.layers.deserialize(normalizer)
            }
            for tr_type, tr_params in config['transformer_params_dict'].items()
            for tr_param, normalizer in tr_params.items()
        }
        return cls(**config)
