package com.example.zadaniemobv

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class LoginFragment : Fragment(R.layout.fragment_login) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val loginButton: Button = view.findViewById(R.id.login_button)
        val forgotPasswordButton: Button = view.findViewById(R.id.forgot_password_button)

        loginButton.setOnClickListener {
            findNavController().navigate(R.id.action_login_fragment_to_mapFragment)
        }
        forgotPasswordButton.setOnClickListener {
            findNavController().navigate(R.id.action_login_fragment_to_recoverPassword1Fragment)

        }
    }
}