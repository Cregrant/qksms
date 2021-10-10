/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.moez.QKSMS.feature.blocking

import android.view.View
import com.bluelinelabs.conductor.RouterTransaction
import com.jakewharton.rxbinding2.view.clicks
import com.moez.QKSMS.R
import com.moez.QKSMS.common.QkChangeHandler
import com.moez.QKSMS.common.QkDialog
import com.moez.QKSMS.common.base.QkController
import com.moez.QKSMS.common.util.Colors
import com.moez.QKSMS.common.util.extensions.animateLayoutChanges
import com.moez.QKSMS.common.widget.PreferenceView
import com.moez.QKSMS.feature.blocking.manager.BlockingManagerController
import com.moez.QKSMS.feature.blocking.messages.BlockedMessagesController
import com.moez.QKSMS.feature.blocking.numbers.BlockedNumbersController
import com.moez.QKSMS.injection.appComponent
import io.reactivex.Observable
import kotlinx.android.synthetic.main.blocking_controller.*
import kotlinx.android.synthetic.main.settings_switch_widget.view.*
import javax.inject.Inject

class BlockingController : QkController<BlockingView, BlockingState, BlockingPresenter>(), BlockingView {

    override val blockingManagerIntent by lazy { blockingManager.clicks() }
    override val blockedNumbersIntent by lazy { blockedNumbers.clicks() }
    override val blockedMessagesIntent by lazy { blockedMessages.clicks() }
    override val dropClickedIntent by lazy { drop.clicks() }
    override val spamSwitchClickedIntent by lazy { spamSwitch.clicks() }

    @Inject lateinit var colors: Colors
    @Inject override lateinit var presenter: BlockingPresenter
    @Inject lateinit var queueSizeDialog: QkDialog
    @Inject lateinit var receiveWindowDialog: QkDialog
    @Inject lateinit var pauseTimeDialog: QkDialog


    init {
        appComponent.inject(this)
        retainViewMode = RetainViewMode.RETAIN_DETACH
        layoutRes = R.layout.blocking_controller
    }

    override fun onViewCreated() {
        super.onViewCreated()
        parent.postDelayed({ parent?.animateLayoutChanges = true }, 100)
        queueSizeDialog.adapter.setData(R.array.spam_sizes)
        receiveWindowDialog.adapter.setData(R.array.spam_time_values)
        pauseTimeDialog.adapter.setData(R.array.spam_pause_values)
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        presenter.bindIntents(this)
        setTitle(R.string.blocking_title)
        showBackButton(true)
    }

    override fun render(state: BlockingState) {
        blockingManager.summary = state.blockingManager
        drop.checkbox.isChecked = state.dropEnabled
        blockedMessages.isEnabled = !state.dropEnabled

        spamSwitch.checkbox.isChecked = state.spamSwitchEnabled

        spamQueueSizeButton.isEnabled = state.spamSwitchEnabled
        spamQueueSizeButton.summary = state.queueSizeSummary
        queueSizeDialog.adapter.selectedItem = state.queueSizeId

        spamReceiveWindowButton.isEnabled = state.spamSwitchEnabled
        spamReceiveWindowButton.summary = state.receiveWindowSummary
        receiveWindowDialog.adapter.selectedItem = state.receiveWindowId

        spamPauseTimeButton.isEnabled = state.spamSwitchEnabled
        spamPauseTimeButton.summary = state.pauseTimeSummary
        pauseTimeDialog.adapter.selectedItem = state.pauseTimeId
    }

    override fun preferenceClicks(): Observable<PreferenceView> = (0 until parent.childCount)
        .map { index -> parent.getChildAt(index) }
        .mapNotNull { view -> view as? PreferenceView }
        .map { preference -> preference.clicks().map { preference } }
        .let { preferences -> Observable.merge(preferences) }

    override fun queueSizeSelected(): Observable<Int> = queueSizeDialog.adapter.menuItemClicks

    override fun receiveWindowSelected(): Observable<Int> = receiveWindowDialog.adapter.menuItemClicks

    override fun pauseTimeSelected(): Observable<Int> = pauseTimeDialog.adapter.menuItemClicks

    override fun showQueueSizeDialog() = queueSizeDialog.show(activity!!)

    override fun showReceiveWindowDialog() = receiveWindowDialog.show(activity!!)

    override fun showPauseTimeDialog() = pauseTimeDialog.show(activity!!)

    override fun openBlockedNumbers() {
        router.pushController(RouterTransaction.with(BlockedNumbersController())
                .pushChangeHandler(QkChangeHandler())
                .popChangeHandler(QkChangeHandler()))
    }

    override fun openBlockedMessages() {
        router.pushController(RouterTransaction.with(BlockedMessagesController())
                .pushChangeHandler(QkChangeHandler())
                .popChangeHandler(QkChangeHandler()))
    }

    override fun openBlockingManager() {
        router.pushController(RouterTransaction.with(BlockingManagerController())
                .pushChangeHandler(QkChangeHandler())
                .popChangeHandler(QkChangeHandler()))
    }

}