import tensorflow as tf
import tensorflow.keras as K
import kapre


class YoloModel1(K.models.Sequential):
    def __init__(self, input_length, num_grid_cells, classes_shape):
        leaky_relu = K.layers.LeakyReLU(alpha=0.1)
        l2_regulizer = K.regularizers.l2(5e-4)
        max_pooling_size = 4

        def conv1d(filters, kernel_size, strides, activation=leaky_relu):
            return K.layers.Conv1D(filters, kernel_size, strides, padding='same', activation=activation, kernel_regularizer=l2_regulizer)

        def maxpooling1d(pool_size=max_pooling_size, strides=max_pooling_size):
            return K.layers.MaxPooling1D(pool_size, strides, padding='same')

        def batchnorm():
            return K.layers.BatchNormalization()

        layers = [
            K.layers.InputLayer((input_length, 1)),
            conv1d(filters=2, kernel_size=3, strides=1),
            batchnorm(),
            maxpooling1d(),
            conv1d(filters=4, kernel_size=3, strides=1),
            batchnorm(),
            maxpooling1d(),
            conv1d(filters=8, kernel_size=3, strides=1),
            batchnorm(),
            maxpooling1d(),
            conv1d(filters=16, kernel_size=3, strides=1),
            batchnorm(),
            maxpooling1d(),
            conv1d(filters=16, kernel_size=3, strides=1),
            batchnorm(),
            maxpooling1d(),
            conv1d(filters=16, kernel_size=3, strides=1),
            batchnorm(),
            maxpooling1d(),
            conv1d(filters=16, kernel_size=3, strides=1),
            batchnorm(),
            maxpooling1d(),

            conv1d(filters=32, kernel_size=3, strides=1),
            conv1d(filters=3 + classes_shape, kernel_size=3,
                   strides=1, activation='sigmoid'),
        ]

        K.models.Sequential.__init__(self, layers)
        self.input_length = input_length
        self.num_grid_cells = num_grid_cells
        self.classes_shape = classes_shape


