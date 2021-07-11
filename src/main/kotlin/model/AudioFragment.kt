package model

class AudioFragment (
    lowerImmutableAreaStartUs: Long,
    mutableAreaStartUs: Long,
    mutableAreaEndUs: Long,
    upperImmutableAreaEndUs: Long,
    maxDurationUs: Long,
    lowerBoundingFragment: AudioFragment?,
    upperBoundingFragment: AudioFragment?,
    val specs: AudioFragmentSpecs
) {
    var lowerImmutableAreaStartUs: Long = lowerImmutableAreaStartUs
        set(value) {
            field = value
            validate()
        }

    var upperImmutableAreaEndUs: Long = upperImmutableAreaEndUs
        set(value) {
            field = value
            validate()
        }

    var mutableAreaStartUs: Long = mutableAreaStartUs
        set(value) {
            field = value
            validate()
        }

    var mutableAreaEndUs: Long = mutableAreaEndUs
        set(value) {
            field = value
            validate()
        }

    var maxDurationUs: Long = maxDurationUs
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
        if (lowerImmutableAreaStartUs - (lowerBoundingFragment?.upperImmutableAreaEndUs ?: (- mutableAreaStartUs + lowerImmutableAreaStartUs)) < 0) {
            throw IllegalArgumentException("Audio fragment's lower immutable area is invalid: $this")
//            throw IllegalArgumentException("Audio fragment's lower immutable area start = $lowerImmutableAreaStartMs is less than lower bounding fragment end ${lowerBoundingFragment?.upperImmutableAreaEndMs ?: 0f}")
        }
        if ((mutableAreaStartUs - lowerImmutableAreaStartUs) < specs.minImmutableAreasDurationUs) {
            throw IllegalArgumentException("Audio fragment's mutable area start is invalid: $this")
//            throw IllegalArgumentException("Audio fragment's mutable area start = $mutableAreaStartMs is less than fragment's immutable area start $lowerImmutableAreaStartMs + constraint ${specs.minImmutableAreasDurationMs}")
        }
        if ((mutableAreaEndUs - mutableAreaStartUs) < specs.minMutableAreaDurationUs) {
            throw IllegalArgumentException("Audio fragment's mutable area end is invalid: $this")
//            throw IllegalArgumentException("Audio fragment's mutable area end = $mutableAreaEndMs is less than fragment's mutable area start $mutableAreaStartMs + constraint ${specs.minMutableAreaDurationMs}")
        }
        if ((upperImmutableAreaEndUs - mutableAreaEndUs) < specs.minImmutableAreasDurationUs) {
            throw IllegalArgumentException("Audio fragment's upper immutable area end is invalid: $this")
//            throw IllegalArgumentException("Audio fragment's upper immutable area end = $upperImmutableAreaEndMs is less than fragment's mutable area end $mutableAreaEndMs + constraint ${specs.minImmutableAreasDurationMs}")
        }
        if ((upperBoundingFragment?.lowerImmutableAreaStartUs ?: (maxDurationUs + upperImmutableAreaEndUs - mutableAreaEndUs)) - upperImmutableAreaEndUs < 0) {
            throw IllegalArgumentException("Audio fragment's upper immutable area end is invalid: $this")
        }
    }

    override fun toString(): String {
        return "Fragment(${lowerBoundingFragment?.upperImmutableAreaEndUs ?: 0} ... [$lowerImmutableAreaStartUs | $mutableAreaStartUs .. $mutableAreaEndUs | $upperImmutableAreaEndUs] ... ${upperBoundingFragment?.lowerImmutableAreaStartUs ?: maxDurationUs })"
    }

    operator fun contains(us: Long): Boolean {
        return us in lowerImmutableAreaStartUs .. upperImmutableAreaEndUs
    }
}