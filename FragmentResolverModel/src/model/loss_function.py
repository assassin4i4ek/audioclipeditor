import tensorflow as tf


class YoloLossFunction(tf.keras.losses.Loss):
    def __init__(self, num_grid_cells, samples_shape, lambdas, encoding_type, **kwargs):
        super().__init__(**kwargs)
        self.num_grid_cells = num_grid_cells
        self.samples_shape = samples_shape
        self.lambdas = None
        self.weights = None
        self._l_cell_bound_feature = tf.Variable(
            0.0, dtype=tf.keras.backend.floatx()
        )
        self._l_cell_unbound_feature = tf.Variable(
            0.0, dtype=tf.keras.backend.floatx()
        )
        self._l_is_obj = tf.Variable(0.0, dtype=tf.keras.backend.floatx())
        self._l_no_obj = tf.Variable(0.0, dtype=tf.keras.backend.floatx())
        self._l_class = tf.Variable(0.0, dtype=tf.keras.backend.floatx())
        self.set_lambdas(lambdas)
        self.encoding_type = encoding_type

    def set_lambdas(self, lambdas):
        self.lambdas = lambdas
        self._l_cell_bound_feature.assign(
            tf.cast(lambdas['l_cbf'], tf.keras.backend.floatx()))
        self._l_cell_unbound_feature.assign(
            tf.cast(lambdas['l_cubf'], tf.keras.backend.floatx()))
        self._l_is_obj.assign(
            tf.cast(lambdas['l_isobj'], tf.keras.backend.floatx()))
        self._l_no_obj.assign(
            tf.cast(lambdas['l_noobj'], tf.keras.backend.floatx()))
        self._l_class.assign(
            tf.cast(lambdas['l_class'], tf.keras.backend.floatx()))
        self.weights = tf.convert_to_tensor([
            self._l_cell_bound_feature, self._l_cell_unbound_feature,
            self._l_is_obj, self._l_no_obj, self._l_class
        ])

    def call(self, out_true, out_predicted):
        errors = self._compute_errors(out_true, out_predicted)
        total_error = tf.reduce_sum(self.weights * errors)
        return total_error

    @tf.function
    def _compute_errors(self, out_true, out_predicted):
        tf.debugging.Assert(
            tf.reduce_all(tf.math.is_finite(out_predicted)),
            [out_predicted],
            summarize=-1
        )

        presence_indicators = out_true[..., 0]
        encoded_cell_bound_feature_true = out_true[..., 1]
        norm_cell_unbound_feature_true = out_true[..., 2]
        classes_true = out_true[..., 3:]

        adjusted_encoded_cell_bound_feature_true, adjusted_norm_cell_unbound_feature_true = self._adjust_features(
            encoded_cell_bound_feature_true, norm_cell_unbound_feature_true
        )

        responsibility_weights = adjusted_norm_cell_unbound_feature_true / \
            norm_cell_unbound_feature_true
        responsibility_weights = tf.where(
            tf.math.is_nan(responsibility_weights),
            tf.zeros_like(responsibility_weights),
            responsibility_weights
        )
        responsibility_weights = tf.clip_by_value(
            responsibility_weights,
            tf.cast(0.0, dtype=responsibility_weights.dtype),
            tf.cast(1.0, dtype=responsibility_weights.dtype)
        )
        # tf.debugging.assert_near(
        #     responsibility_weights,
        #     tf.clip_by_value(
        #         responsibility_weights, 0.0, 1.0
        #     ),
        #     message='responsibility_weights happens to be not in [0, 1] interval\n',
        #     summarize=-1
        # )

        # 1. fragment cell bounded feature
        cell_bounded_feature_error = tf.reduce_sum(
            presence_indicators * responsibility_weights * (
                    adjusted_encoded_cell_bound_feature_true - out_predicted[..., 1]
            ) ** 2
        )
        # 2. fragment cell unbounded feature
        cell_unbounded_feature_error = tf.reduce_sum(
            presence_indicators * responsibility_weights * self.num_grid_cells * (
                    adjusted_norm_cell_unbound_feature_true - out_predicted[..., 2]
            ) ** 2
        )
        # 3. fragment presence
        presence_error = tf.reduce_sum(
            presence_indicators * responsibility_weights * (
                    presence_indicators - out_predicted[..., 0]
            ) ** 2
        )
        # 4. fragment absence
        absence_error = tf.reduce_sum(
            (1 - presence_indicators) * (
                    presence_indicators - out_predicted[..., 0]
            ) ** 2
        )
        # 5. classes error
        class_error = tf.reduce_sum(
            presence_indicators * responsibility_weights * tf.reduce_sum(
                classes_true - out_predicted[..., 3:], axis=-1
            ) ** 2
        )

        error_tensor = tf.stack([
            cell_bounded_feature_error, cell_unbounded_feature_error,
            presence_error, absence_error, class_error
        ])

        tf.debugging.Assert(
            tf.reduce_all(tf.math.is_finite(error_tensor)),
            ['error tensor contains nan\n', error_tensor],
            summarize=-1
        )

        return error_tensor

    @tf.function
    def _adjust_features(self, encoded_cell_bound_feature_true, norm_cell_unbound_feature_true):
        grid_cells = tf.range(
            self.num_grid_cells, dtype=tf.keras.backend.floatx()
        )

        if self.encoding_type == 'START_DURATION':
            encoded_fragment_starts = encoded_cell_bound_feature_true
            norm_fragment_durations = norm_cell_unbound_feature_true
            # adjust fragment starts to be >= 0
            adjusted_encoded_fragment_starts = tf.maximum(
                tf.cast(0.0, dtype=tf.keras.backend.floatx()),
                encoded_fragment_starts
            )
            # adjust fragment ends to be <= input_length
            rel_fragment_starts = encoded_fragment_starts + grid_cells
            norm_fragment_starts = rel_fragment_starts / self.num_grid_cells
            adjusted_norm_fragment_starts = tf.maximum(
                tf.cast(0.0, dtype=tf.keras.backend.floatx()),
                norm_fragment_starts
            )
            norm_fragment_ends = norm_fragment_starts + norm_fragment_durations
            adjusted_norm_fragment_ends = tf.minimum(
                tf.cast(1.0, dtype=tf.keras.backend.floatx()),
                norm_fragment_ends
            )
            adjusted_norm_fragment_duration = adjusted_norm_fragment_ends - \
                adjusted_norm_fragment_starts

            adjusted_encoded_cell_bound_feature_true = adjusted_encoded_fragment_starts
            adjusted_norm_cell_unbound_feature_true = adjusted_norm_fragment_duration
        elif self.encoding_type == 'CENTER_DURATION':
            encoded_fragment_centers = encoded_cell_bound_feature_true
            norm_fragment_durations = norm_cell_unbound_feature_true
            rel_fragment_centers = encoded_fragment_centers + grid_cells
            norm_fragment_centers = rel_fragment_centers / self.num_grid_cells
            # retrieve actual (starts, ends) from (centers, durations)
            norm_fragment_starts = norm_fragment_centers - norm_fragment_durations / 2
            norm_fragment_ends = norm_fragment_centers + norm_fragment_durations / 2
            # adjust starts to be >= 0, ends to be <= input_len
            adjusted_norm_fragment_starts = tf.maximum(
                tf.cast(0.0, dtype=tf.keras.backend.floatx()),
                norm_fragment_starts
            )
            adjusted_norm_fragment_ends = tf.minimum(
                tf.cast(1.0, dtype=tf.keras.backend.floatx()),
                norm_fragment_ends
            )
            # restore adjusted center
            adjusted_norm_fragment_centers = (
                adjusted_norm_fragment_starts + adjusted_norm_fragment_ends
            ) / 2
            adjusted_rel_fragment_centers = adjusted_norm_fragment_centers * self.num_grid_cells
            adjusted_fragment_centers_grid_cells = tf.clip_by_value(
                tf.floor(adjusted_rel_fragment_centers),
                0.0, self.num_grid_cells - 1.0
            )
            adjusted_encoded_fragment_centers = adjusted_rel_fragment_centers - \
                adjusted_fragment_centers_grid_cells
            # restore adjusted duration
            adjusted_norm_fragment_durations = adjusted_norm_fragment_ends - \
                adjusted_norm_fragment_starts

            adjusted_encoded_cell_bound_feature_true = adjusted_encoded_fragment_centers
            adjusted_norm_cell_unbound_feature_true = adjusted_norm_fragment_durations
        else:
            raise ValueError('Unsupported value for self.encoding_type')

        return adjusted_encoded_cell_bound_feature_true, adjusted_norm_cell_unbound_feature_true
