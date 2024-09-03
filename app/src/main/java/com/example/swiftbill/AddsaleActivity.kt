package com.example.swiftbill

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.swiftbill.databinding.ActivityAddsaleBinding

class AddsaleActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityAddsaleBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.statusBarColor = getColor(R.color.blue)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        val switch=binding.switch1
        switch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                switch.text = "Cash"
            // Text when switch is on
            } else {
                switch.text = "Credit"  // Text when switch is off
            }
        }

    }
}