package com.example.budzik

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.provider.BaseColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.alarm_row.view.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

/** Klasa odpowiedzialna za poprawną prezentację danych z bazy danych z wykorzystaniem RecyclerView
 * oraz obsługę zdarzeń z interfejsu graficznego
 */
class MainAdapter(
    private val context: Context,
    private val db: SQLiteDatabase,
    private val dataBaseList: ArrayList<AlarmClockTableRow>
) : RecyclerView.Adapter<MainAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val row = layoutInflater.inflate(R.layout.alarm_row, parent, false)
        return MyViewHolder(row)
    }

    override fun getItemCount(): Int {
        return dataBaseList.size
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val timeView = holder.view.time_id
        val onOff = holder.view.switch1
        val context: Context = holder.view.context
        val alarm = AlarmClockManager()
        timeView.text = dataBaseList[holder.adapterPosition].time
        if (dataBaseList[holder.adapterPosition].isActive == "true") {
            onOff.isChecked = true
        }
        else if (dataBaseList[holder.adapterPosition].isActive == "false") {
            onOff.isChecked = false
        }

        if (onOff.isChecked) {
            alarmOn(timeView, holder.adapterPosition.plus(1), alarm)
        } else {
            alarmOff(holder.adapterPosition.plus(1), alarm)
        }

        // edytowanie rekordu na liscie
        timeView.setOnClickListener {
            val repeatingDays = dataBaseList[holder.adapterPosition].days
            val soundName = dataBaseList[holder.adapterPosition].soundName
            val intent = Intent(context, SetAlarmActivity::class.java)
            val time = timeView.text
            val idEdit = holder.adapterPosition.plus(1).toString()
            intent.putExtra("time", time).toString()
            intent.putExtra("ID", idEdit).toString()
            intent.putExtra("days", repeatingDays).toString()
            intent.putExtra("sound_name", soundName).toString()
            intent.putExtra("is_active", dataBaseList[holder.adapterPosition].isActive)
            context.startActivity(intent)
        }

        // usuwanie rekordu z listy
        timeView.setOnLongClickListener {
            onOff.isChecked = false
            alarmOff(holder.adapterPosition.plus(1), alarm)
            db.delete(
                TableInfo.TABLE_NAME,
                BaseColumns._ID + "=?",
                arrayOf(dataBaseList[holder.adapterPosition].id.toString())
            )
            dataBaseList.removeAt(holder.adapterPosition)
            notifyItemRemoved(holder.adapterPosition)
            if (dataBaseList.size == 0) {
                val intent = Intent(context, MainActivity::class.java)
                context.startActivity(intent)
            }

            true
        }

        onOff.setOnClickListener {
            if (onOff.isChecked) {
                alarmOn(timeView, holder.adapterPosition.plus(1), alarm)
            }
            if (!onOff.isChecked) {
                alarmOff(holder.adapterPosition.plus(1), alarm)
            }
        }
    }

    private fun cut(str: String): List<String> = str.split(":")

    private fun alarmOn(timeView: TextView, position: Int, alarm: AlarmClockManager) {
        val dbHelper = DataBaseHelper(context)
        val db = dbHelper.writableDatabase
        val value = ContentValues()
        value.put(TableInfo.TABLE_COLUMN_IS_ACTIVE, "true")
        val now = Calendar.getInstance()

        var calendar: Calendar = Calendar.getInstance()
        calendar.timeZone = MainActivity.timeZone
        calendar.set(Calendar.HOUR_OF_DAY, cut(timeView.text.toString())[0].toInt())
        calendar.set(Calendar.MINUTE, cut(timeView.text.toString())[1].toInt())
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val days = dataBaseList[position.minus(1)].days

        if (days == "") {
            Log.d("now", now.timeInMillis.toString())
            Log.d("ustawione", calendar.timeInMillis.toString())
            if (calendar.timeInMillis < now.timeInMillis) calendar.add(Calendar.DAY_OF_YEAR, 1)
            alarm.setOneTimeAlarm(
                calendar,
                context,
                position.toString(),
                dataBaseList[position.minus(1)].soundName
            )
        } else {
            calendar = getNextAlarmCalendar(position - 1)
            alarm.setAlarm(calendar, context, dataBaseList[position.minus(1)].soundName)
        }

        val differenceInMinutes =
            TimeUnit.MILLISECONDS.toMinutes(calendar.timeInMillis - now.timeInMillis) + 1

        Toast.makeText(
            context,
            "Alarm włączy się za ${differenceInMinutes / 60} godzin i ${differenceInMinutes % 60} minut",
            Toast.LENGTH_LONG
        ).show()

        db.update(
            TableInfo.TABLE_NAME,
            value,
            BaseColumns._ID + "=?",
            arrayOf(position.toString())
        )
    }

    private fun alarmOff(position: Int, alarm: AlarmClockManager) {
        val dbHelper = DataBaseHelper(context)
        val db = dbHelper.writableDatabase
        val value = ContentValues()
        value.put(TableInfo.TABLE_COLUMN_IS_ACTIVE, "false")
        alarm.cancelAlarm()
        db.update(
            TableInfo.TABLE_NAME,
            value,
            BaseColumns._ID + "=?",
            arrayOf(position.toString())
        )
    }

    private fun getNextAlarmCalendar(position: Int): Calendar {
        val now = Calendar.getInstance()
        val pattern = dataBaseList[position].days
        val time = dataBaseList[position].time
        val days = arrayListOf<Calendar>()

        pattern.toCharArray().forEach {
            val c = Calendar.getInstance()
            c.set(Calendar.HOUR_OF_DAY, cut(time)[0].toInt())
            c.set(Calendar.MINUTE, cut(time)[1].toInt())
            c.set(Calendar.DAY_OF_WEEK, it.toInt().plus(1))
            c.set(Calendar.SECOND, 0)
            if (c.timeInMillis < now.timeInMillis) c.add(Calendar.DAY_OF_YEAR, 7)
            days.add(c)
        }
        return days.min()!!
    }

    inner class MyViewHolder(val view: View) : RecyclerView.ViewHolder(view)
}

