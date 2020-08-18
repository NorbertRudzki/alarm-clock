package com.example.budzik

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.*
import java.util.concurrent.TimeUnit

// todo requestcode jako calendar.millis?
class AlarmM {
    private var alarmMgr: AlarmManager? = null
    private lateinit var alarmIntent: PendingIntent

    fun setAlarm(calendar:Calendar,context: Context){

        alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmIntent = Intent(context, AlarmReciver::class.java).let { intent ->
            PendingIntent.getBroadcast(context, calendar.timeInMillis.toInt(), intent, 0)
        }
        //alarm raz
        alarmMgr?.setExact(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            alarmIntent
        )
    }
    fun setOneTimeAlarm(calendar:Calendar,context: Context, ID: String){
        alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmIntent = Intent(context, AlarmReciver::class.java).putExtra("oneTime",ID).let { intent ->
            PendingIntent.getBroadcast(context, calendar.timeInMillis.toInt(), intent, 0)
        }
        //alarm raz
        alarmMgr?.setExact(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            alarmIntent
        )
    }

    fun cancelAlarm(){
        alarmMgr?.cancel(alarmIntent)
        Log.d("Alarm","canceled")
    }

}