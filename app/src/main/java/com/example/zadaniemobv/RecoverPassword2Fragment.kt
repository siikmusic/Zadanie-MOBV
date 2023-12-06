package com.example.zadaniemobv

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.zadaniemobv.api.DataRepository
import com.example.zadaniemobv.databinding.FragmentRecoverPassword1Binding
import com.example.zadaniemobv.databinding.FragmentRecoverPassword2Binding
import com.example.zadaniemobv.viewModel.AuthViewModel
import com.google.android.material.snackbar.Snackbar

class RecoverPassword2Fragment : Fragment(R.layout.fragment_recover_password2) {
    private lateinit var viewModel: AuthViewModel
    private lateinit var binding: FragmentRecoverPassword2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(requireActivity(), object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AuthViewModel(DataRepository.getInstance(requireContext())) as T
            }
        })[AuthViewModel::class.java]
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecoverPassword2Binding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            model = viewModel
        }.also { bnd ->

            viewModel.changeResult.observe(viewLifecycleOwner) {
                if (it != null) {
                    if (it.isNotEmpty()) {

                        Snackbar.make(
                            bnd.loginButton,
                            it,
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

    }
}