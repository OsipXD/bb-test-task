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
import android.support.v7.util.ListUpdateCallback
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.run
import ru.endlesscode.bbtest.di.UsersScope
import ru.endlesscode.bbtest.mvp.common.AsyncContexts
import ru.endlesscode.bbtest.mvp.model.UserItem
import ru.endlesscode.bbtest.mvp.model.UsersDiffCallback
import ru.endlesscode.bbtest.mvp.model.UsersManager
import ru.endlesscode.bbtest.mvp.view.UserItemView
import ru.endlesscode.bbtest.mvp.view.UsersView
import javax.inject.Inject

@InjectViewState
@UsersScope
class UsersPresenter @Inject constructor(
        private val usersManager: UsersManager,
        private val asyncContexts: AsyncContexts,
        private val homePresenter: HomePresenter) : MvpPresenter<UsersView>() {

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

    private fun loadUsers(onDiff: UsersPresenter.(DiffUtil.DiffResult) -> Unit = { }) {
        if (isInLoading) {
            return
        }

        onLoadingStart()
        usersManager.loadUsersList(
                onSuccess = { usersData ->
                    val usersItems = usersData.map { UserItem(it) }.sortedBy { it.fullName.toLowerCase() }
                    when {
                        users.isNotEmpty() -> updateUsers(usersItems, onDiff)
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

    private fun updateUsers(usersItems: List<UserItem>, onDiff: UsersPresenter.(DiffUtil.DiffResult) -> Unit) = launch(asyncContexts.work) {
        val usersDiff = UsersDiffCallback(users, usersItems)
        val diffResult = DiffUtil.calculateDiff(usersDiff)

        users.clear()
        users.addAll(usersItems)
        run(asyncContexts.ui) {
            viewState.updateUsers(diffResult)
            onDiff(diffResult)
            onFinishLoading()
        }
    }

    private fun onLoadingFailed(error: Throwable) {
        homePresenter.showError(error)
        onFinishLoading()
    }

    private fun onFinishLoading() {
        isInLoading = false
        viewState.hideRefreshing()
    }

    fun onBindUserAtPosition(position: Int, holder: UserItemView) {
        holder.setData(position, users[position])
    }

    fun getUsersAt(position: Int, count: Int): MutableList<UserItem> {
        val toIndex = (position + count).coerceAtMost(users.size)
        return users.subList(position, toIndex)
    }

    fun onUserItemClicked(position: Int, user: UserItem) {
        viewState.showUserEditView(position, user)
    }

    fun updateUser(position: Int, user: UserItem) {
        users[position] = user
        viewState.updateUser(position)
    }

    fun addUser(user: UserItem) = loadUsers {
        val insertedPositions = mutableListOf<Int>()
        it.dispatchUpdatesTo(object : ListUpdateCallback {
            override fun onChanged(p0: Int, p1: Int, p2: Any?) {}

            override fun onMoved(p0: Int, p1: Int) {}

            override fun onInserted(position: Int, count: Int) {
                insertedPositions.addAll(position until position + count)
            }

            override fun onRemoved(p0: Int, p1: Int) {}
        })

        insertedPositions.forEach { postition ->
            val userFromList = users[postition]
            if (userFromList == user.copy(id = userFromList.id)) {
                viewState.scrollTo(postition)
                return@loadUsers
            }
        }
    }
}
