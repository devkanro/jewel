package org.jetbrains.jewel.painter.hints

import androidx.compose.runtime.Immutable
import org.jetbrains.jewel.painter.PainterSuffixHint

@Immutable
object Dark : PainterSuffixHint() {

    override fun suffix(): String = "_dark"

    override fun toString(): String = "Dark"
}
