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

import com.moez.QKSMS.common.base.QkViewContract
import com.moez.QKSMS.common.widget.PreferenceView
import io.reactivex.Observable

interface BlockingView : QkViewContract<BlockingState> {

    val blockingManagerIntent: Observable<*>
    val blockedNumbersIntent: Observable<*>
    val blockedMessagesIntent: Observable<*>
    val dropClickedIntent: Observable<*>
    val spamSwitchClickedIntent: Observable<*>

    fun preferenceClicks(): Observable<PreferenceView>
    fun queueSizeSelected(): Observable<Int>
    fun receiveWindowSelected(): Observable<Int>
    fun pauseTimeSelected(): Observable<Int>

    fun showQueueSizeDialog()
    fun showReceiveWindowDialog()
    fun showPauseTimeDialog()
    fun openBlockingManager()
    fun openBlockedNumbers()
    fun openBlockedMessages()
}