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

package ru.endlesscode.bbtest.mvp.misc

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.text.MatchesPattern.matchesPattern
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith

@RunWith(JUnitPlatform::class)
class DateTimeProviderSpec : Spek({

    val dateTimeProvider = DateTimeProvider()

    it("should provide time stamp according to ISO-8601") {
        val timeStamp = dateTimeProvider.iso8601()

        //language=RegExp
        assertThat(timeStamp, matchesPattern("\\d{8}T\\d{6}(Z|\\+\\d{2})"))
    }

    it("should provide right date") {
        val date = dateTimeProvider.date()

        //language=RegExp
        assertThat(date, matchesPattern("\\d{8}"))
    }

    it("should provide time stamp according to RFC 1123") {
        val timeStamp = dateTimeProvider.rfc1123()

        //language=RegExp
        assertThat(timeStamp, matchesPattern("\\w{3}, \\d{2} \\w{3} \\d{4} \\d{2}:\\d{2}:\\d{2} \\w{3}"))
    }
})
