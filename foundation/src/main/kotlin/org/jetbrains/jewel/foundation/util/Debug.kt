package org.jetbrains.jewel.foundation.util

import org.jetbrains.jewel.foundation.InternalJewelApi

/**
 * Determines whether we're in debug mode. This should not be used in the
 * bridge for logging; instead, you should use the IDE logger.
 */
@InternalJewelApi
public val inDebugMode: Boolean by lazy {
    System.getProperty("org.jetbrains.jewel.debug")?.toBoolean() ?: false
}

@InternalJewelApi
public inline fun debugLog(generateMsg: () -> String) {
    if (inDebugMode) {
        println(generateMsg())
    }
}
