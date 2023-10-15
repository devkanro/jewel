package org.jetbrains.jewel.painter.hints

import androidx.compose.runtime.Immutable
import org.jetbrains.jewel.painter.PainterSuffixHint

@Immutable
object Stroke : PainterSuffixHint() {

    override fun suffix(): String = "_stroke"

    override fun toString(): String = "StrokePainterHint"
}
