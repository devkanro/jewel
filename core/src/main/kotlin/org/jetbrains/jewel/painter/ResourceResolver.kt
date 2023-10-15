package org.jetbrains.jewel.painter

import androidx.compose.runtime.Immutable
import java.net.URL

@Immutable
interface ResourceResolver {

    fun resolve(path: String): URL?

    fun getClassLoaders(): List<ClassLoader>
}
