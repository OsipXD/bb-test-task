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
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import retrofit2.HttpException
import ru.gildor.coroutines.retrofit.util.errorResponse
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import ru.endlesscode.bbtest.mvp.view.`HomeView$$State` as ViewState

@RunWith(JUnitPlatform::class)
class HomePresenterSpec : Spek({

    val viewState: ViewState = mock()
    val presenter = HomePresenter()

    beforeGroup {
        presenter.setViewState(viewState)
    }

    it("should open add user view") {
        presenter.showUserCreatingView()
        verify(viewState, times(1)).showFragment(any(), eq(""), eq(true))
    }

    context(": throwing error") {
        var message = ""
        var error = Exception()

        beforeEachTest {
            clearInvocations(viewState)
            error = Exception()
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
            presenter.showError(error)
            verify(viewState, times(1)).showError(eq(message))
        }
    }

    afterEachTest {
        verifyNoMoreInteractions(viewState)
    }
})
