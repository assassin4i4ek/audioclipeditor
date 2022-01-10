package model.impl.editor.audio.clip.fragment

import model.api.editor.audio.clip.fragment.MutableAudioClipFragment
import model.api.editor.audio.clip.fragment.transformer.FragmentTransformer
import specs.api.immutable.AudioServiceSpecs
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class AudioClipFragmentImpl(
    leftImmutableAreaStartUs: Long,
    mutableAreaStartUs: Long,
    mutableAreaEndUs: Long,
    rightImmutableAreaEndUs: Long,
    audioClipDurationUs: Long,
    private val specs: AudioServiceSpecs,
    transformer: FragmentTransformer,
    private val onMutate: (AudioClipFragmentImpl) -> Unit
): MutableAudioClipFragment {
    override val maxRightBoundUs: Long = audioClipDurationUs
    override val minImmutableAreaDurationUs: Long get() = specs.minImmutableAreaDurationUs
    override val minMutableAreaDurationUs: Long get() = specs.minMutableAreaDurationUs

    private fun validateBounds() {
        check((leftBoundingFragment?.mutableAreaEndUs ?: (- rawLeftImmutableAreaDurationUs)) <= leftImmutableAreaStartUs) {
            "Audio fragment's left immutable area is invalid: $this"
        }
        check((leftBoundingFragment?.rightImmutableAreaEndUs ?: (- rawLeftImmutableAreaDurationUs)) <= mutableAreaStartUs) {
            "Audio fragment's mutable area start is invalid: $this"
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
        check((rightBoundingFragment?.leftImmutableAreaStartUs ?: (maxRightBoundUs + rawRightImmutableAreaDurationUs)) >= mutableAreaEndUs) {
            "Audio fragment's mutable area end is invalid: $this"
        }
        check((rightBoundingFragment?.mutableAreaStartUs ?: (maxRightBoundUs + rawRightImmutableAreaDurationUs)) >= rightImmutableAreaEndUs) {
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
                if (currentValue != value) {
                    onMutate(thisRef)
                }
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

    override var transformer: FragmentTransformer = transformer
        set(value) {
            if (field != value) {
                onMutate(this)
            }
            field = value
        }

    override fun toString(): String {
        return "Fragment(... ${leftBoundingFragment?.mutableAreaEndUs ?: 0} | ${leftBoundingFragment?.rightImmutableAreaEndUs ?: 0}] ... " +
                "[$leftImmutableAreaStartUs | $mutableAreaStartUs .. $mutableAreaEndUs | $rightImmutableAreaEndUs]" +
                " ... [${rightBoundingFragment?.leftImmutableAreaStartUs ?: maxRightBoundUs } | ${rightBoundingFragment?.mutableAreaStartUs ?: maxRightBoundUs } ...)"
    }
}