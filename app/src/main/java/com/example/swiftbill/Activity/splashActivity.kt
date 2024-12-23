package com.example.swiftbill.Activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.swiftbill.R
import com.google.firebase.auth.FirebaseAuth

class splashActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        auth = FirebaseAuth.getInstance()
        android.os.Handler().postDelayed({


        val currentUser = auth.currentUser
        if (currentUser != null) {
            // User is signed in, navigate navigateToHomeScreen()e to main screen
            startActivity(Intent(this, HostActivity::class.java))
            finish()
        } else {
            // No user is signed in, navigate to login screen navigateToLoginScreen()
            startActivity(Intent(this, LogIn_Activity::class.java))
        finish()
        }}, 3000)

    }
}