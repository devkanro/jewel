package org.jetbrains.jewel.painter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import org.jetbrains.jewel.LocalOnDarkBackground
import org.jetbrains.jewel.painter.hints.Dark

/**
 * Provider for assign hints for [PainterProvider].
 *
 * @see DarkPainterHintsProvider
 * @see org.jetbrains.jewel.intui.core.IntUiPainterHintsProvider
 * @see org.jetbrains.jewel.intui.standalone.StandalonePainterHintsProvider
 */
@Immutable
interface PainterHintsProvider {

    @Composable
    fun hints(path: String): List<PainterHint>
}

/**
 * The default [PainterHintsProvider] for patch dark theme icon.
 * It will provide [Dark] hint when [LocalOnDarkBackground] is true.
 */
object DarkPainterHintsProvider : PainterHintsProvider {

    @Composable
    override fun hints(path: String): List<PainterHint> = if (LocalOnDarkBackground.current) {
        listOf(Dark)
    } else {
        emptyList()
    }
}

val LocalPainterHintsProvider = staticCompositionLocalOf<PainterHintsProvider> {
    DarkPainterHintsProvider
}
