package com.example.chat.registerLogin

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.chat.R

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val login : Button = findViewById(R.id.login_button_login)
        login.setOnClickListener {
            val email_view : EditText = findViewById(R.id.email_edittext_login)
            val email = email_view.text.toString()
            val password_view : EditText = findViewById(R.id.password_edittext_login)
            val password = password_view.text.toString()

            Log.d("MainLogin","Attempt login with email/pw: $email/$password")
        }

        val back_to_regis : TextView = findViewById(R.id.back_to_register_button_login)
        back_to_regis.setOnClickListener {
            Log.d("MainLogin","Back to create an account")
            finish()
        }
    }
}