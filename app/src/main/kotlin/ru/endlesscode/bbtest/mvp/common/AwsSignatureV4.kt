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

import ru.endlesscode.bbtest.api.AwsHeaders
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class AwsSignatureV4 private constructor(
        private val host: String,
        private val method: String,
        private val service: String,
        private val region: String,
        private val accessKey: String,
        private val secretKey: String,
        private val dateTime: DateTimeProvider) {

    companion object {
        private const val AWS4_REQUEST = "was4_request"
        private const val ALGORITHM = "AWS4-HMAC-SHA256"
    }

    fun buildRequestHeaders(
            uri: String = "/",
            query: String = "",
            vararg headers: Pair<String, String>,
            payload: ByteArray = "".toByteArray()
    ): AwsHeaders {

        val payloadHash = Hash.sha256(payload)
        val amzDate = dateTime.amz()
        val dateStamp = dateTime.iso8601()

        val awsHeaders = AwsHeaders(*headers)
        awsHeaders.add(
                AwsHeaders.HOST to host,
                AwsHeaders.AMZ_CONTENT_HASH to payloadHash,
                AwsHeaders.AMZ_DATE to amzDate
        )

        val canonicalHeaders = awsHeaders.canonical
        val signedHeaders = awsHeaders.signed

        val canonicalRequest = buildCanonicalRequest(uri, query, canonicalHeaders, signedHeaders, payloadHash)
        val credentialScope = buildCredentialScope(dateStamp)
        val stringToSign = buildStringToSign(credentialScope, amzDate, canonicalRequest)
        val signingKey = getSigningKey(dateStamp)
        val signature = Hash.encodeHex(sign(signingKey, stringToSign))

        awsHeaders.add(AwsHeaders.AUTHORIZATION to buildAuthorizationHeader(credentialScope, signedHeaders, signature))

        return awsHeaders
    }

    private fun buildCanonicalRequest(
            canonicalUri: String,
            canonicalQuery: String,
            canonicalHeaders: String,
            signedHeaders: String,
            payloadHash: String
    ): String {
        return "$method\n$canonicalUri\n$canonicalQuery\n$canonicalHeaders\n$signedHeaders\n$payloadHash"
    }

    private fun buildCredentialScope(dateStamp: String) = "$dateStamp/$region/$service/$AWS4_REQUEST"

    private fun buildStringToSign(credentialScope: String, amzDate: String, canonicalRequest: String): String {
        return "$ALGORITHM\n$amzDate\n$credentialScope\n${Hash.sha256(canonicalRequest)}"
    }

    private fun getSigningKey(dateStamp: String): ByteArray {
        val keyDate = sign("AWS4$secretKey", dateStamp)
        val keyRegion = sign(keyDate, region)
        val keyService = sign(keyRegion, service)
        return sign(keyService, AWS4_REQUEST)
    }

    private fun sign(key: String, data: String): ByteArray {
        return sign(key.toByteArray(), data)
    }

    private fun sign(key: ByteArray, data: String): ByteArray {
        val algorithm = "HmacSHA256"
        val secretKey = SecretKeySpec(key, algorithm)
        val hmac = Mac.getInstance(algorithm)
        hmac.init(secretKey)

        return hmac.doFinal(data.toByteArray())
    }

    private fun buildAuthorizationHeader(credentialScope: String, signedHeaders: String, signature: String)
            = "$ALGORITHM Credential=$accessKey/$credentialScope, SignedHeaders=$signedHeaders, Signature=$signature"

    class Builder(init: Builder.() -> Unit) {
        var host: String = "s3.amazonaws.com"
        var method: String = "PUT"
        var service: String = "s3"
        var region: String = "eu-central-1"
        var accessKey: String? = null
        var secretKey: String? = null
        var dateTime: DateTimeProvider? = null

        init {
            init()
        }

        fun build() = AwsSignatureV4(
                host = host,
                method = method,
                service = service,
                region = region,
                accessKey = accessKey ?: throw IllegalArgumentException("Access key not assigned"),
                secretKey = secretKey ?: throw IllegalArgumentException("Secret key not assigned"),
                dateTime = dateTime ?: DateTimeProvider()
        )
    }
}