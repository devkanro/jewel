package org.jetbrains.jewel.intui.window

import org.jetbrains.jewel.intui.standalone.IntUiTheme
import org.jetbrains.jewel.painter.SmartResourceResolver

object IntUiDecoratedWindowResourceResolver : SmartResourceResolver(
    IntUiDecoratedWindowResourceResolver::class.java.classLoader,
    IntUiTheme::class.java.classLoader,
)
