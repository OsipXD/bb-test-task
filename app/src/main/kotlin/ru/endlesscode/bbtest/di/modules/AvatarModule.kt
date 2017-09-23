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

package ru.endlesscode.bbtest.di.modules

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.ListPreloader
import com.bumptech.glide.RequestManager
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader
import com.bumptech.glide.util.FixedPreloadSizeProvider
import dagger.Module
import dagger.Provides
import ru.endlesscode.bbtest.R
import ru.endlesscode.bbtest.di.UsersScope
import ru.endlesscode.bbtest.mvp.model.UserItem
import ru.endlesscode.bbtest.ui.adapter.UsersAdapter
import javax.inject.Named

@Module
class AvatarModule {

    @Provides
    @UsersScope
    fun provideRecyclerViewPreloader(
            requestManager: RequestManager,
            adapter: UsersAdapter,
            sizeProvider: ListPreloader.PreloadSizeProvider<UserItem>
    ): RecyclerViewPreloader<UserItem> {

        return RecyclerViewPreloader<UserItem>(requestManager, adapter, sizeProvider, 10)
    }

    @Provides
    @UsersScope
    fun provideRequestManager(context: Context): RequestManager = Glide.with(context)

    @Provides
    @UsersScope
    fun provideSizeProvider(@Named("avatarSize") avatarSize: Int): ListPreloader.PreloadSizeProvider<UserItem>
            = FixedPreloadSizeProvider<UserItem>(avatarSize, avatarSize)

    @Provides
    @UsersScope
    @Named("avatarSize")
    fun provideAvatarSize(context: Context): Int = context.resources.getDimensionPixelSize(R.dimen.avatar_size)
}

