/*
 * This file is part of Toothpick, licensed under the MIT License.
 *
 * Copyright (c) 2020-2021 Jason Penilla & Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
import kotlinx.dom.elements
import kotlinx.dom.parseXml
import kotlinx.dom.search
import org.gradle.api.Project
import org.w3c.dom.Document

internal fun Project.parsePom(): Document? {
    val file = file("pom.xml")
    if (!file.exists()) {
        return null
    }
    val contents = file.readText()
    val dom = parseXml(contents.byteInputStream())
    val properties = dom.search("properties").firstOrNull()?.elements() ?: emptyList()
    val propertiesMap = properties.associateBy({ it.nodeName }, { it.textContent }).toMutableMap()

    propertiesMap["project.version"] = project.version.toString()
    propertiesMap["minecraft.version"] = toothpick.minecraftVersion
    propertiesMap["minecraft_version"] = toothpick.nmsPackage

    return parseXml(contents.replaceProperties(propertiesMap).byteInputStream())
}

private fun String.replaceProperties(
    properties: Map<String, String>
): String {
    var result = this
    for ((key, value) in properties) {
        result = result.replace("\${$key}", value)
    }
    return result
}