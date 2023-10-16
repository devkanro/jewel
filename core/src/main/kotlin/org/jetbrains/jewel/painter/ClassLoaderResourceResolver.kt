package org.jetbrains.jewel.painter

import androidx.compose.runtime.Immutable
import org.jetbrains.jewel.util.inDebugMode
import java.net.URL

@Immutable
open class ClassLoaderResourceResolver(private vararg val classLoaders: ClassLoader) : ResourceResolver {

    override fun getClassLoaders(): List<ClassLoader> = classLoaders.toList()

    override fun resolve(path: String): URL? {
        val normalized = path.removePrefix("/")

        for (classLoader in classLoaders) {
            val stream = classLoader.getResource(normalized)
            if (stream != null) {
                if (inDebugMode) println("Found resource: '$normalized'")
                return stream
            }
        }

        return null
    }

    companion object : ClassLoaderResourceResolver()
}
