package org.jetbrains.jewel.painter

import androidx.compose.runtime.Immutable
import org.jetbrains.jewel.util.inDebugMode
import java.net.URL

/**
 * Resolve resource by [ClassLoader]s.
 *
 * It is recommended that the author of the component library
 * provide an object to ensure that the loaded resources are
 * correct when displaying resources in component library modules.
 *
 * @see org.jetbrains.jewel.intui.standalone.StandaloneResourceResolver
 * @see org.jetbrains.jewel.intui.window.IntUiDecoratedWindowResourceResolver
 * @see org.jetbrains.jewel.bridge.BridgeResourceResolver
 */
@Immutable
open class ClassLoaderResourceResolver(vararg classLoaders: ClassLoader) : ResourceResolver {

    private val classLoaders = classLoaders.distinct()

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
}
