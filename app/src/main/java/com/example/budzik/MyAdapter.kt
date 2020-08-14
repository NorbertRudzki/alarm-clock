package com.example.budzik

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.provider.BaseColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.layout_alarm.view.*
import java.util.*
import kotlin.collections.ArrayList

class MyAdapter(val context: Context,val db:SQLiteDatabase,val note:ArrayList<Table>):RecyclerView.Adapter<MyViewHolder>(){



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater= LayoutInflater.from(parent.context)
        val contact = layoutInflater.inflate(R.layout.layout_alarm,parent,false)
        return MyViewHolder(contact)
    }

    override fun getItemCount(): Int {
        val cursor = db.query(TableInfo.TABLE_NAME,null,null, null,null,null,null)
        val  liczbawierszy = cursor.count
        cursor.close()
        return liczbawierszy
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val timeView = holder.view.time_id
        val on_off = holder.view.switch1
        val context:Context = holder.view.context
        val alarm = AlarmM()
        timeView.setText(note[holder.adapterPosition].time)
        if(note[holder.adapterPosition].isActive.equals("true")) on_off.isChecked=true
       else if(note[holder.adapterPosition].isActive.equals("false")) on_off.isChecked=false

        if(on_off.isChecked) alarmOn(timeView,holder.adapterPosition.plus(1),alarm)
        else alarmOff(holder.adapterPosition.plus(1),alarm)
//edytowanie
        timeView.setOnClickListener {
            val repeating_days = note[holder.adapterPosition].days
            val sound_name = note[holder.adapterPosition].sound_name
            val intent = Intent(context,SetAlarm::class.java)
            val time = timeView.text
            val id_edit = holder.adapterPosition.plus(1).toString()
            intent.putExtra("time",time).toString()
            intent.putExtra("ID",id_edit).toString()
            intent.putExtra("days",repeating_days).toString()
            intent.putExtra("sound_name",sound_name).toString()
            intent.putExtra("is_active",note[holder.adapterPosition].isActive)
            context.startActivity(intent)}
//usuwanie
        timeView.setOnLongClickListener(object : View.OnLongClickListener{
            override fun onLongClick(v: View?): Boolean {
                on_off.isChecked = false
                alarmOff(holder.adapterPosition.plus(1),alarm)
                db.delete(TableInfo.TABLE_NAME,BaseColumns._ID+ "=?", arrayOf(note[holder.adapterPosition].id.toString()))
                note.removeAt(holder.adapterPosition)
                notifyItemRemoved(holder.adapterPosition)
                if(note.size==0){
                    val intent = Intent(context, MainActivity::class.java )
                    context.startActivity(intent)
                }

                return true
            }

        })
//on/off switche

            on_off.setOnClickListener {
                    if (on_off.isChecked) {
                        alarmOn(timeView,holder.adapterPosition.plus(1),alarm)
                    }
                    if (!on_off.isChecked) {
                        alarmOff(holder.adapterPosition.plus(1),alarm)
                    }
            }



    }

    fun cut(str:String): List<String> {
        val delim =":"
        val list = str.split(delim)
        return list
    }

    fun alarmOn(timeView: TextView, position: Int, alarm: AlarmM){
        val dbHelper = DataBaseHelper(context)
        val db = dbHelper.writableDatabase
        val value = ContentValues()
        value.put(TableInfo.TABLE_COLUMN_IS_ACTIVE,"true")
        val calendar: Calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, cut(timeView.text.toString())[0].toInt())
        calendar.set(Calendar.MINUTE, cut(timeView.text.toString())[1].toInt())
        calendar.set(Calendar.SECOND, 0)

        alarm.setAlarm(calendar,context)

        Toast.makeText(context, "ON ${cut(timeView.text.toString())[0]}:" +
                cut(timeView.text.toString())[1], Toast.LENGTH_SHORT).show()
        db.update(TableInfo.TABLE_NAME, value, BaseColumns._ID +"=?", arrayOf(position.toString()))
    }
    fun alarmOff(position: Int, alarm: AlarmM){
        val dbHelper = DataBaseHelper(context)
        val db = dbHelper.writableDatabase
        val value = ContentValues()
        value.put(TableInfo.TABLE_COLUMN_IS_ACTIVE,"false")
        alarm.cancelAlarm()
        Toast.makeText(context, "OFF", Toast.LENGTH_SHORT).show()
        db.update(TableInfo.TABLE_NAME, value, BaseColumns._ID +"=?", arrayOf(position.toString()))
    }
    }


class MyViewHolder(val view: View) : RecyclerView.ViewHolder(view)