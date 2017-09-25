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
import org.jetbrains.spek.api.dsl.on
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import ru.endlesscode.bbtest.mvp.model.UserItem
import ru.endlesscode.bbtest.mvp.model.UsersManager
import kotlin.test.assertEquals
import ru.endlesscode.bbtest.mvp.view.`UserEditView$$State` as ViewState

@RunWith(JUnitPlatform::class)
class UserEditPresenterSpec : Spek({

    val usersManager: UsersManager = mock()
    val usersPresenter: UsersPresenter = mock()
    val presenter = UserEditPresenter(usersManager, usersPresenter)
    val viewState: ViewState = mock()

    val error: Throwable = Exception()
    val isError = false

    beforeGroup {
        presenter.setViewState(viewState)
        doAnswer { invocation ->
            val onSuccess: (Unit) -> Unit = invocation.getArgument(1)
            val onError: (Throwable) -> Unit = invocation.getArgument(2)

            when {
                isError -> onError(error)
                else -> onSuccess(Unit)
            }
        }.`when`(usersManager).updateUser(any(), any(), any())
    }

    beforeEachTest {
        clearInvocations(viewState)
    }

    context("edit existence user") {
        val user = UserItem(id = 2, firstName = "Foo", lastName = "Bar", email = "foo@bar.com", avatarUrl = "")
        val position = 1

        on("view created") {
            presenter.onViewCreated(position, user)

            it("should set values to view") {
                verify(viewState, times(1)).setData(eq("Foo"), eq("Bar"), eq("foo@bar.com"), eq(""))
            }

            it("should init 'new' values") {
                assertEquals(user.firstName, presenter.newName)
                assertEquals(user.lastName, presenter.newSurname)
                assertEquals(user.email, presenter.newEmail)
            }
        }

        on("view created again") {
            it("should set right values if there are changed") {
                presenter.newName = "newName"
                presenter.newSurname = "newSurname"
                presenter.newEmail = "new@Email"

                presenter.onViewCreated(position, user)

                verify(viewState, times(1)).setData(eq("newName"), eq("newSurname"), eq("new@Email"), eq(""))
            }
        }

        on("apply button clicked when all fields are valid") {
            presenter.onApplyClicked()

            it("should pass updated item to users presenter") {
                verify(usersPresenter, times(1)).updateUser(eq(1),
                        eq(user.copy(firstName = "newName", lastName = "newSurname", email = "new@Email", avatarUrl = ""))
                )
            }

            it("should notify about result") {
                verify(viewState, times(1)).showMessage(any())
            }
        }

        afterEachTest {
            verifyNoMoreInteractions(viewState)
        }
    }
})