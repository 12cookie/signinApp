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

class SignUpActivity : AppCompatActivity()
{
    private lateinit var sName: EditText
    private lateinit var sEmail: EditText
    private lateinit var sUsername: EditText
    private lateinit var sPassword: EditText
    private lateinit var sConfirmPassword: EditText
    private lateinit var button: Button

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

        sName = findViewById(R.id.name)
        sEmail = findViewById(R.id.email)
        sUsername = findViewById(R.id.username)
        sPassword = findViewById(R.id.password)
        sConfirmPassword = findViewById(R.id.confirmPassword)
        button = findViewById(R.id.newUser)

        button.isEnabled = false

        val loginWatcher = object : TextWatcher
        {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
            {
                val name = sName.text.toString()
                val email = sEmail.text.toString()
                val username = sUsername.text.toString()
                val password = sPassword.text.toString()
                val confirmPassword = sConfirmPassword.text.toString()
                button.isEnabled = name.isNotEmpty() && email.isNotEmpty() &&
                                   confirmPassword.isNotEmpty() &&
                                   username.isNotEmpty() && password.isNotEmpty()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        sName.addTextChangedListener(loginWatcher)
        sEmail.addTextChangedListener(loginWatcher)
        sUsername.addTextChangedListener(loginWatcher)
        sPassword.addTextChangedListener(loginWatcher)
        sConfirmPassword.addTextChangedListener(loginWatcher)

        button.setOnClickListener {
            val name = sName.text.toString()
            val email = sEmail.text.toString()
            val username = sUsername.text.toString()
            val password = sPassword.text.toString()
            val cPassword = sConfirmPassword.text.toString()

            if(password != cPassword)
            {
                Toast.makeText(applicationContext, "This a toast message", Toast.LENGTH_LONG).show()
            }
            else
            {
                val db = DBHelper(this, null)
                db.addUser(name, email, username, password)

                sName.text.clear()
                sEmail.text.clear()
                sUsername.text.clear()
                sPassword.text.clear()
                sConfirmPassword.text.clear()

                val intent = Intent(this, LandingActivity::class.java)
                startActivity(intent)
            }
        }
    }
}