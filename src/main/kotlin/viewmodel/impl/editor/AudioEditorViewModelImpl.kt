package viewmodel.impl.editor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import model.api.editor.clip.AudioClipService
import specs.api.immutable.editor.AudioEditorViewModelSpecs
import specs.api.immutable.editor.InputDevice
import specs.api.mutable.MutableSpecStore
import specs.api.mutable.editor.MutableAudioEditorViewModelSpecs
import states.api.immutable.editor.AudioEditorState
import states.api.mutable.editor.MutableAudioEditorState
import states.api.mutable.editor.panel.MutableAudioPanelState
import states.impl.editor.MutableAudioEditorStateImpl
import states.impl.editor.panel.MutableAudioPanelStateImpl
import states.impl.editor.panel.clip.MutableAudioClipStateImpl
import states.impl.editor.panel.layout.MutableLayoutStateImpl
import states.impl.editor.panel.transform.MutableTransformStateImpl
import viewmodel.api.editor.AudioEditorViewModel
import viewmodel.api.editor.panel.AudioPanelParentViewModel
import viewmodel.api.editor.panel.PcmPathBuilder
import java.io.File

class AudioEditorViewModelImpl(
    private val audioClipService: AudioClipService,
    private val pcmPathBuilder: PcmPathBuilder,
    private val coroutineScope: CoroutineScope,
    specStore: MutableSpecStore
): AudioEditorViewModel, AudioPanelParentViewModel {
    private val mutableAudioEditorState: MutableAudioEditorState = MutableAudioEditorStateImpl(
        showFileDialog = false,
        openedAudioClipStates = LinkedHashMap(),
        selectedAudioClipStateId = null,
    )
    override val audioEditorState: AudioEditorState get() = mutableAudioEditorState

    private val mutableSpecs: MutableAudioEditorViewModelSpecs = with(specStore) { mutableAudioEditorViewModelSpecs }
    override val specs: AudioEditorViewModelSpecs get() = mutableSpecs

    override fun onOpenAudioClips() {
        mutableAudioEditorState.showFileChooser = true
    }

    override fun onSubmitAudioClips(audioClipFiles: List<File>) {
        val intermediateAudioClipFiles = audioClipFiles
            .associateBy { audioClipFile -> audioClipService.getAudioClipId(audioClipFile) }
            .filter { (id, _) -> !audioEditorState.openedAudioPanelStates.containsKey(id) }

        val intermediateStates = intermediateAudioClipFiles
            .mapValues { (_, audioClipFile) ->
                val layoutState = MutableLayoutStateImpl(0f, 0f, 0f)
                val transformState = MutableTransformStateImpl(0f, 1f, layoutState)
                MutableAudioPanelStateImpl(
                    audioClipState = MutableAudioClipStateImpl(
                        name = audioClipFile.nameWithoutExtension,
                        channelPcmPaths = null,
                        audioClip = null
                    ),
                    isLoading = true,
                    transformState = transformState,
                    layoutState = layoutState
                )
            }

        mutableAudioEditorState.showFileChooser = false
        mutableAudioEditorState.openedAudioPanelStates = LinkedHashMap(
            mutableAudioEditorState.openedAudioPanelStates + intermediateStates
        )

        if (audioEditorState.selectedAudioPanelStateId == null && audioEditorState.openedAudioPanelStates.isNotEmpty()) {
            mutableAudioEditorState.selectedAudioPanelStateId = audioEditorState.openedAudioPanelStates.keys.first()
        }

        coroutineScope.launch {
            intermediateAudioClipFiles.forEach { (id, audioClipFile) ->
                if (audioEditorState.openedAudioPanelStates.containsKey(id)) {
                    val fetchedAudioClip = audioClipService.openAudioClip(audioClipFile)

                    if (audioEditorState.openedAudioPanelStates.containsKey(id)) {
                        val audioPanelState = mutableAudioEditorState.openedAudioPanelStates[id]!!.apply {
                            isLoading = false
                            audioClipState.audioClip = fetchedAudioClip
                        }

                        val xStep = pcmPathBuilder.getRecommendedStep(
                            specs.pathCompressionAmplifier,
                            audioPanelState.transformState.zoom
                        )
                        val builtChannelPaths = fetchedAudioClip.channelsPcm.map {
                            pcmPathBuilder.build(it, xStep)
                        }

                        if (audioEditorState.openedAudioPanelStates.containsKey(id)) {
                            mutableAudioEditorState.openedAudioPanelStates[id]!!.apply {
                                audioClipState.channelPcmPaths = builtChannelPaths
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onSelectAudioClip(stateId: String) {
        require(audioEditorState.openedAudioPanelStates.containsKey(stateId)) {
            "Tried to select an absent audio with id = $stateId from ${audioEditorState.openedAudioPanelStates}"
        }
        mutableAudioEditorState.selectedAudioPanelStateId = stateId
    }

    override fun onRemoveAudioClip(stateId: String) {
        require(audioEditorState.openedAudioPanelStates.containsKey(stateId)) {
            "Tried to remove an absent audio with id = $stateId from ${audioEditorState.openedAudioPanelStates}"
        }

        val indexToRemove = audioEditorState.openedAudioPanelStates.keys.indexOf(stateId)

        mutableAudioEditorState.openedAudioPanelStates = LinkedHashMap(
            mutableAudioEditorState.openedAudioPanelStates
        ).apply {
            remove(stateId)
        }.also { newSelectedAudioClips ->
            if (newSelectedAudioClips.isNotEmpty()) {
                // last element has NOT been removed
                if (indexToRemove <= audioEditorState.selectedAudioPanelStateIndex) {
                    mutableAudioEditorState.selectedAudioPanelStateId = newSelectedAudioClips.keys.elementAt(
                        (audioEditorState.selectedAudioPanelStateIndex - 1).coerceAtLeast(0)
                    )
                }
            }
            else {
                mutableAudioEditorState.selectedAudioPanelStateId = null
            }
        }
        mutableAudioEditorState.openedAudioPanelStates = LinkedHashMap(
            mutableAudioEditorState.openedAudioPanelStates
        ).apply {
            remove(stateId)
        }
    }

    override fun onSwitchInputDevice() {
        val currentInputDeviceIndex = InputDevice.values().indexOf(mutableSpecs.inputDevice)
        mutableSpecs.inputDevice = InputDevice.values()[(currentInputDeviceIndex + 1) % InputDevice.values().size]
    }

    override val inputDevice: InputDevice
        get() = specs.inputDevice

    override val pathCompressionAmplifier: Float
        get() = specs.pathCompressionAmplifier

    override val selectedMutableAudioClipState: MutableAudioPanelState
        get() = mutableAudioEditorState.selectedAudioPanelState
}