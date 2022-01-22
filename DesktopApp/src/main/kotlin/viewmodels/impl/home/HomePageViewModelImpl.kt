package viewmodels.impl.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import model.api.accounting.AudioClipAccountingService
import model.api.txrx.AudioClipTxRxService
import specs.api.immutable.ApplicationSpecs
import specs.api.immutable.SavingSpecs
import specs.api.mutable.MutableAudioClipTxRxServiceSpecs
import viewmodels.api.home.HomePageViewModel
import viewmodels.api.home.ProcessingClipViewModel
import java.io.File

class HomePageViewModelImpl(
    private val audioClipTxRxService: AudioClipTxRxService,
    private val audioClipAccountingService: AudioClipAccountingService,
    private val parentViewModel: Parent,
    private val coroutineScope: CoroutineScope,
    private val applicationSpecs: ApplicationSpecs,
    private val savingSpecs: SavingSpecs,
    private val txRxSpecs: MutableAudioClipTxRxServiceSpecs,
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
        fun notifyError(message: String)
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

    private var _userEmail: String by mutableStateOf(txRxSpecs.userEmail)
    override val userEmail: String get() = _userEmail

    private var _userPassword: String by mutableStateOf(txRxSpecs.userPassword)
    override val userPassword: String get() = _userPassword

    private var _receiveFromEmail: String by mutableStateOf(txRxSpecs.receivedFromEmail)
    override val receiveFromEmail: String get() = _receiveFromEmail

    private var _sendToEmail: String by mutableStateOf(txRxSpecs.sendToEmail)
    override val sendToEmail: String get() = _sendToEmail

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

    override fun onUserEmailChange(newEmail: String) {
        _userEmail = newEmail
    }

    override fun onUserPasswordChange(newPassword: String) {
        _userPassword = newPassword
    }

    override fun onReceiveFromEmailChange(newReceiveFromEmail: String) {
        _receiveFromEmail = newReceiveFromEmail
    }

    override fun onSendToEmailChange(newSendToEmail: String) {
        _sendToEmail = newSendToEmail
    }

    /* Methods */
    init {
        if (applicationSpecs.fetchClipsOnAppStart) {
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
        updateCredentials()
        coroutineScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                audioClipTxRxService.receiveAudioClipFiles().collect { clipFile ->
                    parentViewModel.submitClip(clipFile)
                }
            }.onFailure {
                println(it)
                parentViewModel.notifyError("Error during message fetching audio clips:\n${it.message ?: it}")
            }
            _isFetching = false
        }
    }

    private fun processClips() {
        _isProcessing = true
        updateCredentials()
        coroutineScope.launch(Dispatchers.Default) {
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
            audioClipTxRxService.transmitAudioClipFiles(transformedClipFiles)
            // account
            audioClipAccountingService.logProcessed(transformedClipFiles)
            // cleanup
            _processingClips.forEach { (_, clipViewModel) ->
                withContext(Dispatchers.IO) {
                    clipViewModel.clipFile.delete()
                }
            }

            _processingClips = LinkedHashMap()
            _isProcessing = false

            if (applicationSpecs.closeAppOnProcessingFinish) {
                parentViewModel.requestCloseApplication()
            }
        }
    }

    private fun updateCredentials() {
        if (userEmail != txRxSpecs.userEmail) {
            txRxSpecs.userEmail = userEmail
        }
        if (userPassword != txRxSpecs.userPassword) {
            txRxSpecs.userPassword = userPassword
        }
        if (receiveFromEmail != txRxSpecs.receivedFromEmail) {
            txRxSpecs.receivedFromEmail = receiveFromEmail
        }
        if (sendToEmail != txRxSpecs.sendToEmail) {
            txRxSpecs.sendToEmail = sendToEmail
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
