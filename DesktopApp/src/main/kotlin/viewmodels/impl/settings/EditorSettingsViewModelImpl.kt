package viewmodels.impl.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import specs.api.mutable.MutableEditorSpecs
import viewmodels.api.settings.EditorSettingsViewModel

class EditorSettingsViewModelImpl(
    private val editorSpecs: MutableEditorSpecs
) : EditorSettingsViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    private var _editableClipViewCompressionAmplifier: String by mutableStateOf(editorSpecs.editableClipViewCompressionAmplifier.toString())
    override val editableClipViewCompressionAmplifier: String get() = _editableClipViewCompressionAmplifier

    private var _globalClipViewPathCompressionAmplifier: String by mutableStateOf(editorSpecs.globalClipViewPathCompressionAmplifier.toString())
    override val globalClipViewPathCompressionAmplifier: String get() = _globalClipViewPathCompressionAmplifier

    private var _maxPanelViewHeightDp: String by mutableStateOf(editorSpecs.maxPanelViewHeightDp.value.toInt().toString())
    override val maxPanelViewHeightDp: String get() = _maxPanelViewHeightDp

    private var _minPanelViewHeightDp: String by mutableStateOf(editorSpecs.minPanelViewHeightDp.value.toInt().toString())
    override val minPanelViewHeightDp: String get() = _minPanelViewHeightDp

    private var _xStepDpPerSec: String by mutableStateOf(editorSpecs.xStepDpPerSec.value.toInt().toString())
    override val xStepDpPerSec: String get() = _xStepDpPerSec

    private var _transformZoomClickCoef: String by mutableStateOf(editorSpecs.transformZoomClickCoef.toString())
    override val transformZoomClickCoef: String get() = _transformZoomClickCoef

    private var _transformOffsetScrollCoef: String by mutableStateOf(editorSpecs.transformOffsetScrollCoef.toString())
    override val transformOffsetScrollCoef: String get() = _transformOffsetScrollCoef

    private var _transformZoomScrollCoef: String by mutableStateOf(editorSpecs.transformZoomScrollCoef.toString())
    override val transformZoomScrollCoef: String get() = _transformZoomScrollCoef

    private var _mutableDraggableAreaFraction: String by mutableStateOf(editorSpecs.mutableDraggableAreaFraction.toString())
    override val mutableDraggableAreaFraction: String get() = _mutableDraggableAreaFraction

    private var _immutableDraggableAreaFraction: String by mutableStateOf(editorSpecs.immutableDraggableAreaFraction.toString())
    override val immutableDraggableAreaFraction: String get() = _immutableDraggableAreaFraction

    private var _minImmutableAreaWidthWinDp: String by mutableStateOf(editorSpecs.minImmutableAreaWidthWinDp.value.toInt().toString())
    override val minImmutableAreaWidthWinDp: String get() = _minImmutableAreaWidthWinDp

    private var _minMutableAreaWidthWinDp: String by mutableStateOf(editorSpecs.minMutableAreaWidthWinDp.value.toInt().toString())
    override val minMutableAreaWidthWinDp: String get() = _minMutableAreaWidthWinDp

    private var _preferredImmutableAreaWidthWinDp: String by mutableStateOf(editorSpecs.preferredImmutableAreaWidthWinDp.value.toInt().toString())
    override val preferredImmutableAreaWidthWinDp: String get() = _preferredImmutableAreaWidthWinDp

    private var _silenceTransformerSilenceDurationMsIncrementStep: String by mutableStateOf((editorSpecs.silenceTransformerSilenceDurationUsIncrementStep / 1000).toString())
    override val silenceTransformerSilenceDurationMsIncrementStep: String get() = _silenceTransformerSilenceDurationMsIncrementStep

    private var _canSave: Boolean by mutableStateOf(false)
    override val canSave: Boolean get() = _canSave

    /* Callbacks */
    override fun onEditableClipViewCompressionAmplifier(newEditableClipViewCompressionAmplifier: String) {
        if (newEditableClipViewCompressionAmplifier.toFloatOrNull() != null || newEditableClipViewCompressionAmplifier.isEmpty()) {
            _editableClipViewCompressionAmplifier = newEditableClipViewCompressionAmplifier
            _canSave = true
        }
    }

    override fun onGlobalClipViewPathCompressionAmplifier(newGlobalClipViewPathCompressionAmplifier: String) {
        if (newGlobalClipViewPathCompressionAmplifier.toFloatOrNull() != null || newGlobalClipViewPathCompressionAmplifier.isEmpty()) {
            _globalClipViewPathCompressionAmplifier = newGlobalClipViewPathCompressionAmplifier
            _canSave = true
        }
    }

    override fun onMaxPanelViewHeightDp(newMaxPanelViewHeightDp: String) {
        if (newMaxPanelViewHeightDp.toIntOrNull() != null || newMaxPanelViewHeightDp.isEmpty()) {
            _maxPanelViewHeightDp = newMaxPanelViewHeightDp
            _canSave = true
        }
    }

    override fun onMinPanelViewHeightDp(newMinPanelViewHeightDp: String) {
        if (newMinPanelViewHeightDp.toIntOrNull() != null || newMinPanelViewHeightDp.isEmpty()) {
            _minPanelViewHeightDp = newMinPanelViewHeightDp
            _canSave = true
        }
    }

    override fun onXStepDpPerSec(newXStepDpPerSec: String) {
        if (newXStepDpPerSec.toIntOrNull() != null || newXStepDpPerSec.isEmpty()) {
            _xStepDpPerSec = newXStepDpPerSec
            _canSave = true
        }
    }

    override fun onTransformZoomClickCoef(newTransformZoomClickCoef: String) {
        if (newTransformZoomClickCoef.toFloatOrNull() != null || newTransformZoomClickCoef.isEmpty()) {
            _transformZoomClickCoef = newTransformZoomClickCoef
            _canSave = true
        }
    }

    override fun onTransformOffsetScrollCoef(newTransformOffsetScrollCoef: String) {
        if (newTransformOffsetScrollCoef.toFloatOrNull() != null || newTransformOffsetScrollCoef.isEmpty()) {
            _transformOffsetScrollCoef = newTransformOffsetScrollCoef
            _canSave = true
        }
    }

    override fun onTransformZoomScrollCoef(newTransformZoomScrollCoef: String) {
        if (newTransformZoomScrollCoef.toFloatOrNull() != null || newTransformZoomScrollCoef.isEmpty()) {
            _transformZoomScrollCoef = newTransformZoomScrollCoef
            _canSave = true
        }
    }

    override fun onMutableDraggableAreaFraction(newMutableDraggableAreaFraction: String) {
        if (newMutableDraggableAreaFraction.toFloatOrNull() != null || newMutableDraggableAreaFraction.isEmpty()) {
            _mutableDraggableAreaFraction = newMutableDraggableAreaFraction
            _canSave = true
        }
    }

    override fun onImmutableDraggableAreaFraction(newImmutableDraggableAreaFraction: String) {
        if (newImmutableDraggableAreaFraction.toFloatOrNull() != null || newImmutableDraggableAreaFraction.isEmpty()) {
            _immutableDraggableAreaFraction = newImmutableDraggableAreaFraction
            _canSave = true
        }
    }

    override fun onMinImmutableAreaWidthWinDp(newMinImmutableAreaWidthWinDp: String) {
        if (newMinImmutableAreaWidthWinDp.toIntOrNull() != null || newMinImmutableAreaWidthWinDp.isEmpty()) {
            _minImmutableAreaWidthWinDp = newMinImmutableAreaWidthWinDp
            _canSave = true
        }
    }

    override fun onMinMutableAreaWidthWinDp(newMinMutableAreaWidthWinDp: String) {
        if (newMinMutableAreaWidthWinDp.toIntOrNull() != null || newMinMutableAreaWidthWinDp.isEmpty()) {
            _minMutableAreaWidthWinDp = newMinMutableAreaWidthWinDp
            _canSave = true
        }
    }

    override fun onPreferredImmutableAreaWidthWinDp(newPreferredImmutableAreaWidthWinDp: String) {
        if (newPreferredImmutableAreaWidthWinDp.toIntOrNull() != null || newPreferredImmutableAreaWidthWinDp.isEmpty()) {
            _preferredImmutableAreaWidthWinDp = newPreferredImmutableAreaWidthWinDp
            _canSave = true
        }
    }

    override fun onSilenceTransformerSilenceDurationMsIncrementStep(newSilenceTransformerSilenceDurationMsIncrementStep: String) {
        if (newSilenceTransformerSilenceDurationMsIncrementStep.toIntOrNull() != null || newSilenceTransformerSilenceDurationMsIncrementStep.isEmpty()) {
            _silenceTransformerSilenceDurationMsIncrementStep = newSilenceTransformerSilenceDurationMsIncrementStep
            _canSave = true
        }
    }

    override fun onRefreshTextFieldValues() {
        if (editableClipViewCompressionAmplifier.isEmpty()) {
            _editableClipViewCompressionAmplifier = editorSpecs.editableClipViewCompressionAmplifier.toString()
        }
        if (globalClipViewPathCompressionAmplifier.isEmpty()) {
            _globalClipViewPathCompressionAmplifier = editorSpecs.globalClipViewPathCompressionAmplifier.toString()
        }
        if (maxPanelViewHeightDp.isEmpty()) {
            _maxPanelViewHeightDp = editorSpecs.maxPanelViewHeightDp.value.toInt().toString()
        }
        if (minPanelViewHeightDp.isEmpty()) {
            _minPanelViewHeightDp = editorSpecs.minPanelViewHeightDp.value.toInt().toString()
        }
        if (xStepDpPerSec.isEmpty()) {
            _xStepDpPerSec = editorSpecs.xStepDpPerSec.value.toInt().toString()
        }
        if (transformZoomClickCoef.isEmpty()) {
            _transformZoomClickCoef = editorSpecs.transformZoomClickCoef.toString()
        }
        if (transformOffsetScrollCoef.isEmpty()) {
            _transformOffsetScrollCoef = editorSpecs.transformOffsetScrollCoef.toString()
        }
        if (transformZoomScrollCoef.isEmpty()) {
            _transformZoomScrollCoef = editorSpecs.transformZoomScrollCoef.toString()
        }
        if (mutableDraggableAreaFraction.isEmpty()) {
            _mutableDraggableAreaFraction = editorSpecs.mutableDraggableAreaFraction.toString()
        }
        if (immutableDraggableAreaFraction.isEmpty()) {
            _immutableDraggableAreaFraction = editorSpecs.immutableDraggableAreaFraction.toString()
        }
        if (minImmutableAreaWidthWinDp.isEmpty()) {
            _minImmutableAreaWidthWinDp = editorSpecs.minImmutableAreaWidthWinDp.value.toInt().toString()
        }
        if (minMutableAreaWidthWinDp.isEmpty()) {
            _minMutableAreaWidthWinDp = editorSpecs.minMutableAreaWidthWinDp.value.toInt().toString()
        }
        if (preferredImmutableAreaWidthWinDp.isEmpty()) {
            _preferredImmutableAreaWidthWinDp = editorSpecs.preferredImmutableAreaWidthWinDp.value.toInt().toString()
        }
        if (silenceTransformerSilenceDurationMsIncrementStep.isEmpty()) {
            _silenceTransformerSilenceDurationMsIncrementStep = (editorSpecs.silenceTransformerSilenceDurationUsIncrementStep / 1000).toString()
        }
    }

    override fun onSaveClick() {
        editorSpecs.editableClipViewCompressionAmplifier = editableClipViewCompressionAmplifier.toFloat()
        editorSpecs.globalClipViewPathCompressionAmplifier = globalClipViewPathCompressionAmplifier.toFloat()
        editorSpecs.maxPanelViewHeightDp = maxPanelViewHeightDp.toInt().dp
        editorSpecs.minPanelViewHeightDp = minPanelViewHeightDp.toInt().dp
        editorSpecs.xStepDpPerSec = xStepDpPerSec.toInt().dp
        editorSpecs.transformZoomClickCoef = transformZoomClickCoef.toFloat()
        editorSpecs.transformOffsetScrollCoef = transformOffsetScrollCoef.toFloat()
        editorSpecs.transformZoomScrollCoef = transformZoomScrollCoef.toFloat()
        editorSpecs.mutableDraggableAreaFraction = mutableDraggableAreaFraction.toFloat()
        editorSpecs.immutableDraggableAreaFraction = immutableDraggableAreaFraction.toFloat()
        editorSpecs.minImmutableAreaWidthWinDp = minImmutableAreaWidthWinDp.toInt().dp
        editorSpecs.minMutableAreaWidthWinDp = minMutableAreaWidthWinDp.toInt().dp
        editorSpecs.preferredImmutableAreaWidthWinDp = preferredImmutableAreaWidthWinDp.toInt().dp
        editorSpecs.silenceTransformerSilenceDurationUsIncrementStep = silenceTransformerSilenceDurationMsIncrementStep.toLong() * 1000
        _canSave = false
    }

    override fun onResetClick() {
        editorSpecs.reset()
        _editableClipViewCompressionAmplifier = editorSpecs.editableClipViewCompressionAmplifier.toString()
        _globalClipViewPathCompressionAmplifier = editorSpecs.globalClipViewPathCompressionAmplifier.toString()
        _maxPanelViewHeightDp = editorSpecs.maxPanelViewHeightDp.value.toInt().toString()
        _minPanelViewHeightDp = editorSpecs.minPanelViewHeightDp.value.toInt().toString()
        _xStepDpPerSec = editorSpecs.xStepDpPerSec.value.toInt().toString()
        _transformZoomClickCoef = editorSpecs.transformZoomClickCoef.toString()
        _transformOffsetScrollCoef = editorSpecs.transformOffsetScrollCoef.toString()
        _transformZoomScrollCoef = editorSpecs.transformZoomScrollCoef.toString()
        _mutableDraggableAreaFraction = editorSpecs.mutableDraggableAreaFraction.toString()
        _immutableDraggableAreaFraction = editorSpecs.immutableDraggableAreaFraction.toString()
        _minImmutableAreaWidthWinDp = editorSpecs.minImmutableAreaWidthWinDp.value.toInt().toString()
        _minMutableAreaWidthWinDp = editorSpecs.minMutableAreaWidthWinDp.value.toInt().toString()
        _preferredImmutableAreaWidthWinDp = editorSpecs.preferredImmutableAreaWidthWinDp.value.toInt().toString()
        _silenceTransformerSilenceDurationMsIncrementStep = (editorSpecs.silenceTransformerSilenceDurationUsIncrementStep / 1000).toString()
        _canSave = false
    }

    /* Methods */

}