package com.applover.dynamicmotionlayoutbar

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.applover.dynamicmotionlayoutbar.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var availableScreens = Example.values().iterator().also {
        it.next()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        binding.fab.setOnClickListener { view ->
            toggleExample(view)
        }

        setupFirstScreen()
    }

    private fun toggleExample(view: View) {
        if (!availableScreens.hasNext()) {
            availableScreens = Example.values().iterator()
        }
        val currentScreen = availableScreens.next()
        changeVisibilityForExamples(currentScreen)
        Snackbar.make(view, currentScreen.name, Snackbar.LENGTH_LONG).show()
    }

    private fun changeVisibilityForExamples(example: Example) {
        binding.layout.root.isVisible = example == Example.ActivableImageScreen
    }

    private fun setupFirstScreen() = with(binding.layout) {
        buttonActivate.setOnClickListener {
            imageViewProfile.setActive(true)
        }

        buttonDeactivate.setOnClickListener {
            imageViewProfile.setActive(false)
        }
    }

    enum class Example {
        ActivableImageScreen, StepProgressBar, StepProgressBarTwo,
    }
}
