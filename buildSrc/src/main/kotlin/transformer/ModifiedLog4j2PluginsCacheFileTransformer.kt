/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License") you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package xyz.jpenilla.toothpick.transformer

import com.github.jengelman.gradle.plugins.shadow.ShadowStats
import com.github.jengelman.gradle.plugins.shadow.relocation.RelocateClassContext
import com.github.jengelman.gradle.plugins.shadow.relocation.Relocator
import com.github.jengelman.gradle.plugins.shadow.transformers.Transformer
import com.github.jengelman.gradle.plugins.shadow.transformers.TransformerContext
import org.gradle.api.file.FileTreeElement
import shadow.org.apache.commons.io.IOUtils
import shadow.org.apache.commons.io.output.CloseShieldOutputStream
import shadow.org.apache.logging.log4j.core.config.plugins.processor.PluginCache
import shadow.org.apache.logging.log4j.core.config.plugins.processor.PluginProcessor.PLUGIN_CACHE_FILE
import shadow.org.apache.tools.zip.ZipEntry
import shadow.org.apache.tools.zip.ZipOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.Collections
import java.util.Enumeration

/**
 * This class is a modified version of [com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer],
 * used in order to fix the issues when 'element.name' for our Log4jPlugins.dat is just 'Log4jPlugins.dat' instead of [PLUGIN_CACHE_FILE].
 *
 * Besides converting to Kotlin, the only modified part of the class is [canTransformResource].
 */
internal class ModifiedLog4j2PluginsCacheFileTransformer : Transformer {
  private val temporaryFiles: ArrayList<File> = ArrayList()
  private val relocators: ArrayList<Relocator> = ArrayList()
  private lateinit var stats: ShadowStats

  override fun canTransformResource(element: FileTreeElement): Boolean {
    return PLUGIN_CACHE_FILE == element.name || element.name == "Log4j2Plugins.dat"
  }

  override fun transform(context: TransformerContext) {
    val inputStream = context.`is`
    val temporaryFile = File.createTempFile("Log4j2Plugins", ".dat")
    temporaryFile.deleteOnExit()
    this.temporaryFiles.add(temporaryFile)
    FileOutputStream(temporaryFile).use { IOUtils.copy(inputStream, it) }
    val relocators = context.relocators
    if (relocators != null) {
      this.relocators.addAll(relocators)
    }
    this.stats = context.stats
  }

  override fun hasTransformedResource(): Boolean {
    val hasTransformedMultipleFiles = temporaryFiles.size > 1
    val hasAtLeastOneFileAndRelocator = temporaryFiles.isNotEmpty() && relocators.isNotEmpty()
    return hasTransformedMultipleFiles || hasAtLeastOneFileAndRelocator
  }

  override fun modifyOutputStream(zipOutputStream: ZipOutputStream, preserveFileTimestamps: Boolean) {
    val pluginCache = PluginCache()
    pluginCache.loadCacheFiles(urlEnumeration())
    relocatePlugins(pluginCache)
    val entry = ZipEntry(PLUGIN_CACHE_FILE)
    entry.time = TransformerContext.getEntryTimestamp(preserveFileTimestamps, entry.time)
    zipOutputStream.putNextEntry(entry)
    pluginCache.writeCache(CloseShieldOutputStream(zipOutputStream))
    this.temporaryFiles.clear()
  }

  private fun urlEnumeration(): Enumeration<URL> {
    return Collections.enumeration(
      this.temporaryFiles.map { it.toURI().toURL() }.toList()
    )
  }

  private fun relocatePlugins(pluginCache: PluginCache) {
    for (currentMap in pluginCache.allCategories.values) {
      currentMap.values.forEach { currentPluginEntry ->
        val className = currentPluginEntry.className
        val relocateClassContext = RelocateClassContext(className, stats)
        for (currentRelocator in relocators) {
          // If we have a relocator that can relocate our current entry...
          if (currentRelocator.canRelocateClass(className)) {
            // Then we perform that relocation and update the plugin entry to reflect the new value.
            val relocatedClassName = currentRelocator.relocateClass(relocateClassContext)
            currentPluginEntry.className = relocatedClassName
            return@forEach
          }
        }
      }
    }
  }
}