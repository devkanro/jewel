package org.jetbrains.jewel.painter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.res.loadXmlImageVector
import org.w3c.dom.Document
import org.xml.sax.InputSource
import java.io.IOException
import java.io.InputStream
import java.io.StringWriter
import java.util.concurrent.ConcurrentHashMap
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

@Immutable
class ResourcePainterProvider(
    private val basePath: String,
    private val resourceResolver: ResourceResolver = SmartResourceResolver,

) : PainterProvider {

    private val cache = ConcurrentHashMap<Int, Painter>()

    private val documentBuilderFactory = DocumentBuilderFactory.newDefaultInstance()
        .apply { setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true) }

    @Composable
    override fun getPainter(vararg hints: PainterHint): State<Painter> {
        val resolvedHints = (hints.toList() + LocalPainterHintsProvider.current.hints(basePath))
            .filter { it != PainterHint }

        val cacheKey = resolvedHints.hashCode()

        if (cache.contains(cacheKey)) {
            println("Cache hit for $basePath")
        }

        val painter = cache.getOrPut(cacheKey) {
            println("Cache miss for $basePath, load it")
            loadPainter(resolvedHints)
        }

        return rememberUpdatedState(painter)
    }

    @Composable
    private fun loadPainter(hints: List<PainterHint>): Painter {
        val format = basePath.substringAfterLast(".")

        val contextClassLoaders = resourceResolver.getClassLoaders()

        val pathStack = buildSet {
            var path = basePath

            add(path)

            hints.forEach {
                if (it !is PainterPathHint) return@forEach
                path = it.patch(path, contextClassLoaders)
                add(path)
            }
        }.reversed()

        val url = pathStack.firstNotNullOfOrNull {
            resourceResolver.resolve(it)
        } ?: error("Resource '$basePath' not found")

        val density = LocalDensity.current

        return when (format) {
            "svg" -> {
                remember(url, density, hints) {
                    patchSvg(url.openStream(), hints).use {
                        println("Load icon $basePath from $url")
                        loadSvgPainter(it, density)
                    }
                }
            }

            "xml" -> {
                val vector = url.openStream().use {
                    loadXmlImageVector(InputSource(it), density)
                }
                rememberVectorPainter(vector)
            }

            else -> {
                remember(url, density) {
                    val bitmap = url.openStream().use {
                        loadImageBitmap(it)
                    }
                    BitmapPainter(bitmap)
                }
            }
        }
    }

    private fun patchSvg(inputStream: InputStream, hints: List<PainterHint>): InputStream {
        if (hints.all { it !is PainterSvgPatchHint }) {
            return inputStream
        }

        inputStream.use {
            val builder = documentBuilderFactory.newDocumentBuilder()
            val document = builder.parse(inputStream)

            hints.forEach {
                if (it !is PainterSvgPatchHint) return@forEach
                it.patch(document.documentElement)
            }

            return document.writeToString().byteInputStream()
        }
    }

    private fun Document.writeToString(): String {
        val tf = TransformerFactory.newInstance()
        val transformer: Transformer

        try {
            transformer = tf.newTransformer()
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")

            val writer = StringWriter()
            transformer.transform(DOMSource(this), StreamResult(writer))
            return writer.buffer.toString()
        } catch (e: TransformerException) {
            error("Unable to render XML document to string: ${e.message}")
        } catch (e: IOException) {
            error("Unable to render XML document to string: ${e.message}")
        }
    }
}

@Composable
fun rememberPainterProvider(path: String, resourceResolver: ResourceResolver = SmartResourceResolver): PainterProvider {
    return remember(path, resourceResolver) {
        ResourcePainterProvider(path, resourceResolver)
    }
}
