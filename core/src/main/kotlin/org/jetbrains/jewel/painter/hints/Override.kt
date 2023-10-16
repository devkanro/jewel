package org.jetbrains.jewel.painter.hints

import org.jetbrains.jewel.painter.PainterPathHint

class Override(private val iconOverride: Map<String, String>) : PainterPathHint {

    override fun patch(path: String): String = iconOverride[path] ?: path

    override fun toString(): String = "Override(${iconOverride.hashCode()})"

    override fun hashCode(): Int = iconOverride.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Override) return false

        if (iconOverride != other.iconOverride) return false

        return true
    }
}
