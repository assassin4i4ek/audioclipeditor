package model.api.utils

import java.io.File
import java.nio.file.Paths
import kotlin.io.path.absolutePathString

interface ResourceResolver {
    fun getResourceAbsolutePath(resource: String): String
}