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

package ru.endlesscode.bbtest.ui.adapter

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.ListPreloader
import com.bumptech.glide.RequestBuilder
import kotlinx.android.synthetic.main.item_user.view.*
import ru.endlesscode.bbtest.R
import ru.endlesscode.bbtest.TestApp
import ru.endlesscode.bbtest.di.UsersScope
import ru.endlesscode.bbtest.misc.GlideProvider
import ru.endlesscode.bbtest.mvp.model.UserItem
import ru.endlesscode.bbtest.mvp.presenter.UsersPresenter
import ru.endlesscode.bbtest.mvp.view.UserItemView
import ru.endlesscode.bbtest.ui.inflate
import javax.inject.Inject

@UsersScope
class UsersAdapter @Inject constructor(protected val usersPresenter: UsersPresenter) :
        RecyclerView.Adapter<UsersAdapter.UserViewHolder>(),
        ListPreloader.PreloadModelProvider<UserItem> {

    @Inject
    lateinit var glideProvider: GlideProvider

    init {
        TestApp.appComponent.inject(this)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        usersPresenter.onBindUserAtPosition(position, holder)
    }

    override fun onCreateViewHolder(group: ViewGroup, viewType: Int): UserViewHolder = UserViewHolder(group)

    override fun getItemCount() = usersPresenter.count

    override fun getPreloadRequestBuilder(user: UserItem): RequestBuilder<*> = glideProvider.request.load(user.avatarUrl)

    override fun getPreloadItems(position: Int): MutableList<UserItem>
            = usersPresenter.getUsersAt(position, 2)

    inner class UserViewHolder(parent: ViewGroup) :
            RecyclerView.ViewHolder(parent.inflate(R.layout.item_user)),
            UserItemView {

        private val fullName: TextView = itemView.full_name
        private val email: TextView = itemView.email
        private val avatar: ImageView = itemView.avatar_view

        override fun setData(position: Int, user: UserItem) {
            fullName.text = user.fullName
            email.text = user.email

            glideProvider.request
                    .load(user.avatarUrl)
                    .into(avatar)

            itemView.setOnClickListener { usersPresenter.onUserItemClicked(position, user) }
        }
    }
}