package model.impl.fragment

import model.api.fragment.AudioClipFragment
import model.api.fragment.AudioFragmentSpecs

class AudioClipFragmentImpl(
    leftImmutableAreaStartUs: Long,
    mutableAreaStartUs: Long,
    mutableAreaEndUs: Long,
    rightImmutableAreaEndUs: Long,
    override val specs: AudioFragmentSpecs
) : AudioClipFragment {
    private fun <T> validateProperty(prevValue: T, newValue: T, setter: (T) -> Unit) {
        setter(newValue)
        try {
            validate()
        }
        catch (ise: IllegalStateException) {
            setter(prevValue)
            throw ise
        }
    }

    private fun validate() {
        check(leftImmutableAreaStartUs - (leftBoundingFragment?.rightImmutableAreaEndUs ?: (- mutableAreaStartUs + leftImmutableAreaStartUs)) >= 0) {
            "Audio fragment's lower immutable area is invalid: $this"
        }
        check((mutableAreaStartUs - leftImmutableAreaStartUs) >= specs.minImmutableAreasDurationUs) {
            "Audio fragment's mutable area start is invalid: $this"
        }
        check((mutableAreaEndUs - mutableAreaStartUs) >= specs.minMutableAreaDurationUs) {
            "Audio fragment's mutable area end is invalid: $this"
        }
        check((rightImmutableAreaEndUs - mutableAreaEndUs) >= specs.minImmutableAreasDurationUs) {
            "Audio fragment's upper immutable area end is invalid: $this"
        }
        check((rightBoundingFragment?.leftImmutableAreaStartUs ?: (specs.maxRightBoundUs + rightImmutableAreaEndUs - mutableAreaEndUs)) - rightImmutableAreaEndUs >= 0) {
            "Audio fragment's upper immutable area end is invalid: $this"
        }
    }

    override var leftImmutableAreaStartUs: Long = leftImmutableAreaStartUs
        set(value) = validateProperty(field, value) { field = it }

    override var mutableAreaStartUs: Long = mutableAreaStartUs
        set(value) = validateProperty(field, value) {field = it}

    override var mutableAreaEndUs: Long = mutableAreaEndUs
        set(value) = validateProperty(field, value) {field = it}

    override var rightImmutableAreaEndUs: Long = rightImmutableAreaEndUs
        set(value) = validateProperty(field, value) {field = it}

    override var leftBoundingFragment: AudioClipFragment? = null
        set(value) = validateProperty(field, value) {field = it}

    override var rightBoundingFragment: AudioClipFragment? = null
        set(value) = validateProperty(field, value) {field = it}


    override fun toString(): String {
        return "Fragment(${leftBoundingFragment?.rightImmutableAreaEndUs ?: 0} ... [$leftImmutableAreaStartUs | $mutableAreaStartUs .. $mutableAreaEndUs | $rightImmutableAreaEndUs] ... ${rightBoundingFragment?.leftImmutableAreaStartUs ?: specs.maxRightBoundUs })"
    }
}