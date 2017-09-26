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

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import ru.endlesscode.bbtest.di.UsersScope
import ru.endlesscode.bbtest.mvp.model.UserItem
import ru.endlesscode.bbtest.mvp.model.UsersManager
import ru.endlesscode.bbtest.mvp.view.UserEditView
import javax.inject.Inject

@InjectViewState
@UsersScope
class UserEditPresenter @Inject constructor(
        private val usersManager: UsersManager,
        private val usersPresenter: UsersPresenter,
        private val homePresenter: HomePresenter) : MvpPresenter<UserEditView>() {

    private var user = UserItem.EMPTY
    private var newUser = user.copy()

    var newName: String = ""
    var newSurname: String = ""
    var newEmail: String = ""

    private var position: Int = -1
    private var isUpdating = true

    fun onCreteViewCreated() {
        if (isUpdating) {
            user = UserItem.EMPTY
            newName = ""
            newSurname = ""
            newEmail = ""
        }

        isUpdating = false
        viewState.setData(newName, newSurname, newEmail, user.avatarUrl)
    }

    fun onUpdateViewCreated(position: Int, user: UserItem) {
        isUpdating = true
        if (this.user != user) {
            this.user = user
            this.position = position

            newName = this.user.firstName
            newSurname = this.user.lastName
            newEmail = this.user.email
        }

        viewState.setData(newName, newSurname, newEmail, user.avatarUrl)
    }

    fun onClearClicked() {
        viewState.clearFields()
    }

    fun onApplyClicked() {
        newUser = user.copy(firstName = newName, lastName = newSurname, email = newEmail)
        if (newUser == user) {
            viewState.shakeApplyButton()
            return
        }

        if (isUpdating) {
            usersManager.updateUser(newUser,
                    onSuccess = { onUserUpdated() },
                    onError = { onError(it) }
            )
        } else {
            usersManager.createUser(newUser,
                    onSuccess = { onUserCreated() },
                    onError = { onError(it) }
            )
        }
    }

    private fun onUserUpdated() {
        usersPresenter.updateUser(position, newUser)
        onOperationSuccessful("User successfully updated!")
    }

    private fun onUserCreated() {
        isUpdating = true
        homePresenter.back()
        usersPresenter.addUser(newUser)
        onOperationSuccessful("User successfully created!")
    }

    private fun onOperationSuccessful(message: String) {
        user = newUser
        viewState.showMessage(message)
    }

    private fun onError(exception: Throwable) {
        viewState.showError(exception.message.orEmpty())
    }
}
