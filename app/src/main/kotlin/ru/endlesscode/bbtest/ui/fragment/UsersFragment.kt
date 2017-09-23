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
import android.support.design.widget.Snackbar
import android.support.v7.util.DiffUtil
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arellomobile.mvp.MvpAppCompatFragment
import com.arellomobile.mvp.MvpFacade
import com.arellomobile.mvp.presenter.InjectPresenter
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader
import com.bumptech.glide.util.FixedPreloadSizeProvider
import kotlinx.android.synthetic.main.fragment_users.*
import ru.endlesscode.bbtest.R
import ru.endlesscode.bbtest.mvp.model.User
import ru.endlesscode.bbtest.mvp.presenter.HomePresenter
import ru.endlesscode.bbtest.mvp.presenter.UsersPresenter
import ru.endlesscode.bbtest.mvp.view.UsersView
import ru.endlesscode.bbtest.ui.adapter.UsersAdapter
import ru.endlesscode.bbtest.ui.getPresenter

/**
 * A placeholder fragment containing a simple view.
 */
class UsersFragment : MvpAppCompatFragment(), UsersView {

    @InjectPresenter
    lateinit var usersPresenter: UsersPresenter

    private lateinit var usersList: RecyclerView
    private val homePresenter by lazy { MvpFacade.getInstance().getPresenter<HomePresenter>(HomePresenter.TAG) }
    private val usersRefresh by lazy { users_refresh }
    private val buttonAdd by lazy { fab }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_users, container) ?: return null
        usersList = view.findViewById(R.id.users_list)
        usersList.init()

        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonAdd.setOnClickListener { homePresenter.onAddButtonPressed() }
        usersRefresh.setOnRefreshListener { usersPresenter.refreshUsers() }
    }

    override fun showError(message: String) {
        Snackbar.make(usersList, message, Snackbar.LENGTH_LONG)
                .setAction("Retry", { usersPresenter.reloadUsers() }).show()
    }

    override fun onStartLoading() {
        TODO("not implemented")
    }

    override fun onFinishLoading() {
        TODO("not implemented")
    }

    override fun showRefreshing() {
        usersRefresh.isRefreshing = true
    }

    override fun hideRefreshing() {
        usersRefresh.isRefreshing = false
    }

    override fun openAddUserView() {
        TODO("not implemented")
    }

    override fun updateUsers(diffResult: DiffUtil.DiffResult) {
        diffResult.dispatchUpdatesTo(usersList.adapter)
    }

    private fun RecyclerView.init() {
        setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this@UsersFragment.context)

        if (adapter == null) {
            adapter = UsersAdapter(usersPresenter)
        }

        val avatarSize = context.resources.getDimensionPixelSize(R.dimen.avatar_size)
        val sizeProvider = FixedPreloadSizeProvider<User>(avatarSize, avatarSize)
        val preloader = RecyclerViewPreloader<User>(
                Glide.with(this.context), adapter as UsersAdapter, sizeProvider, 10)
        addOnScrollListener(preloader)
    }
}
