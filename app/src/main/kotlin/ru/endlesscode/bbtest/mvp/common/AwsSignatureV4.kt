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

import android.support.annotation.VisibleForTesting
import ru.endlesscode.bbtest.api.AwsHeaders

class AwsSignatureV4 private constructor(
        private val host: String,
        private val method: String,
        private val service: String,
        private val region: String,
        private val accessKey: String,
        private val secretKey: String,
        private val dateTime: DateTimeProvider) {

    companion object {
        private const val AWS4_REQUEST = "aws4_request"
        private const val ALGORITHM = "AWS4-HMAC-SHA256"
    }

    private lateinit var date: String
    private lateinit var timeStamp: String
    private lateinit var headers: AwsHeaders
    private lateinit var payloadHash: String

    fun buildRequestHeaders(
            uri: String = "/",
            query: String = "",
            vararg headers: Pair<String, String>,
            payload: ByteArray = "".toByteArray()
    ): AwsHeaders {

        this.saveTime()
        this.initHeaders(headers)
        this.setPayload(payload)

        val canonicalRequest = buildCanonicalRequest(uri, query)
        val credentialScope = buildCredentialScope()
        val stringToSign = buildStringToSign(credentialScope, canonicalRequest)
        val signature = getSignature(stringToSign)

        this.headers.add(AwsHeaders.AUTHORIZATION to buildAuthorizationHeader(credentialScope, signature))

        return this.headers
    }

    @VisibleForTesting
    fun setPayload(payload: ByteArray) {
        this.payloadHash = Hash.sha256(payload)

        if (payload.isNotEmpty()) {
            this.headers.add(AwsHeaders.AMZ_CONTENT_HASH to payloadHash)
        }
    }

    @VisibleForTesting
    fun saveTime() {
        this.date = dateTime.date()
        this.timeStamp = dateTime.iso8601()
    }

    @VisibleForTesting
    fun initHeaders(headers: Array<out Pair<String, String>>) {
        this.headers = AwsHeaders(headers)
        this.headers.add(
                AwsHeaders.HOST to host,
                AwsHeaders.AMZ_DATE to timeStamp
        )
    }

    @VisibleForTesting
    fun buildCanonicalRequest(
            canonicalUri: String,
            canonicalQuery: String
    ): String {
        return "$method\n$canonicalUri\n$canonicalQuery\n${headers.canonical}\n${headers.signed}\n$payloadHash"
    }

    @VisibleForTesting
    fun buildCredentialScope() = "$date/$region/$service/$AWS4_REQUEST"

    @VisibleForTesting
    fun buildStringToSign(credentialScope: String, canonicalRequest: String): String {
        return "$ALGORITHM\n$timeStamp\n$credentialScope\n${Hash.sha256(canonicalRequest)}"
    }

    @VisibleForTesting
    fun getSignature(stringToSign: String): String {
        val keyDate = Hash.hmacSha256("AWS4$secretKey", date)
        val keyRegion = Hash.hmacSha256(keyDate, region)
        val keyService = Hash.hmacSha256(keyRegion, service)
        val keySigning = Hash.hmacSha256(keyService, AWS4_REQUEST)

        return Hash.encodeHex(Hash.hmacSha256(keySigning, stringToSign))
    }

    @VisibleForTesting
    fun buildAuthorizationHeader(credentialScope: String, signature: String)
            = "$ALGORITHM Credential=$accessKey/$credentialScope, SignedHeaders=${headers.signed}, Signature=$signature"

    class Builder() {

        constructor(init: Builder.() -> Unit) : this() {
            init()
        }

        var host: String = "s3.amazonaws.com"
        var method: String = "PUT"
        var service: String = "s3"
        var region: String = "eu-central-1"
        var accessKey: String? = null
        var secretKey: String? = null
        var dateTime: DateTimeProvider? = null

        fun build() = AwsSignatureV4(
                host = host,
                method = method,
                service = service,
                region = region,
                accessKey = accessKey ?: throw IllegalStateException("Access key not assigned"),
                secretKey = secretKey ?: throw IllegalStateException("Secret key not assigned"),
                dateTime = dateTime ?: DateTimeProvider()
        )
    }
}