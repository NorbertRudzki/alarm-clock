package com.example.budzik

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private val db_data = ArrayList<AlarmClockTableRow>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val dbHelper = DataBaseHelper(applicationContext)
        val db = dbHelper.writableDatabase

        //zczytywanie z bazy do Table
        val cursor = db.query(TableInfo.TABLE_NAME,null,null, null,null,null,null)

        alarm_info.visibility = View.GONE

        if (cursor.count>0){
            cursor.moveToFirst()
            while (!cursor.isAfterLast){
                val from_db = AlarmClockTableRow()
                from_db.id = cursor.getInt(0)
                from_db.time = cursor.getString(1)
                from_db.days = cursor.getString(2)
                from_db.sound_name = cursor.getString(3)
                from_db.isActive = cursor.getString(4)
                db_data.add(from_db)
                cursor.moveToNext()
            }

        }else{
            alarm_info.visibility= View.VISIBLE
        }

        cursor.close()
        recycle_view.layoutManager = LinearLayoutManager(applicationContext)
        recycle_view.adapter = MyAdapter(applicationContext,db,db_data)

    }

    override fun onResume() {
        super.onResume()
        if (db_data.size==0) alarm_info.visibility = View.VISIBLE
        else alarm_info.visibility = View.GONE


    }

    fun intentToSetAlarm(v: View){
        val intent = Intent(applicationContext,SetAlarm::class.java)
        startActivity(intent)
    }

    override fun onBackPressed() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
}
