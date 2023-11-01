package com.example.zadaniemobv.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.zadaniemobv.R

class RecoverPassword1Fragment : Fragment(R.layout.fragment_recover_password1) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val submitButton: Button = view.findViewById(R.id.login_button)
        submitButton.setOnClickListener {
            /* val password: String = view.findViewById<EditText>(R.id.password).text.toString()
             val password_confirmed: String = view.findViewById<EditText>(R.id.password2).text.toString()
             if (password != password_confirmed){
                 Log.d("Error", "Passwords dont match")
             }
             val username: String = view.findViewById<EditText>(R.id.username).text.toString()
             val email: String = view.findViewById<EditText>(R.id.email).text.toString()
                         */

            findNavController().navigate(R.id.action_recoverPassword1Fragment_to_recoverPassword2Fragment)

        }

    }
}