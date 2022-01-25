package viewmodels.impl.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import specs.api.mutable.MutableAudioEditingServiceSpecs
import viewmodels.api.settings.AudioClipSettingsViewModel

class AudioClipSettingsViewModelImpl(
    private val editingServiceSpecs: MutableAudioEditingServiceSpecs
) : AudioClipSettingsViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    private var _dataLineMaxBufferDesolation: String by mutableStateOf(editingServiceSpecs.dataLineMaxBufferDesolation.toString())
    override val dataLineMaxBufferDesolation: String get() = _dataLineMaxBufferDesolation

    private var _minImmutableAreaDurationMs: String by mutableStateOf((editingServiceSpecs.minImmutableAreaDurationUs / 1000).toString())
    override val minImmutableAreaDurationMs: String get() = _minImmutableAreaDurationMs

    private var _minMutableAreaDurationMs: String by mutableStateOf((editingServiceSpecs.minMutableAreaDurationUs / 1000).toString())
    override val minMutableAreaDurationMs: String get() = _minMutableAreaDurationMs

    private var _defaultSilenceTransformerSilenceDurationMs: String by mutableStateOf((editingServiceSpecs.defaultSilenceTransformerSilenceDurationUs / 1000).toString())
    override val defaultSilenceTransformerSilenceDurationMs: String get() = _defaultSilenceTransformerSilenceDurationMs

    private var _useBellTransformerForFirstFragment: Boolean by mutableStateOf(editingServiceSpecs.useBellTransformerForFirstFragment)
    override val useBellTransformerForFirstFragment: Boolean get() = _useBellTransformerForFirstFragment

    private var _lastFragmentSilenceDurationMs: String by mutableStateOf((editingServiceSpecs.lastFragmentSilenceDurationUs / 1000).toString())
    override val lastFragmentSilenceDurationMs: String get() = _lastFragmentSilenceDurationMs

    private var _normalizationRmsDb: String by mutableStateOf(editingServiceSpecs.normalizationRmsDb.toString())
    override val normalizationRmsDb: String get() = _normalizationRmsDb

    private var _normalizationCompressorThresholdDb: String by mutableStateOf(editingServiceSpecs.normalizationCompressorThresholdDb.toString())
    override val normalizationCompressorThresholdDb: String get() = _normalizationCompressorThresholdDb

    private var _normalizationCompressorAttackTimeMs: String by mutableStateOf(editingServiceSpecs.normalizationCompressorAttackTimeMs.toString())
    override val normalizationCompressorAttackTimeMs: String get() = _normalizationCompressorAttackTimeMs

    private var _normalizationCompressorReleaseTimeMs: String by mutableStateOf(editingServiceSpecs.normalizationCompressorReleaseTimeMs.toString())
    override val normalizationCompressorReleaseTimeMs: String get() = _normalizationCompressorReleaseTimeMs

    private var _saveMp3bitRate: String by mutableStateOf(editingServiceSpecs.saveMp3bitRate.toString())
    override val saveMp3bitRate: String get() = _saveMp3bitRate

    private var _canSave: Boolean by mutableStateOf(false)
    override val canSave: Boolean get() = _canSave

    /* Callbacks */
    override fun onDataLineMaxBufferDesolation(newDataLineMaxBufferDesolation: String) {
        if (newDataLineMaxBufferDesolation.toFloatOrNull() != null || newDataLineMaxBufferDesolation.isEmpty()) {
            _dataLineMaxBufferDesolation = newDataLineMaxBufferDesolation
            _canSave = true
        }
    }

    override fun onMinImmutableAreaDurationMs(newMinImmutableAreaDurationMs: String) {
        if (newMinImmutableAreaDurationMs.toLongOrNull() != null || newMinImmutableAreaDurationMs.isEmpty()) {
            _minImmutableAreaDurationMs = newMinImmutableAreaDurationMs
            _canSave = true
        }
    }

    override fun onMinMutableAreaDurationMs(newMinMutableAreaDurationMs: String) {
        if (newMinMutableAreaDurationMs.toLongOrNull() != null || newMinMutableAreaDurationMs.isEmpty()) {
            _minMutableAreaDurationMs = newMinMutableAreaDurationMs
            _canSave = true
        }
    }

    override fun onDefaultSilenceTransformerSilenceDurationMs(newDefaultSilenceTransformerSilenceDurationMs: String) {
        if (newDefaultSilenceTransformerSilenceDurationMs.toLongOrNull() != null || newDefaultSilenceTransformerSilenceDurationMs.isEmpty()) {
            _defaultSilenceTransformerSilenceDurationMs = newDefaultSilenceTransformerSilenceDurationMs
            _canSave = true
        }
    }

    override fun onUseBellTransformerForFirstFragment(newUseBellTransformerForFirstFragment: Boolean) {
        _useBellTransformerForFirstFragment = newUseBellTransformerForFirstFragment
        _canSave = true
    }

    override fun onLastFragmentSilenceDurationMs(newLastFragmentSilenceDurationMs: String) {
        if (newLastFragmentSilenceDurationMs.toLongOrNull() != null || newLastFragmentSilenceDurationMs.isEmpty()) {
            _lastFragmentSilenceDurationMs = newLastFragmentSilenceDurationMs
            _canSave = true
        }
    }

    override fun onNormalizationRmsDb(newNormalizationRmsDb: String) {
        if (newNormalizationRmsDb.toFloatOrNull() != null || newNormalizationRmsDb.isEmpty()) {
            _normalizationRmsDb = newNormalizationRmsDb
            _canSave = true
        }
    }

    override fun onNormalizationCompressorThresholdDb(newNormalizationCompressorThresholdDb: String) {
        if (newNormalizationCompressorThresholdDb.toFloatOrNull() != null || newNormalizationCompressorThresholdDb.isEmpty()) {
            _normalizationCompressorThresholdDb = newNormalizationCompressorThresholdDb
            _canSave = true
        }
    }

    override fun onNormalizationCompressorAttackTimeMs(newNormalizationCompressorAttackTimeMs: String) {
        if (newNormalizationCompressorAttackTimeMs.toFloatOrNull() != null || newNormalizationCompressorAttackTimeMs.isEmpty()) {
            _normalizationCompressorAttackTimeMs = newNormalizationCompressorAttackTimeMs
            _canSave = true
        }
    }

    override fun onNormalizationCompressorReleaseTimeMs(newNormalizationCompressorReleaseTimeMs: String) {
        if (newNormalizationCompressorReleaseTimeMs.toFloatOrNull() != null || newNormalizationCompressorReleaseTimeMs.isEmpty()) {
            _normalizationCompressorReleaseTimeMs = newNormalizationCompressorReleaseTimeMs
            _canSave = true
        }
    }

    override fun onSaveMp3bitRate(newSaveMp3bitRate: String) {
        if (newSaveMp3bitRate.toIntOrNull() != null || newSaveMp3bitRate.isEmpty()) {
            _saveMp3bitRate = newSaveMp3bitRate
            _canSave = true
        }
    }

    override fun onRefreshTextFieldValues() {
        if (dataLineMaxBufferDesolation.isEmpty()) {
            _dataLineMaxBufferDesolation = editingServiceSpecs.dataLineMaxBufferDesolation.toString()
        }
        if (minImmutableAreaDurationMs.isEmpty()) {
            _minImmutableAreaDurationMs = (editingServiceSpecs.minImmutableAreaDurationUs / 1000).toString()
        }
        if (minMutableAreaDurationMs.isEmpty()) {
            _minMutableAreaDurationMs = (editingServiceSpecs.minMutableAreaDurationUs / 1000).toString()
        }
        if (defaultSilenceTransformerSilenceDurationMs.isEmpty()) {
            _defaultSilenceTransformerSilenceDurationMs = (editingServiceSpecs.defaultSilenceTransformerSilenceDurationUs / 1000).toString()
        }
        if (lastFragmentSilenceDurationMs.isEmpty()) {
            _lastFragmentSilenceDurationMs = (editingServiceSpecs.lastFragmentSilenceDurationUs / 1000).toString()
        }
        if (normalizationRmsDb.isEmpty()) {
            _normalizationRmsDb = editingServiceSpecs.normalizationRmsDb.toString()
        }
        if (normalizationCompressorThresholdDb.isEmpty()) {
            _normalizationCompressorThresholdDb = editingServiceSpecs.normalizationCompressorThresholdDb.toString()
        }
        if (normalizationCompressorAttackTimeMs.isEmpty()) {
            _normalizationCompressorAttackTimeMs = editingServiceSpecs.normalizationCompressorAttackTimeMs.toString()
        }
        if (normalizationCompressorReleaseTimeMs.isEmpty()) {
            _normalizationCompressorReleaseTimeMs = editingServiceSpecs.normalizationCompressorReleaseTimeMs.toString()
        }
        if (saveMp3bitRate.isEmpty()) {
            _saveMp3bitRate = editingServiceSpecs.saveMp3bitRate.toString()
        }
    }

    override fun onSaveClick() {
        editingServiceSpecs.dataLineMaxBufferDesolation = dataLineMaxBufferDesolation.toFloat()
        editingServiceSpecs.minImmutableAreaDurationUs = minImmutableAreaDurationMs.toLong() * 1000
        editingServiceSpecs.minMutableAreaDurationUs = minMutableAreaDurationMs.toLong() * 1000
        editingServiceSpecs.defaultSilenceTransformerSilenceDurationUs = defaultSilenceTransformerSilenceDurationMs.toLong() * 1000
        editingServiceSpecs.useBellTransformerForFirstFragment = useBellTransformerForFirstFragment
        editingServiceSpecs.lastFragmentSilenceDurationUs = lastFragmentSilenceDurationMs.toLong() * 1000
        editingServiceSpecs.normalizationRmsDb = normalizationRmsDb.toFloat()
        editingServiceSpecs.normalizationCompressorThresholdDb = normalizationCompressorThresholdDb.toFloat()
        editingServiceSpecs.normalizationCompressorAttackTimeMs = normalizationCompressorReleaseTimeMs.toFloat()
        editingServiceSpecs.normalizationCompressorReleaseTimeMs = normalizationCompressorReleaseTimeMs.toFloat()
        editingServiceSpecs.saveMp3bitRate = saveMp3bitRate.toInt()
        _canSave = false
    }

    override fun onResetClick() {
        editingServiceSpecs.reset()
        _dataLineMaxBufferDesolation = editingServiceSpecs.dataLineMaxBufferDesolation.toString()
        _minImmutableAreaDurationMs = (editingServiceSpecs.minImmutableAreaDurationUs / 1000).toString()
        _minMutableAreaDurationMs = (editingServiceSpecs.minMutableAreaDurationUs / 1000).toString()
        _defaultSilenceTransformerSilenceDurationMs = (editingServiceSpecs.defaultSilenceTransformerSilenceDurationUs / 1000).toString()
        _useBellTransformerForFirstFragment = editingServiceSpecs.useBellTransformerForFirstFragment
        _lastFragmentSilenceDurationMs = (editingServiceSpecs.lastFragmentSilenceDurationUs / 1000).toString()
        _normalizationRmsDb = editingServiceSpecs.normalizationRmsDb.toString()
        _normalizationCompressorThresholdDb = editingServiceSpecs.normalizationCompressorThresholdDb.toString()
        _normalizationCompressorAttackTimeMs = editingServiceSpecs.normalizationCompressorAttackTimeMs.toString()
        _normalizationCompressorReleaseTimeMs = editingServiceSpecs.normalizationCompressorReleaseTimeMs.toString()
        _saveMp3bitRate = editingServiceSpecs.saveMp3bitRate.toString()
        _canSave = false
    }

    /* Methods */

}