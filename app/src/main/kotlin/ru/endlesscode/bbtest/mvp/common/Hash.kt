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

import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object Hash {

    private val SHA_256 = "SHA-256"
    private val DIGITS = "0123456789abcdef"

    fun hmacSha256(key: String, data: String): ByteArray {
        return hmacSha256(key.toByteArray(), data)
    }

    fun hmacSha256(key: ByteArray, data: String): ByteArray {
        val algorithm = "HmacSHA256"
        val hmac = Mac.getInstance(algorithm)
        val secretKey = SecretKeySpec(key, algorithm)
        hmac.init(secretKey)

        return hmac.doFinal(data.toByteArray())
    }

    fun sha256(text: String): String {
        return sha256(text.toByteArray())
    }

    fun sha256(data: ByteArray): String {
        return encodeHex(sha256Bytes(data))
    }

    private fun sha256Bytes(data: ByteArray): ByteArray {
        return getDigest(SHA_256).digest(data)
    }

    private fun getDigest(algorithm: String): MessageDigest = MessageDigest.getInstance(algorithm)

    fun encodeHex(data: ByteArray, toLowerCase: Boolean = true): String {
        return encodeHex(data, if (toLowerCase) DIGITS else DIGITS.toUpperCase())
    }

    private fun encodeHex(data: ByteArray, toDigits: String): String {
        val length = data.size
        val out = CharArray(length shl 1)
        var j = 0
        for (i in 0 until length) {
            out[j++] = toDigits[(0xF0 and data[i].toInt()) ushr 0x4]
            out[j++] = toDigits[0xF and data[i].toInt()]
        }

        return String(out)
    }
}