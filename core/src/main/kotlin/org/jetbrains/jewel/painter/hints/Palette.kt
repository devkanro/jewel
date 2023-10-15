package org.jetbrains.jewel.painter.hints

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import org.jetbrains.jewel.painter.PainterHint
import org.jetbrains.jewel.painter.PainterSvgPatchHint
import org.jetbrains.jewel.util.toRgba
import org.w3c.dom.Element
import kotlin.math.roundToInt

@Immutable
class Palette private constructor(val map: Map<Color, Color>) : PainterSvgPatchHint {

    override fun patch(element: Element) {
        element.patchColorAttribute("fill", map)
        element.patchColorAttribute("stroke", map)

        val nodes = element.childNodes
        val length = nodes.length
        for (i in 0 until length) {
            val item = nodes.item(i)
            if (item is Element) {
                patch(item)
            }
        }
    }

    private fun Element.patchColorAttribute(attrName: String, pattern: Map<Color, Color>) {
        val color = getAttribute(attrName)
        val opacity = getAttribute("$attrName-opacity")

        if (color.isNotEmpty()) {
            val alpha = opacity.toFloatOrNull() ?: 1.0f
            val originalColor = tryParseColor(color, alpha) ?: return
            val newColor = pattern[originalColor] ?: return
            setAttribute(attrName, newColor.copy(alpha = alpha).toRgba())
        }
    }

    private fun tryParseColor(color: String, alpha: Float): Color? {
        val rawColor = color.lowercase()
        if (rawColor.startsWith("#") && rawColor.length - 1 <= 8) {
            return fromHexOrNull(rawColor, alpha)
        }
        return null
    }

    private fun fromHexOrNull(rawColor: String, alpha: Float): Color? {
        val startPos = if (rawColor.startsWith("#")) 1 else if (rawColor.startsWith("0x")) 2 else 0
        val length = rawColor.length - startPos
        val alphaOverride = alpha.takeIf { it != 1.0f }?.let { (it * 255).roundToInt() }

        return when (length) {
            3 -> Color(
                red = rawColor.substring(startPos, startPos + 1).toInt(16),
                green = rawColor.substring(startPos + 1, startPos + 2).toInt(16),
                blue = rawColor.substring(startPos + 2, startPos + 3).toInt(16),
                alpha = alphaOverride ?: 255,
            )

            4 -> Color(
                red = rawColor.substring(startPos, startPos + 1).toInt(16),
                green = rawColor.substring(startPos + 1, startPos + 2).toInt(16),
                blue = rawColor.substring(startPos + 2, startPos + 3).toInt(16),
                alpha = alphaOverride ?: rawColor.substring(startPos + 3, startPos + 4).toInt(16),
            )

            6 -> Color(
                red = rawColor.substring(startPos, startPos + 2).toInt(16),
                green = rawColor.substring(startPos + 2, startPos + 4).toInt(16),
                blue = rawColor.substring(startPos + 4, startPos + 6).toInt(16),
                alpha = alphaOverride ?: 255,
            )

            8 -> Color(
                red = rawColor.substring(startPos, startPos + 2).toInt(16),
                green = rawColor.substring(startPos + 2, startPos + 4).toInt(16),
                blue = rawColor.substring(startPos + 4, startPos + 6).toInt(16),
                alpha = alphaOverride ?: rawColor.substring(startPos + 6, startPos + 8).toInt(16),
            )

            else -> null
        }
    }

    companion object {

        operator fun invoke(map: Map<Color, Color>): PainterHint {
            return if (map.isEmpty()) {
                PainterHint
            } else {
                Palette(map)
            }
        }
    }
}