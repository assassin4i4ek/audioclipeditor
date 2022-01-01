package model.impl.utils

import model.api.utils.ResourceResolver
import java.nio.file.Paths
import kotlin.io.path.absolutePathString

class ResourceResolverImpl: ResourceResolver {
    override fun getResourceAbsolutePath(resource: String): String {
        return Paths.get(Unit.javaClass.classLoader.getResource(resource)!!.toURI()).absolutePathString()
    }
}