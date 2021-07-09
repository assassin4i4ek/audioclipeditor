package model

class AudioFragment (
    lowerImmutableAreaStartMs: Float,
    mutableAreaStartMs: Float,
    mutableAreaEndMs: Float,
    upperImmutableAreaEndMs: Float,
    maxDurationMs: Float,
    lowerBoundingFragment: AudioFragment?,
    upperBoundingFragment: AudioFragment?,
    val specs: AudioFragmentSpecs
) {
    var lowerImmutableAreaStartMs: Float = lowerImmutableAreaStartMs
        set(value) {
            field = value
            validate()
        }

    var upperImmutableAreaEndMs: Float = upperImmutableAreaEndMs
        set(value) {
            field = value
            validate()
        }

    var mutableAreaStartMs: Float = mutableAreaStartMs
        set(value) {
            field = value
            validate()
        }

    var mutableAreaEndMs: Float = mutableAreaEndMs
        set(value) {
            field = value
            validate()
        }

    var maxDurationMs: Float = maxDurationMs
        set(value) {
            field = value
            validate()
        }

    var lowerBoundingFragment: AudioFragment? = lowerBoundingFragment
        set(value) {
            field = value
            validate()
        }

    var upperBoundingFragment: AudioFragment? = upperBoundingFragment
        set(value) {
            field = value
            validate()
        }

    init {
        validate()
    }

    private fun validate() {
//        val isValid = lowerImmutableAreaStartMs - (lowerBoundingFragment?.upperImmutableAreaEndMs ?: 0f) >= 0 &&
//                (mutableAreaStartMs - lowerImmutableAreaStartMs) >= minImmutableAreasDurationMs &&
//                (mutableAreaEndMs - mutableAreaStartMs) >= minMutableAreaDurationMs &&
//                (upperImmutableAreaEndMs - mutableAreaEndMs) >= minImmutableAreasDurationMs &&
//                (upperBoundingFragment?.lowerImmutableAreaStartMs ?: maxDurationMs - upperImmutableAreaEndMs) >= 0

        if (lowerImmutableAreaStartMs - (lowerBoundingFragment?.upperImmutableAreaEndMs ?: (- mutableAreaStartMs + lowerImmutableAreaStartMs)) < 0) {
            throw IllegalArgumentException("Audio fragment's lower immutable area start = $lowerImmutableAreaStartMs is less than lower bounding fragment end ${lowerBoundingFragment?.upperImmutableAreaEndMs ?: 0f}")
        }
        if ((mutableAreaStartMs - lowerImmutableAreaStartMs) < specs.minImmutableAreasDurationMs) {
            throw IllegalArgumentException("Audio fragment's mutable area start = $mutableAreaStartMs is less than fragment's immutable area start $lowerImmutableAreaStartMs + constraint ${specs.minImmutableAreasDurationMs}")
        }
        if ((mutableAreaEndMs - mutableAreaStartMs) < specs.minMutableAreaDurationMs) {
            throw IllegalArgumentException("Audio fragment's mutable area end = $mutableAreaEndMs is less than fragment's mutable area start $mutableAreaStartMs + constraint ${specs.minMutableAreaDurationMs}")
        }
        if ((upperImmutableAreaEndMs - mutableAreaEndMs) < specs.minImmutableAreasDurationMs) {
            throw IllegalArgumentException("Audio fragment's upper immutable area end = $upperImmutableAreaEndMs is less than fragment's mutable area end $mutableAreaEndMs + constraint ${specs.minImmutableAreasDurationMs}")
        }
        if ((upperBoundingFragment?.lowerImmutableAreaStartMs ?: (maxDurationMs + upperImmutableAreaEndMs - mutableAreaEndMs)) - upperImmutableAreaEndMs < 0) {
            throw IllegalArgumentException("Audio fragment's upper immutable area end = $upperImmutableAreaEndMs is greater than upper bounding fragment start ${upperBoundingFragment?.lowerImmutableAreaStartMs ?: maxDurationMs}")
        }
//        if (!isValid) {
//            throw IllegalArgumentException("Audio fragment is invalid")
//        }
    }

    override fun toString(): String {
        return "Fragment(${lowerBoundingFragment?.upperImmutableAreaEndMs ?: 0} ... [$lowerImmutableAreaStartMs | $mutableAreaStartMs .. $mutableAreaEndMs | $upperImmutableAreaEndMs] ... ${upperBoundingFragment?.lowerImmutableAreaStartMs ?: maxDurationMs })"
    }

    operator fun contains(ms: Float): Boolean {
        return lowerImmutableAreaStartMs < ms && ms < upperImmutableAreaEndMs
    }
}