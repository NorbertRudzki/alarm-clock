package com.example.budzik

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.provider.BaseColumns
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.alarm_set.*
import java.util.*
import java.util.jar.Manifest

class SetAlarm: AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {
    /////////////////////////////

    private val REQUEST_PERMISSION_CAMERA = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alarm_set)
        val dbHelper = DataBaseHelper(applicationContext)
        val db = dbHelper.writableDatabase
        chooseTime()
        showCameraPermission()

//PRZYCISK OK
        ok_button.setOnClickListener {

            val intentMainActivity = Intent(applicationContext,MainActivity::class.java)
            savetoDB(db)
            startActivity(intentMainActivity)
        }
        powtarzaj_BT.setOnClickListener {
          if(switche.visibility == View.GONE)  switche.visibility = View.VISIBLE
            else switche.visibility = View.GONE
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
        if(intent.hasExtra("time")) time_text.setText(intent.getStringExtra("time"))
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

    fun savetoDB(db:SQLiteDatabase){

        val timedb = time_text.text.toString()
        val value = ContentValues()
        if(intent.hasExtra("is_active")) value.put(TableInfo.TABLE_COLUMN_IS_ACTIVE,intent.getStringExtra("is_active"))
        value.put(TableInfo.TABLE_COLUMN_TIME, timedb)
        // TODO repeating i soundname
        value.put(TableInfo.TABLE_COLUMN_REPEATING_DAYS,getRepeatingDays())
        value.put(TableInfo.TABLE_COLUMN_ID_SOUND,"id_sound")
        value.put(TableInfo.TABLE_COLUMN_IS_ACTIVE,"false")
        if (intent.hasExtra("ID")){
            db.update(TableInfo.TABLE_NAME, value, BaseColumns._ID +"=?", arrayOf(intent.getStringExtra("ID")))
            Toast.makeText(applicationContext, "Zapisano poprawkę", Toast.LENGTH_SHORT).show()
        }
        else {
            db.insertOrThrow(TableInfo.TABLE_NAME, null, value)
            Toast.makeText(applicationContext, "Zapisano", Toast.LENGTH_SHORT).show()
        }
      //  if(ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
      //      Log.d("cameraPermission","false")
      //     // ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA),20)
      //      requestPermissions(arrayOf(android.Manifest.permission.CAMERA),20)
      //  }
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
            REQUEST_PERMISSION_CAMERA -> if(grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(applicationContext, "Permission Granted!", Toast.LENGTH_SHORT).show()
            } else Toast.makeText(applicationContext, "Permission DENIED!", Toast.LENGTH_SHORT).show()
        }
    }


    fun showCameraPermission(){
        val permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
        if(permissionCheck != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA),REQUEST_PERMISSION_CAMERA)
        }
    }
}