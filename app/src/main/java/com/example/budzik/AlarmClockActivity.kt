package com.example.budzik

import android.content.Context
import android.content.Intent
import android.graphics.Color
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
import kotlin.math.abs
import kotlin.math.absoluteValue

class AlarmClockActivity: AppCompatActivity(), SensorEventListener {
    private lateinit var mySensorManager: SensorManager

    private lateinit var accelerometer: Sensor
    private lateinit var gyroscope: Sensor
    private lateinit var lightSensor: Sensor
    private lateinit var magnetometer: Sensor

    private var isNapAlreadyPlayed = false
    private var gyroscopeList= arrayListOf<Float>()
    private lateinit var soundName:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alarm_clock_activity)

        soundName= intent?.getStringExtra("soundName").toString()
        mySensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        if(mySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) !=null) {
            accelerometer = mySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        }
        if(mySensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
            gyroscope = mySensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        }
        if(mySensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null) {
            lightSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        }
        if(mySensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
            magnetometer = mySensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        }

        off_BT.setOnClickListener {
            stopService(Intent(this, AlarmClockService::class.java))
            Log.d("stopBT","stopBT")
        }

        nap_BT.setOnClickListener {
            nap()
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                AlarmClockService.mediaPlayer.setVolume( (progress / 20.0).toFloat(),
                    (progress/20.0).toFloat()
                )
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        }
        )
    }

    override fun onBackPressed() {
        startActivity(Intent(applicationContext,MainActivity::class.java))
        super.onBackPressed()
    }

    private fun nap() {
        stopService(Intent(this, AlarmClockService::class.java))
        val alarmManager = AlarmClockManager()
        val calendar = Calendar.getInstance()
        calendar.timeZone = MainActivity.timeZone
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.add(Calendar.MINUTE, 1)
        alarmManager.setAlarm(calendar, applicationContext, soundName)
        Toast.makeText(applicationContext, "Ustawiono drzemkę", Toast.LENGTH_SHORT).show()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent?) {
        when(event?.sensor?.type){
            Sensor.TYPE_ACCELEROMETER -> {

                if(abs(event.values[0]) <0.5 && abs(event.values[1]) <0.5 &&
                    event.values[2]<-9.5){
                    Log.d("xyz","${event.values[0]}, ${event.values[1]}, ${event.values[2]}")
                    if(!isNapAlreadyPlayed){
                        nap()
                        isNapAlreadyPlayed = true
                    }
                }
            }
            Sensor.TYPE_GYROSCOPE -> {
                if(abs(event.values[2]) >3){
                    gyroscopeList.add(event.values[2].absoluteValue)
                    Log.d("gyroZ","${event.values[2]}")
                    Log.d("gyros", gyroscopeList.size.toString())
                    Log.d("gyros_2", (gyroscopeList.sum()).toString())
                    if (gyroscopeList.size==5){
                        if (gyroscopeList.sum()/gyroscopeList.size>7){
                            stopService(Intent(this, AlarmClockService::class.java))
                            Log.d("gyros_stop","stop")
                        }
                        gyroscopeList.clear()
                    }
                }
            }
            Sensor.TYPE_LIGHT->{
                if (event.values[0]>1000) event.values[0]=999F
                seekBar.progress = 50/((event.values[0]) * (50-1) / (1000-0) + 1).toInt()
                lightval.text = event.values[0].toString()
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                Log.d("magneticX",event.values[0].toString())
                Log.d("magneticY",event.values[1].toString())
                Log.d("magneticZ",event.values[2].toString())
                alarmClockLL.setBackgroundColor(Color.argb(255,
                    ((event.values[0]+60)*2.125).toInt(),
                    ((event.values[1]+60)*2.125).toInt(),
                    ((event.values[2]+60)*2.125).toInt()))
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mySensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        if(mySensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null){
            mySensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL)
        }
        mySensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL)
        mySensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onStop() {
        super.onStop()
        mySensorManager.unregisterListener(this)
    }
}