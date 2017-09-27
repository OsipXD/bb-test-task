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
import android.graphics.BitmapFactory
import android.support.annotation.VisibleForTesting
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import ru.endlesscode.bbtest.di.UsersScope
import ru.endlesscode.bbtest.mvp.model.UserItem
import ru.endlesscode.bbtest.mvp.model.UsersManager
import ru.endlesscode.bbtest.mvp.view.UserEditView
import javax.inject.Inject
import javax.inject.Named

@InjectViewState
@UsersScope
class UserEditPresenter @Inject constructor(
        private val usersManager: UsersManager,
        private val usersPresenter: UsersPresenter,
        private val homePresenter: HomePresenter,
        private @Named("host") val host: String) : MvpPresenter<UserEditView>() {

    private var user = UserItem.EMPTY

    var newName: String = ""
    var newSurname: String = ""
    var newEmail: String = ""
    var newAvatar: String = ""

    private var position: Int = -1
    private var isUpdating = true

    fun onCreteViewCreated() {
        if (isUpdating) {
            user = UserItem.EMPTY
            newName = ""
            newSurname = ""
            newEmail = ""
            newAvatar = ""
        }

        isUpdating = false
        updateViewData()
    }

    fun onUpdateViewCreated(position: Int, user: UserItem) {
        isUpdating = true
        if (this.user != user) {
            this.user = user
            this.position = position

            newName = user.firstName
            newSurname = user.lastName
            newEmail = user.email
            newAvatar = user.avatarUrl
        }

        updateViewData()
    }

    private fun updateViewData() {
        viewState.setData(newName, newSurname, newEmail, newAvatar)
    }

    fun onClearClicked() {
        viewState.clearFields()
    }


    fun updateAvatar(path: String) {
        onAvatarUploadStarted(path)
        updateAvatarBitmap(BitmapFactory.decodeFile(path))
    }

    @VisibleForTesting
    fun updateAvatarBitmap(bitmap: Bitmap) {
        usersManager.uploadAvatar(user, bitmap,
                onSuccess = { onAvatarUploaded(user.avatarS3Url) },
                onError = { onError(it) }
        )
    }

    @VisibleForTesting
    fun onAvatarUploadStarted(path: String) {
        viewState.setAvatar(path)
        viewState.showAvatarUpdatingIndicator()
    }

    private fun onAvatarUploaded(url: String) {
        newAvatar = url
        if (isUpdating) {
            sendUserUpdate(user.copy(avatarUrl = url), notify = false)
            viewState.hideAvatarUpdateIndicator()
        }
    }

    fun onApplyClicked() {
        val newUser = user.copy(firstName = newName, lastName = newSurname, email = newEmail, avatarUrl = newAvatar)
        if (newUser == user) {
            viewState.shakeApplyButton()
            return
        }

        if (isUpdating) sendUserUpdate(newUser) else sendUserCreate(newUser)
    }

    private fun sendUserUpdate(newUser: UserItem, notify: Boolean = true) {
        usersManager.updateUser(newUser,
                onSuccess = { onUserUpdated(newUser, notify) },
                onError = { onError(it) }
        )
    }

    private fun sendUserCreate(newUser: UserItem) {
        usersManager.createUser(newUser,
                onSuccess = { onUserCreated(newUser) },
                onError = { onError(it) }
        )
    }

    private fun onUserUpdated(newUser: UserItem, notify: Boolean) {
        usersPresenter.updateUser(position, newUser)
        if (notify) {
            homePresenter.showMessage("User successfully updated!")
        }
        onOperationSuccessful(newUser)
    }

    private fun onUserCreated(newUser: UserItem) {
        isUpdating = true
        usersPresenter.addUser(newUser)
        homePresenter.back()
        homePresenter.showMessage("User successfully created!")
        onOperationSuccessful(newUser)
    }

    private fun onOperationSuccessful(newUser: UserItem) {
        user = newUser
    }

    private fun onError(exception: Throwable) {
        homePresenter.showError(exception)
    }

    private val UserItem.avatarS3Url: String
        get() = "https://$host/$id.jpg"
}
