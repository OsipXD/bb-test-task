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

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.support.design.widget.Snackbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
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
import ru.endlesscode.bbtest.ui.common.validateIsEmail
import ru.endlesscode.bbtest.ui.common.validateNotBlank
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
    private val nameInputLayout by lazy { name_input_layout }
    private val surnameInputLayout by lazy { surname_input_layout }
    private val emailInputLayout by lazy { email_input_layout }
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

        if (arguments == null) {
            presenter.onCreteViewCreated()
        } else {
            val position: Int = arguments.getInt("position")
            val user: UserItem = arguments.getParcelable("user")
            presenter.onUpdateViewCreated(position, user)
        }

        btnClear.setOnClickListener { presenter.onClearClicked() }
        btnApply.setOnClickListener {
            if (validateFields()) {
                presenter.onApplyClicked()
            } else {
                shakeApplyButton()
            }
        }

        nameField.setOnFocusLostListener { validateNameField() }
        surnameField.setOnFocusLostListener { validateSurnameField() }
        emailField.setOnFocusLostListener { validateEmailField() }
    }

    override fun shakeApplyButton() {
        val shake = AnimationUtils.loadAnimation(this.context, R.anim.shake)
        btnApply.startAnimation(shake)
        this.vibrate(duration = 15, delay = 10, times = 20)
    }

    private fun vibrate(duration: Long, delay: Long, times: Int) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            var pattern = longArrayOf(0)
            repeat(times) { pattern += duration + delay }
            if (Build.VERSION.SDK_INT >= 26) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                vibrator.vibrate(pattern, -1)
            }
        }
    }

    private fun validateFields(): Boolean {
        return validateNameField() and validateSurnameField() and validateEmailField()
    }

    private fun validateNameField(): Boolean {
        val isValid = nameField.validateNotBlank(nameInputLayout)
        presenter.newName = nameField.text.toString()

        return isValid
    }

    private fun validateSurnameField(): Boolean {
        val isValid = surnameField.validateNotBlank(surnameInputLayout)
        presenter.newSurname = surnameField.text.toString()

        return isValid
    }

    private fun validateEmailField(): Boolean {
        val isValid = emailField.validateIsEmail(emailInputLayout)
        presenter.newEmail = emailField.text.toString()

        return isValid
    }

    override fun setData(name: String, surname: String, email: String, avatarUrl: String) {
        nameField.setText(name)
        surnameField.setText(surname)
        emailField.setText(email)

        glideProvider.request.clone().apply {
            load(avatarUrl)
            placeholder(R.drawable.ic_avatar_placeholder_24px)
            error(R.drawable.ic_avatar_placeholder_24px)
            override(avatar.width, avatar.height)
            into(avatar)
        }
    }

    override fun clearFields() {
        nameField.setText("")
        surnameField.setText("")
        emailField.setText("")
    }

    override fun showMessage(message: String) {
        Snackbar.make(avatar, message, Snackbar.LENGTH_SHORT)
                .setAction("OK", { }).show()
    }

    override fun showError(message: String) {
        Snackbar.make(avatar, message, Snackbar.LENGTH_LONG)
                .setAction("OK", { }).show()
    }
}
