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

import android.widget.TextView
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import ru.endlesscode.bbtest.di.UsersScope
import ru.endlesscode.bbtest.mvp.model.UserItem
import ru.endlesscode.bbtest.mvp.model.UsersManager
import ru.endlesscode.bbtest.mvp.view.UserEditView
import ru.endlesscode.bbtest.ui.common.validateIsEmail
import ru.endlesscode.bbtest.ui.common.validateNotBlank
import javax.inject.Inject

@InjectViewState
@UsersScope
class UserEditPresenter @Inject constructor(
        private val usersManager: UsersManager,
        private val usersPresenter: UsersPresenter) : MvpPresenter<UserEditView>() {

    lateinit var user: UserItem
    lateinit var newName: String
    lateinit var newSurname: String
    lateinit var newEmail: String

    fun onViewCreated(user: UserItem) {
        viewState.setData(user)
        this.user = user.copy()

        newName = this.user.firstName
        newSurname = this.user.lastName
        newEmail = this.user.email
    }

    fun onClearClicked() {
        viewState.clearFields()
    }

    fun validateName(view: TextView) {
        newName = view.validateNotBlank()
    }

    fun validateSurname(view: TextView) {
        newSurname = view.validateNotBlank()
    }

    fun validateEmail(view: TextView) {
        newEmail = view.validateIsEmail()
    }

    fun onApplyClicked() {
        if (newName.isNotEmpty() && newSurname.isNotEmpty() && newEmail.isNotEmpty()) {
            usersManager.updateUser(user.copy(firstName = newName, lastName = newSurname, email = newEmail),
                    onSuccess = { onSuccess() },
                    onError = { onError(it) }
            )
        }
    }

    private fun onSuccess() {
        // TODO: Make it normally
        usersPresenter.refreshUsers()
    }

    private fun onError(exception: Throwable) {
        TODO("not implemented yet")
    }
}
