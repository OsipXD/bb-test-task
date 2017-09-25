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

package ru.endlesscode.bbtest.mvp.common

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DateTimeProvider @Inject constructor() {

    companion object {
        private val GTM = TimeZone.getTimeZone("GMT")
    }

    private val formatRfc1123: DateFormat
        get() = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US).apply { timeZone = GTM }

    private val formatIso8601: DateFormat
        get() = SimpleDateFormat("yyyyMMdd'T'HHmmssX", Locale.US).apply { timeZone = GTM }

    private val formatDate: DateFormat
        get() = SimpleDateFormat("yyyyMMdd", Locale.US).apply { timeZone = GTM }

    fun rfc1123(): String = formatted(formatRfc1123)

    fun iso8601(): String = formatted(formatIso8601)

    fun date(): String = formatted(formatDate)

    private fun formatted(format: DateFormat): String {
        val cal = Calendar.getInstance(format.timeZone)
        return format.format(cal.time)
    }
}