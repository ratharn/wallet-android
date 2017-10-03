/*
 * Copyright 2013-2015 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mycelium.spvmodule

import android.os.Handler
import android.util.Log

import org.bitcoinj.core.Coin
import org.bitcoinj.core.Transaction
import org.bitcoinj.core.listeners.TransactionConfidenceEventListener
import org.bitcoinj.wallet.Wallet
import org.bitcoinj.wallet.listeners.WalletChangeEventListener
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener
import org.bitcoinj.wallet.listeners.WalletCoinsSentEventListener
import org.bitcoinj.wallet.listeners.WalletReorganizeEventListener

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

abstract class ThrottlingWalletChangeListener constructor(private val throttleMs : Long = DEFAULT_THROTTLE_MS)
    : WalletChangeEventListener, WalletCoinsSentEventListener, WalletCoinsReceivedEventListener,
        WalletReorganizeEventListener, TransactionConfidenceEventListener {
    private val lastMessageTime = AtomicLong(0)
    private val handler = Handler()

    override fun onWalletChanged(walletAccount: Wallet) {
        handler.removeCallbacksAndMessages(null)

        val now = System.currentTimeMillis()

        val runnable = Runnable {
            lastMessageTime.set(System.currentTimeMillis())
            onChanged(walletAccount)
        }

        if (now - lastMessageTime.get() > throttleMs)
            handler.post(runnable)
        else
            handler.postDelayed(runnable, throttleMs)
    }

    abstract fun onChanged(walletAccount: Wallet)

    companion object {
        private val DEFAULT_THROTTLE_MS: Long = 500
    }
}
