package org.jetbrains.jewel.painter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.ui.graphics.painter.Painter

/**
 * Provide [Painter] by specified [PainterHint], implementations should handle
 * all [PainterHint] correctly, at now, it only need to call [PainterPathHint.patch] and
 * [PainterSvgPatchHint.patch] correctly.
 * Also [PainterProvider] should hold the resource path and [ClassLoader] references.
 */
@Immutable
interface PainterProvider {

    @Composable
    fun getPainter(vararg hints: PainterHint): State<Painter>
}
