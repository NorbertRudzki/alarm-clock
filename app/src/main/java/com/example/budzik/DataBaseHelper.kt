package com.example.budzik

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

/** Informacje o strukturze tabeli w bazie danych*/
object TableInfo : BaseColumns {
    const val TABLE_NAME = "ALARMS"
    const val TABLE_COLUMN_TIME = "TIME"
    const val TABLE_COLUMN_REPEATING_DAYS = "REPEATING_DAYS"
    const val TABLE_COLUMN_ID_SOUND = "ID_SOUND"
    const val TABLE_COLUMN_IS_ACTIVE = "IS_ACTIVE"
}
/** Podstawowe komendy wymagane do obsługi bazy danych*/
object BasicCommand {
    const val SQL_CREATE_TABLE =
        "CREATE TABLE ${TableInfo.TABLE_NAME} (" +
                "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                "${TableInfo.TABLE_COLUMN_TIME} TEXT NOT NULL," +
                "${TableInfo.TABLE_COLUMN_REPEATING_DAYS} TEXT NOT NULL," +
                "${TableInfo.TABLE_COLUMN_ID_SOUND} TEXT NOT NULL," +
                "${TableInfo.TABLE_COLUMN_IS_ACTIVE} TEXT NOT NULL)"

    const val SQL_DELETE_TABLE = "DROP TABLE IF EXISTS ${TableInfo.TABLE_NAME}"
}

/** Klasa odpowiedzialna za tworzenie i modyfikacje bazy danych*/
class DataBaseHelper(context: Context) : SQLiteOpenHelper(
    context,
    TableInfo.TABLE_NAME,
    null,
    1
) {
    /** Tworzenie bazy danych*/
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(BasicCommand.SQL_CREATE_TABLE)
    }
    /** Modyfikacja bazy danych*/
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL(BasicCommand.SQL_DELETE_TABLE)
        onCreate(db)
    }
}