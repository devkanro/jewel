package org.jetbrains.jewel.intui.standalone

import org.jetbrains.jewel.painter.ResourcePainterProvider
import org.jetbrains.jewel.painter.ResourceResolver
import org.jetbrains.jewel.painter.SmartResourceResolver

fun standalonePainterProvider(path: String, resourceResolver: ResourceResolver = IntUiResourceResolver) =
    ResourcePainterProvider(path, resourceResolver)

object IntUiResourceResolver : SmartResourceResolver(IntUiTheme::class.java.classLoader)
