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
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.run
import retrofit2.Call
import ru.endlesscode.bbtest.api.UserData
import ru.endlesscode.bbtest.api.UserUpdateBody
import ru.endlesscode.bbtest.api.UsersApi
import ru.gildor.coroutines.retrofit.Result
import ru.gildor.coroutines.retrofit.awaitResult
import kotlin.coroutines.experimental.CoroutineContext

class UsersManager(private val api: UsersApi) {

    fun loadUsersList(onSuccess: (List<UserData>) -> Unit, onError: (Throwable) -> Unit)
            = doRequest(api.usersList(), onSuccess, onError)

    fun createUser(user: User, onSuccess: (Unit) -> Unit, onError: (Throwable) -> Unit)
            = doRequest(api.createUser(UserUpdateBody(user)), onSuccess, onError)

    fun updateUser(user: User, onSuccess: (Unit) -> Unit, onError: (Throwable) -> Unit)
            = doRequest(api.createUser(UserUpdateBody(user)), onSuccess, onError)

    @VisibleForTesting
    fun <T : Any> doRequest(call: Call<T>,
                            onSuccess: (T) -> Unit,
                            onError: (Throwable) -> Unit,
                            runContext: CoroutineContext = CommonPool,
                            resultContext: CoroutineContext = UI) = launch(runContext) {

        val result = call.awaitResult()
        run(resultContext) {
            when (result) {
                is Result.Ok -> onSuccess(result.value)
                is Result.Error -> onError(result.exception)
                is Result.Exception -> onError(result.exception)
            }
        }
    }
}