def YoloModel2(input_length, num_grid_cells, classes_shape):
    leaky_relu = K.layers.LeakyReLU(alpha=0.1)
    l2_regulizer = K.regularizers.l2(5e-4)
    max_pooling_size = 2

    def conv1d(filters, kernel_size, strides, activation=leaky_relu):
        return K.layers.Conv1D(filters, kernel_size, strides, padding='same', activation=activation, kernel_regularizer=l2_regulizer)

    def maxpooling1d(pool_size=max_pooling_size, strides=max_pooling_size):
        return K.layers.MaxPooling1D(pool_size, strides, padding='same')

    def batchnorm():
        return K.layers.BatchNormalization()

    layers = [
        K.layers.InputLayer((input_length, 1)),
        conv1d(filters=2, kernel_size=7, strides=1),
        # batchnorm(),
        maxpooling1d(),
        conv1d(filters=2, kernel_size=3, strides=1),
        # conv1d(filters=1, kernel_size=1, strides=1),
        # conv1d(filters=2, kernel_size=3, strides=1),
        # batchnorm(),
        maxpooling1d(),
        conv1d(filters=4, kernel_size=3, strides=1),
        # conv1d(filters=2, kernel_size=1, strides=1),
        # conv1d(filters=4, kernel_size=3, strides=1),
        # batchnorm(),
        maxpooling1d(),
        conv1d(filters=4, kernel_size=3, strides=1),
        # conv1d(filters=2, kernel_size=1, strides=1),
        # conv1d(filters=4, kernel_size=3, strides=1),
        # batchnorm(),
        maxpooling1d(),
        conv1d(filters=8, kernel_size=3, strides=1),
        # conv1d(filters=4, kernel_size=1, strides=1),
        # conv1d(filters=8, kernel_size=3, strides=1),
        # batchnorm(),
        maxpooling1d(),
        conv1d(filters=8, kernel_size=3, strides=1),
        # conv1d(filters=4, kernel_size=1, strides=1),
        # conv1d(filters=8, kernel_size=3, strides=1),
        # batchnorm(),
        maxpooling1d(),
        conv1d(filters=16, kernel_size=3, strides=1),
        # conv1d(filters=8, kernel_size=1, strides=1),
        # conv1d(filters=16, kernel_size=3, strides=1),
        # batchnorm(),
        maxpooling1d(),
        conv1d(filters=16, kernel_size=3, strides=1),
        # conv1d(filters=8, kernel_size=1, strides=1),
        # conv1d(filters=16, kernel_size=3, strides=1),
        # batchnorm(),
        maxpooling1d(),
        conv1d(filters=32, kernel_size=3, strides=1),
        # conv1d(filters=16, kernel_size=1, strides=1),
        # conv1d(filters=32, kernel_size=3, strides=1),
        # batchnorm(),
        maxpooling1d(),
        conv1d(filters=32, kernel_size=3, strides=1),
        # conv1d(filters=16, kernel_size=1, strides=1),
        # conv1d(filters=32, kernel_size=3, strides=1),
        # batchnorm(),
        maxpooling1d(),
        conv1d(filters=64, kernel_size=3, strides=1),
        # conv1d(filters=32, kernel_size=1, strides=1),
        # conv1d(filters=64, kernel_size=3, strides=1),
        # batchnorm(),
        maxpooling1d(),
        conv1d(filters=64, kernel_size=3, strides=1),
        # conv1d(filters=32, kernel_size=1, strides=1),
        # conv1d(filters=64, kernel_size=3, strides=1),
        # batchnorm(),
        maxpooling1d(),
        conv1d(filters=128, kernel_size=3, strides=1),
        # conv1d(filters=64, kernel_size=1, strides=1),
        # conv1d(filters=128, kernel_size=3, strides=1),
        # batchnorm(),
        maxpooling1d(),
        conv1d(filters=128, kernel_size=3, strides=1),
        # conv1d(filters=64, kernel_size=1, strides=1),
        # conv1d(filters=128, kernel_size=3, strides=1),
        # batchnorm(),
        maxpooling1d(),
        conv1d(filters=256, kernel_size=3, strides=1),
        # conv1d(filters=128, kernel_size=1, strides=1),
        # conv1d(filters=256, kernel_size=3, strides=1),
        # batchnorm(),
        maxpooling1d(),

        conv1d(filters=64, kernel_size=3, strides=1),
        conv1d(filters=16, kernel_size=3, strides=1),
        conv1d(filters=3 + classes_shape, kernel_size=3,
               strides=1, activation='sigmoid'),
    ]

    return K.models.Sequential(layers)

    # def get_config(self):
    #     return {
    #         'input_length': self.input_length,
    #         'num_grid_cells': self.num_grid_cells,
    #         'classes_shape': self.classes_shape
    #     }

    # @classmethod
    # def from_config(cls, config):
    #     cls(**config)


