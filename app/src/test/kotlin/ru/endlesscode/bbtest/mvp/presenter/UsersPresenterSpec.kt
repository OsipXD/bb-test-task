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
import ru.endlesscode.bbtest.api.UserData
import ru.endlesscode.bbtest.mvp.misc.AsyncContexts
import ru.endlesscode.bbtest.mvp.model.UserItem
import ru.endlesscode.bbtest.mvp.model.UsersManager
import ru.endlesscode.bbtest.mvp.view.UserItemView
import kotlin.test.assertEquals
import ru.endlesscode.bbtest.mvp.view.`UsersView$$State` as ViewState

@RunWith(JUnitPlatform::class)
class UsersPresenterSpec : Spek({

    val usersManager: UsersManager = mock()
    val homePresenter: HomePresenter = mock()
    var presenter = UsersPresenter(usersManager, AsyncContexts(Unconfined, Unconfined), homePresenter)
    val viewState: ViewState = mock()

    val users: List<UserData> = listOf(
            UserData(id = 1, firstName = "Baz", lastName = "Qux", email = "baz@qux.com", avatarUrl = "http://www.fillmurray.com/200/200", updatedAt = ""),
            UserData(id = 2, firstName = "Foo", lastName = "Bar", email = "foo@bar.com", avatarUrl = "", updatedAt = "")
    )
    val error: Throwable = Exception()
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
        presenter = UsersPresenter(usersManager, AsyncContexts(Unconfined, Unconfined), homePresenter)
        presenter.setViewState(viewState)
        clearInvocations(viewState)

        isError = false
    }

    context(": refreshing users list") {
        on("successful list init") {
            presenter.initUsers()

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
            presenter.initUsers()
            clearInvocations(viewState)
            presenter.refreshUsers()

            it("should update users list") {
                verify(viewState, times(1)).updateUsers(any())
            }
        }

        on("error list init") {
            isError = true
            presenter.initUsers()

            it("should hide add button") {
                verify(viewState, times(1)).hideAdd()
            }

            it("should show error") {
                verify(homePresenter, times(1)).showError(any())
            }
        }

        on("error list refreshing") {
            presenter.initUsers()
            clearInvocations(viewState)

            isError = true
            presenter.refreshUsers()

            it("should show error") {
                verify(homePresenter, times(2)).showError(any())
            }
        }

        on("successful first refresh after error") {
            isError = true
            presenter.initUsers()
            clearInvocations(viewState)

            isError = false
            presenter.refreshUsers()

            it("should init users") {
                verify(viewState, times(1)).initUsers()
            }

            it("should show add button") {
                verify(viewState, times(1)).showAdd()
            }
        }

        on("successful refresh after error") {
            presenter.initUsers()
            isError = true
            presenter.refreshUsers()
            clearInvocations(viewState)

            isError = false
            presenter.refreshUsers()

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

    context(": getting users at position") {

        beforeEachTest {
            presenter.initUsers()
        }

        it("should return one user in list") {
            val givenUsers = presenter.getUsersAt(0, 1)
            assertEquals(1, givenUsers.size)
        }

        it("should return right number of users in list") {
            val count = 2
            val givenUsers = presenter.getUsersAt(0, count)
            assertEquals(count, givenUsers.size)
        }

        it("should not out of the list bounds") {
            val givenUsers = presenter.getUsersAt(0, users.lastIndex + 1)
            assertEquals(users.size, givenUsers.size)
        }
    }

    it("should bind data to holder") {
        presenter.initUsers()

        val holder: UserItemView = mock()
        presenter.onBindUserAtPosition(0, holder)

        verify(holder).setData(0, UserItem(users[0]))
    }

    it("should return right count") {
        presenter.initUsers()

        val actual = presenter.count

        assertEquals(users.size, actual)
    }
})
