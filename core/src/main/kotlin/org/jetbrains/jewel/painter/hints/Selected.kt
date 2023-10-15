package org.jetbrains.jewel.painter.hints

import androidx.compose.runtime.Immutable
import org.jetbrains.jewel.SelectableComponentState
import org.jetbrains.jewel.painter.PainterHint
import org.jetbrains.jewel.painter.PainterSuffixHint

@Immutable
object Selected : PainterSuffixHint() {

    override fun suffix(): String = "Selected"

    operator fun invoke(state: SelectableComponentState): PainterHint = if (state.isSelected) {
        Selected
    } else {
        PainterHint
    }

    override fun toString(): String {
        return "Selected"
    }
}
