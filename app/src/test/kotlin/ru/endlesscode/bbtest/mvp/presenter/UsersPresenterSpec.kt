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

package ru.endlesscode.bbtest.mvp.presenter

import com.nhaarman.mockito_kotlin.*
import kotlinx.coroutines.experimental.Unconfined
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import retrofit2.HttpException
import ru.endlesscode.bbtest.api.UserData
import ru.endlesscode.bbtest.mvp.common.AsyncContexts
import ru.endlesscode.bbtest.mvp.model.UsersManager
import ru.endlesscode.bbtest.mvp.view.`UsersView$$State`
import ru.gildor.coroutines.retrofit.util.errorResponse
import java.net.SocketTimeoutException
import java.net.UnknownHostException

@RunWith(JUnitPlatform::class)
class UsersPresenterSpec : Spek({

    val usersManager: UsersManager = mock()
    var usersPresenter = UsersPresenter(usersManager, AsyncContexts(Unconfined, Unconfined))
    val viewState: `UsersView$$State` = mock()

    val users: List<UserData> = listOf(UserData(id = 1, firstName = "Foo", lastName = "Bar", email = "foo@bar.com", avatarUrl = ""))
    var error: Throwable = Exception()
    var isError = false

    beforeGroup {
        doAnswer { invocation ->
            val onSuccess: (List<UserData>) -> Unit = invocation.getArgument(0)
            val onError: (Throwable) -> Unit = invocation.getArgument(1)

            when {
                isError -> onError(error)
                else -> onSuccess(users)
            }
        }.`when`(usersManager).loadUsersList(any(), any())
    }

    beforeEachTest {
        usersPresenter = UsersPresenter(usersManager, AsyncContexts(Unconfined, Unconfined))
        usersPresenter.setViewState(viewState)
        clearInvocations(viewState)

        isError = false
    }

    context(": refreshing users list") {
        on("successful list init") {
            usersPresenter.initUsers()

            it("should hide add button") {
                verify(viewState, times(1)).hideAdd()
            }

            it("should init users") {
                verify(viewState, times(1)).initUsers()
            }

            it("should show add button") {
                verify(viewState, times(1)).showAdd()
            }
        }

        on("successful list refreshing") {
            usersPresenter.initUsers()
            clearInvocations(viewState)
            usersPresenter.refreshUsers()

            it("should update users list") {
                verify(viewState, times(1)).updateUsers(any())
            }
        }

        on("error list init") {
            isError = true
            usersPresenter.initUsers()

            it("should hide add button") {
                verify(viewState, times(1)).hideAdd()
            }

            it("should show error") {
                verify(viewState, times(1)).showError(any())
            }
        }

        on("error list refreshing") {
            usersPresenter.initUsers()
            clearInvocations(viewState)

            isError = true
            usersPresenter.refreshUsers()

            it("should show error") {
                verify(viewState, times(1)).showError(any())
            }
        }

        on("successful first refresh after error") {
            isError = true
            usersPresenter.initUsers()
            clearInvocations(viewState)

            isError = false
            usersPresenter.refreshUsers()

            it("should init users") {
                verify(viewState, times(1)).initUsers()
            }

            it("should show add button") {
                verify(viewState, times(1)).showAdd()
            }
        }

        on("successful refresh after error") {
            usersPresenter.initUsers()
            isError = true
            usersPresenter.refreshUsers()
            clearInvocations(viewState)

            isError = false
            usersPresenter.refreshUsers()

            it("should update users") {
                verify(viewState, times(1)).updateUsers(any())
            }
        }

        afterEachTest {
            verify(viewState, times(1)).showRefreshing()
            verify(viewState, times(1)).hideRefreshing()
            verifyNoMoreInteractions(viewState)
        }
    }

    context(": throwing error") {
        var message = ""

        beforeEachTest {
            error = Exception()
            isError = true
        }

        it("should return right message on UnknownHostException") {
            message = "Are you connected to the Internet?"
            error = UnknownHostException()
        }

        it("should return right message on SocketTimeoutException") {
            message = "Can't connect to server"
            error = SocketTimeoutException()
        }

        it("should return right message on HttpException") {
            val errorCode = 500
            message = "Something wrong with server ($errorCode)"
            error = HttpException(errorResponse<String>(errorCode))
        }

        it("should return default message on exception with null") {
            message = "Error"
        }

        it("should return default message on exception with empty message") {
            message = "Error"
            error = Exception("")
        }

        it("should return default message on exception with message") {
            val errorMessage = "message here"
            message = "Error: $errorMessage"
            error = Exception(errorMessage)
        }

        afterEachTest {
            usersPresenter.initUsers()
            verify(viewState, times(1)).showError(message)
        }
    }
})