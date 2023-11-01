package com.example.zadaniemobv.fragments
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.example.zadaniemobv.R

class SignupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_signup)

        val submitButton: Button = findViewById(R.id.signup_button)
        submitButton.setOnClickListener {
            // Logika po kliknutí na tlačidlo, napríklad na získanie textu z EditText
            val password: String = findViewById<EditText>(R.id.password).text.toString()
            val password_confirmed: String = findViewById<EditText>(R.id.password2).text.toString()
            if (password != password_confirmed){
                Log.d("Error", "Passwords dont match")
            }
            val username: String = findViewById<EditText>(R.id.code_text).text.toString()
            val email: String = findViewById<EditText>(R.id.email_signup).text.toString()

        }
    }
}
