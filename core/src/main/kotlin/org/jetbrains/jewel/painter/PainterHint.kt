package org.jetbrains.jewel.painter

import androidx.compose.runtime.Immutable
import org.w3c.dom.Element

@Immutable
interface PainterHint {

    companion object Empty : PainterHint
}

@Immutable
interface PainterPathHint : PainterHint {

    fun patch(path: String, classLoaders: List<ClassLoader>): String {
        return patch(path)
    }

    fun patch(path: String): String
}

@Immutable
interface PainterSvgPatchHint : PainterHint {

    fun patch(element: Element)
}

@Immutable
abstract class PainterPrefixHint : PainterPathHint {

    override fun patch(path: String): String = buildString {
        append(path.substringBeforeLast('/', ""))
        append('/')
        append(prefix())
        append(path.substringBeforeLast('.').substringAfterLast('/'))

        append('.')
        append(path.substringAfterLast('.'))
    }

    abstract fun prefix(): String
}

@Immutable
abstract class PainterSuffixHint : PainterPathHint {

    override fun patch(path: String): String = buildString {
        append(path.substringBeforeLast('/', ""))
        append('/')
        append(path.substringBeforeLast('.').substringAfterLast('/'))
        append(suffix())

        append('.')
        append(path.substringAfterLast('.'))
    }

    abstract fun suffix(): String
}
