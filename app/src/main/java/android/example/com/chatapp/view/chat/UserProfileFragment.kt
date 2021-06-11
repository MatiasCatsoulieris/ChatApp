package android.example.com.chatapp.view.chat

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.example.com.chatapp.R
import android.example.com.chatapp.databinding.CardGallerySmallBinding
import android.example.com.chatapp.databinding.UserProfileFragmentBinding
import android.example.com.chatapp.model.Message
import android.example.com.chatapp.util.UID
import android.example.com.chatapp.util.getProgressDrawable
import android.example.com.chatapp.util.loadImage
import android.example.com.chatapp.view.adapters.GalleryAdapter
import android.example.com.chatapp.viewModel.UserProfileViewModel
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class UserProfileFragment : Fragment() {

    ////ViewBinding
    private var _binding : UserProfileFragmentBinding? = null
    private val binding get() = _binding!!
    /////ViewModel
    private lateinit var viewModel: UserProfileViewModel

    private var uid: String? = null
    private lateinit var photoChatAdapter: FirestoreRecyclerAdapter<Message, GalleryAdapter.GalleryViewHolder>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = UserProfileFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getExtras()
        viewModel = ViewModelProvider(this).get(UserProfileViewModel::class.java)
        NavigationUI.setupWithNavController(binding.collapsingToolbarUserProfile, binding.toolbarUserProfile, findNavController())
        binding.toolbarUserProfile.inflateMenu(R.menu.menu_user_profile)
        setMenuListeners()
        getUserData()
        setAdapter()
        observeViewModel()
    }
    
    private fun getExtras() {
        arguments?.let {
            uid = requireArguments().getString(UID, null)
        }
    }

    private fun getUserData() {
        uid?.let {
            viewModel.getUserData(uid!!)
        }
    }

    private fun setAdapter() {
        val options = FirestoreRecyclerOptions.Builder<Message>()
            .setQuery(viewModel.getPhotoListChatQuery(uid!!), Message::class.java)
            .setLifecycleOwner(this)
            .build()

        photoChatAdapter = object: FirestoreRecyclerAdapter<Message, GalleryAdapter.GalleryViewHolder>(options) {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): GalleryAdapter.GalleryViewHolder {
                val inflater = LayoutInflater.from(requireContext())
                val view = CardGallerySmallBinding.inflate(inflater)
                return GalleryAdapter.GalleryViewHolder(null, view)
            }

            override fun onBindViewHolder(
                holder: GalleryAdapter.GalleryViewHolder,
                position: Int,
                model: Message
            ) {
                holder.imgView.loadImage(getItem(position).dataUrl, getProgressDrawable(requireContext()))
                holder.itemView.setOnClickListener {
                    Navigation.findNavController(it).navigate(
                        UserProfileFragmentDirections.actionUserProfileFragmentToWatchImageFragment(
                            getItem(position).dataUrl!!
                        )
                    )
                }
            }

            override fun onDataChanged() {
                super.onDataChanged()
                if(itemCount == 0) {
                    binding.layoutChatPhotos.visibility = View.GONE
                } else {
                    binding.layoutChatPhotos.visibility = View.VISIBLE
                }
                binding.txtNumberPhotosChat.text = itemCount.toString()
            }
        }
        binding.recyclerViewChatPictures.apply {
            adapter = photoChatAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.userLiveData.observe(viewLifecycleOwner, {
            it?.let {
                binding.imgUserProfile.loadImage(it.imageUrl, getProgressDrawable(requireContext()))
                binding.collapsingToolbarUserProfile.title = it.userName
                if(it.state != null) {
                    binding.txtProfileStatus.text = it.state
                } else {
                    binding.txtProfileStatus.text = "No status"
                }
            }
        })
    }

    private fun setMenuListeners() {
        binding.toolbarUserProfile.setOnMenuItemClickListener { 
            when(it.itemId) {
                R.id.menuUserShare -> {
                    Toast.makeText(requireContext(), "Share..", Toast.LENGTH_SHORT).show()
                    return@setOnMenuItemClickListener true   
                }
                R.id.menuUserEdit -> {
                    Toast.makeText(requireContext(), "Edit...", Toast.LENGTH_SHORT).show()
                    return@setOnMenuItemClickListener true
                }
                R.id.menuUserSeeInContactList -> {
                    Toast.makeText(requireContext(), "See in contact list...", Toast.LENGTH_SHORT).show()
                    return@setOnMenuItemClickListener true
                }
                else -> return@setOnMenuItemClickListener true
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}