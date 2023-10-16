package org.jetbrains.jewel.intui.window

import org.jetbrains.jewel.intui.standalone.IntUiTheme
import org.jetbrains.jewel.painter.ClassLoaderResourceResolver
import org.jetbrains.jewel.painter.ResourceResolver

/**
 * [ResourceResolver] to resolve resource in DecoratedWindow and Standalone module.
 */
object IntUiDecoratedWindowResourceResolver : ClassLoaderResourceResolver(
    IntUiDecoratedWindowResourceResolver::class.java.classLoader,
    IntUiTheme::class.java.classLoader,
)
