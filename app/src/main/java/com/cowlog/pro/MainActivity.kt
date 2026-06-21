package com.cowlog.pro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cowlog.pro.data.*
import com.cowlog.pro.ui.*
import com.cowlog.pro.ui.screens.*
import com.cowlog.pro.ui.documents.*
import com.cowlog.pro.ui.screens.StopWorkScreen
import com.cowlog.pro.ui.screens.DelayNoticeScreen
import com.cowlog.pro.ui.screens.MaterialRejectionScreen
import com.cowlog.pro.ui.screens.MeetingScreen
import com.cowlog.pro.ui.screens.PlantScreen
import com.google.gson.Gson

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val prefs = getSharedPreferences("cowlog", MODE_PRIVATE)
        val gson = Gson()
        
        var settings by mutableStateOf(loadSettings(prefs, gson))
        var appData by mutableStateOf(loadAppData(prefs, gson))
        
        fun saveData() {
            prefs.edit()
                .putString("settings", gson.toJson(settings))
                .putString("appData", gson.toJson(appData))
                .apply()
        }
        
        setContent {
            CoWLogTheme {
                val navController = rememberNavController()
                
                NavHost(navController, startDestination = "diary") {
                    composable("diary") { DiaryScreen(appData, settings, navController) { appData = it; saveData() } }
                    composable("inspections") { InspectionScreen(appData, settings, navController) { appData = it; saveData() } }
                    composable("ncr") { NCRScreen(appData, settings, navController) { appData = it; saveData() } }
                    composable("si") { SIScreen(appData, settings, navController) { appData = it; saveData() } }
                    composable("rfi") { RFIScreen(appData, settings, navController) { appData = it; saveData() } }
                    composable("drawings") { DrawingScreen(appData, settings, navController, onUpdate = {}) }
                    composable("materials") { MaterialScreen(appData, settings, navController) { appData = it; saveData() } }
                    composable("concrete") { ConcreteScreen(appData, settings, navController) { appData = it; saveData() } }
                    composable("attendance") { AttendanceScreen(appData, settings, navController) { appData = it; saveData() } }
                    composable("delays") { DelayScreen(appData, settings, navController, onUpdate = {}) }
                    composable("report") { ReportScreen(appData, settings, navController, onUpdate = {}) }
                    composable("settings") { SettingsScreen(settings, navController) { settings = it; saveData() } }
                    composable("ncrDoc/{id}") { entry -> NCRDocument(appData, entry.arguments?.getString("id") ?: "", navController) }
                    composable("siDoc/{id}") { entry -> SIDocument(appData, entry.arguments?.getString("id") ?: "", navController) }
                    composable("rfiDoc/{id}") { entry -> RFIDocument(appData, entry.arguments?.getString("id") ?: "", navController) }
                    composable("stopwork") { StopWorkScreen(appData, settings, navController) { appData = it; saveData() } }
                    composable("swodoc/{id}") { entry -> StopWorkDocument(appData, entry.arguments?.getString("id") ?: "", navController) }
                    composable("delaynotices") { DelayNoticeScreen(appData, settings, navController) { appData = it; saveData() } }
                    composable("dndoc/{id}") { entry -> DelayNoticeDocument(appData, entry.arguments?.getString("id") ?: "", navController) }
                    composable("rejections") { MaterialRejectionScreen(appData, settings, navController) { appData = it; saveData() } }
                    composable("mrndoc/{id}") { entry -> MaterialRejectionDocument(appData, entry.arguments?.getString("id") ?: "", navController) }
                    composable("plant") { PlantScreen(appData, settings, navController) { appData = it; saveData() } }
                    composable("meetings") { MeetingScreen(appData, settings, navController) { appData = it; saveData() } }
                    composable("meetingdoc/{id}") { entry -> MeetingDocument(appData, entry.arguments?.getString("id") ?: "", navController) }
                }
            }
        }
    }
    
    private fun loadSettings(prefs: android.content.SharedPreferences, gson: Gson): ProjectSettings {
        val json = prefs.getString("settings", null) ?: return ProjectSettings()
        return try { gson.fromJson(json, ProjectSettings::class.java) } catch (e: Exception) { ProjectSettings() }
    }
    
    private fun loadAppData(prefs: android.content.SharedPreferences, gson: Gson): AppData {
        val json = prefs.getString("appData", null) ?: return AppData()
        return try { gson.fromJson(json, AppData::class.java) } catch (e: Exception) { AppData() }
    }
}
