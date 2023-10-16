package org.jetbrains.jewel.bridge

import com.intellij.util.ui.DirProvider
import org.jetbrains.jewel.painter.ClassLoaderResourceResolver
import org.jetbrains.jewel.painter.ResourcePainterProvider
import org.jetbrains.jewel.painter.ResourceResolver
import java.net.URL

/**
 * [ResourceResolver] to resolve resource in Intellij Module and Bridge module.
 */
object BridgeResourceResolver : ClassLoaderResourceResolver(
    DirProvider::class.java.classLoader,
    BridgeResourceResolver::class.java.classLoader,
) {

    private val dirProvider = DirProvider()

    override fun resolve(path: String): URL? {
        val normalizedPath = path.removePrefix("/")
        val fallbackPath = path.removePrefix(dirProvider.dir())
        return super.resolve(normalizedPath) ?: super.resolve(fallbackPath)
    }
}

fun bridgePainterProvider(path: String, resourceResolver: ResourceResolver = BridgeResourceResolver) =
    ResourcePainterProvider(path, resourceResolver)
