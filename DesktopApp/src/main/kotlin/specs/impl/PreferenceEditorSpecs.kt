package specs.impl

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import specs.api.immutable.InputDevice
import specs.api.mutable.MutableEditorSpecs
import specs.impl.utils.BaseStatefulPreferenceSpecsImpl
import java.io.File
import java.util.prefs.Preferences

class PreferenceEditorSpecs: BaseStatefulPreferenceSpecsImpl(), MutableEditorSpecs {
    override val preferences: Preferences = Preferences.userNodeForPackage(this.javaClass)

    override var inputDevice: InputDevice by savableProperty(
        InputDevice.Touchpad, ::inputDevice, { it.name }, { InputDevice.valueOf(it) }
    )

    override var editableClipViewCompressionAmplifier: Float by savableProperty(
        41f, ::editableClipViewCompressionAmplifier
    )

    override var globalClipViewPathCompressionAmplifier: Float by savableProperty(
        41f, ::globalClipViewPathCompressionAmplifier
    )

    override var maxPanelViewHeightDp: Dp by savableProperty(
        400.dp, ::maxPanelViewHeightDp
    )

    override var minPanelViewHeightDp: Dp by savableProperty(
        200.dp, ::minPanelViewHeightDp
    )

    override var xStepDpPerSec: Dp by savableProperty(
        300.dp, ::xStepDpPerSec
    )

    override var transformZoomClickCoef: Float by savableProperty(
        1.5f, ::transformZoomClickCoef
    )

    override var transformOffsetScrollCoef: Float by savableProperty(
        50f, ::transformOffsetScrollCoef
    )

    override var transformZoomScrollCoef: Float by savableProperty(
        1.5f, ::transformZoomScrollCoef
    )


    override var immutableDraggableAreaFraction: Float by savableProperty(
        0.75f, ::immutableDraggableAreaFraction
    )
    override var mutableDraggableAreaFraction: Float by savableProperty(
        0.25f, ::mutableDraggableAreaFraction
    )

    override var minImmutableAreaWidthWinDp: Dp by savableProperty(
        40.dp, ::minImmutableAreaWidthWinDp
    )
    override var minMutableAreaWidthWinDp: Dp by savableProperty(
        32.dp, ::minMutableAreaWidthWinDp
    )
    override var preferredImmutableAreaWidthWinDp: Dp by savableProperty(
        160.dp, ::preferredImmutableAreaWidthWinDp
    )

    override var silenceTransformerSilenceDurationUsIncrementStep: Long by savableProperty(
        50e3.toLong(), ::silenceTransformerSilenceDurationUsIncrementStep
    )

    override var defaultClipSavingDirPath: File by savableProperty(
        File(System.getProperty("user.dir")).resolve("Processed Clips"),
        ::defaultClipSavingDirPath, { it.absolutePath }, { File(it) }
    )

    override var defaultClipMetadataSavingDirPath: File by savableProperty(
        File(System.getProperty("user.dir")).resolve("Processed Clips Metadata"),
        ::defaultClipMetadataSavingDirPath, { it.absolutePath }, { File(it) }
    )
}