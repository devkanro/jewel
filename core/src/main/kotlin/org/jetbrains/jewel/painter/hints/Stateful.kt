package org.jetbrains.jewel.painter.hints

import androidx.compose.runtime.Immutable
import org.jetbrains.jewel.FocusableComponentState
import org.jetbrains.jewel.InteractiveComponentState
import org.jetbrains.jewel.painter.PainterSuffixHint

@Immutable
class Stateful(private val state: InteractiveComponentState) : PainterSuffixHint() {

    override fun suffix(): String = buildString {
        if (state.isEnabled) {
            when {
                state is FocusableComponentState && state.isFocused -> append("Focused")
                state.isPressed -> append("Pressed")
                state.isHovered -> append("Hovered")
            }
        } else {
            append("Disabled")
        }
    }

    override fun toString(): String = "Stateful(state=$state)"

    override fun hashCode(): Int = state.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Stateful) return false

        if (state != other.state) return false

        return true
    }
}
