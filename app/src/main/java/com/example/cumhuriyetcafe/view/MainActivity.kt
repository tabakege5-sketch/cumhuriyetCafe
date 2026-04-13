package com.example.cumhuriyetcafe.view

import android.content.Context
import android.content.res.Configuration
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
        val kaydedilenDil = sharedPref.getString("seciliDil", "tr") ?: "tr"
        val locale = Locale(kaydedilenDil)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
        if (baslangicZamani == 0L) {
            baslangicZamani = System.currentTimeMillis()
        }
        val isDarkMode = sharedPref.getBoolean("isDarkMode", false)
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController
        val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)

        if (savedInstanceState == null) {
            if (isLoggedIn) {
                val navGraph = navController.navInflater.inflate(R.navigation.naw_graph)
                navGraph.setStartDestination(R.id.masalarFragment)
                navController.graph = navGraph
            }
        }

        val bottomNav = binding.bottomNavigation
        bottomNav.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.girisFragment -> bottomNav.visibility = View.GONE
                else -> bottomNav.visibility = View.VISIBLE
            }
        }
    }

    companion object {
        @JvmField
        var baslangicZamani: Long = 0
    }
}