def YoloModel3(input_length, num_grid_cells, classes_shape):
    leaky_relu = K.layers.LeakyReLU(alpha=0.1)
    l2_regulizer = K.regularizers.l2(5e-4)
    max_pooling_size = 2

    def conv1d(filters, kernel_size, strides, activation=leaky_relu):
        return K.layers.Conv1D(filters, kernel_size, strides, padding='same', activation=activation, kernel_regularizer=l2_regulizer, kernel_initializer=K.initializers.RandomUniform(minval=-1., maxval=1., seed=None))

    def maxpooling1d(pool_size=max_pooling_size, strides=max_pooling_size):
        return K.layers.MaxPooling1D(pool_size, strides, padding='same')

    def batchnorm():
        return K.layers.BatchNormalization()

    layers = [
        K.layers.InputLayer((input_length, 1)),
        # conv1d(filters=2, kernel_size=7, strides=1),
        # batchnorm(),
        # maxpooling1d(),
        # conv1d(filters=2, kernel_size=3, strides=1),
        # conv1d(filters=1, kernel_size=1, strides=1),
        conv1d(filters=2, kernel_size=21, strides=1),
        conv1d(filters=2, kernel_size=7, strides=1),
        # batchnorm(),
        maxpooling1d(),
        conv1d(filters=4, kernel_size=3, strides=1),
        # conv1d(filters=2, kernel_size=1, strides=1),
        # conv1d(filters=4, kernel_size=3, strides=1),
        # batchnorm(),
        maxpooling1d(),
        conv1d(filters=4, kernel_size=3, strides=1),
        # conv1d(filters=2, kernel_size=1, strides=1),
        # conv1d(filters=4, kernel_size=3, strides=1),
        # batchnorm(),
        maxpooling1d(),
        conv1d(filters=8, kernel_size=3, strides=1),
        # conv1d(filters=4, kernel_size=1, strides=1),
        # conv1d(filters=8, kernel_size=3, strides=1),
        # batchnorm(),
        maxpooling1d(),
        conv1d(filters=8, kernel_size=3, strides=1),
        # conv1d(filters=4, kernel_size=1, strides=1),
        # conv1d(filters=8, kernel_size=3, strides=1),
        # batchnorm(),
        maxpooling1d(),
        conv1d(filters=16, kernel_size=3, strides=1),
        # conv1d(filters=8, kernel_size=1, strides=1),
        # conv1d(filters=16, kernel_size=3, strides=1),
        # batchnorm(),
        maxpooling1d(),
        conv1d(filters=16, kernel_size=3, strides=1),
        # conv1d(filters=8, kernel_size=1, strides=1),
        # conv1d(filters=16, kernel_size=3, strides=1),
        # batchnorm(),
        maxpooling1d(),
        conv1d(filters=32, kernel_size=3, strides=1),
        # conv1d(filters=16, kernel_size=1, strides=1),
        # conv1d(filters=32, kernel_size=3, strides=1),
        # batchnorm(),
        maxpooling1d(),
        conv1d(filters=32, kernel_size=3, strides=1),
        # conv1d(filters=16, kernel_size=1, strides=1),
        # conv1d(filters=32, kernel_size=3, strides=1),
        # batchnorm(),
        maxpooling1d(),
        conv1d(filters=64, kernel_size=3, strides=1),
        # conv1d(filters=32, kernel_size=1, strides=1),
        # conv1d(filters=64, kernel_size=3, strides=1),
        # batchnorm(),
        maxpooling1d(),
        conv1d(filters=64, kernel_size=3, strides=1),
        # conv1d(filters=32, kernel_size=1, strides=1),
        # conv1d(filters=64, kernel_size=3, strides=1),
        # batchnorm(),
        maxpooling1d(),
        conv1d(filters=128, kernel_size=3, strides=1),
        # conv1d(filters=64, kernel_size=1, strides=1),
        # conv1d(filters=128, kernel_size=3, strides=1),
        # batchnorm(),
        maxpooling1d(),
        conv1d(filters=128, kernel_size=3, strides=1),
        # conv1d(filters=64, kernel_size=1, strides=1),
        # conv1d(filters=128, kernel_size=3, strides=1),
        # batchnorm(),
        maxpooling1d(),
        conv1d(filters=256, kernel_size=3, strides=1),
        # conv1d(filters=128, kernel_size=1, strides=1),
        # conv1d(filters=256, kernel_size=3, strides=1),
        # batchnorm(),
        # maxpooling1d(),

        conv1d(filters=128, kernel_size=3, strides=1),
        conv1d(filters=64, kernel_size=3, strides=1),
        conv1d(filters=32, kernel_size=3, strides=1),
        conv1d(filters=16, kernel_size=3, strides=1),
        conv1d(filters=8, kernel_size=3, strides=1),
        conv1d(filters=3 + classes_shape, kernel_size=3,
               strides=1, activation='sigmoid'),
    ]

    return K.models.Sequential(layers)


