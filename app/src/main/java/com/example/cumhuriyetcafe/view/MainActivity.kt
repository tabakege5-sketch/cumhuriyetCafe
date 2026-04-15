package com.example.cumhuriyetcafe.view

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.cumhuriyetcafe.R
import com.example.cumhuriyetcafe.databinding.ActivityMainBinding
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPref = getSharedPreferences("UygulamaAyarlari", Context.MODE_PRIVATE)
        ayarlariYukle(sharedPref)
        createNotificationChannel()
        if (baslangicZamani == 0L) {
            baslangicZamani = System.currentTimeMillis()
        }
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController
        val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)
        if (savedInstanceState == null && isLoggedIn) {
            val navGraph = navController.navInflater.inflate(R.navigation.naw_graph)
            navGraph.setStartDestination(R.id.masalarFragment)
            navController.graph = navGraph
        }
        binding.bottomNavigation.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNavigation.visibility =
                if (destination.id == R.id.girisFragment) View.GONE else View.VISIBLE
        }
    }

    private fun ayarlariYukle(sharedPref: android.content.SharedPreferences) {
        val kaydedilenDil = sharedPref.getString("seciliDil", "tr") ?: "tr"
        val locale = Locale(kaydedilenDil)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
        val isDarkMode = sharedPref.getBoolean("isDarkMode", false)
        AppCompatDelegate.setDefaultNightMode(if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Cafe Bildirimleri"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("CAFE_NOTIF_CHANNEL", name, importance).apply {
                description = "Sipariş ve Rapor güncellemeleri"
            }
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        @JvmField
        var baslangicZamani: Long = 0
    }
}