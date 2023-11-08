package com.example.zadaniemobv

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class IntroFragment: Fragment(R.layout.fragment_intro) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val loginButton: Button = view.findViewById(R.id.login_button)
        val forgotPasswordButton: Button = view.findViewById(R.id.signup_button)
        loginButton.setOnClickListener {
            findNavController().navigate(R.id.action_introFragment_to_login_fragment)
        }
        forgotPasswordButton.setOnClickListener {
            findNavController().navigate(R.id.action_introFragment_to_signup_fragment)
        }
    }
}