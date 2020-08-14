package com.example.budzik

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class AlarmReciver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {



        val intentService = Intent(context, MyService::class.java)
        if(intent?.hasExtra("oneTime")!!){
            Log.d("AlarmReceiverOneTime","jest")
            intentService.putExtra("oneTime",intent.getStringExtra("oneTime"))
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context!!.startForegroundService(intentService)
        } else {
            context!!.startService(intentService)
        }
    }

}