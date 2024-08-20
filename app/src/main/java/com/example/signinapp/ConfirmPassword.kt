package com.example.signinapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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

        val intent = intent
        val extras = intent.extras
        val username = extras?.getString("Username")

        val etpassword= findViewById<EditText>(R.id.newpass)
        val etconfirm = findViewById<EditText>(R.id.renterpass)
        val submitbtn = findViewById<Button> (R.id.submit)

        var password = ""
        var confirm = ""
        val loginWatcher = object : TextWatcher
        {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
            {
                password = etpassword.text.toString()
                confirm = etconfirm.text.toString()
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        etconfirm.addTextChangedListener(loginWatcher)
        etpassword.addTextChangedListener(loginWatcher)

        submitbtn.setOnClickListener {
            if (password == confirm)
            {
                val db = DBHelper(this, null)
                if (username != null)
                {
                    db.updatePassword(username, password)
                }
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