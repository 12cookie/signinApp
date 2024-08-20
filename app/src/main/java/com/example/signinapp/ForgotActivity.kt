package com.example.signinapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.util.Log
import android.widget.EditText
import android.widget.Toast
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
        val sUsername = findViewById<EditText> (R.id.username)

        var username = ""
        val loginWatcher = object : TextWatcher
        {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
            {
                username = sUsername.text.toString()
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        sUsername.addTextChangedListener(loginWatcher)

        val db = DBHelper(this, null)
        getOTPButton.setOnClickListener {
            if(db.checkUserInDB(username))
            {
                val intent = Intent(this, OTPInput::class.java)
                intent.putExtra("Username", username)
                startActivity(intent)
            }

            else
            {
                Toast.makeText(this, "Enter Valid Username", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun generateOtp(): String
    {
        val otp = Random.nextInt(100000, 999999)
        Log.d("ForgotActivity", "Generated OTP: $otp")
        return otp.toString().padStart(6, '0')
    }
}