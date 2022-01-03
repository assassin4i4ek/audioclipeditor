import tensorflow as tf


class FragmentBatchResolver:
    def __init__(self, sample_rate, frame_duration_sec, num_grid_cells):
        self.num_grid_cells = num_grid_cells
        self.samples_in_frame = int(frame_duration_sec * sample_rate)

    def resolve(self, fragments, frame_offsets_samples):
        fragment_start_grid_cells, fragment_end_grid_cells = tf.map_fn(
            self._get_fragment_bounds_grid_cells,
            (fragments, frame_offsets_samples),
            fn_output_signature=(
                tf.RaggedTensorSpec(dtype=tf.int32, ragged_rank=0),
                tf.RaggedTensorSpec(dtype=tf.int32, ragged_rank=0)
            )
        )
        fragments = fragments.merge_dims(0, 1)
        fragment_start_grid_cells = fragment_start_grid_cells.merge_dims(0, 1)
        fragment_end_grid_cells = fragment_end_grid_cells.merge_dims(0, 1)
        # Sort by fragment start
        fragments_sort_indices = tf.argsort(fragments[..., 0])
        fragments = tf.gather(fragments, fragments_sort_indices)
        fragment_start_grid_cells = tf.gather(
            fragment_start_grid_cells, fragments_sort_indices
        )
        fragment_end_grid_cells = tf.gather(
            fragment_end_grid_cells, fragments_sort_indices
        )

        return self._resolve_duplicates(fragments, fragment_start_grid_cells, fragment_end_grid_cells)

    @tf.function
    def _get_fragment_bounds_grid_cells(self, fragments_with_offsets):
        fragments, offset = fragments_with_offsets
        offset_floatx = tf.cast(offset, dtype=fragments.dtype)
        bound_coef = tf.cast(
            self.num_grid_cells / self.samples_in_frame,
            dtype=fragments.dtype
        )
        fragment_start_grid_cells = tf.math.maximum(
            0,
            tf.cast(
                tf.math.floor(
                    (fragments[..., 0] - offset_floatx) * bound_coef
                ),
                dtype=tf.int32
            )
        )
        fragment_end_grid_cells = tf.math.minimum(
            self.num_grid_cells - 1,
            tf.cast(
                tf.math.floor(
                    (fragments[..., 1] - offset_floatx) * bound_coef
                ),
                dtype=tf.int32
            )
        )
        return (fragment_start_grid_cells, fragment_end_grid_cells)

    @tf.function
    def _resolve_duplicates(self, fragments, fragment_start_grid_cells, fragment_end_grid_cells):
        filtered_fragments = tf.zeros(
            (0, tf.shape(fragments)[1]),
            dtype=fragments.dtype
        )
        current_group_fragments = tf.expand_dims(fragments[0], axis=0)
        current_group_ranks = tf.expand_dims(
            self._estimate_fragment_accuracy_rank(
                fragment_start_grid_cells[0], fragment_end_grid_cells[0]
            ),
            axis=0
        )

        for i_next_fragment in tf.range(1, tf.shape(fragments)[0]):
            tf.autograph.experimental.set_loop_options(
                shape_invariants=[
                    (filtered_fragments, tf.TensorShape([None, None])),
                    (current_group_ranks, tf.TensorShape([None]))
                ]
            )
            next_fragment = fragments[i_next_fragment]
            next_fragment_rank = self._estimate_fragment_accuracy_rank(
                fragment_start_grid_cells[i_next_fragment], fragment_end_grid_cells[i_next_fragment]
            )

            if tf.reduce_any(
                tf.map_fn(
                    lambda current_group_fragment: self._intersect_fragments(
                        current_group_fragment, next_fragment
                    ),
                    current_group_fragments,
                    fn_output_signature=tf.bool
                )
            ):
                # append next fragment to current group of fragments
                current_group_fragments = tf.concat([
                    current_group_fragments,
                    tf.expand_dims(next_fragment, axis=0)
                ], axis=0)
                current_group_ranks = tf.concat([
                    current_group_ranks,
                    tf.expand_dims(next_fragment_rank, axis=0)
                ], axis=0)
            else:
                # create new group for next fragment
                filtered_fragments = tf.concat([
                    filtered_fragments,
                    tf.expand_dims(
                        self._resolve_fragment_group(
                            current_group_fragments, current_group_ranks
                        ),
                        axis=0
                    )
                ], axis=0)
                current_group_fragments = tf.expand_dims(
                    next_fragment, axis=0
                )
                current_group_ranks = tf.expand_dims(
                    next_fragment_rank, axis=0
                )

        filtered_fragments = tf.concat([
            filtered_fragments,
            tf.expand_dims(
                self._resolve_fragment_group(
                    current_group_fragments, current_group_ranks
                ),
                axis=0
            )
        ], axis=0)

        return filtered_fragments

    @ tf.function
    def _estimate_fragment_accuracy_rank(self, fragment_start_grid_cell, fragment_end_grid_cell):
        # fragments on the frame bounds can be invalid
        if (fragment_start_grid_cell == 0) or fragment_end_grid_cell == (self.num_grid_cells - 1):
            return 0
        else:
            return 1

    @ tf.function
    def _intersect_fragments(self, fragment1, fragment2):
        return (fragment1[0] <= fragment2[0] and fragment2[0] <= fragment1[1]) \
            or (fragment2[0] <= fragment1[0] and fragment1[0] <= fragment2[1])

    @tf.function
    def _resolve_fragment_group(self, group_fragments, group_ranks):
        max_rank = tf.reduce_max(group_ranks)
        max_rank_fragments_mask = group_ranks == max_rank
        max_rank_fragments = tf.boolean_mask(
            group_fragments, max_rank_fragments_mask
        )
        avaraged_fragment = tf.reduce_mean(
            max_rank_fragments, axis=0
        )
        return avaraged_fragment


