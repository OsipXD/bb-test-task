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

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.stub
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import ru.endlesscode.bbtest.api.HttpHeaders
import kotlin.test.assertEquals
import kotlin.test.fail

@RunWith(JUnitPlatform::class)
class AwsSignatureV4Spec : Spek({

    given("a Builder") {
        it("should throw IllegalStateException if access key not assigned") {
            try {
                AwsSignature.Builder().build()
                fail()
            } catch (ignored: IllegalStateException) {
            }
        }

        it("should throw IllegalStateException if secret key not assigned") {
            try {
                AwsSignature.Builder {
                    accessKey = ""
                }.build()
                fail()
            } catch (ignored: IllegalStateException) {
            }
        }
    }

    given("a AwsSignature") {
        val dateTimeProvider: DateTimeProvider = mock()
        val awsSignature = AwsSignature.Builder {
            host = "example.amazonaws.com"
            method = "GET"
            service = "service"
            region = "us-east-1"
            accessKey = "AKIDEXAMPLE"
            secretKey = "wJalrXUtnFEMI/K7MDENG+bPxRfiCYEXAMPLEKEY"
            dateTime = dateTimeProvider
        }.build()

        var canonicalRequest = ""
        var credentialScope = ""
        var stringToSign = ""

        beforeGroup {
            dateTimeProvider.stub {
                on { date() } doReturn "20150830"
                on { iso8601() } doReturn "20150830T123600Z"
            }
        }

        it("should build right canonical request") {
            awsSignature.saveTime()
            awsSignature.initHeaders()
            awsSignature.setPayload("".toByteArray())

            canonicalRequest = awsSignature.buildCanonicalRequest("/", "Param1=value1&Param2=value2")

            assertEquals(
                    expected = "GET\n" +
                            "/\n" +
                            "Param1=value1&Param2=value2\n" +
                            "host:example.amazonaws.com\n" +
                            "x-amz-date:20150830T123600Z\n" +
                            "\n" +
                            "host;x-amz-date\n" +
                            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                    actual = canonicalRequest
            )
        }

        it("should build right string to sign") {
            credentialScope = awsSignature.buildCredentialScope()
            stringToSign = awsSignature.buildStringToSign(credentialScope, canonicalRequest)

            assertEquals(
                    expected = "AWS4-HMAC-SHA256\n" +
                            "20150830T123600Z\n" +
                            "20150830/us-east-1/service/aws4_request\n" +
                            "816cd5b414d056048ba4f7c5386d6e0533120fb1fcfa93762cf0fc39e2cf19e0",
                    actual = stringToSign
            )
        }

        it("should calculate right authorization header") {
            val signature = awsSignature.getSignature(stringToSign)
            val authorizationHeader = awsSignature.buildAuthorizationHeader(credentialScope, signature)

            assertEquals(
                    expected = "AWS4-HMAC-SHA256 Credential=AKIDEXAMPLE/20150830/us-east-1/service/aws4_request, SignedHeaders=host;x-amz-date, Signature=b97d918cfa904a5beff61c982a1b6f458b799221646efd99d3219ec94cdf2500",
                    actual = authorizationHeader
            )
        }

        on("building request headers headers") {
            val headers = awsSignature.buildRequestHeaders(query = "Param1=value1&Param2=value2")

            it("should contains right Host header") {
                assertEquals(
                        expected = "example.amazonaws.com",
                        actual = headers[HttpHeaders.HOST]
                )
            }

            it("should contains right X-Amz-Date header") {
                assertEquals(
                        expected = "20150830T123600Z",
                        actual = headers[HttpHeaders.AMZ_DATE]
                )
            }
        }
    }
})
