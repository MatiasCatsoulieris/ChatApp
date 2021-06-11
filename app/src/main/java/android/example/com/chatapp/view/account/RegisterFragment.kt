package android.example.com.chatapp.view.account

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.example.com.chatapp.databinding.FragmentRegisterBinding
import android.example.com.chatapp.util.createCustomDialog
import android.example.com.chatapp.util.isValidEmail
import android.example.com.chatapp.viewModel.RegisterViewModel
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation


class RegisterFragment : Fragment() {

    //ViewBinding
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    //Dialog
    private lateinit var dialog: Dialog
    //ViewModel
    private lateinit var viewmodel: RegisterViewModel
    //TextWatcher
    private val textWatcher = object: TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            validateInfo()
        }

        override fun afterTextChanged(s: Editable?) {

        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        viewmodel = ViewModelProvider(this).get(RegisterViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()
        observeViewmodel()
    }

    private fun setListeners() {
        binding.maiTextView.addTextChangedListener(textWatcher)
        binding.passwordTextView.addTextChangedListener(textWatcher)
        binding.confirmPasswordTextView.addTextChangedListener(textWatcher)
        binding.usernameTextView.addTextChangedListener(textWatcher)

        binding.loginTextView.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(RegisterFragmentDirections.actionRegisterToLogin())
        }
        binding.confirmPasswordTextView.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if(validateInfo()) {
                    registerUser()
                }
            }
            return@setOnEditorActionListener false
        }
        binding.registerButton.setOnClickListener {
            if(validateInfo()) {
                dialog = createCustomDialog(it.context, "Logging in...", "Please wait")
                dialog.show()
                registerUser()
        } else {
            Toast.makeText(it.context, "Please fill the forms correctly", Toast.LENGTH_SHORT).show()
        }
        }
    }

    private fun registerUser() {
        val username = binding.usernameTextView.text.toString()
        val email = binding.maiTextView.text.toString()
        val password = binding.passwordTextView.text.toString()
        viewmodel.customRegister(username, email, password)

    }

    private fun validateInfo(): Boolean {
        return if (binding.maiTextView.text.toString().isValidEmail()
            && binding.passwordTextView.text.length > 5
            && binding.usernameTextView.text.length > 2
            && binding.confirmPasswordTextView.text.toString() == binding.passwordTextView.text.toString()
        ) {
            binding.registerButton.isEnabled = true
            true
        } else {
            binding.registerButton.isEnabled = false
            false
        }
    }

    private fun observeViewmodel() {
        viewmodel.loginResult.observe(viewLifecycleOwner, {
            dialog.dismiss()
            if (it) {
                Toast.makeText(context, "Login was successful", Toast.LENGTH_SHORT).show()
                Navigation.findNavController(binding.root)
                    .navigate(RegisterFragmentDirections.actionRegisterToHome())
            } else {
                Toast.makeText(context, "Login failed", Toast.LENGTH_SHORT).show()
            }
        })
        viewmodel.register.observe(viewLifecycleOwner, {
            dialog.dismiss()
            if (it) {
                Toast.makeText(context, "Register was successful", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Register failed", Toast.LENGTH_SHORT).show()
            }
        })
        viewmodel.error.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) {
                dialog.dismiss()
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        })
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}