package viewmodels.impl.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Density
import kotlinx.coroutines.CoroutineScope
import model.api.editor.audio.AudioClipEditingService
import specs.api.mutable.MutableEditorSpecs
import viewmodels.api.editor.EditorViewModel
import viewmodels.api.editor.panel.ClipPanelViewModel
import viewmodels.api.utils.AdvancedPcmPathBuilder
import viewmodels.impl.editor.panel.ClipPanelViewModelImpl
import java.io.File

class EditorViewModelImpl(
    private val audioClipEditingService: AudioClipEditingService,
    private val pcmPathBuilder: AdvancedPcmPathBuilder,
    private val parentViewModel: Parent,
    private val coroutineScope: CoroutineScope,
    private val density: Density,
    private val specs: MutableEditorSpecs
): EditorViewModel, ClipPanelViewModelImpl.Parent {
    /* Parent ViewModels */
    interface Parent {
        val selectedClipId: String?
        val canOpenClips: Boolean
        fun openClips()
        fun notifyMutated(clipId: String, mutated: Boolean)
        fun notifySaving(clipId: String, saving: Boolean)
    }

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    private var _panelViewModels: Map<String, ClipPanelViewModel> by mutableStateOf(emptyMap())

    override val selectedPanel: ClipPanelViewModel get() = _panelViewModels[parentViewModel.selectedClipId]!!

    override val canOpenClips: Boolean get() = parentViewModel.canOpenClips

    /* Callbacks */

    /* Methods */
    override fun submitClip(clipId: String, clipFile: File) {
        if (!_panelViewModels.containsKey(clipId)) {
            val newPanelViewModel = ClipPanelViewModelImpl(
                clipId, clipFile, this, audioClipEditingService, pcmPathBuilder,
                coroutineScope, density, specs
            )

            _panelViewModels = HashMap(_panelViewModels) + (clipId to newPanelViewModel)
        }
    }

    override suspend fun saveClip(clipId: String) {
        _panelViewModels[clipId]!!.save()
    }

    override suspend fun removeClip(clipId: String) {
        _panelViewModels = HashMap(_panelViewModels).apply {
            remove(clipId)!!.close()
        }
    }

    override fun isMutated(clipId: String): Boolean {
        return _panelViewModels[clipId]!!.isMutated
    }

    override fun openClips() {
        parentViewModel.openClips()
    }

    override fun notifyMutated(clipId: String, mutated: Boolean) {
        parentViewModel.notifyMutated(clipId, mutated)
    }

    override fun notifySaving(clipId: String, saving: Boolean) {
        parentViewModel.notifySaving(clipId, saving)
    }
}
/*
class EditorViewModelImpl(
    private val audioClipEditingService: AudioClipEditingService,
    private val pcmPathBuilder: AdvancedPcmPathBuilder,
    private val parentViewModel: Parent,
    private val coroutineScope: CoroutineScope,
    private val density: Density,
    private val specs: MutableEditorSpecs
): EditorViewModel, ClipPanelViewModelImpl.Parent {
    /* Parent ViewModels */
    interface Parent {
        val selectedClipId: String?
        val canOpenClips: Boolean
        fun openClips()
        fun notifyMutated(clipId: String, isMutated: Boolean)
//        fun removeClip(clipId: String)
    }

    /* Child ViewModels */

    /* Simple properties */

    /* Stateful properties */
    private var _panelViewModels: Map<String, ClipPanelViewModel> by mutableStateOf(emptyMap())

    override val selectedPanel: ClipPanelViewModel? by derivedStateOf {
        parentViewModel.selectedClipId?.let { _panelViewModels[it] }
    }

    override val canOpenClips: Boolean get() = parentViewModel.canOpenClips

    /* Callbacks */

    /* Methods */
    override fun submitClip(clipId: String, clipFile: File) {
        if (!_panelViewModels.containsKey(clipId)) {
            val newClipPanelViewModel = ClipPanelViewModelImpl(
                clipFile, clipId, this, audioClipEditingService,
                pcmPathBuilder, coroutineScope, density, specs
            )
            _panelViewModels = HashMap(_panelViewModels) + (clipId to newClipPanelViewModel)
        }
    }

    override fun openClips() {
        parentViewModel.openClips()
    }

    override fun notifyMutated(clipId: String, isMutated: Boolean) {
        parentViewModel.notifyMutated(clipId, isMutated)
    }
}
 */