package com.example.budzik

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.media.MediaRecorder
import android.os.Bundle
import android.provider.BaseColumns
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.alarm_set.*
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


class SetAlarm: AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {
    /////////////////////////////

    private val REQUEST_PERMISSION_CAMERA = 1
    private val REQUEST_PERMISSION_RECORD_AUDIO = 2
    private lateinit var directory: File
    private var mediaRecorder: MediaRecorder? = null
    private lateinit var model: SoundViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alarm_set)
        val dbHelper = DataBaseHelper(applicationContext)
        val db = dbHelper.writableDatabase
        soundsRecycler.layoutManager = LinearLayoutManager(applicationContext)
        chooseTime()
        showCameraPermission()
        var isRecording = false
        model = ViewModelProvider(this).get(SoundViewModel::class.java)
        model.getString().observe(this, androidx.lifecycle.Observer { t -> sound_name.text = t })
//PRZYCISK OK
        ok_button.setOnClickListener {

            val intentMainActivity = Intent(applicationContext,MainActivity::class.java)
            savetoDB(db)
            startActivity(intentMainActivity)
        }
        all_switch.setOnClickListener {
            if(all_switch.isChecked){

                pn_switch.isChecked = true
                wt_switch.isChecked = true
                sr_switch.isChecked = true
                czw_switch.isChecked = true
                pt_switch.isChecked = true
                sb_switch.isChecked = true
                nd_switch.isChecked = true
            }
        }
        powtarzaj_BT.setOnClickListener {
            sounds.visibility = View.GONE
            dzwiek_BT.setBackgroundColor(Color.parseColor("#E0F7FA"))
          if(switche.visibility == View.GONE){
              switche.visibility = View.VISIBLE
              powtarzaj_BT.setBackgroundColor(Color.parseColor("#4DD0E1"))
          }
            else{
              switche.visibility = View.GONE
              powtarzaj_BT.setBackgroundColor(Color.parseColor("#E0F7FA"))
          }
        }

        dzwiek_BT.setOnClickListener {
            switche.visibility = View.GONE
            powtarzaj_BT.setBackgroundColor(Color.parseColor("#E0F7FA"))
            if(sounds.visibility == View.GONE){
                sounds.visibility = View.VISIBLE
                dzwiek_BT.setBackgroundColor(Color.parseColor("#4DD0E1"))
            }
            else{
                sounds.visibility = View.GONE
                dzwiek_BT.setBackgroundColor(Color.parseColor("#E0F7FA"))
            }
            refreshSoundsList()

        }
        var fileName = ""
        recordBT.setOnClickListener {
            showRecordAudioPermission()
            if(ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                fileName = recordNameET.text.toString()
                when {
                    fileName.equals("") -> {
                        recordNameET.visibility = View.VISIBLE
                    }
                    isRecording == false -> {
                        //zaczynamy nagrywanie
                        recordNameET.visibility = View.GONE
                        isRecording = true
                        recordBT.setBackgroundColor(Color.parseColor("#C62828"))
                        startRecording(fileName)
                    }
                    else -> {
                        isRecording = false
                        recordBT.setBackgroundColor(Color.parseColor("#F48FB1"))
                        stopRecording()
                        fileName =""
                    }
                }
            }
        }


        if(intent.hasExtra("time")) time_text.text = intent.getStringExtra("time")
        if(intent.hasExtra("sound_name")) sound_name.text = intent.getStringExtra("sound_name")
        else sound_name.text = "buzzer"
        if(intent.hasExtra("days")) {
            intent.getStringExtra("days")?.toCharArray()?.forEach {
                when(it){
                    '1'-> nd_switch.isChecked = true
                    '2'-> pn_switch.isChecked = true
                    '3'-> wt_switch.isChecked = true
                    '4'-> sr_switch.isChecked = true
                    '5'-> czw_switch.isChecked = true
                    '6'-> pt_switch.isChecked = true
                    '7'-> sb_switch.isChecked = true
                }
            }
        }

    }

    @SuppressLint("SetTextI18n")
    fun chooseTime(){
        time_text.setOnClickListener {
            val calendar = Calendar.getInstance()
            val timePickerDialog = TimePickerDialog(this, TimePickerDialog.OnTimeSetListener
            { view, hourOfDay, minute ->
                if (minute<10) {
                    time_text.text = hourOfDay.toString()+":0"+minute.toString()
                }
                else {
                    time_text.text = hourOfDay.toString()+":"+minute.toString()
                }
            },calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE),android.text.format.DateFormat.is24HourFormat(applicationContext))
            timePickerDialog.show()
        }
    }

    private fun savetoDB(db:SQLiteDatabase){

        val timedb = time_text.text.toString()
        val soundName = sound_name.text.toString()
        val value = ContentValues()
        if(intent.hasExtra("is_active")) value.put(TableInfo.TABLE_COLUMN_IS_ACTIVE,intent.getStringExtra("is_active"))
        value.put(TableInfo.TABLE_COLUMN_TIME, timedb)
        value.put(TableInfo.TABLE_COLUMN_REPEATING_DAYS, getRepeatingDays())
        value.put(TableInfo.TABLE_COLUMN_ID_SOUND, soundName)
        value.put(TableInfo.TABLE_COLUMN_IS_ACTIVE, "false")
        if (intent.hasExtra("ID")){
            db.update(TableInfo.TABLE_NAME, value, BaseColumns._ID +"=?", arrayOf(intent.getStringExtra("ID")))
            Toast.makeText(applicationContext, "Zapisano poprawkę", Toast.LENGTH_SHORT).show()
        }
        else {
            db.insertOrThrow(TableInfo.TABLE_NAME, null, value)
            Toast.makeText(applicationContext, "Zapisano", Toast.LENGTH_SHORT).show()
        }

    }
    fun getRepeatingDays() :String{
        var pattern = ""

        if(nd_switch.isChecked) pattern+="${Calendar.SUNDAY}"
        if(pn_switch.isChecked) pattern+="${Calendar.MONDAY}"
        if(wt_switch.isChecked) pattern+="${Calendar.TUESDAY}"
        if(sr_switch.isChecked) pattern+="${Calendar.WEDNESDAY}"
        if(czw_switch.isChecked) pattern+="${Calendar.THURSDAY}"
        if(pt_switch.isChecked) pattern+="${Calendar.FRIDAY}"
        if(sb_switch.isChecked) pattern+="${Calendar.SATURDAY}"


        Log.d("pattern",pattern)
        return pattern
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            REQUEST_PERMISSION_CAMERA -> if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(applicationContext, "Permission Granted!", Toast.LENGTH_SHORT).show()
            } else Toast.makeText(applicationContext, "Permission DENIED!", Toast.LENGTH_SHORT).show()
            REQUEST_PERMISSION_RECORD_AUDIO -> if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(applicationContext, "Permission Granted!", Toast.LENGTH_SHORT).show()
            } else Toast.makeText(applicationContext, "Permission DENIED!", Toast.LENGTH_SHORT).show()
        }
    }


    private fun showCameraPermission(){
        val permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
        if(permissionCheck != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA),REQUEST_PERMISSION_CAMERA)
        }
    }
    private fun showRecordAudioPermission(){
        val permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
        if(permissionCheck != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.RECORD_AUDIO),REQUEST_PERMISSION_RECORD_AUDIO)
        }
    }
    private fun startRecording(filneName: String = "default"){
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(File(directory,filneName))
            try {
                prepare()
            } catch (e: IOException) {
                Log.e("LOG_TAG", "prepare() failed")
            }
            start()
        }
    }
    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        refreshSoundsList()
        mediaRecorder = null
    }
    private fun refreshSoundsList(){
        directory = File(applicationContext.dataDir.path+"/sounds")
        if(!directory.exists())directory.mkdir()
        val soundsList = ArrayList<String>()
        arrayOf("buzzer","forstarr","guitar","osmium","plusii").forEach { soundsList.add(it) }
        directory.list()?.forEach { soundsList.add(it) }
        soundsRecycler.adapter = SoundNameAdapter(soundsList, model)
    }
}