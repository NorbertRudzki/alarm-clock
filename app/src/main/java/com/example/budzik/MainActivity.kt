package com.example.budzik

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks {
    companion object {
        var timeZone: TimeZone = TimeZone.getDefault()
    }

    private lateinit var locationRequest: LocationRequest
    private lateinit var fusedlocation: FusedLocationProviderClient
    private lateinit var googleApiClient: GoogleApiClient
    private lateinit var gson: Gson
    private var REQUEST_PERMISSION_GPS = 3
    private val db_data = ArrayList<AlarmClockTableRow>()
    private var longitude: String = ""
    private var latitude: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        locationRequest = LocationRequest()
        fusedlocation = FusedLocationProviderClient(applicationContext)
        locationRequest.interval = 30000
        locationRequest.fastestInterval = 50000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        googleApiClient = GoogleApiClient.Builder(this)
            .addApi(LocationServices.API)
            .addConnectionCallbacks(this)
            .build()

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(applicationContext, "Proszę włączyć GPS", Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationManager.addNmeaListener { message, _ ->
                Log.d("nmea", message)
            }
        }

        val dbHelper = DataBaseHelper(applicationContext)
        val db = dbHelper.writableDatabase

        val cursor = db.query(
            TableInfo.TABLE_NAME, null, null,
            null, null, null, null
        )

        alarm_info.visibility = View.GONE

        if (cursor.count > 0) {
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                val fromDb = AlarmClockTableRow()
                fromDb.id = cursor.getInt(0)
                fromDb.time = cursor.getString(1)
                fromDb.days = cursor.getString(2)
                fromDb.soundName = cursor.getString(3)
                fromDb.isActive = cursor.getString(4)
                db_data.add(fromDb)
                cursor.moveToNext()
            }

        } else {
            alarm_info.visibility = View.VISIBLE
        }

        cursor.close()
        recycle_view.layoutManager = LinearLayoutManager(applicationContext)
        recycle_view.adapter = MyAdapter(applicationContext, db, db_data)

    }

    override fun onStart() {
        super.onStart()
        googleApiClient.connect()
    }

    override fun onStop() {
        super.onStop()
        googleApiClient.disconnect()
    }

    override fun onResume() {
        super.onResume()
        if (db_data.size == 0) alarm_info.visibility = View.VISIBLE
        else alarm_info.visibility = View.GONE
    }

    fun intentToSetAlarm(v: View) {
        val intent = Intent(applicationContext, SetAlarmActivity::class.java)
        startActivity(intent)
    }

    override fun onBackPressed() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_PERMISSION_GPS
            && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(applicationContext, "Przyznano uprawnienia GPS", Toast.LENGTH_SHORT)
                .show()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun updateValue(location: Location) {
        latitude = location.latitude.toString()
        longitude = location.longitude.toString()
        val job = CoroutineScope(Dispatchers.IO).launch {
            Log.d("JSON_HTTP", TimeZoneAPI().getJSONString(latitude, longitude))
            gson = Gson()
            val gson = Gson()
            val gsonTimeZone = gson.fromJson(
                TimeZoneAPI().getJSONString(latitude, longitude),
                JSONTimeZone::class.java
            )
            if (gsonTimeZone != null) {
                timeZone = TimeZone.getTimeZone(gsonTimeZone.zoneName)
            }
        }
        job.start()
        if (job.isCompleted) {
            job.cancel()
        }
    }

    override fun onConnected(p0: Bundle?) {
        fusedlocation = LocationServices.getFusedLocationProviderClient(applicationContext)
        if (ActivityCompat.checkSelfPermission(applicationContext, ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            fusedlocation.lastLocation.addOnSuccessListener {
                if (it != null && it.accuracy < 100) {
                    updateValue(it)
                } else {
                    Log.d("gps", "null")
                    val mLocationCallBack = object : LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult?) {
                            if (locationResult != null
                                && locationResult.locations.isNotEmpty()
                                && locationResult.locations[0].accuracy < 100
                            ) {
                                val newLocation = locationResult.locations[0]
                                updateValue(newLocation)
                            }
                        }
                    }
                    fusedlocation.requestLocationUpdates(locationRequest, mLocationCallBack, null)
                }
            }
        } else {
            requestPermissions(arrayOf(ACCESS_FINE_LOCATION), REQUEST_PERMISSION_GPS)
        }
    }

    override fun onConnectionSuspended(p0: Int) {}
}


