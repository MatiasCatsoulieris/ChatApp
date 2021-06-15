package android.example.com.chatapp.view.chat

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.example.com.chatapp.R
import android.example.com.chatapp.databinding.FragmentPhotoMessageBinding
import android.example.com.chatapp.model.MessageSent
import android.example.com.chatapp.model.MessageType
import android.example.com.chatapp.util.*
import android.example.com.chatapp.viewModel.ChatViewModel
import android.net.Uri
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import java.io.File


class PhotoMessageFragment : Fragment(), MessageSent {

    /////VIEW BINDING
    private var _binding : FragmentPhotoMessageBinding? = null
    private val binding get() = _binding!!

    /////
    private var uidReceiver: String? = null
    private var usernameReceiver: String? = null
    private lateinit var imgUriString : String
    private lateinit var dialog: Dialog
    /////VIEWMODEL
    private val viewModel: ChatViewModel by activityViewModels<ChatViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPhotoMessageBinding.inflate(inflater, container, false)
        getExtras()
        setListeners()
        changeFabIconSendMessage()
        binding.imgPhotoToSend.loadImage(imgUriString, getProgressDrawable(requireContext()))
        observeViewModel()
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        findNavController().currentBackStackEntry?.savedStateHandle
            ?.getLiveData<String>(UID)?.observe(viewLifecycleOwner, {
                uidReceiver = it
                changeFabIconSendMessage()
            })
        findNavController().currentBackStackEntry?.savedStateHandle
            ?.getLiveData<String>(USERNAME)?.observe(viewLifecycleOwner, {
                usernameReceiver = it
                changeFabIconSendMessage()
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            })
    }


    private fun getExtras() {
        arguments?.let{
            uidReceiver = it.getString(UID, null)
            imgUriString = it.getString(IMG_URI, null)
        }
    }

    private fun setListeners() {
        binding.buttonFinish.setOnClickListener {
            Navigation.findNavController(it).navigateUp()
        }
        binding.fabSedPhotoMessage.setOnClickListener {
            if(uidReceiver == null) {
                Navigation.findNavController(it).navigate(PhotoMessageFragmentDirections.actionPhotoMessageToContacts(
                ACTION_SEND_PHOTO_MESSAGE))
            } else {
                viewModel.receiverUid.value = uidReceiver
                viewModel.sendFileMessage(Uri.fromFile(
                    File(imgUriString)), uidReceiver!!,
                    binding.txtPhotoMessage.text.toString(),"photos", ".jpeg", MessageType.TYPE_PHOTO)
                dialog = createCustomDialog(requireContext(),
                    "Sending image...", null)
                    dialog.show()
            }
        }
    }
    private fun changeFabIconSendMessage() {
        if(uidReceiver == null) {
            binding.txtSendTo.visibility = View.GONE
            AnimUtils.changeIconButtonSendMessage(R.drawable.ic_check, binding.fabSedPhotoMessage)
        } else {
            binding.txtSendTo.text = "Send to $usernameReceiver"
            binding.txtSendTo.visibility = View.VISIBLE
            AnimUtils.changeIconButtonSendMessage(R.drawable.ic_action_send, binding.fabSedPhotoMessage)
        }
    }

    private fun observeViewModel() {
        viewModel.isMessageSent.observe(viewLifecycleOwner, {
             it.getContentIfNotHandled()?.let { isSent -> onMessageSent(isSent) }

        })
        viewModel.fileUploadProgress.observe(viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { progress ->
                if(this::dialog.isInitialized) {
                    val messageDialogTextView = dialog.findViewById<TextView>(R.id.messageDialog)
                    messageDialogTextView.text = "Upload progress: " + progress.toInt() + "%"
                }
            }
        })
        viewModel.fileUploadSuccessful.observe(viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { isSuccessful ->
                if (isSuccessful) {
                    if (this::dialog.isInitialized) {
                        dialog.dismiss()
                    }

                } else {
                    Toast.makeText(requireContext(), "Upload failed", Toast.LENGTH_SHORT).show()
                    if (this::dialog.isInitialized) {
                        dialog.dismiss()
                    }
                }
            }
        })
    }

    override fun onMessageSent(isMessageSent: Boolean) {
        if(isMessageSent) {
            Navigation.findNavController(binding.root)
                .navigate(PhotoMessageFragmentDirections.actionPhotoMessageToChat(uidReceiver!!))
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}