package viewmodels.impl.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import model.api.accounting.AudioClipAccountingService
import model.api.mailing.AudioClipMailingService
import specs.api.immutable.ProcessingSpecs
import specs.api.immutable.SavingSpecs
import viewmodels.api.home.HomePageViewModel
import viewmodels.api.home.ProcessingClipViewModel
import java.io.File

class HomePageViewModelImpl(
    private val audioClipMailingService: AudioClipMailingService,
    private val audioClipAccountingService: AudioClipAccountingService,
    private val parentViewModel: Parent,
    private val coroutineScope: CoroutineScope,
    private val processingSpecs: ProcessingSpecs,
    private val savingSpecs: SavingSpecs
): HomePageViewModel, ProcessingClipViewModelImpl.Parent {
    /* Parent ViewModels */
    interface Parent {
        val canOpenClips: Boolean
        fun openClips()
        fun isClipOpened(clipId: String): Boolean
        fun submitClip(clipFile: File)
        fun selectClip(clipId: String)
        suspend fun saveClip(clipId: String)
        fun requestCloseApplication()
    }

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    private var _isFetching: Boolean by mutableStateOf(false)
    override val isFetchingClips: Boolean get() = _isFetching
    override val canFetchClips: Boolean get() = !_isFetching && !_isProcessing

    override val canOpenClips: Boolean get() = parentViewModel.canOpenClips && !_isProcessing

    private var _isProcessing: Boolean by mutableStateOf(false)
    override val canProcessClips: Boolean get() = !_isFetching && !_isProcessing && _processingClips.isNotEmpty()
    override val isProcessingClips: Boolean get() = _isProcessing

    private var _processingClips: Map<String, ProcessingClipViewModel> by mutableStateOf(LinkedHashMap())
    override val processingClips: List<ProcessingClipViewModel> get() = _processingClips.values.toList()

    /* Callbacks */
    override fun onOpenClipsClick() {
        parentViewModel.openClips()
    }

    override fun onFetchClipsClick() {
        fetchClips()
    }

    override fun onProcessClipsClick() {
        processClips()
    }

    /* Methods */
    init {
        if (processingSpecs.fetchClipsOnAppStart) {
            fetchClips()
        }
    }

    override fun submitClip(clipId: String, clipFile: File) {
        if (!_processingClips.containsKey(clipId)) {
            _processingClips = LinkedHashMap(_processingClips) + (clipId to ProcessingClipViewModelImpl(clipId, clipFile, this))
        }
    }

    override fun openClipInEditor(clipId: String, clipFile: File) {
        parentViewModel.submitClip(clipFile)
        parentViewModel.selectClip(clipId)
    }

    private fun fetchClips() {
        _isFetching = true
        coroutineScope.launch {
            audioClipMailingService.fetchAudioClipFromMailBox().collect { clipFile ->
                parentViewModel.submitClip(clipFile)
            }
            _isFetching = false
        }
    }

    private fun processClips() {
        _isProcessing = true
        coroutineScope.launch {
            // save
            _processingClips.map { (clipId, clipViewModel) ->
                if (parentViewModel.isClipOpened(clipId) && clipViewModel.isMutated && !clipViewModel.isSaving) {
                    launch {
                        println("Saving clip $clipId")
                        parentViewModel.saveClip(clipId)
                    }
                }
                else if (clipViewModel.isSaving) {
                    launch {
                        clipViewModel.waitOnSaved()
                    }
                }
                else null
            }.filterNotNull().joinAll()

            // send
            val transformedClipFiles = _processingClips.map { (clipId, clipViewModel) ->
                savingSpecs.defaultTransformedClipSavingDir.resolve(clipViewModel.name).apply {
                    check(exists()) {
                        "Expected transformed file $clipId does NOT exists in expected path ${savingSpecs.defaultTransformedClipSavingDir.absolutePath}"
                    }
                }
            }
            audioClipMailingService.sendAudioClipToReceiver(transformedClipFiles)
            // account
            audioClipAccountingService.logProcessed(transformedClipFiles)
            // remove
            audioClipMailingService.cleanup(transformedClipFiles)
            _processingClips = LinkedHashMap()

            if (processingSpecs.closeAppOnProcessingFinish) {
                parentViewModel.requestCloseApplication()
            }

            _isProcessing = false
        }
    }

    override fun canOpenInEditorClip(clipId: String): Boolean {
        return !_isProcessing
    }

    override fun canRemoveClip(clipId: String): Boolean {
        return !_isProcessing
    }

    override fun removeClipFromProcessing(clipId: String) {
        _processingClips = LinkedHashMap(_processingClips).apply { remove(clipId)!! }
    }

    override fun notifyMutated(clipId: String, mutated: Boolean) {
        _processingClips[clipId]!!.notifyMutated(mutated)
    }

    override fun notifySaving(clipId: String, saving: Boolean) {
        _processingClips[clipId]!!.notifySaving(saving)
    }
}
