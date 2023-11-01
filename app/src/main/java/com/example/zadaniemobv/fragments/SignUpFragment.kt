package com.example.zadaniemobv.fragments

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.zadaniemobv.R
import com.example.zadaniemobv.viewmodels.AuthViewModel
import com.example.zadaniemobv.data.api.DataRepository
import com.google.android.material.snackbar.Snackbar


class SignUpFragment : Fragment(R.layout.fragment_signup) {

    private lateinit var viewModel: AuthViewModel


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity(), object : ViewModelProvider.Factory {
            override fun <T : ViewModel>create(modelClass: Class<T>): T {
                return AuthViewModel(DataRepository.getInstance()) as T
            }
        })[AuthViewModel::class.java]

        viewModel.registrationResult.observe(viewLifecycleOwner){
            if (it.second != null){
                findNavController().navigate(R.id.action_signup_fragment_to_login_fragment)
            }else{
                Snackbar.make(
                    view.findViewById(R.id.signup_button),
                    it.first,
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }

        view.findViewById<TextView>(R.id.signup_button).apply {
            setOnClickListener {
                viewModel.registerUser(
                    )
            }
        }
    }

}