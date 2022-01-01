package utils

import model.api.utils.ResourceResolver
import java.io.File
import java.nio.file.Paths
import kotlin.io.path.absolutePathString

class ComposeResourceResolverImpl: ResourceResolver {
    override fun getResourceAbsolutePath(resource: String): String {
        val localResourceDirPath = System.getProperty("compose.application.resources.dir")
        return if (localResourceDirPath != null) {
            val localResourceDir = File(localResourceDirPath)
            val adjustedResource = resource.substringAfter("common/").ifEmpty { resource }
            localResourceDir.resolve(adjustedResource).absolutePath
        } else {
            Paths.get(Unit.javaClass.classLoader.getResource(resource)!!.toURI()).absolutePathString()
        }
    }
}