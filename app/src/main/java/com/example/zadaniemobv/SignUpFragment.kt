package com.example.zadaniemobv

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.zadaniemobv.api.DataRepository
import com.example.zadaniemobv.viewModel.AuthViewModel
import com.google.android.material.snackbar.Snackbar


class SignUpFragment : Fragment() {

    private lateinit var viewModel: AuthViewModel


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity(), object : ViewModelProvider.Factory {
            override fun <T : ViewModel>create(modelClass: Class<T>): T {
                return AuthViewModel(DataRepository.getInstance(requireContext())) as T
            }
        })[AuthViewModel::class.java]

        viewModel.registrationResult.observe(viewLifecycleOwner){
            if (it.second != null){
                requireView().findNavController().navigate(R.id.action_signup_fragment_to_mapFragment)
            }else{
                Snackbar.make(
                    view.findViewById(R.id.signup_button),
                    it.first,
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }

        view.findViewById< TextView>(R.id.signup_button).apply {
            setOnClickListener {
                viewModel.registerUser(
                    view.findViewById< EditText >(R.id.username_input).text.toString(),
                    view.findViewById<EditText>(R.id.email_signup).text.toString(),
                    view.findViewById<EditText>(R.id.password).text.toString()
                )
            }
        }
    }
}
