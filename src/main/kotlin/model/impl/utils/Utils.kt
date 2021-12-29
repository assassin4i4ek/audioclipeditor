package model.impl.utils

import java.io.File
import java.nio.file.Paths
import kotlin.io.path.absolutePathString

fun getLocalResourcePath(resource: String): String {
    val localResourceDirPath = System.getProperty("compose.application.resources.dir")
    return if (localResourceDirPath != null) {
        val localResourceDir = File(localResourceDirPath)
        val adjustedResource = resource.substringAfter("common/").ifEmpty { resource }
        localResourceDir.resolve(adjustedResource).absolutePath
    }
    else {
        Paths.get(Unit.javaClass.classLoader.getResource(resource)!!.toURI()).absolutePathString()
    }
}