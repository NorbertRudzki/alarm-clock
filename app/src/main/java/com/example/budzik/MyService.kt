package com.example.budzik

import android.app.*
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.BaseColumns
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat

class MyService: Service() {

    companion object{
        lateinit var mediaPlayer: MediaPlayer
    }
    private lateinit var v: Vibrator
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        mediaPlayer = MediaPlayer.create(applicationContext,R.raw.buzzer)
        v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if(!mediaPlayer.isPlaying){
            val notificationIntent = Intent(this, AlarmClockActivity::class.java)
            if(intent?.hasExtra("oneTime")!!){
                val dbHelper = DataBaseHelper(applicationContext)
                val db = dbHelper.writableDatabase
                val value = ContentValues()
                value.put(TableInfo.TABLE_COLUMN_IS_ACTIVE,"false")
                Log.d("id",intent.getStringExtra("oneTime")!!)
                db.update(TableInfo.TABLE_NAME, value, BaseColumns._ID +"=?", arrayOf(intent.getStringExtra("oneTime")))
            }
            val pendingIntent = PendingIntent.getActivity(this,0,notificationIntent,0)

            val notification = NotificationCompat.Builder(this, App.CHANNEL_ID)
                .setContentTitle("Alarm!")
                .setSmallIcon(R.drawable.ic_baseline_access_alarm_24)
                .setContentIntent(pendingIntent)
                .build()
            mediaPlayer.start()
            mediaPlayer.isLooping = true
            vibrate(v)

            startForeground(1, notification)
        }

        return START_STICKY
    }

    override fun onDestroy() {
        mediaPlayer.stop()
        v.cancel()
        Log.d("ondestroy","ondestroy")
        super.onDestroy()
    }
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    fun vibrate(v:Vibrator)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createWaveform(longArrayOf(200,300,400,500),0))
        } else {
            v.vibrate(longArrayOf(200,300,400,500),0)
        }
    }


}