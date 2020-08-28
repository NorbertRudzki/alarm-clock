package com.example.budzik

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

/**
 * Klasa odpowiedzialna za stworzenie kanału dla powiadomienia
 */
class App : Application() {
    companion object {
        const val CHANNEL_ID = "exampleService"
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID, "ExampleServiceChannel",
                NotificationManager.IMPORTANCE_HIGH
            )

            val mng = getSystemService(NotificationManager::class.java)
            mng?.createNotificationChannel(serviceChannel)
        }
    }
}