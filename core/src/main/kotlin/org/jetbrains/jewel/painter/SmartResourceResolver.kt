package org.jetbrains.jewel.painter

class SmartResourceResolver : ClassLoaderResourceResolver(StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).callerClass.classLoader)
