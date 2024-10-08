package com.example.signinapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.chaos.view.PinView

class OTPInput : AppCompatActivity()
{

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_otpinput)

        val verifyButton = findViewById<Button>(R.id.verify)
        val regenerateButton = findViewById<Button>(R.id.regenerate)

        val intent = intent
        val extras = intent.extras
        val username = extras?.getString("Username")

        val fPassword = ForgotActivity()
        var otp = fPassword.generateOtp()

        verifyButton.setOnClickListener {
            val otpInput = findViewById<PinView>(R.id.pinview).text.toString()
            if(otpInput == otp)
            {
                val intent = Intent(this, ConfirmPassword::class.java)
                intent.putExtra("Username", username)
                startActivity(intent)
            }
            else
            {
                Toast.makeText(this, "Invalid OTP or OTP expired", Toast.LENGTH_SHORT).show()
            }
        }

        regenerateButton.setOnClickListener {
            otp = fPassword.generateOtp()
        }
    }
}