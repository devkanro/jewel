package org.jetbrains.jewel.painter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.ui.graphics.painter.Painter

@Immutable
interface PainterProvider {

    @Composable
    fun getPainter(vararg hints: PainterHint): State<Painter>
}