class FragmentBatchResolverLayer(tf.keras.layers.Layer):
    def __init__(self, sample_rate, frame_duration_sec, num_grid_cells, **kwargs):
        super().__init__(**kwargs)
        self.sample_rate = sample_rate
        self.frame_duration_sec = frame_duration_sec
        self.num_grid_cells = num_grid_cells
        self.samples_in_frame = int(frame_duration_sec * sample_rate)

    @tf.function
    def call(self, frames_of_fragments_batch, frame_offsets_samples_batch):
        resolved_fragments_batch = tf.map_fn(
            self._resolve_map_fn_alias,
            (frames_of_fragments_batch, frame_offsets_samples_batch),
            fn_output_signature=tf.RaggedTensorSpec(
                shape=[None, frames_of_fragments_batch.shape[-1]],
                dtype=frames_of_fragments_batch.dtype, ragged_rank=0
            )
        )
        return resolved_fragments_batch

    @tf.function
    def _resolve_map_fn_alias(self, frames_of_fragments_with_offsets):
        frames_of_fragments, frame_offsets_samples = frames_of_fragments_with_offsets
        return self.resolve(frames_of_fragments, frame_offsets_samples)

    @tf.function
    def resolve(self, frames_of_fragments, frame_offsets_samples):
        frames_of_fragment_start_grid_cells, frames_of_fragment_end_grid_cells = tf.map_fn(
            self._get_fragment_bounds_grid_cells,
            (frames_of_fragments, frame_offsets_samples),
            fn_output_signature=(
                tf.RaggedTensorSpec(
                    shape=[None], dtype=tf.int32, ragged_rank=0),
                tf.RaggedTensorSpec(
                    shape=[None], dtype=tf.int32, ragged_rank=0)
            )
        )

        flat_fragments, flat_fragments_start_grid_cells, flat_fragments_end_grid_cells = self\
            ._flatten_and_sort_by_start(
                frames_of_fragments, frames_of_fragment_start_grid_cells,
                frames_of_fragment_end_grid_cells
            )

        resolved_fragments = self._resolve_fragments_duplicates(
            flat_fragments, flat_fragments_start_grid_cells, flat_fragments_end_grid_cells)

        return resolved_fragments

    @tf.function
    def _get_fragment_bounds_grid_cells(self, fragments_with_offsets):
        fragments, offset = fragments_with_offsets
        if len(fragments) > 0:
            offset_floatx = tf.cast(offset, dtype=fragments.dtype)
            bound_coef = tf.cast(
                self.num_grid_cells / self.samples_in_frame,
                dtype=fragments.dtype
            )
            fragment_start_grid_cells = tf.math.maximum(
                0,
                tf.cast(
                    tf.math.floor(
                        (fragments[..., 0] - offset_floatx) * bound_coef
                    ),
                    dtype=tf.int32
                )
            )
            fragment_end_grid_cells = tf.math.minimum(
                self.num_grid_cells - 1,
                tf.cast(
                    tf.math.floor(
                        (fragments[..., 1] - offset_floatx) * bound_coef
                    ),
                    dtype=tf.int32
                )
            )
        else:
            fragment_start_grid_cells = tf.constant([], dtype=tf.int32)
            fragment_end_grid_cells = tf.constant([], dtype=tf.int32)
        return fragment_start_grid_cells, fragment_end_grid_cells

    @tf.function
    def _flatten_and_sort_by_start(
            self, frames_of_fragments,
            frames_of_fragment_start_grid_cells,
            frames_of_fragment_end_grid_cells
    ):
        flat_fragments = frames_of_fragments.merge_dims(0, 1)
        flat_fragments_start_grid_cells = frames_of_fragment_start_grid_cells\
            .merge_dims(0, 1)
        flat_fragments_end_grid_cells = frames_of_fragment_end_grid_cells\
            .merge_dims(0, 1)
        # Sort by fragment start
        fragments_sort_indices = tf.argsort(flat_fragments[..., 0])
        sorted_flat_fragments = tf.gather(
            flat_fragments, fragments_sort_indices
        )
        sorted_flat_fragments_start_grid_cells = tf.gather(
            flat_fragments_start_grid_cells, fragments_sort_indices
        )
        sorted_flat_fragments_end_grid_cells = tf.gather(
            flat_fragments_end_grid_cells, fragments_sort_indices
        )
        return (
            sorted_flat_fragments,
            sorted_flat_fragments_start_grid_cells,
            sorted_flat_fragments_end_grid_cells
        )

    @tf.function
    def _resolve_fragments_duplicates(self, fragments, fragments_start_grid_cells, fragments_end_grid_cells):
        if len(fragments) > 0:
            resolved_fragments = tf.TensorArray(
                fragments.dtype, size=0, dynamic_size=True)
            current_group_fragments = tf.TensorArray(
                fragments.dtype, size=1, dynamic_size=True
            )
            current_group_ranks = tf.TensorArray(
                tf.int32, size=1, dynamic_size=True)

            current_group_fragments = current_group_fragments\
                .unstack([fragments[0]])
            current_group_ranks = current_group_ranks\
                .unstack([self._estimate_fragment_accuracy_rank(
                    fragments_start_grid_cells[0], fragments_end_grid_cells[0]
                )])

            for i_next_fragment in tf.range(1, len(fragments)):
                next_fragment = fragments[i_next_fragment]
                next_fragment_rank = self._estimate_fragment_accuracy_rank(
                    fragments_start_grid_cells[i_next_fragment], fragments_end_grid_cells[i_next_fragment]
                )

                if self._intersect_fragments(current_group_fragments.stack(), next_fragment):
                    # append next fragment to current group of fragments
                    current_group_fragments = current_group_fragments.write(
                        current_group_fragments.size(), next_fragment
                    )
                    current_group_ranks = current_group_ranks.write(
                        current_group_ranks.size(), next_fragment_rank
                    )
                else:
                    # create new group for next fragment
                    new_resolved_fragment = self._resolve_fragments_group(
                        current_group_fragments.stack(), current_group_ranks.stack()
                    )
                    resolved_fragments = resolved_fragments.write(
                        resolved_fragments.size(), new_resolved_fragment
                    )
                    current_group_fragments = current_group_fragments.unstack([
                        next_fragment
                    ])
                    current_group_ranks = current_group_ranks.unstack([
                        next_fragment_rank
                    ])

            resolved_fragments = resolved_fragments.write(
                resolved_fragments.size(),
                self._resolve_fragments_group(
                    current_group_fragments.stack(), current_group_ranks.stack()
                )
            )

            result_fragments = resolved_fragments.stack()
            resolved_fragments.close()
            current_group_fragments.close()
            current_group_ranks.close()
        else:
            result_fragments = fragments
        return result_fragments

    @tf.function
    def _estimate_fragment_accuracy_rank(self, fragment_start_grid_cell, fragment_end_grid_cell):
        # fragments on the frame bounds can be invalid
        if (fragment_start_grid_cell == 0) or fragment_end_grid_cell == (self.num_grid_cells - 1):
            return 0
        else:
            return 1

    @ tf.function
    def _intersect_fragments(self, fragment1, fragment2):
        return tf.reduce_any(
            (
                (fragment1[..., 0] <= fragment2[..., 0])
                & (fragment2[..., 0] <= fragment1[..., 1])
            ) | (
                (fragment2[..., 0] <= fragment1[..., 0])
                & (fragment1[..., 0] <= fragment2[..., 1])
            )
        )

    @tf.function
    def _resolve_fragments_group(self, group_fragments, group_ranks):
        max_rank = tf.reduce_max(group_ranks)
        max_rank_fragments_mask = group_ranks == max_rank
        max_rank_fragments = tf.boolean_mask(
            group_fragments, max_rank_fragments_mask
        )
        avaraged_fragment = tf.reduce_mean(
            max_rank_fragments, axis=0
        )
        return avaraged_fragment

    def get_config(self):
        config = super().get_config()
        config.update({
            'sample_rate': self.sample_rate,
            'frame_duration_sec': self.frame_duration_sec,
            'num_grid_cells': self.num_grid_cells
        })
        return config