def YoloModel4(input_length, num_grid_cells, classes_shape):
    leaky_relu = K.layers.LeakyReLU(alpha=0.1)
    l2_regulizer = K.regularizers.l2(5e-4)
    max_pooling_size = 2

    def conv2d(filters, kernel_size, strides, activation=leaky_relu):
        if kernel_size is int:
            kernel_size = (kernel_size, kernel_size)
        if strides is int:
            strides = (strides, strides)
        return K.layers.Conv2D(filters, kernel_size=kernel_size, strides=strides, padding='same', activation=activation)

    def conv1d(filters, kernel_size, strides, activation=leaky_relu):
        return K.layers.Conv1D(filters, kernel_size, strides, padding='same', activation=activation, kernel_regularizer=l2_regulizer, kernel_initializer=K.initializers.RandomUniform(minval=-1., maxval=1., seed=None))

    def maxpooling2d(pool_size=max_pooling_size, strides=max_pooling_size):
        if pool_size is int:
            pool_size = (pool_size, pool_size)
        if strides is int:
            strides = (strides, strides)
        return K.layers.MaxPooling2D(pool_size=pool_size, strides=strides, padding='same')

    def maxpooling1d(pool_size=max_pooling_size, strides=max_pooling_size):
        return K.layers.MaxPooling1D(pool_size, strides, padding='same')

    def batchnorm():
        return K.layers.BatchNormalization()

    def norm():
        return K.layers.LayerNormalization()

    layers = [
        K.layers.InputLayer((input_length, 1)),
        kapre.STFT(
            n_fft=63,
            pad_begin=True,
            pad_end=True,
            win_length=1024,
            hop_length=16,
        ),
        kapre.Magnitude(),
        kapre.MagnitudeToDecibel(),
        conv2d(2, (1, 63), 1),
        conv2d(2, 3, 1),
        maxpooling2d((2, 1), (2, 1)),
        conv2d(4, 3, 1),
        maxpooling2d((2, 1), (2, 1)),
        conv2d(8, 3, 1),
        maxpooling2d((2, 1), (2, 1)),
        conv2d(16, 3, 1),
        maxpooling2d((2, 1), (2, 1)),
        conv2d(32, 3, 1),
        maxpooling2d(),
        conv2d(64, 3, 1),
        maxpooling2d(),
        conv2d(128, 3, 1),
        maxpooling2d(),
        conv2d(512, 3, 1),
        maxpooling2d(2, 2),
        conv2d(512, 3, 1),
        conv2d(512, 3, 1),
        conv2d(512, 3, 1),
        maxpooling2d(2, 2),
        conv2d(512, 3, 1),
        conv2d(1024, 3, 1),
        conv2d(2048, 3, 1),

        conv2d(filters=3 + classes_shape, kernel_size=1,
               strides=1, activation='sigmoid'),
        K.layers.Lambda(lambda x: tf.squeeze(x, axis=2)),

        # conv1d(filters=3 + classes_shape, kernel_size=3,
        #    strides=1, activation='sigmoid'),
        # conv1d(192, 7, 1),
        # maxpooling1d(),
        # conv1d(128, 5, 1),
        # maxpooling1d(),
        # conv1d(64, 3, 1),
        # maxpooling1d(),
        # conv1d(32, 3, 1),
        # maxpooling1d(),
        # conv1d(16, 3, 1),
        # maxpooling1d(),
        # conv1d(8, 3, 1),
        # maxpooling1d(),
        # conv1d(filters=3 + classes_shape, kernel_size=3,
        #        strides=1, activation='sigmoid'),
    ]

    return K.models.Sequential(layers)


