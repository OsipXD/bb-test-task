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
import ru.endlesscode.bbtest.TestApp
import ru.endlesscode.bbtest.api.UserData
import ru.endlesscode.bbtest.mvp.model.UsersManager
import ru.endlesscode.bbtest.mvp.view.UsersView
import java.net.UnknownHostException
import javax.inject.Inject

@InjectViewState
class UsersPresenter : MvpPresenter<UsersView>() {

    @Inject
    lateinit var usersManager: UsersManager

    private var isInLoading = false

    init {
        TestApp.apiComponent.inject(this)
    }

    override fun onFirstViewAttach() {
        this.loadUsers()
    }

    fun refreshUsers() {
        loadUsers()
    }

    fun reloadUsers() {
        loadUsers()
    }

    private fun loadUsers() {
        if (isInLoading) {
            return
        }

        onLoadingStart()
        usersManager.loadUsersList(
                onSuccess = { onLoadingSuccess(it) },
                onError = { onLoadingFailed(it) }
        )
    }

    private fun onLoadingStart() {
        isInLoading = true
        viewState.showRefreshing()
    }

    private fun onLoadingSuccess(users: List<UserData>) {
        viewState.setUsers(users)
        onFinishLoading()
    }

    private fun onLoadingFailed(error: Throwable) {
        val message = when (error) {
            is UnknownHostException -> "Can't connect to server"
            else -> error.message ?: "Error"
        }

        viewState.showError(message)
        onFinishLoading()
    }

    private fun onFinishLoading() {
        isInLoading = false
        viewState.hideRefreshing()
    }
}