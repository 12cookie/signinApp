package com.example.signinapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class SignUpActivity : AppCompatActivity()
{
    private lateinit var sName: EditText
    private lateinit var sUsername: EditText
    private lateinit var sPassword: EditText
    private lateinit var button: Button

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

        sName = findViewById(R.id.name)
        sUsername = findViewById(R.id.username)
        sPassword = findViewById(R.id.password)
        button = findViewById(R.id.newUser)

        button.isEnabled = false

        val loginWatcher = object : TextWatcher
        {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
            {
                val name = sName.text.toString()
                val username = sUsername.text.toString()
                val password = sPassword.text.toString()
                button.isEnabled = name.isNotEmpty() && username.isNotEmpty() && password.isNotEmpty()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        sName.addTextChangedListener(loginWatcher)
        sUsername.addTextChangedListener(loginWatcher)
        sPassword.addTextChangedListener(loginWatcher)

        button.setOnClickListener {
            val name = sName.text.toString()
            val username = sUsername.text.toString()
            val password = sPassword.text.toString()
            val db = DBHelper(this, null)
            db.addUser(name, username, password)

            sName.text.clear()
            sUsername.text.clear()
            sPassword.text.clear()

            val intent = Intent(this, LandingActivity::class.java)
            startActivity(intent)
        }
    }
}