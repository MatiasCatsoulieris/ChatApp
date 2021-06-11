package android.example.com.chatapp.view.menu

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.example.com.chatapp.R
import android.example.com.chatapp.databinding.SettingsFragmentBinding
import android.example.com.chatapp.util.TxtUtils
import android.example.com.chatapp.util.getProgressDrawable
import android.example.com.chatapp.util.loadImage
import android.example.com.chatapp.viewModel.SettingsViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI

class SettingsFragment : Fragment() {

    //View Binding
    private var _binding: SettingsFragmentBinding? = null
    private val binding get() = _binding!!
    //ViewModel
    private lateinit var viewModel: SettingsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = SettingsFragmentBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)
        NavigationUI.setupWithNavController(binding.toolbarSettings, findNavController())
        changeButtonText()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        viewModel.fetchUserData()
        setListeners()
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.currentUser.observe(viewLifecycleOwner, {
            it?.let {
                binding.profileImageRounded2.loadImage(it.imageUrl, getProgressDrawable(requireContext()))
                binding.txtUserName.text = it.userName
                if(it.state != null && it.state!!.isNotEmpty()) {
                    binding.txtUserState.text = it.state
                    binding.txtUserState.visibility = View.VISIBLE
                } else {
                    binding.txtUserState.visibility = View.GONE
                }
            }
        })
    }

    private fun setListeners() {
        binding.layoutUserData.setOnClickListener {
            val extras = FragmentNavigatorExtras(
                Pair(binding.profileImageRounded2, getString(R.string.imgUserTransition)))
            Navigation.findNavController(it)
                .navigate(R.id.actionSettingsToProfile, null, null, extras)
        }

    }



    private fun changeButtonText() {
        binding.accountSettingsButton.text = TxtUtils.setTitleSubtitleButton(
            "Account\n", "Privacy, security, change number")
        binding.ChatsSettingsButton.text = TxtUtils.setTitleSubtitleButton(
            "Chats\n","Theme, background, chat history")
        binding.notificationsSettingsBottom.text = TxtUtils.setTitleSubtitleButton(
            "Notifications\n", "Ringtones, groups, calls")
        binding.storageDataSettingsBottom.text = TxtUtils.setTitleSubtitleButton(
            "Data and Storage\n", "Network, automatic download")
        binding.helpSettingsBottom.text = TxtUtils.setTitleSubtitleButton(
            "Help\n", "FAQ, contact us, policy")
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}