package model.impl.editor.clip.fragment

import model.api.editor.clip.fragment.MutableAudioClipFragment
import specs.api.immutable.audio.AudioServiceSpecs
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class AudioClipFragmentImpl(
    leftImmutableAreaStartUs: Long,
    mutableAreaStartUs: Long,
    mutableAreaEndUs: Long,
    rightImmutableAreaEndUs: Long,
    private val specs: AudioServiceSpecs,
    audioClipDurationUs: Long,
): MutableAudioClipFragment {
    override val maxRightBoundUs: Long = audioClipDurationUs
    override val minImmutableAreaDurationUs: Long get() = specs.minImmutableAreaDurationUs
    override val minMutableAreaDurationUs: Long get() = specs.minMutableAreaDurationUs

    private fun validateBounds() {
        check((leftBoundingFragment?.rightImmutableAreaEndUs ?: (- rawLeftImmutableAreaDurationUs)) <= leftImmutableAreaStartUs) {
            "Audio fragment's left immutable area is invalid: $this"
        }
        check((rawLeftImmutableAreaDurationUs) >= minImmutableAreaDurationUs) {
            "Audio fragment's mutable area start is invalid: $this"
        }
        check((mutableAreaDurationUs) >= minMutableAreaDurationUs) {
            "Audio fragment's mutable area end is invalid: $this"
        }
        check((rawRightImmutableAreaDurationUs) >= minImmutableAreaDurationUs) {
            "Audio fragment's right immutable area end is invalid: $this"
        }
        check((rightBoundingFragment?.leftImmutableAreaStartUs ?: (maxRightBoundUs + rawRightImmutableAreaDurationUs)) >= rightImmutableAreaEndUs) {
            "Audio fragment's right immutable area end is invalid: $this"
        }
    }

    private fun <T> validatingProperty(initValue: T): ReadWriteProperty<AudioClipFragmentImpl, T> {
        return object : ReadWriteProperty<AudioClipFragmentImpl, T> {
            private var currentValue = initValue

            override fun getValue(thisRef: AudioClipFragmentImpl, property: KProperty<*>): T {
                return currentValue
            }

            override fun setValue(thisRef: AudioClipFragmentImpl, property: KProperty<*>, value: T) {
                currentValue = value
                validateBounds()
            }
        }
    }
    
    override var leftImmutableAreaStartUs: Long by validatingProperty(leftImmutableAreaStartUs)
    override var mutableAreaStartUs: Long by validatingProperty(mutableAreaStartUs)
    override var mutableAreaEndUs: Long by validatingProperty(mutableAreaEndUs)
    override var rightImmutableAreaEndUs: Long by validatingProperty(rightImmutableAreaEndUs)

    override var leftBoundingFragment: MutableAudioClipFragment? by validatingProperty(null)
    override var rightBoundingFragment: MutableAudioClipFragment? by validatingProperty(null)

    override fun toString(): String {
        return "Fragment(${leftBoundingFragment?.rightImmutableAreaEndUs ?: 0} ... [$leftImmutableAreaStartUs | $mutableAreaStartUs .. $mutableAreaEndUs | $rightImmutableAreaEndUs] ... ${rightBoundingFragment?.leftImmutableAreaStartUs ?: maxRightBoundUs })"
    }
}