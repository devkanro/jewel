package org.jetbrains.jewel.intui.standalone

import org.jetbrains.jewel.painter.ClassLoaderResourceResolver
import org.jetbrains.jewel.painter.ResourcePainterProvider
import org.jetbrains.jewel.painter.ResourceResolver

fun standalonePainterProvider(path: String, resourceResolver: ResourceResolver = IntUiResourceResolver) =
    ResourcePainterProvider(path, resourceResolver)

object IntUiResourceResolver : ClassLoaderResourceResolver(IntUiTheme::class.java.classLoader)
