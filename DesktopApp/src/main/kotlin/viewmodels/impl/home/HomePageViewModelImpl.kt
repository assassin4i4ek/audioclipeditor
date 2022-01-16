package viewmodels.impl.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import model.api.mailing.AudioClipMailingService
import viewmodels.api.BaseViewModel
import viewmodels.api.home.HomePageViewModel
import java.io.File

class HomePageViewModelImpl(
    private val audioClipMailingService: AudioClipMailingService,
    private val parentViewModel: ParentViewModel,
    private val coroutineScope: CoroutineScope
): HomePageViewModel {
    /* Parent ViewModels */
    interface ParentViewModel {
        fun submitClips(audioClipFiles: List<File>)
    }

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    private var _canFetchAudioClips: Boolean by mutableStateOf(true)
    override val canFetchAudioClips: Boolean get() = _canFetchAudioClips

    private var _downloadedFiles: List<File> by mutableStateOf(emptyList())
    override val downloadedFiles: List<File> get() = _downloadedFiles

    /* Callbacks */
    override fun onFetchAudioClipsClick() {
        coroutineScope.launch {
            audioClipMailingService.fetchAudioClipFromMailBox().collect { clipFile ->
                _downloadedFiles = downloadedFiles + clipFile
                parentViewModel.submitClips(listOf(clipFile))
            }
        }
    }

    /* Methods */

}