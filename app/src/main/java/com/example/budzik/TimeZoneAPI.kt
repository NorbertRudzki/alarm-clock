package com.example.budzik

import android.net.Uri
import android.util.Log
import org.json.JSONException
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/** Klasa odpowiedzialna za komunikację z API TimeZoneDB */
class TimeZoneAPI {

    /** Zwraca tablice bajtów z wynikiem zapytania */
    fun getUrlBytes(urlSpec: String): ByteArray {
        val url = URL(urlSpec)
        val connection = url.openConnection() as HttpsURLConnection
        try {
            val out = ByteArrayOutputStream()
            val input = connection.inputStream

            if (connection.responseCode != HttpsURLConnection.HTTP_OK) {
                throw IOException(connection.responseMessage)
            }
            var bytesRead: Int
            val buffer = ByteArray(2048)
            do {
                bytesRead = input.read(buffer)
                out.write(buffer, 0, bytesRead)
            } while (input.read(buffer) > 0)
            out.close()

            return out.toByteArray()
        } catch (e: IOException) {
            Log.d("error", e.message!!)
            return ByteArray(0)
        } finally {
            connection.disconnect()
        }
    }

    /** Tworzy łańcuch znaków z tablicy bajtów
     */
    fun getUrlString(urlSpec: String): String {
        return String(getUrlBytes(urlSpec))
    }

    /** Zwraca komunikat będący wynikiem zapytania do API TimeZoneDB z podaną lokalizacją*/
    fun getJSONString(latitude: String, longitude: String): String {
        var jsonString = "Zle"
        try {
            val url =
                Uri.parse("https://api.timezonedb.com/v2.1/get-time-zone?key=MF5EL8NS38TX&format=json&by=position&lat=$latitude&lng=$longitude")
                    .buildUpon()
                    .build().toString()

            jsonString = getUrlString(url)
        } catch (je: JSONException) {
            Log.d("JSOnError", je.message!!)
        }
        return jsonString
    }
}