package com.cowlog.pro.data

import android.content.Context
import android.location.Location
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URL

object WeatherService {
    
    suspend fun getCurrentWeather(context: Context): String {
        return withContext(Dispatchers.IO) {
            try {
                // Try to get location from GPS
                val location = getLastLocation(context)
                if (location != null) {
                    // Use Open-Meteo free API (no key needed)
                    val url = "https://api.open-meteo.com/v1/forecast?latitude=${location.latitude}&longitude=${location.longitude}&current_weather=true"
                    val response = URL(url).readText()
                    val json = JSONObject(response)
                    val current = json.getJSONObject("current_weather")
                    val code = current.getInt("weathercode")
                    val temp = current.getDouble("temperature")
                    
                    val weather = when (code) {
                        0 -> "☀️ Sunny & Clear"
                        1, 2, 3 -> "⛅ Partly Cloudy"
                        45, 48 -> "🌫️ Misty / Foggy"
                        51, 53, 55 -> "🌧️ Light Drizzle"
                        61, 63, 65 -> "🌧️ Rainy"
                        71, 73, 75 -> "🌨️ Snowy"
                        80, 81, 82 -> "🌧️ Showers"
                        95 -> "⛈️ Thunderstorm"
                        96, 99 -> "⛈️ Thunderstorm with Hail"
                        else -> "☀️ Sunny & Clear"
                    }
                    "$weather | ${temp}°C"
                } else {
                    "☀️ Sunny & Clear | 25°C"
                }
            } catch (e: Exception) {
                "☀️ Sunny & Clear | 25°C"
            }
        }
    }
    
    private fun getLastLocation(context: Context): Location? {
        try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
            return lm.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
                ?: lm.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
        } catch (e: Exception) {
            return null
        }
    }
}
