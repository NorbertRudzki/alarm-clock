package com.example.budzik

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.alarm_clock_activity.*
import java.util.*

class AlarmClockActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alarm_clock_activity)

        off_BT.setOnClickListener {
            stopService(Intent(this, MyService::class.java))
            Log.d("stopBT","stopBT")
        }
        nap_BT.setOnClickListener {
            stopService(Intent(this, MyService::class.java))
            val alarmManager = AlarmM()
            val c = Calendar.getInstance()
            c.add(Calendar.MINUTE,1)
            alarmManager.setAlarm(c, applicationContext)
        }
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                MyService.mediaPlayer.setVolume((progress/10.0).toFloat(),
                    (progress/10.0).toFloat()
                )
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        }

        )
    }
}