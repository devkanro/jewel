package org.jetbrains.jewel.samples.standalone.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.GroupHeader
import org.jetbrains.jewel.Icon

@Composable
internal fun Icons() {
    GroupHeader("Icons")

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon("icons/jewel-logo.svg", "Jewel Logo", Modifier.size(16.dp))
        Icon("icons/jewel-logo.svg", "Jewel Logo", Modifier.size(32.dp))
        Icon("icons/jewel-logo.svg", "Jewel Logo", Modifier.size(64.dp))
        Icon("icons/jewel-logo.svg", "Jewel Logo", Modifier.size(128.dp))
        Icon(
            "icons/jewel-logo.svg",
            "Jewel Logo",
            Modifier.size(128.dp),
            ColorFilter.tint(Color.Magenta, BlendMode.Multiply),
        )
    }
}
