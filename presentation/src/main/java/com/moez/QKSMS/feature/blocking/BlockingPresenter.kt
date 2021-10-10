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

import android.content.Context
import com.moez.QKSMS.R
import com.moez.QKSMS.blocking.BlockingClient
import com.moez.QKSMS.common.base.QkPresenter
import com.moez.QKSMS.interactor.ReceiveSms
import com.moez.QKSMS.util.Preferences
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import io.reactivex.rxkotlin.plusAssign
import javax.inject.Inject

class BlockingPresenter @Inject constructor(
    context: Context,
    private val blockingClient: BlockingClient,
    private val prefs: Preferences
) : QkPresenter<BlockingView, BlockingState>(BlockingState()) {

    init {
        disposables += prefs.blockingManager.asObservable()
                .map { client ->
                    when (client) {
                        Preferences.BLOCKING_MANAGER_SIA -> R.string.blocking_manager_sia_title
                        Preferences.BLOCKING_MANAGER_CC -> R.string.blocking_manager_call_control_title
                        else -> R.string.blocking_manager_qksms_title
                    }
                }
                .map(context::getString)
                .subscribe { manager -> newState { copy(blockingManager = manager) } }

        disposables += prefs.drop.asObservable()
                .subscribe { enabled -> newState { copy(dropEnabled = enabled) } }

        disposables += prefs.spamSwitch.asObservable()
            .subscribe { enabled -> newState { copy(spamSwitchEnabled = enabled) } }

        val queueSizeLabels = context.resources.getStringArray(R.array.spam_sizes)
        val queueSizeValues = context.resources.getIntArray(R.array.spam_sizes_values)
        disposables += prefs.queueSize.asObservable()
            .subscribe { id ->  newState { copy(queueSizeSummary = queueSizeLabels[id], queueSizeId = id) }
                                prefs.queueSizeValue = queueSizeValues[id] }

        val receiveWindowLabels = context.resources.getStringArray(R.array.spam_time_values)
        val receiveWindowValues = context.resources.getIntArray(R.array.spam_time_diffs_ids)
        disposables += prefs.receiveWindow.asObservable()
            .subscribe { id ->  newState { copy(receiveWindowSummary = receiveWindowLabels[id], receiveWindowId = id) }
                                prefs.receiveWindowValue = receiveWindowValues[id].toLong()
            }

        val pauseTimeLabels = context.resources.getStringArray(R.array.spam_pause_values)
        val pauseTimeValues = context.resources.getIntArray(R.array.spam_pause_times_ids)
        disposables += prefs.pauseTime.asObservable()
            .subscribe { id ->  newState { copy(pauseTimeSummary = pauseTimeLabels[id], pauseTimeId = id) }
                                prefs.pauseTimeValue = pauseTimeValues[id].toLong()
            }
    }

    override fun bindIntents(view: BlockingView) {
        super.bindIntents(view)

        view.preferenceClicks()
            .autoDisposable(view.scope())
            .subscribe{
                when (it.id) {
                    R.id.spamQueueSizeButton -> view.showQueueSizeDialog()

                    R.id.spamReceiveWindowButton -> view.showReceiveWindowDialog()

                    R.id.spamPauseTimeButton -> view.showPauseTimeDialog()
                }
            }

        view.blockingManagerIntent
                .autoDisposable(view.scope())
                .subscribe { view.openBlockingManager() }

        view.blockedNumbersIntent
                .autoDisposable(view.scope())
                .subscribe {
                    if (prefs.blockingManager.get() == Preferences.BLOCKING_MANAGER_QKSMS) {
                        // TODO: This is a hack, get rid of it once we implement AndroidX navigation
                        view.openBlockedNumbers()
                    } else {
                        blockingClient.openSettings()
                    }
                }

        view.blockedMessagesIntent
                .autoDisposable(view.scope())
                .subscribe { view.openBlockedMessages() }

        view.dropClickedIntent
                .autoDisposable(view.scope())
                .subscribe { prefs.drop.set(!prefs.drop.get()) }

        view.spamSwitchClickedIntent
                .autoDisposable(view.scope())
                .subscribe {
                    if (prefs.spamSwitch.get())
                        ReceiveSms.resetBlock()
                    prefs.spamSwitch.set(!prefs.spamSwitch.get())
                }

        view.queueSizeSelected()
            .doOnNext(prefs.queueSize::set)
            .autoDisposable(view.scope())
            .subscribe()

        view.receiveWindowSelected()
            .doOnNext(prefs.receiveWindow::set)
            .autoDisposable(view.scope())
            .subscribe()

        view.pauseTimeSelected()
            .doOnNext(prefs.pauseTime::set)
            .autoDisposable(view.scope())
            .subscribe()
    }

}