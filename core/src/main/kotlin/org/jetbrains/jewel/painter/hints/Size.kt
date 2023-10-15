package org.jetbrains.jewel.painter.hints

import androidx.compose.runtime.Immutable
import org.jetbrains.jewel.painter.PainterSuffixHint

@Immutable
class Size(private val size: String) : PainterSuffixHint() {

    override fun suffix(): String = buildString {
        append("@")
        append(size)
    }

    override fun toString(): String = "Size(size=$size)"

    override fun hashCode(): Int = size.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Size) return false

        if (size != other.size) return false

        return true
    }
}
