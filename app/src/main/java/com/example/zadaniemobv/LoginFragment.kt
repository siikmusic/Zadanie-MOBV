package com.example.zadaniemobv

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.zadaniemobv.api.DataRepository
import com.example.zadaniemobv.databinding.FragmentLoginBinding
import com.example.zadaniemobv.viewModel.AuthViewModel
import com.google.android.material.snackbar.Snackbar

class LoginFragment : Fragment(R.layout.fragment_login) {
    private var binding: FragmentLoginBinding? = null
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity(), object : ViewModelProvider.Factory {
            override fun <T : ViewModel>create(modelClass: Class<T>): T {
                return AuthViewModel(DataRepository.getInstance(requireContext())) as T
            }
        })[AuthViewModel::class.java]
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val loginButton: Button = view.findViewById(R.id.login_button)
        val forgotPasswordButton: Button = view.findViewById(R.id.signup_button)

         binding = FragmentLoginBinding.bind(view).apply {
             lifecycleOwner = viewLifecycleOwner
             model = viewModel
         }.also { bnd ->
             viewModel.loginResult.observe(viewLifecycleOwner) { result ->
                 view.findNavController().navigate(R.id.action_login_fragment_to_mapFragment)
             }
         }
        loginButton.setOnClickListener {
            findNavController().navigate(R.id.action_login_fragment_to_mapFragment)
        }
        forgotPasswordButton.setOnClickListener {
            findNavController().navigate(R.id.action_login_fragment_to_recoverPassword1Fragment)
        }
    }
}