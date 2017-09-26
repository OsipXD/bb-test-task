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

package ru.endlesscode.bbtest.ui.activity

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.view.Menu
import android.view.MenuItem
import com.arellomobile.mvp.MvpAppCompatActivity
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import kotlinx.android.synthetic.main.activity_main.*
import ru.endlesscode.bbtest.R
import ru.endlesscode.bbtest.TestApp
import ru.endlesscode.bbtest.mvp.presenter.HomePresenter
import ru.endlesscode.bbtest.mvp.view.HomeView
import ru.endlesscode.bbtest.ui.fragment.UsersFragment
import javax.inject.Inject

class HomeActivity : MvpAppCompatActivity(), HomeView {

    @Inject
    @InjectPresenter
    lateinit var presenter: HomePresenter

    private val toolbar by lazy { main_toolbar }
    private val mainLayout by lazy { main_layout }

    @ProvidePresenter
    fun providePresenter() = presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        TestApp.appComponent.inject(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        if (savedInstanceState != null) {
            return
        }

        this.showFragment(UsersFragment(), addToBackStack = false)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        return if (id == R.id.action_settings) true else super.onOptionsItemSelected(item)
    }

    override fun showFragment(fragment: Fragment, title: String, addToBackStack: Boolean) {
        val manager = supportFragmentManager
        val tag = fragment.tag

        manager.beginTransaction().apply {
            add(R.id.fragment_container, fragment, tag)
            if (addToBackStack) {
                addToBackStack(tag)
            }
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        }.commit()

        if (title.isNotBlank()) {
            toolbar.title = title
        }
    }

    override fun back() {
        this.onBackPressed()
    }

    override fun showError(message: String) {
        Snackbar.make(mainLayout, message, Snackbar.LENGTH_LONG)
                .setAction("Got it", { }).show()
    }

    override fun showMessage(message: String) {
        Snackbar.make(mainLayout, message, Snackbar.LENGTH_SHORT)
                .setAction("OK", { }).show()
    }
}
