package android.example.com.chatapp.view.account


import android.app.Dialog
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.example.com.chatapp.databinding.FragmentLoginBinding
import android.example.com.chatapp.util.createCustomDialog
import android.example.com.chatapp.util.isValidEmail
import android.example.com.chatapp.viewModel.LoginViewModel
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.navigation.Navigation

class LoginFragment : Fragment() {

    //ViewBinding
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    //ViewModel
    private lateinit var viewModel: LoginViewModel
    //Dialog
    private lateinit var dialog: Dialog
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
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        checkSession(view)
        setListeners()
        observeViewModel()

    }

    private fun setListeners() {
        binding.maiTextView.addTextChangedListener(textWatcher)
        binding.passwordTextView.addTextChangedListener(textWatcher)

        binding.forgotPasswordTv.setOnClickListener {
            Navigation.findNavController(it).navigate(LoginFragmentDirections.actionLoginToRecover())
        }

        binding.registerTextView.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(LoginFragmentDirections.actionLoginToRegister())
        }
        binding.loginButton.setOnClickListener {
            if(validateInfo()) {
                dialog = createCustomDialog(it.context, "Logging in...", "Please wait")
                dialog.show()
                val mail = binding.maiTextView.text.toString()
                val password = binding.passwordTextView.text.toString()
                viewModel.customLogin(mail, password)
            } else {
                Toast.makeText(it.context, "You must enter a valid mail/password",
                Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun validateInfo() : Boolean {
        return if (binding.maiTextView.text.toString().isValidEmail()
            && binding.passwordTextView.text.isNotEmpty()) {
            binding.loginButton.isEnabled = true
            true
        } else {
            binding.loginButton.isEnabled = false
            false
        }
    }
    private fun checkSession(view: View) {
        val isLoggedIn = viewModel.checkSession()
        if (isLoggedIn != null) {
            val action = LoginFragmentDirections.actionLoginFragmentToHomeFragment()
            Navigation.findNavController(view).navigate(action)

        }
    }

    private fun observeViewModel() {
        viewModel.login.observe(viewLifecycleOwner, {
            if (it) {
                dialog.dismiss()
                Navigation.findNavController(binding.root)
                    .navigate(LoginFragmentDirections.actionLoginFragmentToHomeFragment())
            }
        })
        viewModel.error.observe(viewLifecycleOwner, {
            if(it.isNotEmpty()) {
                dialog.dismiss()
                binding.passwordTextView.text.clear()
                Toast.makeText(context, it,Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()

    }
}