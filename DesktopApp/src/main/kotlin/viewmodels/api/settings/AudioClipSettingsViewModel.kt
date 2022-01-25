package viewmodels.api.settings

import viewmodels.api.BaseViewModel

interface AudioClipSettingsViewModel: BaseViewModel {
    /* Parent ViewModels */

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    val useBellTransformerForFirstFragment: Boolean

    val minImmutableAreaDurationMs: String
    val minMutableAreaDurationMs: String

    val defaultSilenceTransformerSilenceDurationMs: String
    val lastFragmentSilenceDurationMs: String

    val normalizationRmsDb: String
    val normalizationCompressorThresholdDb: String
    val normalizationCompressorAttackTimeMs: String
    val normalizationCompressorReleaseTimeMs: String

    val dataLineMaxBufferDesolation: String
    val saveMp3bitRate: String

    val canSave: Boolean

    /* Callbacks */
    fun onDataLineMaxBufferDesolation(newDataLineMaxBufferDesolation: String)
    fun onMinImmutableAreaDurationMs(newMinImmutableAreaDurationMs: String)
    fun onMinMutableAreaDurationMs(newMinMutableAreaDurationMs: String)
    fun onDefaultSilenceTransformerSilenceDurationMs(newDefaultSilenceTransformerSilenceDurationMs: String)
    fun onUseBellTransformerForFirstFragment(newUseBellTransformerForFirstFragment: Boolean)
    fun onLastFragmentSilenceDurationMs(newLastFragmentSilenceDurationMs: String)
    fun onNormalizationRmsDb(newNormalizationRmsDb: String)
    fun onNormalizationCompressorThresholdDb(newNormalizationCompressorThresholdDb: String)
    fun onNormalizationCompressorAttackTimeMs(newNormalizationCompressorAttackTimeMs: String)
    fun onNormalizationCompressorReleaseTimeMs(newNormalizationCompressorReleaseTimeMs: String)
    fun onSaveMp3bitRate(newSaveMp3bitRate: String)
    fun onRefreshTextFieldValues()
    fun onSaveClick()
    fun onResetClick()

    /* Methods */

}