package org.jetbrains.jewel.painter.hints

import org.jetbrains.jewel.painter.PainterPathHint

class Override(private val iconOverride: Map<String, String>) : PainterPathHint {

    override fun patch(path: String): String = iconOverride[path] ?: path
}
