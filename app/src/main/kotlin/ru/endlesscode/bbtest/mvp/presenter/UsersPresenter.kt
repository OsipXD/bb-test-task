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

import android.support.v7.util.DiffUtil
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.run
import retrofit2.HttpException
import ru.endlesscode.bbtest.mvp.common.AsyncContexts
import ru.endlesscode.bbtest.mvp.model.UserItem
import ru.endlesscode.bbtest.mvp.model.UsersDiffCallback
import ru.endlesscode.bbtest.mvp.model.UsersManager
import ru.endlesscode.bbtest.mvp.view.UserItemView
import ru.endlesscode.bbtest.mvp.view.UsersView
import java.net.SocketTimeoutException
import java.net.UnknownHostException

@InjectViewState
class UsersPresenter(private val usersManager: UsersManager, private val asyncContexts: AsyncContexts) : MvpPresenter<UsersView>() {

    private var isInLoading = false
    private val users: MutableList<UserItem> = mutableListOf()

    val count: Int
        get() = users.size

    override fun onFirstViewAttach() {
        initUsers()
    }

    fun initUsers() {
        viewState.hideAdd()
        loadUsers()
    }

    fun refreshUsers() {
        loadUsers()
    }

    private fun loadUsers() {
        if (isInLoading) {
            return
        }

        onLoadingStart()
        usersManager.loadUsersList(
                onSuccess = { usersData ->
                    val usersItems = usersData.map { UserItem(it) }.sortedBy { it.fullName.toLowerCase() }
                    when {
                        users.isNotEmpty() -> updateUsers(usersItems)
                        else -> initUsers(usersItems)
                    }
                },
                onError = { onLoadingFailed(it) }
        )
    }

    private fun onLoadingStart() {
        isInLoading = true
        viewState.showRefreshing()
    }

    private fun initUsers(usersItems: List<UserItem>) {
        users.addAll(usersItems)
        viewState.initUsers()
        viewState.showAdd()
        onFinishLoading()
    }

    private fun updateUsers(usersItems: List<UserItem>) = launch(asyncContexts.work) {
        val usersDiff = UsersDiffCallback(users, usersItems)
        val diffResult = DiffUtil.calculateDiff(usersDiff)

        users.clear()
        users.addAll(usersItems)
        run(asyncContexts.ui) {
            viewState.updateUsers(diffResult)
            onFinishLoading()
        }
    }

    private fun onLoadingFailed(error: Throwable) {
        val message = when (error) {
            is UnknownHostException -> "Are you connected to the Internet?"
            is SocketTimeoutException -> "Can't connect to server"
            is HttpException -> "Something wrong with server (${error.code()})"
            else -> {
                val errorMessage = if (error.message.isNullOrEmpty()) "" else ": ${error.message}"
                "Error$errorMessage"
            }
        }

        viewState.showError(message)
        onFinishLoading()
    }

    private fun onFinishLoading() {
        isInLoading = false
        viewState.hideRefreshing()
    }

    fun onBindUserAtPosition(position: Int, holder: UserItemView) {
        holder.setData(users[position])
    }

    fun getUsersAt(position: Int, count: Int): MutableList<UserItem> {
        val toIndex = (position + count).coerceAtMost(users.size)
        return users.subList(position, toIndex)
    }
}