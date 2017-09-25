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

package ru.endlesscode.bbtest.mvp.model

import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Call
import ru.endlesscode.bbtest.api.AwsHeaders
import ru.endlesscode.bbtest.api.AwsS3Api
import ru.endlesscode.bbtest.mvp.common.AwsSignatureV4
import ru.endlesscode.bbtest.mvp.common.DateTimeProvider
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AwsS3Service @Inject constructor(
        private val api: AwsS3Api,
        private val awsSignature: AwsSignatureV4,
        private val dateTime: DateTimeProvider) {

    fun upload(fileName: String, file: File): Call<String> {
        val contentType = "image/jpg"
        val body = RequestBody.create(MediaType.parse(contentType), file)
        val payload = file.readBytes()

        val headers = awsSignature.buildRequestHeaders(
                uri = "/$fileName", headers = "Content-Type" to contentType, payload = payload)

        return api.upload(
                fileName = fileName,
                length = body.contentLength(),
                host = headers[AwsHeaders.HOST],
                date = dateTime.rfc1123(),
                contentType = headers[AwsHeaders.CONTENT_TYPE],
                authorization = headers[AwsHeaders.AUTHORIZATION],
                amzContentHash = headers[AwsHeaders.AMZ_CONTENT_HASH],
                amzDate = headers[AwsHeaders.AMZ_DATE],
                body = body
        )
    }
}