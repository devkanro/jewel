package org.jetbrains.jewel.intui.standalone

import org.jetbrains.jewel.painter.ClassLoaderResourceResolver
import org.jetbrains.jewel.painter.ResourcePainterProvider
import org.jetbrains.jewel.painter.ResourceResolver

/**
 * Create [PainterProvider][org.jetbrains.jewel.painter.PainterProvider] for show standalone module resource.
 */
fun standalonePainterProvider(path: String, resourceResolver: ResourceResolver = StandaloneResourceResolver) =
    ResourcePainterProvider(path, resourceResolver)

/**
 * [ResourceResolver] to resolve resource in standalone module.
 */
object StandaloneResourceResolver : ClassLoaderResourceResolver(IntUiTheme::class.java.classLoader)
