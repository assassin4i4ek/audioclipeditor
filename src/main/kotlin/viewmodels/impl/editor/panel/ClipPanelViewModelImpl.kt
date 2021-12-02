package viewmodels.impl.editor.panel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import model.api.editor.clip.AudioClip
import model.api.editor.clip.AudioClipService
import viewmodels.api.InputDevice
import viewmodels.api.editor.panel.ClipPanelViewModel
import viewmodels.api.editor.panel.clip.ClipViewModel
import viewmodels.api.utils.AdvancedPcmPathBuilder
import viewmodels.api.utils.PcmPathBuilder
import viewmodels.api.utils.PreferenceHolder
import viewmodels.api.utils.PreferenceSavableStatefulProperty
import viewmodels.impl.editor.panel.clip.ClipViewModelImpl
import java.io.File

class ClipPanelViewModelImpl(
    clipFile: File,
    private val parentViewModel: Parent,
    private val audioClipService: AudioClipService,
    private val pcmPathBuilder: AdvancedPcmPathBuilder,
    coroutineScope: CoroutineScope,
    private val preferenceHolder: PreferenceHolder
): ClipPanelViewModel {
    /* Parent ViewModels */
    interface Parent {
        val inputDevice: InputDevice

        fun openClips()
        fun switchInputDevice()
    }

    /* Child ViewModels */
    override val editableClipViewModel: ClipViewModel = ClipViewModelImpl(
        object : ClipViewModelImpl.Parent {
            override val pathBuilderXStep: Int
                get() = pcmPathBuilder.getRecommendedStep(1f, 1f)
        },
        pcmPathBuilder, coroutineScope
    )
    override val globalClipViewModel: ClipViewModel = ClipViewModelImpl(
        object : ClipViewModelImpl.Parent {
            override val pathBuilderXStep: Int
                get() = 15
        }, pcmPathBuilder, coroutineScope
    )

    /* Stateful properties */
    private var _isLoading: Boolean by mutableStateOf(true)
    override val isLoading: Boolean get() = _isLoading
    override val inputDevice: InputDevice get() = parentViewModel.inputDevice

    private var _maxPanelViewHeightDp: Dp by PreferenceSavableStatefulProperty(
        300.dp, preferenceHolder.preferences,
        { it.value }, { it.dp }
    )
    override val maxPanelViewHeightDp: Dp
        get() = _maxPanelViewHeightDp

    private var _minPanelViewHeightDp: Dp by PreferenceSavableStatefulProperty(
        100.dp, preferenceHolder.preferences,
        { it.value }, { it.dp }
    )
    override val minPanelViewHeightDp: Dp
        get() = _minPanelViewHeightDp
    
    /* Callbacks */
    init {
        coroutineScope.launch {
            val fetchedAudioClip = audioClipService.openAudioClip(clipFile)

            editableClipViewModel.submitClip(fetchedAudioClip)
            globalClipViewModel.submitClip(fetchedAudioClip)

            _isLoading = false
        }
    }

    override fun onOpenClips() {
        parentViewModel.openClips()
    }

    override fun onSwitchInputDevice() {
        parentViewModel.switchInputDevice()
    }

    override fun onSizeChanged(size: IntSize) {

    }

    /* Methods */
}