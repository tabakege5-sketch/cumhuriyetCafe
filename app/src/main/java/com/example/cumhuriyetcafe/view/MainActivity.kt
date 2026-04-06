package com.example.cumhuriyetcafe.view

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.cumhuriyetcafe.R
import com.example.cumhuriyetcafe.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // View Binding kurulumu
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Navigation Controller kurulumu
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController

        // Bottom Navigation barını Controller ile bağlama
        val bottomNav = binding.bottomNavigation
        bottomNav.setupWithNavController(navController)

        // Fragment değişimlerini dinle (Giriş ekranında menüyü gizlemek için)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.girisFragment -> {
                    bottomNav.visibility = View.GONE
                }
                else -> {
                    bottomNav.visibility = View.VISIBLE
                }
            }
        }
    }
}