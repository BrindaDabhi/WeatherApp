package com.example.weatherapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.KeyEvent
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private lateinit var city:String

    private lateinit var txtUpdatedAt: TextView
    private lateinit var txtStatus: TextView
    private lateinit var txtTemp: TextView
    private lateinit var txtTempMin: TextView
    private lateinit var txtTempMax: TextView
    private lateinit var txtPressure: TextView
    private lateinit var txtHumidity: TextView
    private lateinit var txtWind: TextView
    private lateinit var txtSunRise: TextView
    private lateinit var txtSunSet: TextView


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

            txtUpdatedAt = findViewById(R.id.updated_at)
            txtStatus = findViewById(R.id.status)
            txtTemp = findViewById(R.id.temp)
            txtTempMin = findViewById(R.id.temp_min)
            txtTempMax = findViewById(R.id.temp_max)
            txtPressure = findViewById(R.id.pressure)
            txtHumidity = findViewById(R.id.humidity)
            txtWind = findViewById(R.id.wind)
            txtSunRise = findViewById(R.id.sun_rise)
            txtSunSet = findViewById(R.id.sun_set)


        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),101)
            return
        }
        fusedLocationProviderClient.lastLocation.addOnSuccessListener {
            if(it!=null){

                val lat = it.latitude
                val lon = it.longitude

                val geoCoder = Geocoder(this, Locale.getDefault())
                val address: List<Address> = geoCoder.getFromLocation(lat, lon, 1)

                city = address[0].locality
                runFun()
            }
        }

        search_bar.setOnKeyListener { _, keyCode, event ->

            when {

                ((keyCode == KeyEvent.KEYCODE_ENTER) && (event.action == KeyEvent.ACTION_DOWN)) -> {

                    city = findViewById<EditText>(R.id.search_bar).text.toString().trim()

                    runFun()

                    return@setOnKeyListener true
                }
                else -> false
            }

        }

    }

    @SuppressLint("SimpleDateFormat")
    fun formatTIme(sun:Long):String{
        val timeformat = SimpleDateFormat("h:m a", Locale.ENGLISH)
        return  timeformat.format(sun)
    }

    @SuppressLint("SetTextI18n")
    private fun runFun() {

        val queue = Volley.newRequestQueue(this)
        val url = "https://api.openweathermap.org/data/2.5/weather?q=$city&appid=536caddf0ede7cc4450aa06308ed4bfa"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->

                txtUpdatedAt.text = response.getString("name")

                val weatherArray: JSONArray = response.getJSONArray("weather")
                val weatherObject: JSONObject = weatherArray.getJSONObject(0)
                txtStatus.text = weatherObject.getString("main")

                val tempObject: JSONObject = response.getJSONObject("main")
                txtTemp.text = "%.2f".format(tempObject.getDouble("temp")-273.15) + "\u00B0" + "C"
                txtTempMin.text = "Min temp " + "%.2f".format(tempObject.getDouble("temp_min")-273.15) + "\u00B0" + "C"
                txtTempMax.text = "Max temp " + "%.2f".format(tempObject.getDouble("temp_max")-273.15) + "\u00B0" + "C"
                txtPressure.text = tempObject.getString("pressure") + "hPa"
                txtHumidity.text = tempObject.getString("humidity") + "%"

                val windObject: JSONObject = response.getJSONObject("wind")
                txtWind.text = windObject.getString("speed") + "km/h"

                val sysObject: JSONObject = response.getJSONObject("sys")


                txtSunRise.text = formatTIme(sysObject.getLong("sunrise"))
                txtSunSet.text = formatTIme(sysObject.getLong("sunset"))

            },
            {
                Toast.makeText(this, "something went wrong", Toast.LENGTH_LONG).show()
            })

        queue.add(jsonObjectRequest)

    }

}