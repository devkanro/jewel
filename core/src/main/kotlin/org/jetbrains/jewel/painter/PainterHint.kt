package org.jetbrains.jewel.painter

import androidx.compose.runtime.Immutable
import org.w3c.dom.Element

/**
 * The [PainterHint] is a hint for [PainterProvider] to load a [Painter][androidx.compose.ui.graphics.painter.Painter].
 * It can used to patch svg paths, handle the dark style icon,
 * replace svg palette by theme etc.
 * Since it is a sealed interface, deriving classes directly from
 * [PainterHint] is restricted.
 *
 * Use [PainterPathHint] to modify the resource path.
 * Use [PainterSvgPatchHint] to patch the SVG content.
 *
 * All [PainterHint] implementations need to implement the hashcode method,
 * or ensure it is an object, [PainterHintProvider][org.jetbrains.jewel.painter.PainterHintsProvider]
 * use it as cache key for patched svg.
 *
 * @see PainterPathHint
 * @see PainterSvgPatchHint
 */
@Immutable
sealed interface PainterHint {

    /**
     * An empty [PainterHint], it will be ignored.
     */
    companion object Empty : PainterHint {

        override fun toString(): String = "Empty"
    }
}

/**
 * A [PainterHint] for modify the resource path.
 */
@Immutable
interface PainterPathHint : PainterHint {

    /**
     * Patch the resource path with context classLoaders(for bridge module).
     */
    fun patch(path: String, classLoaders: List<ClassLoader>): String = patch(path)

    /**
     * Simply method to patch the resource path.
     */
    fun patch(path: String): String
}

/**
 * A [PainterHint] for patch the SVG content, it only will apply to
 * SVG resources.
 */
@Immutable
interface PainterSvgPatchHint : PainterHint {

    /**
     * Patch the SVG content.
     */
    fun patch(element: Element)
}

/**
 * A [PainterHint] for adding prefix to resource path file name.
 * For example, the resource path is `icons/MyIcon.svg`, the prefix is `Dark`,
 * the patched resource path will be `icons/DarkMyIcon.svg`.
 */
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

/**
 * A [PainterHint] for adding prefix to resource path file name.
 * For example, the resource path is `icons/MyIcon.svg`, the prefix is `_dark`,
 * the patched resource path will be `icons/MyIcon_dark.svg`.
 */
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
