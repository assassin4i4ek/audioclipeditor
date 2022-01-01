package model.impl.editor.audio.preprocess

import kotlinx.coroutines.*
import model.api.editor.audio.clip.AudioClip
import model.api.editor.audio.preprocess.FragmentResolver
import model.api.utils.ResourceResolver
import org.tensorflow.SavedModelBundle

class FragmentResolverImpl(
    resourceResolver: ResourceResolver,
    coroutineScope: CoroutineScope
): FragmentResolver {
    private val model: Deferred<SavedModelBundle> = coroutineScope.async {
        loadModel(resourceResolver.getResourceAbsolutePath("models/saved_model_1"))
    }

    private suspend fun loadModel(modelPath: String): SavedModelBundle {
        return withContext(Dispatchers.IO) {
            SavedModelBundle.load(modelPath)
        }
    }

    override suspend fun resolve(clip: AudioClip) {
        withContext(Dispatchers.Default) {
            val model = model.await()

        }
    }
}