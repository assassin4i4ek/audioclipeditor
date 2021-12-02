package viewmodels.impl.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import model.api.editor.clip.AudioClipService
import viewmodels.api.InputDevice
import viewmodels.api.utils.PreferenceSavableStatefulProperty
import viewmodels.api.editor.ClipEditorViewModel
import viewmodels.api.editor.OpenedClipsTabViewModel
import viewmodels.api.editor.panel.ClipPanelViewModel
import viewmodels.api.utils.AdvancedPcmPathBuilder
import viewmodels.api.utils.PcmPathBuilder
import viewmodels.api.utils.PreferenceHolder
import viewmodels.impl.editor.panel.ClipPanelViewModelImpl
import java.io.File

class ClipEditorViewModelImpl(
    private val audioClipService: AudioClipService,
    private val pcmPathBuilder: AdvancedPcmPathBuilder,
    private val coroutineScope: CoroutineScope,
    private val preferenceHolder: PreferenceHolder
): ClipEditorViewModel, OpenedClipsTabViewModelImpl.Parent, ClipPanelViewModelImpl.Parent {
    /* Parent ViewModels */

    /* Child ViewModels */
    override val openedClipsTabViewModel: OpenedClipsTabViewModel = OpenedClipsTabViewModelImpl(this)

    /* Stateful Properties */
    private var _showFileChooser by mutableStateOf(false)
    override val showFileChooser: Boolean get() = _showFileChooser

    private var _panelViewModels: Map<String, ClipPanelViewModel> by mutableStateOf(emptyMap())

    override val selectedPanel: ClipPanelViewModel?
        get() = openedClipsTabViewModel.selectedClipId?.let { _panelViewModels[it] }

    private var _inputDevice by PreferenceSavableStatefulProperty(
        InputDevice.Touchpad, preferenceHolder.preferences,
        { it.name }, { InputDevice.valueOf(it) }
    )
    override val inputDevice: InputDevice get() = _inputDevice

    /* Callbacks */
    override fun onOpenClips() {
        _showFileChooser = true
    }

    override fun onSubmitClips(audioClipFiles: List<File>) {
        val clipFilesToAppend = audioClipFiles
            .associateBy { audioClipFile -> audioClipService.getAudioClipId(audioClipFile) }
            .filter { (id, _) -> !_panelViewModels.containsKey(id) }

        val viewModelsToAppend = clipFilesToAppend
            .mapValues { (_, clipFile) ->
                ClipPanelViewModelImpl(
                    clipFile = clipFile,
                    parentViewModel = this,
                    audioClipService = audioClipService,
                    pcmPathBuilder = pcmPathBuilder,
                    coroutineScope = coroutineScope,
                    preferenceHolder = preferenceHolder
                )
            }

        _showFileChooser = false
        _panelViewModels = HashMap(_panelViewModels + viewModelsToAppend)
        openedClipsTabViewModel.submitClips(clipFilesToAppend.mapValues { it.value.nameWithoutExtension })
    }

    /* Methods */
    override fun removeClip(clipId: String) {
        require(_panelViewModels.containsKey(clipId)) {
            "Trying to remove panel view model with id $clipId which is absent if $_panelViewModels"
        }
        _panelViewModels = HashMap(_panelViewModels).apply { remove(clipId) }
    }

    override fun openClips() {
        onOpenClips()
    }

    override fun switchInputDevice() {
        val currentInputDeviceIndex = InputDevice.values().indexOf(inputDevice)
        _inputDevice = InputDevice.values()[(currentInputDeviceIndex + 1) % InputDevice.values().size]
    }
}