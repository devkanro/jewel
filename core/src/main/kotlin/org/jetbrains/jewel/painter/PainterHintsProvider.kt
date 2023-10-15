package org.jetbrains.jewel.painter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import org.jetbrains.jewel.LocalOnDarkBackground
import org.jetbrains.jewel.painter.hints.Dark

@Immutable
interface PainterHintsProvider {

    @Composable
    fun hints(path: String): List<PainterHint>
}

object DarkPainterHintsProvider : PainterHintsProvider {

    @Composable
    override fun hints(path: String): List<PainterHint> = if (LocalOnDarkBackground.current) {
        listOf(Dark)
    } else {
        listOf()
    }
}

val LocalPainterHintsProvider = staticCompositionLocalOf<PainterHintsProvider> {
    DarkPainterHintsProvider
}
