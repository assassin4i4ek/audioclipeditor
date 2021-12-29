package model.impl.editor.audio.preprocess

import model.api.editor.audio.clip.AudioClip
import model.api.editor.audio.preprocess.PreprocessRoutine

class PreprocessRoutineImpl: PreprocessRoutine {
    private val routineSequence: MutableList<suspend (AudioClip) -> Unit> = mutableListOf()

    override fun then(fn: suspend (clip: AudioClip) -> Unit): PreprocessRoutine {
        routineSequence.add(fn)
        return this
    }

    override suspend fun apply(clip: AudioClip) {
        routineSequence.forEach {
            it(clip)
        }
    }
}