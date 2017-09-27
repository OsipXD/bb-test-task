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

import android.os.Bundle
import android.support.v4.app.Fragment
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import retrofit2.HttpException
import ru.endlesscode.bbtest.mvp.model.UserItem
import ru.endlesscode.bbtest.mvp.view.HomeView
import ru.endlesscode.bbtest.ui.fragment.UserEditFragment
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@InjectViewState
@Singleton
class HomePresenter @Inject constructor() : MvpPresenter<HomeView>() {

    fun showUserCreatingView() {
        this.showFragment(UserEditFragment())
    }

    fun showUserEditView(position: Int, user: UserItem) {
        val bundle = Bundle()
        bundle.putParcelable("user", user)
        bundle.putInt("position", position)
        this.showFragment(UserEditFragment().apply { arguments = bundle })
    }

    private fun showFragment(fragment: Fragment) {
        viewState.showFragment(fragment)
    }

    fun showError(error: Throwable) {
        val message = when (error) {
            is UnknownHostException -> "Are you connected to the Internet?"
            is SocketTimeoutException -> "Can't connect to server"
            is HttpException -> when (error.code()) {
                422 -> "Invalid email, please check it and retry"
                else -> "Something wrong with server (${error.code()})"
            }
            else -> {
                val errorMessage = if (error.message.isNullOrBlank()) "" else ": ${error.message}"
                "Error$errorMessage"
            }
        }
        error.printStackTrace()

        viewState.showError(message)
    }

    fun showMessage(message: String) {
        viewState.showMessage(message)
    }

    fun back() {
        viewState.back()
    }
}
