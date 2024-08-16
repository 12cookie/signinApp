package com.example.signinapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ConfirmPassword : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_confirm_password)

        val etpassword= findViewById<EditText>(R.id.newpass)
        val etconfirm = findViewById<EditText>(R.id.renterpass)
        val submitbtn = findViewById<Button> (R.id.submit)
        submitbtn.setOnClickListener {
            val password = etpassword.text.toString()
            val confirmPassword = etconfirm.text.toString()
            if (password == confirmPassword)
            {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            else
            {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            }
        }
    }
}