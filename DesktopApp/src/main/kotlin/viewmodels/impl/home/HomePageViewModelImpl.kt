package viewmodels.impl.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import model.api.mailing.AudioClipMailingService
import viewmodels.api.home.HomePageViewModel
import viewmodels.api.home.ProcessingClipViewModel
import java.io.File

class HomePageViewModelImpl(
    private val audioClipMailingService: AudioClipMailingService,
    private val parentViewModel: Parent,
    private val coroutineScope: CoroutineScope
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
    override val canFetchClips: Boolean get() = !_isFetching

    override val canOpenClips: Boolean get() = parentViewModel.canOpenClips

    private var _processingClips: Map<String, ProcessingClipViewModel> by mutableStateOf(LinkedHashMap())
    override val processingClips: List<ProcessingClipViewModel> get() = _processingClips.values.toList()

    /* Callbacks */
    override fun onOpenClipsClick() {
        parentViewModel.openClips()
    }

    override fun onFetchClipsClick() {
        coroutineScope.launch {
            _isFetching = true
            audioClipMailingService.fetchAudioClipFromMailBox().collect { clipFile ->
                parentViewModel.submitClip(clipFile)
            }
            _isFetching = false
        }
    }

    /* Methods */
    override fun submitClip(clipId: String, clipFile: File) {
        if (!_processingClips.containsKey(clipId)) {
            _processingClips = LinkedHashMap(_processingClips) + (clipId to ProcessingClipViewModelImpl(clipId, clipFile, this))
        }
    }

    override fun openClipInEditor(clipId: String, clipFile: File) {
        parentViewModel.submitClip(clipFile)
        parentViewModel.selectClip(clipId)
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