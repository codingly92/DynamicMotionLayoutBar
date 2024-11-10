package com.applover.dynamicmotionlayoutbar

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.applover.dynamicmotionlayoutbar.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupZoomExample()
    }

    private fun setupZoomExample(){
        with(binding.contentZoom){
            zoomContainer.initialize(listOf(1,2,3,4))
            btnTransOri.setOnClickListener{ zoomContainer.tranAngle()}
        }
    }
}
