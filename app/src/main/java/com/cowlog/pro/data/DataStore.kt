package com.cowlog.pro.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object DataStore {
    private const val PREFS_NAME = "cowlog_data"
    private const val KEY_APP_DATA = "app_data"
    private val gson = Gson()

    fun save(context: Context, appData: AppData) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = gson.toJson(appData)
        prefs.edit().putString(KEY_APP_DATA, json).apply()
    }

    fun load(context: Context): AppData {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_APP_DATA, null)
        return if (json != null) {
            try { gson.fromJson(json, AppData::class.java) } 
            catch (e: Exception) { AppData() }
        } else AppData()
    }
}
