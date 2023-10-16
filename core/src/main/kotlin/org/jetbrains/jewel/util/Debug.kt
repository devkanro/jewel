package org.jetbrains.jewel.util

val inDebugMode by lazy {
    System.getProperty("org.jetbrains.jewel.debug")?.toBoolean() ?: false
}
