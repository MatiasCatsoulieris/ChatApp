package android.example.com.chatapp.view.account

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.example.com.chatapp.databinding.FragmentRecoverPasswordBinding
import android.example.com.chatapp.util.createCustomDialog
import android.example.com.chatapp.util.isValidEmail
import android.example.com.chatapp.viewModel.RecoverPasswordViewModel
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider


class RecoverPasswordFragment : Fragment() {

    //ViewBinding
    private var _binding: FragmentRecoverPasswordBinding? = null
    private val binding get() = _binding!!
    //ViewModel
    private lateinit var viewModel: RecoverPasswordViewModel
    //Dialog
    private lateinit var dialog:  Dialog
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
        _binding = FragmentRecoverPasswordBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(RecoverPasswordViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()
        observeViewModel()
    }

    private fun setListeners() {
        binding.mailRecoverPasswordEdit.addTextChangedListener(textWatcher)
        binding.sendEmailRecoverPassword.setOnClickListener {
            if(validateInfo()) {
                dialog = createCustomDialog(it.context, "Logging in...", "Please wait")
                dialog.show()
                viewModel.recoverPassword(binding.mailRecoverPasswordEdit.text.toString())
            }
        }
        binding.mailRecoverPasswordEdit.setOnEditorActionListener { v, actionId, event ->
            if(actionId == EditorInfo.IME_ACTION_DONE) {
                if(validateInfo()) {
                    dialog = createCustomDialog(requireContext(), "Logging in...", "Please wait")
                    dialog.show()
                    viewModel.recoverPassword(binding.mailRecoverPasswordEdit.text.toString())
                }
            }
            return@setOnEditorActionListener false
        }
    }
    private fun validateInfo() : Boolean {
        return if (binding.mailRecoverPasswordEdit.text.toString().isValidEmail()) {
            binding.sendEmailRecoverPassword.isEnabled = true
            true
        } else {
            binding.sendEmailRecoverPassword.isEnabled = false
            false
        }
    }
    private fun observeViewModel() {
        viewModel.recoverTask.observe(viewLifecycleOwner, {
            if(it){
                dialog.dismiss()
                Toast.makeText(requireContext(), "Request successful, check your email", Toast.LENGTH_SHORT)
                    .show()
            } else {
                dialog.dismiss()
                Toast.makeText(requireContext(), "Request failed, please try again later", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}