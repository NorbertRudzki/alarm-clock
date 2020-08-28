package com.example.budzik

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.*

/** Klasa odpowiedzialna za ustawianie alarmów */
class AlarmClockManager {
    private var alarmMgr: AlarmManager? = null
    private lateinit var alarmIntent: PendingIntent

    /** Ustawia alarm cykliczny, który nie dezaktywuje się po zadzwonieniu i ustawi się na kolejną date */
    fun setAlarm(calendar:Calendar,context: Context, soundName: String) {
        alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmIntent = Intent(context, AlarmReciver::class.java)
            .putExtra("soundName",soundName)
            .let { intent ->
            PendingIntent.getBroadcast(context, calendar.timeInMillis.toInt(), intent, 0)
        }

        alarmMgr?.setExact(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            alarmIntent)
    }

    /** Ustawia alarm jednorazowy, który dezaktywuje się po zadzwonieniu*/
    fun setOneTimeAlarm(calendar:Calendar,context: Context, ID: String, soundName: String) {
        alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmIntent = Intent(context, AlarmReciver::class.java).putExtra("oneTime", ID)
            .putExtra("soundName", soundName)
            .let { intent ->
            PendingIntent.getBroadcast(context, calendar.timeInMillis.toInt(), intent, 0)
        }

        alarmMgr?.setExact(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            alarmIntent)
    }

    /** Anuluje zaplanowany wcześniej alarm */
    fun cancelAlarm(){
        alarmMgr?.cancel(alarmIntent)
        Log.d("Alarm","canceled")
    }
}