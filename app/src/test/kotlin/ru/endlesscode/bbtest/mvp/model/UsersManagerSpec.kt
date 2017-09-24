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

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.stub
import kotlinx.coroutines.experimental.Unconfined
import kotlinx.coroutines.experimental.runBlocking
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import retrofit2.Call
import retrofit2.HttpException
import ru.endlesscode.bbtest.api.UserData
import ru.endlesscode.bbtest.api.UsersApi
import ru.endlesscode.bbtest.mvp.common.AsyncContexts
import ru.gildor.coroutines.retrofit.util.MockedCall
import ru.gildor.coroutines.retrofit.util.errorResponse
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

@RunWith(JUnitPlatform::class)
class UsersManagerSpec : Spek({
    val api: UsersApi = mock()
    val usersManager = spy(UsersManager(api, mock(), AsyncContexts(Unconfined, Unconfined)))

    context(": making request") {
        val call: MockedCall<List<UserData>> = MockedCall()

        beforeGroup {
            api.stub {
                on { usersList() } doReturn call
            }
        }

        beforeEachTest {
            call.refresh()
        }

        it("should receive right data") {
            val users = listOf(
                    UserData(id = 1, firstName = "Foo", lastName = "Bar", email = "foo@bar.com", avatarUrl = ""),
                    UserData(id = 2, firstName = "Baz", lastName = "Qux", email = "baz@qux.com", avatarUrl = "http://www.fillmurray.com/200/200")
            )
            call.ok = users
            usersManager.testLoadUsersList(call, onSuccess = { assertEquals(users, it) })
        }

        it("should call #onError when there are server error") {
            val exception = HttpException(errorResponse<String>())
            call.error = exception
            usersManager.testLoadUsersList(call) { assertTrue(it is HttpException) }
        }

        it("should call #onError when there are exception") {
            val exception = IOException()
            call.exception = exception
            usersManager.testLoadUsersList(call) { assertTrue(it is IOException) }
        }
    }
})

private fun <T : Any> UsersManager.testLoadUsersList(
        call: Call<T>,
        onSuccess: (T) -> Unit = { fail("It should be failure") },
        onError: (Throwable) -> Unit = { fail("It should be successful") }
) = runBlocking {
    this@testLoadUsersList.doRequest(
            call = call,
            onSuccess = { onSuccess(it) },
            onError = { onError(it) }).join()
}