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
import kotlin.math.abs
import kotlin.math.absoluteValue

class AlarmClockActivity: AppCompatActivity(), SensorEventListener {
    private lateinit var mySensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private lateinit var gyroscope: Sensor
    private lateinit var light_sensor: Sensor
    private var light_value:Float=0F
    private var gyroscope_list= arrayListOf<Float>()
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
        if(mySensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null){
            light_sensor = mySensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
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
                MyService.mediaPlayer.setVolume((progress/20.0).toFloat(),
                    (progress/20.0).toFloat()
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
    private fun nap(){
        stopService(Intent(this, MyService::class.java))
        val alarmManager = AlarmM()
        val c = Calendar.getInstance()
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND,0)
        c.add(Calendar.MINUTE,1)
        alarmManager.setAlarm(c, applicationContext, "buzzer")
        Toast.makeText(applicationContext,"Ustawiono drzemkę",Toast.LENGTH_SHORT).show()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onSensorChanged(event: SensorEvent?) {
        when(event?.sensor?.type){
            Sensor.TYPE_ACCELEROMETER -> {

                if(abs(event.values[0]) <0.5 && abs(event.values[1]) <0.5 &&
                    event.values[2]<-9.5){
                    Log.d("xyz","${event.values[0]}, ${event.values[1]}, ${event.values[2]}")
                    nap()
                }
            }
            Sensor.TYPE_GYROSCOPE -> {
                if(abs(event.values[2]) >3){
                    gyroscope_list.add(event.values[2].absoluteValue)
                    Log.d("gyroZ","${event.values[2]}")
                    Log.d("gyros", gyroscope_list.size.toString())
                    Log.d("gyros_2", (gyroscope_list.sum()).toString())
                    if (gyroscope_list.size==5){
                        if (gyroscope_list.sum()/gyroscope_list.size>7){
                            stopService(Intent(this, MyService::class.java))
                            Log.d("gyros_stop","stop")
                        }
                        gyroscope_list.clear()
                    }
                }
            }
            Sensor.TYPE_LIGHT->{
                light_value=event.values[0]
                if (light_value>1000) light_value=999F
                seekBar.progress = 20/((light_value) * (20-1) / (1000-0) + 1).toInt()
               // lightval.text=seekBar.progress.toString()
                lightval.text = light_value.toString()
            }
        }

    }

    override fun onStart() {
        super.onStart()
        mySensorManager.registerListener(this, accelerometer, 10000)
        mySensorManager.registerListener(this, gyroscope, 500000)
        mySensorManager.registerListener(this, light_sensor, 500000)
    }

    override fun onStop() {
        super.onStop()
        mySensorManager.unregisterListener(this)
    }
}