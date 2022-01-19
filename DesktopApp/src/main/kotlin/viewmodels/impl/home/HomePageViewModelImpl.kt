package viewmodels.impl.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import model.api.mailing.AudioClipMailingService
import specs.api.immutable.ProcessingSpecs
import viewmodels.api.home.HomePageViewModel
import viewmodels.api.home.ProcessingClipViewModel
import java.io.File

class HomePageViewModelImpl(
    private val audioClipMailingService: AudioClipMailingService,
    private val parentViewModel: Parent,
    private val coroutineScope: CoroutineScope,
    private val specs: ProcessingSpecs
): HomePageViewModel, ProcessingClipViewModelImpl.Parent {
    /* Parent ViewModels */
    interface Parent {
        val canOpenClips: Boolean
        fun openClips()
        fun submitClip(clipFile: File)
        fun selectClip(clipId: String)
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
        if (specs.fetchClipsOnAppStart) {
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
        coroutineScope.launch {
            _isFetching = true
            audioClipMailingService.fetchAudioClipFromMailBox().collect { clipFile ->
                parentViewModel.submitClip(clipFile)
            }
            _isFetching = false
        }
    }

    private fun processClips() {
        coroutineScope.launch {
            _isProcessing = true
            // save
            // send
            // account
            // remove
            delay(5000)
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
}

/*
class HomePageViewModelImpl(
    private val audioClipMailingService: AudioClipMailingService,
    private val parentViewModel: ParentViewModel,
    private val coroutineScope: CoroutineScope
): HomePageViewModel {
    /* Parent ViewModels */
    interface ParentViewModel {
        fun submitClip(audioClipFile: File)
    }

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    private var _canFetchAudioClips: Boolean by mutableStateOf(true)
    override val canFetchAudioClips: Boolean get() = _canFetchAudioClips

    private var _openedClips: Map<String, HomePageClipViewModel> by mutableStateOf(emptyMap())
    override val openedClips: Map<String, HomePageClipViewModel> get() = _openedClips

    /* Callbacks */
    override fun onFetchAudioClipsClick() {
        coroutineScope.launch {
            audioClipMailingService.fetchAudioClipFromMailBox().collect { clipFile ->
                parentViewModel.submitClip(clipFile)
            }
        }
    }

    /* Methods */
    override fun submitClip(clipId: String, clipFile: File) {
        if (!openedClips.containsKey(clipId)) {
            _openedClips = LinkedHashMap(openedClips) + (clipId to HomePageClipViewModelImpl(clipFile))
        }
    }
}
 */