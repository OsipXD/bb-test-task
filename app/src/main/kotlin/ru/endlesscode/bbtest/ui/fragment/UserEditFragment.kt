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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arellomobile.mvp.MvpAppCompatFragment
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import kotlinx.android.synthetic.main.fragment_edit_user.*
import ru.endlesscode.bbtest.R
import ru.endlesscode.bbtest.TestApp
import ru.endlesscode.bbtest.misc.GlideProvider
import ru.endlesscode.bbtest.mvp.model.UserItem
import ru.endlesscode.bbtest.mvp.presenter.UserEditPresenter
import ru.endlesscode.bbtest.mvp.view.UserEditView
import ru.endlesscode.bbtest.ui.common.setOnFocusLostListener
import javax.inject.Inject

class UserEditFragment : MvpAppCompatFragment(), UserEditView {

    @Inject
    @InjectPresenter
    lateinit var presenter: UserEditPresenter

    @Inject
    lateinit var glideProvider: GlideProvider

    private val nameField by lazy { name_field }
    private val surnameField by lazy { surname_field }
    private val emailField by lazy { email_field }
    private val avatar by lazy { avatar_view }
    private val btnApply by lazy { button_apply }
    private val btnClear by lazy { button_clear }

    @ProvidePresenter
    fun providePresenter(): UserEditPresenter = presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        TestApp.usersComponent.inject(this)

        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_edit_user, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user: UserItem = this.arguments.getParcelable("user")
        val position: Int = this.arguments.getInt("position")
        presenter.onViewCreated(position, user)

        btnClear.setOnClickListener { presenter.onClearClicked() }
        btnApply.setOnClickListener {
            presenter.validateName(nameField)
            presenter.validateSurname(surnameField)
            presenter.validateEmail(emailField)
            presenter.onApplyClicked()
        }

        nameField.setOnFocusLostListener { presenter.validateName(nameField) }
        surnameField.setOnFocusLostListener { presenter.validateSurname(surnameField) }
        emailField.setOnFocusLostListener { presenter.validateEmail(emailField) }
    }

    override fun setData(name: String, surname: String, email: String, avatarUrl: String) {
        nameField.setText(name)
        surnameField.setText(surname)
        emailField.setText(email)

        glideProvider.request.clone().apply {
            load(avatarUrl)
            override(avatar.width, avatar.height)
            into(avatar)
        }
    }

    override fun clearFields() {
        nameField.setText("")
        surnameField.setText("")
        emailField.setText("")
    }

    override fun showRefreshing() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun hideRefreshing() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onUpdated() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
