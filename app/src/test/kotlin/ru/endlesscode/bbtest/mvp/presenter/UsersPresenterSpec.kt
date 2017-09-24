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
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import ru.endlesscode.bbtest.api.UserData
import ru.endlesscode.bbtest.mvp.common.AsyncContexts
import ru.endlesscode.bbtest.mvp.model.UsersManager
import ru.endlesscode.bbtest.mvp.view.`UsersView$$State`

@RunWith(JUnitPlatform::class)
class UsersPresenterSpec : Spek({

    given("a UsersPresenter") {
        val usersManager: UsersManager = mock()
        val usersPresenter = UsersPresenter(usersManager, AsyncContexts(Unconfined, Unconfined))
        val viewState: `UsersView$$State` = mock()

        var users: List<UserData>? = null
        var throwable: Throwable? = null

        beforeGroup {
            usersPresenter.setViewState(viewState)

            doAnswer { invocation ->
                val onSuccess: (List<UserData>) -> Unit = invocation.getArgument(0)
                val onError: (Throwable) -> Unit = invocation.getArgument(1)

                val givenUsers = users
                val thrownError = throwable
                when {
                    givenUsers != null -> onSuccess(givenUsers)
                    thrownError != null -> onError(thrownError)
                }
            }.`when`(usersManager).loadUsersList(any(), any())
        }

        beforeEachTest {
            users = null
            throwable = null
        }

        on("successful list init") {
            users = listOf(UserData(id = 1, firstName = "Foo", lastName = "Bar", email = "foo@bar.com", avatarUrl = ""))
            usersPresenter.initUsers()

            it("should hide add button") {
                verify(viewState, times(1)).hideAdd()
            }

            it("should show refreshing") {
                verify(viewState, times(1)).showRefreshing()
            }

            it("should init users") {
                verify(viewState, times(1)).initUsers()
            }

            it("should show add button") {
                verify(viewState, times(1)).showAdd()
            }

            it("should hide refreshing") {
                verify(viewState, times(1)).hideRefreshing()
            }

            it("shouldn't do anything others") {
                verifyNoMoreInteractions(viewState)
            }
        }

        on("successful list refreshing") {
            users = listOf(UserData(id = 1, firstName = "Foo", lastName = "Bar", email = "foo@bar.com", avatarUrl = ""))
            clearInvocations(viewState)
            usersPresenter.refreshUsers()

            it("should show refreshing") {
                verify(viewState, times(1)).showRefreshing()
            }

            it("should update users list") {
                verify(viewState, times(1)).updateUsers(any())
            }

            it("should hide refreshing") {
                verify(viewState, times(1)).hideRefreshing()
            }

            it("shouldn't do anything others") {
                verifyNoMoreInteractions(viewState)
            }
        }
    }
})