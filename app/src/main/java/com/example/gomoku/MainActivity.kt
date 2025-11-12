package com.example.gomoku

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.gomoku.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.gomokuView.onStatusChanged = { statusText ->
            binding.statusText.text = statusText
        }

        binding.resetButton.setOnClickListener {
            binding.gomokuView.resetGame()
        }

        // Initialize with the correct status text
        binding.gomokuView.refreshStatus()
    }
}
