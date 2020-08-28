package com.example.budzik

/** Klasa reprezentuje pojedynczy wiersz w bazie danych z zapisanymi budziakmi */
class AlarmClockTableRow {
    var id: Int = -1
    var time: String = ""
    var days: String = ""
    var soundName = "NOTNULL"
    var isActive = "true"
}