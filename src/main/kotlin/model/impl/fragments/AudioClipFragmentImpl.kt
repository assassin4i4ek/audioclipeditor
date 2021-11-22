package model.impl.fragments

import model.api.fragments.AudioClipFragment
import model.api.fragments.AudioFragmentSpecs
import model.api.fragments.transformers.FragmentTransformer

class AudioClipFragmentImpl(
    leftImmutableAreaStartUs: Long,
    mutableAreaStartUs: Long,
    mutableAreaEndUs: Long,
    rightImmutableAreaEndUs: Long,
    override val specs: AudioFragmentSpecs,
    override var transformer: FragmentTransformer
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
            "Audio fragment's left immutable area is invalid: $this"
        }
        check((mutableAreaStartUs - leftImmutableAreaStartUs) >= specs.minImmutableAreasDurationUs) {
            "Audio fragment's mutable area start is invalid: $this"
        }
        check((mutableAreaEndUs - mutableAreaStartUs) >= specs.minMutableAreaDurationUs) {
            "Audio fragment's mutable area end is invalid: $this"
        }
        check((rightImmutableAreaEndUs - mutableAreaEndUs) >= specs.minImmutableAreasDurationUs) {
            "Audio fragment's right immutable area end is invalid: $this"
        }
        check((rightBoundingFragment?.leftImmutableAreaStartUs ?: (specs.maxRightBoundUs + rightImmutableAreaEndUs - mutableAreaEndUs)) - rightImmutableAreaEndUs >= 0) {
            "Audio fragment's right immutable area end is invalid: $this"
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