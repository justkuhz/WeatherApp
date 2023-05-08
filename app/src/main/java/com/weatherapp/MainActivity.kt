// Weather Phone Application written in Kotlin
// By Ken Zhu

package com.weatherapp

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.textfield.TextInputEditText
import com.squareup.picasso.Picasso
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.Locale

// Suppressing deprecation for Picasso library
@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private lateinit var homeRL : RelativeLayout
    private lateinit var loadingPB: ProgressBar
    private lateinit var cityNameTV: TextView
    private lateinit var temperatureTV: TextView
    private lateinit var conditionTV: TextView
    private lateinit var weatherRV: RecyclerView
    private lateinit var cityEdt: TextInputEditText
    private lateinit var backIV: ImageView
    private lateinit var iconIV: ImageView
    private lateinit var searchIV: ImageView
    private lateinit var todayTV : TextView
    private lateinit var weatherRVModalArrayList : ArrayList<WeatherRVModal>
    private lateinit var weatherRVAdapter : WeatherRVAdapter

    // API Key for weather API
    // https://www.weatherapi.com/
    private val key = "590dc4ce1cf749d6986202249232404"

    // This was originally for location permission
    private var PERMISSION_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // initializing variables
        setContentView(R.layout.activity_main)
        homeRL = findViewById(R.id.idRLHome)
        loadingPB = findViewById(R.id.idPBLoading)
        cityNameTV = findViewById(R.id.idTVCityName)
        temperatureTV = findViewById(R.id.idTVTemperature)
        conditionTV = findViewById(R.id.idTVCondition)
        weatherRV = findViewById(R.id.idRVWeather)
        cityEdt = findViewById(R.id.idEdtCity)
        backIV = findViewById(R.id.idIVBack)
        iconIV = findViewById(R.id.idIVIcon)
        searchIV = findViewById(R.id.idIVSearch)
        todayTV = findViewById(R.id.idTVtodayhour)
        weatherRVModalArrayList = ArrayList()
        weatherRVAdapter = WeatherRVAdapter(this, weatherRVModalArrayList)
        weatherRV.adapter = weatherRVAdapter


        // Check user location permissions for this app (no longer needed or used)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // if permissions not granted, request them
            ActivityCompat.requestPermissions(this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), PERMISSION_CODE)
        }

        // add on click listener to search button
        searchIV.setOnClickListener {
            var city = cityEdt.text.toString()
            if (city.isEmpty()) {
                Toast.makeText(this, "Please enter a city name", Toast.LENGTH_SHORT).show()
            } else {
                getWeatherInfo(city)
            }
        }

    }

    // Request permission from user for locations to make sure we can use the app (no longer needed or used)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissions granted...", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please approve of the permissions", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    // Requests data from API and display through XML
    private fun getWeatherInfo (cityName : String) {
        var url : String =
            "http://api.weatherapi.com/v1/forecast.json?key=$key&q=$cityName&days=1&aqi=no&alerts=no"

        // Use Volley Library to process each request made to weather API
        var requestQueue = Volley.newRequestQueue(this)
        var jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                loadingPB.visibility = View.GONE
                homeRL.visibility = View.VISIBLE
                weatherRVModalArrayList.clear()

                try {
                    // Location text views
                    var city = response.getJSONObject("location").getString("name")
                    var region = response.getJSONObject("location").getString("region")
                    var country = response.getJSONObject("location").getString("country")
                    cityNameTV.text = "$city, $region\n$country"

                    // Temperate text views
                    var temperature = response.getJSONObject("current").getString("temp_f")
                    temperatureTV.text = "$temperature°F"
                    temperatureTV.visibility = View.VISIBLE

                    // Weather text views
                    var condition = response.getJSONObject("current").getJSONObject("condition").getString("text")
                    var conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon")
                    conditionTV.text = "$condition"
                    conditionTV.visibility = View.VISIBLE
                    Picasso.get().load("http:$conditionIcon").into(iconIV)
                    iconIV.visibility = View.VISIBLE

                    // Set background depending on time at location
                    var day = response.getJSONObject("current").getInt("is_day")
                    if(day == 1) {
                        // daytime
                        Picasso.get().load(R.drawable.daytime).into(backIV)
                    } else {
                        // nighttime
                        Picasso.get().load(R.drawable.nighttime).into(backIV)
                    }

                    // Take weather information and break it into hours. Stored into the array 'hourArray'
                    var forecastObj = response.getJSONObject("forecast")
                    var forecast0 = forecastObj.getJSONArray("forecastday").getJSONObject(0)
                    var hourArray = forecast0.getJSONArray("hour")

                    // extracting data from each hour of the day for the weather in a city (24-hours)
                    for (i in 0 .. hourArray.length()) {
                        var hourObj = hourArray.getJSONObject(i)
                        var time = hourObj.getString("time")
                        var temp = hourObj.getString("temp_f")
                        var img = hourObj.getJSONObject("condition").getString("icon")
                        var wind = hourObj.getString("wind_mph")

                        // create a Modal for the information for the hour
                        weatherRVModalArrayList.add(WeatherRVModal(time, temp, img, wind))
                    }

                    // notify adapter of change in data to update
                    weatherRVAdapter.notifyDataSetChanged()
                    weatherRV.visibility = View.VISIBLE
                    todayTV.visibility = View.VISIBLE

                } catch (e : JSONException) {
                    e.printStackTrace()
                }
            },
            { error ->
                Toast.makeText(this, "Please enter a valid city name...", Toast.LENGTH_SHORT).show()
                todayTV.visibility = View.INVISIBLE
            }
        )
        requestQueue.add(jsonObjectRequest)
    }
}