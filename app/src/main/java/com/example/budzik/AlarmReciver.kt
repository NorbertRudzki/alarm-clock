package com.example.budzik

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * BroadcastReceiver, który po odebraniu sygnału wysłanego
 * przez AlarmClockManager uruchamia AlarmClockService
 */
class AlarmReciver : BroadcastReceiver() {

    /** Po odebraniu sygnału wysłanego przez AlarmClockManager uruchamia AlarmClockService
     * przekazując mu informacje, czy budzik był jednorazowy, czy cykliczny oraz
     * nazwe dźwięku, który ma być odtwarzany*/
    override fun onReceive(context: Context?, intent: Intent?) {
        val intentService = Intent(context, AlarmClockService::class.java)
        if (intent?.hasExtra("oneTime")!!) {
            intentService.putExtra("oneTime", intent.getStringExtra("oneTime"))
        }
        intentService.putExtra("soundName", intent.getStringExtra("soundName"))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context!!.startForegroundService(intentService)
        } else {
            context!!.startService(intentService)
        }
    }
}