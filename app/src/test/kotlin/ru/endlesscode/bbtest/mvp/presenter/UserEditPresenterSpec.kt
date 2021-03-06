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

import android.graphics.Bitmap
import com.nhaarman.mockito_kotlin.*
import okhttp3.ResponseBody
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import ru.endlesscode.bbtest.mvp.model.UserItem
import ru.endlesscode.bbtest.mvp.model.UsersManager
import ru.endlesscode.bbtest.test.FileHelper
import kotlin.test.assertEquals
import ru.endlesscode.bbtest.mvp.view.`UserEditView$$State` as ViewState

@RunWith(JUnitPlatform::class)
class UserEditPresenterSpec : Spek({

    val usersManager: UsersManager = mock()
    val usersPresenter: UsersPresenter = mock()
    val homePresenter: HomePresenter = mock()
    var presenter = UserEditPresenter(usersManager, usersPresenter, homePresenter, "host.org")
    val viewState: ViewState = mock()

    var isError = false

    beforeEachTest {
        isError = false
        clearInvocations(viewState)
        doAnswer { invocation ->
            val onSuccess: (ResponseBody) -> Unit = invocation.getArgument(2)
            val onError: (Throwable) -> Unit = invocation.getArgument(3)

            when {
                isError -> onError(Exception())
                else -> onSuccess(mock())
            }
        }.`when`(usersManager).uploadAvatar(any(), any(), any(), any())
    }

    context(": edit existing user") {
        val user = UserItem(id = 2, firstName = "Foo", lastName = "Bar", email = "foo@bar.com", avatarUrl = "")
        val newUser = UserItem(id = 2, firstName = "newName", lastName = "newSurname", email = "new@Email", avatarUrl = "")
        val position = 1

        beforeGroup {
            presenter = UserEditPresenter(usersManager, usersPresenter, homePresenter, "host.org")
            presenter.setViewState(viewState)
            doAnswer { invocation ->
                val onSuccess: (Unit) -> Unit = invocation.getArgument(1)
                val onError: (Throwable) -> Unit = invocation.getArgument(2)

                when {
                    isError -> onError(Exception())
                    else -> onSuccess(Unit)
                }
            }.`when`(usersManager).updateUser(any(), any(), any())
        }

        on("view created") {
            presenter.onUpdateViewCreated(position, user)

            it("should set values to view") {
                verify(viewState, times(1)).setData(eq("Foo"), eq("Bar"), eq("foo@bar.com"), eq(""), eq(""))
            }

            it("should init 'new' values") {
                assertEquals(user.firstName, presenter.newName)
                assertEquals(user.lastName, presenter.newSurname)
                assertEquals(user.email, presenter.newEmail)
            }
        }

        it("should apply shake apply button if fields not changed") {
            presenter.onApplyClicked()
            verify(viewState, times(1)).shakeApplyButton()
        }

        on("view created again with same user but fields already changed") {
            presenter.newName = "newName"
            presenter.newSurname = "newSurname"
            presenter.newEmail = "new@Email"

            presenter.onUpdateViewCreated(position, user)
            it("should set right values") {
                verify(viewState, times(1)).setData(eq("newName"), eq("newSurname"), eq("new@Email"), eq(""), eq(""))
            }
        }

        it("should show errors") {
            isError = true
            presenter.onApplyClicked()
            verify(homePresenter, times(1)).showError(any())
        }

        on("apply button clicked") {
            presenter.onApplyClicked()

            it("should pass updated item to users presenter") {
                verify(usersPresenter, times(1)).updateUser(eq(1), eq(newUser))
            }

            it("should notify about result") {
                verify(homePresenter, times(1)).showMessage(any())
            }
        }

        on("new avatar selected and successfully updated") {
            val avatarPath = FileHelper.path("image/test.jpg")
            val bmp: Bitmap = mock()
            presenter.onAvatarUploadStarted(avatarPath)
            presenter.updateAvatarBitmap(bmp)

            it("should show indicator") {
                verify(viewState, times(1)).showAvatarUpdatingIndicator()
            }

            it("should set new avatar") {
                verify(viewState, times(1)).setAvatar(eq(avatarPath), eq(""))
            }

            it("should update user avatar on server") {
                val newAvatarUrl = "https://host.org/${user.id}.jpg"
                verify(usersPresenter, times(1)).updateUser(eq(position), eq(newUser.copy(avatarUrl = newAvatarUrl)))
            }

            it("should hide indicator") {
                verify(viewState, times(1)).hideAvatarUpdateIndicator()
            }
        }
    }

    context(": add new user") {
        beforeGroup {
            presenter = UserEditPresenter(usersManager, usersPresenter, homePresenter, "host.org")
            presenter.setViewState(viewState)
            doAnswer { invocation ->
                val onSuccess: (Unit) -> Unit = invocation.getArgument(1)
                val onError: (Throwable) -> Unit = invocation.getArgument(2)

                when {
                    isError -> onError(Exception())
                    else -> onSuccess(Unit)
                }
            }.`when`(usersManager).createUser(any(), any(), any())
        }

        it("should set empty data on first view creation") {
            presenter.onCreteViewCreated()
            verify(viewState).setData(eq(""), eq(""), eq(""), eq(""), eq(""))
        }

        on("view created again but fields already changed") {
            presenter.newName = "newName"
            presenter.newSurname = "newSurname"
            presenter.newEmail = "new@Email"

            presenter.onCreteViewCreated()
            it("should set right values") {
                verify(viewState, times(1)).setData(eq("newName"), eq("newSurname"), eq("new@Email"), eq(""), eq(""))
            }
        }

        it("should show errors") {
            isError = true
            presenter.onApplyClicked()
            verify(homePresenter, times(2)).showError(any())
        }

        on("apply button clicked") {
            presenter.onApplyClicked()

            it("should pass created item to users presenter") {
                verify(usersPresenter, times(1)).addUser(
                        eq(UserItem(id = -1, firstName = "newName", lastName = "newSurname", email = "new@Email", avatarUrl = ""))
                )
            }

            it("should notify about result") {
                verify(homePresenter, times(2)).showMessage(any())
            }
        }
    }

    it("should clear fields on clear button clicked") {
        presenter.onClearClicked()
        verify(viewState, times(1)).clearFields()
    }

    afterEachTest {
        verifyNoMoreInteractions(viewState)
        verifyNoMoreInteractions(usersPresenter)
    }
})
