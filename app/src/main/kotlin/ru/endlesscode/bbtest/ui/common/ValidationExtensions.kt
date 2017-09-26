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

package ru.endlesscode.bbtest.ui.common

import android.support.design.widget.TextInputLayout
import android.view.View
import android.widget.TextView

fun TextView.setOnFocusLostListener(listener: (View) -> Unit) {
    this.setOnFocusChangeListener { view, inFocus ->
        if (!inFocus) listener(view)
    }
}

fun TextView.validateNotBlank(layout: TextInputLayout) =
        validate(layout, this.text.isNotBlank(), "This field can not be blank")

/**
 * Why you not use strong regex to restrict email format?
 * Because it is useless: <https://hackernoon.com/the-100-correct-way-to-validate-email-addresses-7c4818f24643>
 */
fun TextView.validateIsEmail(layout: TextInputLayout): Boolean {
    text = text.trim()
    val isValid = " " in text
            || text.count { it == '@' } > 1
            || text matches Regex(".+@.+\\.[a-zA-Z]{2,}")
    return validate(layout, isValid, "Please, enter correct email ")
}

private fun TextView.validate(layout: TextInputLayout, valid: Boolean, errorMessage: String): Boolean {
    text = text.trim()
    return if (!valid) {
        layout.error = errorMessage
        false
    } else {
        layout.error = null
        true
    }
}