package com.example.budzik

import android.app.*
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.camera2.CameraManager
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.BaseColumns
import android.util.Log
import androidx.activity.result.ActivityResultCallback
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import java.lang.IllegalArgumentException
import java.util.jar.Manifest

class MyService: Service() {

    companion object{
        lateinit var mediaPlayer: MediaPlayer
    }
    private lateinit var v: Vibrator
    private lateinit var cam: CameraManager
    private var permissionGranded = false
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
            permissionGranded = true
            cam = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        }

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
            if(permissionGranded){
                try{
                    cam.setTorchMode(cam.cameraIdList[0],true)
                }catch (e: IllegalArgumentException){
                    Log.d("torchException",e.message!!)
                }
            }


            startForeground(1, notification)
        }

        return START_STICKY
    }

    override fun onDestroy() {
        mediaPlayer.stop()
        v.cancel()
        if(permissionGranded){
            try{
                cam.setTorchMode(cam.cameraIdList[0],false)
            }catch (e: IllegalArgumentException){
                Log.d("torchException",e.message!!)
            }
        }
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