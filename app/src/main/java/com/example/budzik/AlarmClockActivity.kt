package com.example.budzik

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.alarm_clock_activity.*
import java.util.*

class AlarmClockActivity: AppCompatActivity(), SensorEventListener {
    private lateinit var mySensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private lateinit var gyroscope: Sensor
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alarm_clock_activity)

        mySensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        if(mySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) !=null){
            accelerometer = mySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        }
        if(mySensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null){
            gyroscope = mySensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        }



        off_BT.setOnClickListener {
            stopService(Intent(this, MyService::class.java))
            Log.d("stopBT","stopBT")
        }
        nap_BT.setOnClickListener {
            nap()
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

    override fun onBackPressed() {
        startActivity(Intent(applicationContext,MainActivity::class.java))
        super.onBackPressed()
    }
    fun nap(){
        stopService(Intent(this, MyService::class.java))
        val alarmManager = AlarmM()
        val c = Calendar.getInstance()
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND,0)
        c.add(Calendar.MINUTE,1)
        alarmManager.setAlarm(c, applicationContext)
        Toast.makeText(applicationContext,"Ustawiono drzemkę",Toast.LENGTH_SHORT).show()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onSensorChanged(event: SensorEvent?) {
        when(event?.sensor?.type){
            Sensor.TYPE_ACCELEROMETER -> {
                if(Math.abs(event.values[0])<0.5 && Math.abs(event.values[1])<0.5 &&
                    event.values[2]<-9.5){
                    Log.d("xyz","${event.values[0]}, ${event.values[1]}, ${event.values[2]}")
                    nap()
                }
            }
            Sensor.TYPE_GYROSCOPE -> {
                if(Math.abs(event.values[0])>1 || Math.abs(event.values[1])>1 || Math.abs(event.values[2])>1){
                    Log.d("gyroX","${event.values[0]}")
                    Log.d("gyroY","${event.values[1]}")
                    Log.d("gyroZ","${event.values[2]}")
                }
            }
        }

    }

    override fun onStart() {
        super.onStart()
        mySensorManager.registerListener(this, accelerometer, 10000)
        mySensorManager.registerListener(this, gyroscope, 10000)
    }

    override fun onStop() {
        super.onStop()
        mySensorManager.unregisterListener(this)
    }
}