package com.example.budzik

import android.app.*
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.BaseColumns
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import java.lang.IllegalArgumentException

class AlarmClockService : Service() {

    companion object {
        lateinit var mediaPlayer: MediaPlayer
    }

    private val scope = CoroutineScope(Job() + Dispatchers.Default)
    private lateinit var vibrator: Vibrator
    private lateinit var cam: CameraManager
    private var permissionGranded = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.CAMERA
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            permissionGranded = true
            cam = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        }

        mediaPlayer = when (val soundName = intent?.getStringExtra("soundName")) {
            "buzzer" -> MediaPlayer.create(applicationContext, R.raw.buzzer)
            "forstarr" -> MediaPlayer.create(applicationContext, R.raw.forstarr)
            "guitar" -> MediaPlayer.create(applicationContext, R.raw.guitar)
            "osmium" -> MediaPlayer.create(applicationContext, R.raw.osmium)
            "plusii" -> MediaPlayer.create(applicationContext, R.raw.plusii)
            else -> MediaPlayer.create(
                applicationContext,
                Uri.parse(applicationContext.dataDir.path + "/sounds/" + soundName)
            )
        }

        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (!mediaPlayer.isPlaying) {
            val notificationIntent = Intent(this, AlarmClockActivity::class.java)
            notificationIntent.putExtra("soundName", intent?.getStringExtra("soundName"))

            if (intent?.hasExtra("oneTime")!!) {
                val dbHelper = DataBaseHelper(applicationContext)
                val db = dbHelper.writableDatabase
                val value = ContentValues()
                value.put(TableInfo.TABLE_COLUMN_IS_ACTIVE, "false")
                Log.d("id", intent.getStringExtra("oneTime")!!)
                db.update(
                    TableInfo.TABLE_NAME,
                    value,
                    BaseColumns._ID + "=?",
                    arrayOf(intent.getStringExtra("oneTime"))
                )
            }

            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            val notification = NotificationCompat.Builder(this, App.CHANNEL_ID)
                .setContentTitle("Alarm!")
                .setSmallIcon(R.drawable.ic_baseline_access_alarm_24)
                .setContentIntent(pendingIntent)
                .build()
            mediaPlayer.start()
            mediaPlayer.isLooping = true
            vibrate(vibrator)
            if (permissionGranded) {
                scope.launch {
                    while (true) {
                        try {
                            cam.setTorchMode(cam.cameraIdList[0], true)
                            delay(500)
                            cam.setTorchMode(cam.cameraIdList[0], false)
                            delay(500)
                        } catch (e: IllegalArgumentException) {
                            Log.d("torchException", e.message!!)
                        }
                    }
                }
            }
            startForeground(1, notification)
        }
        return START_STICKY
    }

    override fun onDestroy() {
        mediaPlayer.stop()
        vibrator.cancel()
        if (permissionGranded) {
            scope.cancel()
            try {
                cam.setTorchMode(cam.cameraIdList[0], false)
            } catch (e: IllegalArgumentException) {
                Log.d("torchException", e.message!!)
            }
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun vibrate(v: Vibrator) {
            v.vibrate(VibrationEffect.createWaveform(longArrayOf(200, 300, 400, 500), 0))
    }
}