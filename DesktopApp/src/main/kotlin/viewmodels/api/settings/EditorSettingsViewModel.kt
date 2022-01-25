package viewmodels.api.settings

import androidx.compose.ui.unit.Dp
import viewmodels.api.BaseViewModel

interface EditorSettingsViewModel: BaseViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    val editableClipViewCompressionAmplifier: String
    val globalClipViewPathCompressionAmplifier: String
    val maxPanelViewHeightDp: String
    val minPanelViewHeightDp: String
    val xStepDpPerSec: String
    val transformZoomClickCoef: String
    val transformOffsetScrollCoef: String
    val transformZoomScrollCoef: String
    val mutableDraggableAreaFraction: String
    val immutableDraggableAreaFraction: String
    val minImmutableAreaWidthWinDp: String
    val minMutableAreaWidthWinDp: String
    val preferredImmutableAreaWidthWinDp: String
    val silenceTransformerSilenceDurationMsIncrementStep: String
    val canSave: Boolean

    /* Callbacks */
    fun onEditableClipViewCompressionAmplifier(newEditableClipViewCompressionAmplifier: String)
    fun onGlobalClipViewPathCompressionAmplifier(newGlobalClipViewPathCompressionAmplifier: String)
    fun onMaxPanelViewHeightDp(newMaxPanelViewHeightDp: String)
    fun onMinPanelViewHeightDp(newMinPanelViewHeightDp: String)
    fun onXStepDpPerSec(newXStepDpPerSec: String)
    fun onTransformZoomClickCoef(newTransformZoomClickCoef: String)
    fun onTransformOffsetScrollCoef(newTransformOffsetScrollCoef: String)
    fun onTransformZoomScrollCoef(newTransformZoomScrollCoef: String)
    fun onMutableDraggableAreaFraction(newMutableDraggableAreaFraction: String)
    fun onImmutableDraggableAreaFraction(newImmutableDraggableAreaFraction: String)
    fun onMinImmutableAreaWidthWinDp(newMinImmutableAreaWidthWinDp: String)
    fun onMinMutableAreaWidthWinDp(newMinMutableAreaWidthWinDp: String)
    fun onPreferredImmutableAreaWidthWinDp(newPreferredImmutableAreaWidthWinDp: String)
    fun onSilenceTransformerSilenceDurationMsIncrementStep(newSilenceTransformerSilenceDurationMsIncrementStep: String)
    fun onRefreshTextFieldValues()
    fun onSaveClick()
    fun onResetClick()

    /* Methods */

}