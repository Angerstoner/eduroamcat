package de.unigoe.eduroamcat.backend.util

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList

// Adds a method for getting the first element with given [tag]
internal fun Element.getFirstElementByTag(tag: String): Element? {
    val element = getElementsByTagName(tag)
    return if (element.length > 0) element.item(0) as Element
    else null
}

internal fun Document.getFirstElementByTag(tag: String): Element {
    val element = getElementsByTagName(tag)
    if (element.length > 0)
        return element.item(0) as Element
    else
        throw NoSuchElementException()
}

internal operator fun NodeList.iterator(): Iterator<Node> =
    (0 until length).asSequence().map { item(it) as Node }.iterator()

// highly likely, that this will be used withs paths not starting with ProviderInfo
@Suppress("SameParameterValue")
internal fun Document.getTextContentForXmlPath(vararg tags: String): String {
    if (tags.isEmpty()) throw IllegalArgumentException()
    var currentElement: Element = this.getFirstElementByTag(tags[0]) ?: throw NoSuchElementException()

    (1 until tags.size).forEach {
        val nextElement = currentElement.getFirstElementByTag(tags[it])
        if (nextElement != null)
            currentElement = nextElement
    }
    return currentElement.textContent
}

internal fun Element.getTextContentForXmlPath(vararg tags: String): String {
    if (tags.isEmpty()) throw IllegalArgumentException()
    var currentElement: Element = this.getFirstElementByTag(tags[0]) ?: throw NoSuchElementException()

    (1 until tags.size).forEach {
        val nextElement = currentElement.getFirstElementByTag(tags[it])
        if (nextElement != null)
            currentElement = nextElement
    }
    return currentElement.textContent
}