def YoloModel5(input_length, num_grid_cells, classes_shape):
    leaky_relu = K.layers.LeakyReLU(alpha=0.1)
    l2_regulizer = K.regularizers.l2(5e-4)
    max_pooling_size = 2

    def conv2d(filters, kernel_size, strides, activation=leaky_relu):
        if kernel_size is int:
            kernel_size = (kernel_size, kernel_size)
        if strides is int:
            strides = (strides, strides)
        return K.layers.Conv2D(filters, kernel_size=kernel_size, strides=strides, padding='same', activation=activation)

    def conv1d(filters, kernel_size, strides, activation=leaky_relu):
        return K.layers.Conv1D(filters, kernel_size, strides, padding='same', activation=activation, kernel_regularizer=l2_regulizer, kernel_initializer=K.initializers.RandomUniform(minval=-1., maxval=1., seed=None))

    def maxpooling2d(pool_size=max_pooling_size, strides=max_pooling_size):
        if pool_size is int:
            pool_size = (pool_size, pool_size)
        if strides is int:
            strides = (strides, strides)
        return K.layers.MaxPooling2D(pool_size=pool_size, strides=strides, padding='same')

    def maxpooling1d(pool_size=max_pooling_size, strides=max_pooling_size):
        return K.layers.MaxPooling1D(pool_size, strides, padding='same')

    def batchnorm():
        return K.layers.BatchNormalization()

    def norm():
        return K.layers.LayerNormalization()

    def residual(x, filters, kernel_size, strides, activation=leaky_relu):
        if kernel_size is int:
            kernel_size = (kernel_size, kernel_size)
        if strides is int:
            strides = (strides, strides)

        fx = K.layers.Conv2D(filters, kernel_size=kernel_size,
                             strides=strides, padding='same', activation=activation)(x)
        # fx = K.layers.BatchNormalization()(fx)
        fx = K.layers.Conv2D(filters, kernel_size=kernel_size,
                             strides=strides, padding='same')(fx)
        out = K.layers.Add()([x, fx])
        # out = K.layers.BatchNormalization()(out)
        out = activation(out)
        return out

    inp = K.layers.Input((input_length, 1))
    stft = kapre.STFT(n_fft=63, win_length=1024,
                      hop_length=16, pad_end=True, pad_begin=True)(inp)
    stft = kapre.Magnitude()(stft)
    stft = kapre.MagnitudeToDecibel()(stft)
    x = stft
    x = conv2d(1, (1, 63), 1)(x)
    x = residual(x, 2, 3, 1)
    x = maxpooling2d((2, 1), (2, 1))(x)
    x = conv2d(4, 3, 1)(x)
    x = residual(x, 4, 3, 1)
    x = maxpooling2d((2, 1), (2, 1))(x)
    x = conv2d(8, 3, 1)(x)
    x = residual(x, 8, 3, 1)
    x = maxpooling2d((2, 1), (2, 1))(x)
    x = conv2d(16, 3, 1)(x)
    x = residual(x, 16, 3, 1)
    x = maxpooling2d((2, 1), (2, 1))(x)
    x = conv2d(32, 3, 1)(x)
    x = residual(x, 32, 3, 1)
    x = maxpooling2d()(x)
    x = conv2d(64, 3, 1)(x)
    x = residual(x, 64, 3, 1)
    x = maxpooling2d()(x)
    x = conv2d(128, 3, 1)(x)
    x = residual(x, 128, 3, 1)
    x = maxpooling2d()(x)
    x = conv2d(256, 3, 1)(x)
    x = residual(x, 256, 3, 1)
    x = maxpooling2d()(x)
    x = conv2d(512, 3, 1)(x)
    x = residual(x, 512, 3, 1)
    x = maxpooling2d()(x)
    x = residual(x, 512, 3, 1)
    x = conv2d(512, 1, 1)(x)
    x = residual(x, 512, 3, 1)

    x = conv2d(filters=3 + classes_shape, kernel_size=3,
               strides=1, activation='sigmoid')(x)
    x = K.layers.Lambda(lambda x: tf.squeeze(x, axis=2))(x)

    return K.models.Model(inputs=inp, outputs=x)


