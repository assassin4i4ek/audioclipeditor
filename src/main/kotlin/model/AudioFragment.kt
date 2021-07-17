package model

import model.transformers.AudioTransformer

class AudioFragment (
    lowerImmutableAreaStartUs: Long,
    mutableAreaStartUs: Long,
    mutableAreaEndUs: Long,
    upperImmutableAreaEndUs: Long,
    maxDurationUs: Long,
    lowerBoundingFragment: AudioFragment?,
    upperBoundingFragment: AudioFragment?,
    val specs: AudioFragmentSpecs,
    var transformer: AudioTransformer
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

    val upperImmutableAreaDurationUs: Long get() = upperImmutableAreaEndUs - mutableAreaEndUs
    val mutableAreaDurationUs: Long get() = mutableAreaEndUs - mutableAreaStartUs
    val lowerImmutableAreaDurationUs: Long get() = mutableAreaStartUs - lowerImmutableAreaStartUs
    val totalDurationUs: Long get() = upperImmutableAreaEndUs - lowerImmutableAreaStartUs

    init {
        validate()
    }

    private fun validate() {
        check(lowerImmutableAreaStartUs - (lowerBoundingFragment?.upperImmutableAreaEndUs ?: (- mutableAreaStartUs + lowerImmutableAreaStartUs)) >= 0) {
            "Audio fragment's lower immutable area is invalid: $this"
        }
        check((mutableAreaStartUs - lowerImmutableAreaStartUs) >= specs.minImmutableAreasDurationUs) {
            "Audio fragment's mutable area start is invalid: $this"
        }
        check((mutableAreaEndUs - mutableAreaStartUs) >= specs.minMutableAreaDurationUs) {
            "Audio fragment's mutable area end is invalid: $this"
        }
        check((upperImmutableAreaEndUs - mutableAreaEndUs) >= specs.minImmutableAreasDurationUs) {
            "Audio fragment's upper immutable area end is invalid: $this"
        }
        check((upperBoundingFragment?.lowerImmutableAreaStartUs ?: (maxDurationUs + upperImmutableAreaEndUs - mutableAreaEndUs)) - upperImmutableAreaEndUs >= 0) {
            "Audio fragment's upper immutable area end is invalid: $this"
        }
    }

    override fun toString(): String {
        return "Fragment(${lowerBoundingFragment?.upperImmutableAreaEndUs ?: 0} ... [$lowerImmutableAreaStartUs | $mutableAreaStartUs .. $mutableAreaEndUs | $upperImmutableAreaEndUs] ... ${upperBoundingFragment?.lowerImmutableAreaStartUs ?: maxDurationUs })"
    }

    operator fun contains(us: Long): Boolean {
        return us in lowerImmutableAreaStartUs .. upperImmutableAreaEndUs
    }
}