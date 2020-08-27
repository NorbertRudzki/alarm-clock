package com.example.budzik

data class JSONTimeZone(
    val abbreviation: String,
    val countryCode: String,
    val countryName: String,
    val dst: String,
    val formatted: String,
    val gmtOffset: Int,
    val message: String,
    val nextAbbreviation: String,
    val status: String,
    val timestamp: Int,
    val zoneEnd: Int,
    val zoneName: String,
    val zoneStart: Int
)