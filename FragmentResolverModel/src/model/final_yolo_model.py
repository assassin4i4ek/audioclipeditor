import tensorflow as tf
import tensorflow.keras as K
import kapre
from tensorflow.python.ops.ragged.ragged_tensor import RaggedTensorSpec


class YoloLayer(K.layers.Layer):
    def __init__(self, input_length, num_grid_cells, classes_shape, weights_path=None, **kwargs):
        super().__init__(**kwargs)
        self.input_length = input_length
        self.classes_shape = classes_shape
        self.num_grid_cells = num_grid_cells
        self.yolo_model = YoloModel(
            input_length, num_grid_cells, classes_shape)
        if weights_path:
            self.yolo_model.load_weights(weights_path)

    @tf.function
    def call(self, frames_of_samples):
        yolo_outputs = tf.map_fn(
            self.yolo_model.call,
            frames_of_samples,
            fn_output_signature=RaggedTensorSpec(
                shape=self.yolo_model.output_shape,
                dtype=self.yolo_model.output.dtype,
                ragged_rank=0
            )
        )
        return yolo_outputs

    def get_config(self):
        config = super().get_config()
        config.update({
            'input_length': self.input_length,
            'classes_shape': self.classes_shape,
            'num_grid_cells': self.num_grid_cells
        })
        return config


class ReorgLayer(K.layers.Layer):
    def __init__(self, stride):
        super().__init__()
        self.stride = stride

    @ tf.function
    def call(self, x):
        return tf.image.extract_patches(
            x, [1, self.stride, self.stride, 1],
            [1, self.stride, self.stride, 1],
            [1, 1, 1, 1], 'SAME'
        )

    def get_config(self):
        return {'stride': self.stride}


class SqueezeDimLayer(K.layers.Layer):
    def __init__(self, axis):
        super().__init__()
        self.axis = axis

    @ tf.function
    def call(self, x):
        return tf.squeeze(x, axis=self.axis)

    def get_config(self):
        return {'axis': self.axis}


def YoloModel(input_length, num_grid_cells, classes_shape):
    leaky_relu = K.layers.LeakyReLU(alpha=0.1)
    max_pooling_size = 2

    def conv2d(filters, kernel_size, strides, activation=leaky_relu):
        if kernel_size is int:
            kernel_size = (kernel_size, kernel_size)
        if strides is int:
            strides = (strides, strides)
        return K.layers.Conv2D(filters, kernel_size=kernel_size, strides=strides, padding='same', activation=activation)

    def maxpooling2d(pool_size=max_pooling_size, strides=max_pooling_size):
        if pool_size is int:
            pool_size = (pool_size, pool_size)
        if strides is int:
            strides = (strides, strides)
        return K.layers.MaxPooling2D(pool_size=pool_size, strides=strides, padding='same')

    inp = K.layers.Input((input_length, 1))
    stft = kapre.STFT(n_fft=63, win_length=1024,
                      hop_length=16, pad_end=True, pad_begin=True)(inp)
    stft = kapre.Magnitude()(stft)
    stft = kapre.MagnitudeToDecibel()(stft)
    x = stft
    x = conv2d(1, (1, 63), 1)(x)
    x = maxpooling2d((2, 1), (2, 1))(x)
    x = conv2d(2, 3, 1)(x)
    x = maxpooling2d((2, 1), (2, 1))(x)
    x = conv2d(2, 3, 1)(x)
    x = maxpooling2d((2, 1), (2, 1))(x)
    x = conv2d(4, 3, 1)(x)
    x = maxpooling2d((2, 1), (2, 1))(x)
    x = conv2d(4, 3, 1)(x)
    x = maxpooling2d()(x)
    x = conv2d(8, 3, 1)(x)
    x = maxpooling2d()(x)
    r3 = ReorgLayer(8)(x)
    x = conv2d(16, 3, 1)(x)
    x = maxpooling2d()(x)
    r2 = ReorgLayer(4)(x)
    x = conv2d(32, 3, 1)(x)
    x = maxpooling2d()(x)
    r1 = ReorgLayer(2)(x)
    x = conv2d(64, 3, 1)(x)
    x = maxpooling2d()(x)
    x = conv2d(128, 3, 1)(x)
    x = K.layers.Add()([r1, x])
    x = conv2d(256, 3, 1)(x)
    x = K.layers.Add()([r2, x])
    x = conv2d(512, 3, 1)(x)
    x = K.layers.Add()([r3, x])

    x = conv2d(filters=3 + classes_shape, kernel_size=3,
               strides=1, activation='sigmoid')(x)
    x = SqueezeDimLayer(-2)(x)

    if num_grid_cells != x.shape[-2]:
        raise ValueError(
            f"num_grid_cells {num_grid_cells} doesn't match actual output shape {x.shape}")

    return K.Model(inputs=inp, outputs=x)
