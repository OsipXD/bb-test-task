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

package ru.endlesscode.bbtest.di

import dagger.Subcomponent
import ru.endlesscode.bbtest.di.modules.AvatarModule
import ru.endlesscode.bbtest.di.modules.UsersModule
import ru.endlesscode.bbtest.mvp.model.UsersManager
import ru.endlesscode.bbtest.ui.fragment.UsersFragment

@UsersScope
@Subcomponent(modules = arrayOf(UsersModule::class, AvatarModule::class))
interface UsersComponent {

    @Subcomponent.Builder
    interface Builder {
        fun usersModule(usersModule: UsersModule): UsersComponent.Builder
        fun avatarModule(avatarModule: AvatarModule): UsersComponent.Builder
        fun build(): UsersComponent
    }

    fun usersManager(): UsersManager
    fun inject(fragment: UsersFragment)
}