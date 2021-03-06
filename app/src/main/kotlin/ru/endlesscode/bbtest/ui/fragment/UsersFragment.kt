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

package ru.endlesscode.bbtest.ui.fragment

import android.os.Bundle
import android.support.v7.util.DiffUtil
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arellomobile.mvp.MvpAppCompatFragment
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader
import dagger.Lazy
import kotlinx.android.synthetic.main.fragment_users.*
import ru.endlesscode.bbtest.R
import ru.endlesscode.bbtest.TestApp
import ru.endlesscode.bbtest.mvp.model.UserItem
import ru.endlesscode.bbtest.mvp.presenter.HomePresenter
import ru.endlesscode.bbtest.mvp.presenter.UsersPresenter
import ru.endlesscode.bbtest.mvp.view.UsersView
import ru.endlesscode.bbtest.ui.adapter.UsersAdapter
import javax.inject.Inject

/**
 * A placeholder fragment containing a simple view.
 */
class UsersFragment : MvpAppCompatFragment(), UsersView {

    @Inject
    @InjectPresenter
    lateinit var presenter: UsersPresenter

    @Inject
    lateinit var adapter: Lazy<UsersAdapter>
    @Inject
    lateinit var avatarsPreloader: Lazy<RecyclerViewPreloader<UserItem>>
    @Inject
    lateinit var homePresenter: Lazy<HomePresenter>

    private lateinit var usersList: RecyclerView
    private val usersRefresh by lazy { users_refresh }
    private val buttonAdd by lazy { fab }

    @ProvidePresenter
    fun provideUsersPresenter(): UsersPresenter = presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        TestApp.usersComponent.inject(this)

        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_users, container, false) ?: return null
        usersList = view.findViewById(R.id.users_list)
        usersList.init()

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        buttonAdd.setOnClickListener { homePresenter.get().showUserCreatingView() }
        usersRefresh.setOnRefreshListener { presenter.refreshUsers() }
    }

    override fun showRefreshing() {
        usersRefresh.isRefreshing = true
    }

    override fun hideRefreshing() {
        usersRefresh.isRefreshing = false
    }

    override fun initUsers() {
        usersList.adapter.notifyDataSetChanged()
    }

    override fun updateUsers(diffResult: DiffUtil.DiffResult) {
        diffResult.dispatchUpdatesTo(usersList.adapter)
    }

    override fun hideAdd() {
        buttonAdd.hide()
    }

    override fun showAdd() {
        buttonAdd.show()
    }

    private fun RecyclerView.init() {
        setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this@UsersFragment.context)

        if (adapter == null) {
            this.adapter = this@UsersFragment.adapter.get()
        }

        addOnScrollListener(avatarsPreloader.get())
    }

    override fun showUserEditView(position: Int, user: UserItem) {
        homePresenter.get().showUserEditView(position, user)
    }

    override fun updateUser(position: Int) {
        adapter.get().notifyItemChanged(position)
    }

    override fun scrollTo(position: Int) {
        usersList.scrollToPosition(position)
    }
}
