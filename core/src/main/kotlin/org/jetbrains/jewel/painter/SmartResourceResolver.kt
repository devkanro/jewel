package org.jetbrains.jewel.painter

/**
 * Get [ClassLoader] from caller class, it's constructor must be not wrapped,
 * otherwise the wrong [ClassLoader] will be chosen.
 *
 * Put it in parameter's default value to avoid the caller class being wrapped.
 */
class SmartResourceResolver : ClassLoaderResourceResolver(StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).callerClass.classLoader)