def YoloModel6(input_length, num_grid_cells, classes_shape):
    leaky_relu = K.layers.LeakyReLU(alpha=0.1)
    l2_regulizer = K.regularizers.l2(5e-4)
    max_pooling_size = 2

    def conv2d(filters, kernel_size, strides, activation=leaky_relu):
        if kernel_size is int:
            kernel_size = (kernel_size, kernel_size)
        if strides is int:
            strides = (strides, strides)
        return K.layers.Conv2D(filters, kernel_size=kernel_size, strides=strides, padding='same', activation=activation)

    def conv1d(filters, kernel_size, strides, activation=leaky_relu):
        return K.layers.Conv1D(filters, kernel_size, strides, padding='same', activation=activation, kernel_regularizer=l2_regulizer, kernel_initializer=K.initializers.RandomUniform(minval=-1., maxval=1., seed=None))

    def maxpooling2d(pool_size=max_pooling_size, strides=max_pooling_size):
        if pool_size is int:
            pool_size = (pool_size, pool_size)
        if strides is int:
            strides = (strides, strides)
        return K.layers.MaxPooling2D(pool_size=pool_size, strides=strides, padding='same')

    def maxpooling1d(pool_size=max_pooling_size, strides=max_pooling_size):
        return K.layers.MaxPooling1D(pool_size, strides, padding='same')

    def batchnorm():
        return K.layers.BatchNormalization()

    def norm():
        return K.layers.LayerNormalization()

    def residual(x, filters, kernel_size, strides, activation=leaky_relu):
        if kernel_size is int:
            kernel_size = (kernel_size, kernel_size)
        if strides is int:
            strides = (strides, strides)

        fx = K.layers.Conv2D(filters, kernel_size=kernel_size,
                             strides=strides, padding='same', activation=activation)(x)
        # fx = K.layers.BatchNormalization()(fx)
        fx = K.layers.Conv2D(filters, kernel_size=kernel_size,
                             strides=strides, padding='same')(fx)
        out = K.layers.Add()([x, fx])
        # out = K.layers.BatchNormalization()(out)
        out = activation(out)
        return out

    def reorg(stride):
        return K.layers.Lambda(lambda x: tf.image.extract_patches(x, [1, stride, stride, 1], [1, stride, stride, 1], [1, 1, 1, 1], 'SAME'))

    inp = K.layers.Input((input_length, 1))
    stft = kapre.STFT(n_fft=63, win_length=1024,
                      hop_length=16, pad_end=True, pad_begin=True)(inp)
    stft = kapre.Magnitude()(stft)
    stft = kapre.MagnitudeToDecibel()(stft)
    x = stft
    x = conv2d(1, (1, 63), 1)(x)
    # x = residual(x, 1, 3, 1)
    x = maxpooling2d((2, 1), (2, 1))(x)
    x = conv2d(2, 3, 1)(x)
    # x = residual(x, 2, 3, 1)
    x = maxpooling2d((2, 1), (2, 1))(x)
    x = conv2d(2, 3, 1)(x)
    # x = residual(x, 2, 3, 1)
    x = maxpooling2d((2, 1), (2, 1))(x)
    x = conv2d(4, 3, 1)(x)
    # x = residual(x, 4, 3, 1)
    x = maxpooling2d((2, 1), (2, 1))(x)
    x = conv2d(4, 3, 1)(x)
    # x = residual(x, 4, 3, 1)
    x = maxpooling2d()(x)
    x = conv2d(4, 3, 1)(x)
    # x = residual(x, 8, 3, 1)
    x = maxpooling2d()(x)
    r3 = reorg(8)(x)
    x = conv2d(12, 3, 1)(x)
    # x = residual(x, 16, 3, 1)
    x = maxpooling2d()(x)
    r2 = reorg(4)(x)
    x = conv2d(25, 3, 1)(x)
    # x = residual(x, 32, 3, 1)
    x = maxpooling2d()(x)
    r1 = reorg(2)(x)
    x = conv2d(50, 3, 1)(x)
    # x = residual(x, 64, 3, 1)
    x = maxpooling2d()(x)
    # x = residual(x, 64, 3, 1)
    x = conv2d(100, 3, 1)(x)
    x = K.layers.Add()([r1, x])
    # x = residual(x, 128, 3, 1)
    x = conv2d(192, 3, 1)(x)
    x = K.layers.Add()([r2, x])
    x = conv2d(256, 3, 1)(x)
    x = K.layers.Add()([r3, x])

    x = conv2d(filters=3 + classes_shape, kernel_size=3,
               strides=1, activation='sigmoid')(x)
    x = K.layers.Lambda(lambda x: tf.squeeze(x, axis=2))(x)

    return K.models.Model(inputs=inp, outputs=x)


