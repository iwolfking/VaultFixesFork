package net.cdnbcn.vaultfixes.data

import java.util.TimerTask

class DelegatingTimerTask(private val func: Runnable) : TimerTask() {
    override fun run() {
        func.run()
    }
}