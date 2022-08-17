package com.applover.dynamicmotionlayoutbar

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.applover.dynamicmotionlayoutbar.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        setupFirstScreen()
    }

    private fun setupFirstScreen() = with(binding.layout) {
        buttonActivate.setOnClickListener {
            imageViewProfile.setActive(true)
        }

        buttonDeactivate.setOnClickListener {
            imageViewProfile.setActive(false)
        }
    }
}
