/*
 * This file is part of bb-test-task, licensed under the MIT License (MIT).
 *
 * Copyright (c) Osip Fatkullin <osip.fatkullin@gmail.com>
 * Copyright (c) contributors
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

package ru.endlesscode.bbtest.api

class AwsHeaders(headers: Array<out Pair<String, String>>) {

    companion object {
        const val HOST = "Host"
        const val AMZ_CONTENT_HASH = "X-Amz-Content-sha256"
        const val AMZ_DATE = "X-Amz-Date"
        const val CONTENT_TYPE = "Content-Type"
        const val AUTHORIZATION = "Authorization"
    }

    private val headers = headers.toMap().toMutableMap()

    val canonical
        get() = preparedHeaders.joinToString(separator = "\n", postfix = "\n") { "${it.first}:${it.second}" }

    val signed
        get() = preparedHeaders.joinToString(";") { it.first }

    private val preparedHeaders
        get() = headers.map { entry ->
            entry.key.toLowerCase() to entry.value.trim()
        }.sortedBy { it.first }

    fun add(vararg addHeaders: Pair<String, String>) {
        headers.putAll(addHeaders)
    }

    operator fun get(header: String): String = headers.getValue(header)
}