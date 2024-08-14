package com.example.signinapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity()
{
    private lateinit var sUsername: EditText
    private lateinit var sPassword: EditText
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main))
        { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sUsername = findViewById(R.id.username)
        sPassword = findViewById(R.id.password)
        loginButton = findViewById(R.id.login)

        loginButton.isEnabled = false

        val loginWatcher = object : TextWatcher
        {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
            {
                val username = sUsername.text.toString()
                val password = sPassword.text.toString()
                loginButton.isEnabled = username.isNotEmpty() && password.isNotEmpty()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        sUsername.addTextChangedListener(loginWatcher)
        sPassword.addTextChangedListener(loginWatcher)

        loginButton.setOnClickListener {
            val username = sUsername.text.toString()
            val password = sPassword.text.toString()
            val db = DBHelper(this, null)

            if(db.checkUser(username, password))
            {
                val intent = Intent(this, LandingActivity::class.java)
                sUsername.text.clear()
                sPassword.text.clear()
                startActivity(intent)
            }
            else
            {
                Toast.makeText(applicationContext, "This a toast message", Toast.LENGTH_LONG).show()
            }
        }

        val signUpButton = findViewById<Button> (R.id.newUser)
        signUpButton.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        val forgotButton = findViewById<Button> (R.id.forgotPassword)
        forgotButton.setOnClickListener {
            val intent = Intent(this, ForgotActivity::class.java)
            startActivity(intent)
        }
    }
}