def YoloModel7(input_length, num_grid_cells, classes_shape):
    leaky_relu = K.layers.LeakyReLU(alpha=0.1)
    l2_regulizer = K.regularizers.l2(5e-4)
    max_pooling_size = 2

    def conv2d(filters, kernel_size, strides, activation=leaky_relu):
        if kernel_size is int:
            kernel_size = (kernel_size, kernel_size)
        if strides is int:
            strides = (strides, strides)
        return K.layers.Conv2D(filters, kernel_size=kernel_size, strides=strides, padding='same', activation=activation)

    def conv1d(filters, kernel_size, strides, activation=leaky_relu):
        return K.layers.Conv1D(filters, kernel_size, strides, padding='same', activation=activation, kernel_regularizer=l2_regulizer, kernel_initializer=K.initializers.RandomUniform(minval=-1., maxval=1., seed=None))

    def maxpooling2d(pool_size=max_pooling_size, strides=max_pooling_size):
        if pool_size is int:
            pool_size = (pool_size, pool_size)
        if strides is int:
            strides = (strides, strides)
        return K.layers.MaxPooling2D(pool_size=pool_size, strides=strides, padding='same')

    def maxpooling1d(pool_size=max_pooling_size, strides=max_pooling_size):
        return K.layers.MaxPooling1D(pool_size, strides, padding='same')

    def batchnorm():
        return K.layers.BatchNormalization()

    def norm():
        return K.layers.LayerNormalization()

    def residual(x, filters, kernel_size, strides, activation=leaky_relu):
        if kernel_size is int:
            kernel_size = (kernel_size, kernel_size)
        if strides is int:
            strides = (strides, strides)

        fx = K.layers.Conv2D(filters, kernel_size=kernel_size,
                             strides=strides, padding='same', activation=activation)(x)
        # fx = K.layers.BatchNormalization()(fx)
        fx = K.layers.Conv2D(filters, kernel_size=kernel_size,
                             strides=strides, padding='same')(fx)
        out = K.layers.Add()([x, fx])
        # out = K.layers.BatchNormalization()(out)
        out = activation(out)
        return out

    def reorg(stride):
        return K.layers.Lambda(lambda x: tf.image.extract_patches(x, [1, stride, stride, 1], [1, stride, stride, 1], [1, 1, 1, 1], 'SAME'))

    def cast(dtype):
        return K.layers.Lambda(lambda x: tf.cast(x, dtype))

    inp = K.layers.Input((input_length, 1))
    x = inp
    x = cast(tf.float32)(x)
    stft = kapre.STFT(n_fft=63, win_length=1024,
                      hop_length=16, pad_end=True, pad_begin=True)(x)
    stft = kapre.Magnitude()(stft)
    stft = kapre.MagnitudeToDecibel()(stft)
    stft = cast(tf.keras.backend.floatx())(stft)
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
    r3 = reorg(8)(x)
    x = conv2d(16, 3, 1)(x)
    x = maxpooling2d()(x)
    r2 = reorg(4)(x)
    x = conv2d(32, 3, 1)(x)
    x = maxpooling2d()(x)
    r1 = reorg(2)(x)
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
    x = K.layers.Lambda(lambda x: tf.squeeze(x, axis=-2))(x)

    if num_grid_cells != x.shape[-2]:
        raise ValueError(
            f"num_grid_cells {num_grid_cells} doesn't match actual output shape {x.shape}")
    return K.models.Model(inputs=inp, outputs=x)


