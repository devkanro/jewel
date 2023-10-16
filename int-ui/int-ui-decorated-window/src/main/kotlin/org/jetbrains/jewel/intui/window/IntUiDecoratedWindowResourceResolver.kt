package org.jetbrains.jewel.intui.window

import org.jetbrains.jewel.intui.standalone.IntUiTheme
import org.jetbrains.jewel.painter.ClassLoaderResourceResolver

object IntUiDecoratedWindowResourceResolver : ClassLoaderResourceResolver(
    IntUiDecoratedWindowResourceResolver::class.java.classLoader,
    IntUiTheme::class.java.classLoader,
)
