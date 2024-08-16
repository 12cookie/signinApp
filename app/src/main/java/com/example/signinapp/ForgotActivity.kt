package com.example.signinapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class ForgotActivity : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgot)

        val getOTPButton = findViewById<Button> (R.id.getOTP)
        getOTPButton.setOnClickListener {
            val intent = Intent(this, OTPInput::class.java)
            val otp = generateOtp()
            Log.d("ForgotActivity", "Generated OTP: $otp")
            startActivity(intent)
        }
    }

    fun generateOtp(): String
    {
        val otp = Random.nextInt(100000, 999999)
        return otp.toString().padStart(6, '0')
    }
}