def YoloModel8(input_length, num_grid_cells, classes_shape):
    leaky_relu = K.layers.LeakyReLU(alpha=0.1)
    l2_regulizer = K.regularizers.l2(5e-4)
    max_pooling_size = 2

    def conv2d(filters, kernel_size, strides, activation=leaky_relu):
        if kernel_size is int:
            kernel_size = (kernel_size, kernel_size)
        if strides is int:
            strides = (strides, strides)
        return K.layers.Conv2D(filters, kernel_size=kernel_size, strides=strides, padding='same', activation=activation)

    def conv1d(filters, kernel_size, strides, activation=leaky_relu):
        return K.layers.Conv1D(filters, kernel_size, strides, padding='same', activation=activation, kernel_regularizer=l2_regulizer, kernel_initializer=K.initializers.RandomUniform(minval=-1., maxval=1., seed=None))

    def maxpooling2d(pool_size=max_pooling_size, strides=max_pooling_size):
        if pool_size is int:
            pool_size = (pool_size, pool_size)
        if strides is int:
            strides = (strides, strides)
        return K.layers.MaxPooling2D(pool_size=pool_size, strides=strides, padding='same')

    def maxpooling1d(pool_size=max_pooling_size, strides=max_pooling_size):
        return K.layers.MaxPooling1D(pool_size, strides, padding='same')

    def batchnorm():
        return K.layers.BatchNormalization()

    def norm():
        return K.layers.LayerNormalization()

    def residual(x, filters, kernel_size, strides, activation=leaky_relu):
        if kernel_size is int:
            kernel_size = (kernel_size, kernel_size)
        if strides is int:
            strides = (strides, strides)

        fx = K.layers.Conv2D(filters, kernel_size=kernel_size,
                             strides=strides, padding='same', activation=activation)(x)
        # fx = K.layers.BatchNormalization()(fx)
        fx = K.layers.Conv2D(filters, kernel_size=kernel_size,
                             strides=strides, padding='same')(fx)
        out = K.layers.Add()([x, fx])
        # out = K.layers.BatchNormalization()(out)
        out = activation(out)
        return out

    def reorg(stride):
        return K.layers.Lambda(lambda x: tf.image.extract_patches(x, [1, stride, stride, 1], [1, stride, stride, 1], [1, 1, 1, 1], 'SAME'))

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
    x = conv2d(8, 3, 1)(x)
    x = maxpooling2d()(x)
    x = conv2d(16, 3, 1)(x)
    x = maxpooling2d()(x)
    r3 = reorg(8)(x)
    x = conv2d(32, 3, 1)(x)
    x = maxpooling2d()(x)
    r2 = reorg(4)(x)
    x = conv2d(64, 3, 1)(x)
    x = maxpooling2d()(x)
    r1 = reorg(2)(x)
    x = conv2d(128, 3, 1)(x)
    x = maxpooling2d()(x)
    x = conv2d(256, 3, 1)(x)
    x = K.layers.Add()([r1, x])
    x = conv2d(512, 3, 1)(x)
    x = K.layers.Add()([r2, x])
    x = conv2d(1024, 3, 1)(x)
    x = K.layers.Add()([r3, x])

    x = conv2d(filters=3 + classes_shape, kernel_size=3,
               strides=1, activation='sigmoid')(x)
    x = K.layers.Lambda(lambda x: tf.squeeze(x, axis=2))(x)

    return K.models.Model(inputs=inp, outputs=x)
