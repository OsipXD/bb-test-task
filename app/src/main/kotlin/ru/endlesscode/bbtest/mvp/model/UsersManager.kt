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

import android.support.annotation.VisibleForTesting
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.run
import retrofit2.Call
import ru.endlesscode.bbtest.api.UserData
import ru.endlesscode.bbtest.api.UserUpdateBody
import ru.endlesscode.bbtest.api.UsersApi
import ru.endlesscode.bbtest.mvp.common.AsyncContexts
import ru.gildor.coroutines.retrofit.Result
import ru.gildor.coroutines.retrofit.awaitResult
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsersManager @Inject constructor(
        private val api: UsersApi,
        private val s3: AwsS3Service,
        private val asyncContexts: AsyncContexts) {

    fun loadUsersList(onSuccess: (List<UserData>) -> Unit, onError: (Throwable) -> Unit) {
        doRequest(api.usersList(), onSuccess, onError)
    }

    fun createUser(user: User, onSuccess: (Unit) -> Unit, onError: (Throwable) -> Unit) {
        doRequest(api.createUser(UserUpdateBody(user)), onSuccess, onError)
    }

    fun updateUser(user: User, onSuccess: (Unit) -> Unit, onError: (Throwable) -> Unit) {
        doRequest(api.updateUser(user.id, UserUpdateBody(user)), onSuccess, onError)
    }

    fun uploadAvatar(user: UserItem, avatar: File, onSuccess: (String) -> Unit, onError: (Throwable) -> Unit) {
        doRequest(s3.upload(
                fileName = "${user.fullName.toLowerCase().replace(' ', '_')}.jpg",
                file = avatar
        ), onSuccess, onError)
    }

    @VisibleForTesting
    fun <T : Any> doRequest(call: Call<T>,
                            onSuccess: (T) -> Unit,
                            onError: (Throwable) -> Unit) = launch(asyncContexts.work) {

        val result = call.awaitResult()
        run(asyncContexts.ui) {
            when (result) {
                is Result.Ok -> onSuccess(result.value)
                is Result.Error -> onError(result.exception)
                is Result.Exception -> onError(result.exception)
            }
        }
    }
}