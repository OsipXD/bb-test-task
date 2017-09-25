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

package ru.endlesscode.bbtest.di.modules

import dagger.Module
import dagger.Provides
import ru.endlesscode.bbtest.BuildConfig
import ru.endlesscode.bbtest.mvp.common.AwsSignatureV4
import ru.endlesscode.bbtest.mvp.common.DateTimeProvider
import javax.inject.Named
import javax.inject.Singleton

@Module
class AwsSignatureModule {

    @Provides
    @Singleton
    fun provideAwsSignature(
            @Named("bucketName") bucket: String,
            @Named("accessKey") accessKey: String,
            @Named("secretKey") secretKey: String,
            dateTime: DateTimeProvider): AwsSignatureV4 {
        return AwsSignatureV4.Builder {
            this.host = "$bucket.s3.amazonaws.com"
            this.accessKey = accessKey
            this.secretKey = secretKey
            this.dateTime = dateTime
        }.build()
    }

    @Provides
    @Singleton
    @Named("bucketName")
    fun provideBucket() = "osipxd-bbtest"

    @Provides
    @Singleton
    @Named("accessKey")
    fun provideAccesskey() = BuildConfig.S3_ACCESS_KEY

    @Provides
    @Singleton
    @Named("secretKey")
    fun provideSecretkey() = BuildConfig.S3_SECRET_KEY
}