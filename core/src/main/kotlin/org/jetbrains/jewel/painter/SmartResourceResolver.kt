package org.jetbrains.jewel.painter

import androidx.compose.runtime.Immutable
import java.net.URL

@Immutable
open class SmartResourceResolver(private vararg val classLoaders: ClassLoader) : ResourceResolver {

    private val verbose = true

    override fun getClassLoaders(): List<ClassLoader> {
        val parentClassLoader =
            StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).callerClass.classLoader

        return listOf(*classLoaders, parentClassLoader)
    }

    override fun resolve(path: String): URL? {
        val path = path.removePrefix("/")
        val parentClassLoader =
            StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).callerClass.classLoader

        for (classLoader in classLoaders) {
            val stream = classLoader.getResource(path)
            if (stream != null) {
                if (verbose) println("Found resource: '$path'")
                return stream
            }
        }

        return parentClassLoader.getResource(path)
    }

    companion object : SmartResourceResolver()
}
