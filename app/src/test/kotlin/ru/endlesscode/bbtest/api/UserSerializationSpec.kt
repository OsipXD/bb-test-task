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

package ru.endlesscode.bbtest.api

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import ru.endlesscode.bbtest.di.DaggerTestApiComponent
import ru.endlesscode.bbtest.di.TestApiComponent
import kotlin.test.assertEquals

@RunWith(JUnitPlatform::class)
class UserSerializationSpec : Spek({
    val apiComponent: TestApiComponent = DaggerTestApiComponent.builder().build()
    val gson: Gson = apiComponent.gson()

    given("a json with users list") {
        val jsonText = FileHelper.readJson("Users")

        on("deserialize") {
            val listType = object : TypeToken<ArrayList<UserData>>() {}.type
            val users: List<UserData> = gson.fromJson(jsonText, listType)

            it("should be with right size") {
                assertEquals(2, users.size)
            }

            it("should contains right elements") {
                val expected = listOf(
                        UserData(id = 1, firstName = "Foo", lastName = "Bar", email = "foo@bar.com", avatarUrl = ""),
                        UserData(id = 2, firstName = "Baz", lastName = "Qux", email = "baz@qux.com", avatarUrl = "http://www.fillmurray.com/200/200")
                )
                assertEquals(expected, users)
            }
        }
    }

    given("a user data") {
        val user = UserData(id = 1, firstName = "Foo", lastName = "Bar", email = "foo@bar.com", avatarUrl = "")

        on("serialize user update body") {
            val jsonText = gson.toJson(UserUpdateBody(user))

            it("should be serialized right") {
                val expected = FileHelper.readJson("OneUser")

                assertEquals(expected, jsonText)
            }
        }
    }
})