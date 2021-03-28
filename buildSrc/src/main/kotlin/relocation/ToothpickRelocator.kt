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
package relocation

import com.github.jengelman.gradle.plugins.shadow.relocation.Relocator
import com.github.jengelman.gradle.plugins.shadow.relocation.SimpleRelocator
import java.lang.reflect.Method
import java.util.regex.Pattern

internal class ToothpickRelocator(
  pattern: String,
  shadedPattern: String,
  rawString: Boolean = false,
  includes: List<String> = emptyList(),
  excludes: List<String> = emptyList(),
  private val simpleRelocator: SimpleRelocator = SimpleRelocator(pattern, shadedPattern, includes, excludes, rawString)
) : Relocator by simpleRelocator {
  override fun canRelocatePath(path: String): Boolean {
    // Respect includes/excludes for rawString too
    if (simpleRelocator.rawString) {
      return isIncludedMethod(simpleRelocator, path) as Boolean
        && !(isExcludedMethod(simpleRelocator, path) as Boolean)
        && Pattern.compile(simpleRelocator.pathPattern).matcher(path).find()
    }
    return simpleRelocator.canRelocatePath(path)
  }

  companion object {
    private val isExcludedMethod: Method = SimpleRelocator::class.java.getDeclaredMethod("isExcluded", String::class.java)
    private val isIncludedMethod: Method = SimpleRelocator::class.java.getDeclaredMethod("isIncluded", String::class.java)

    init {
      isExcludedMethod.isAccessible = true
      isIncludedMethod.isAccessible = true
    }
  }
}