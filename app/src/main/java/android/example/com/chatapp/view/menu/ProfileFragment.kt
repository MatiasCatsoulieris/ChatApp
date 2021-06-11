package android.example.com.chatapp.view.menu

import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Intent
import android.example.com.chatapp.R
import android.example.com.chatapp.databinding.FragmentProfileBinding
import android.example.com.chatapp.model.User
import android.example.com.chatapp.util.*
import android.example.com.chatapp.view.MainActivity
import android.example.com.chatapp.viewModel.ProfileViewModel
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.transition.TransitionInflater
import androidx.transition.TransitionSet
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.yalantis.ucrop.UCrop
import java.io.File
import java.io.IOException
import java.lang.NullPointerException
import java.text.SimpleDateFormat
import java.util.*

class ProfileFragment : Fragment() {

    //ViewBinding
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    //ViewModel
    private lateinit var viewModel: ProfileViewModel

    private var userImgUrl : String? = null
    private var cameraRequest = false
    private lateinit var currentPhotoPath: String
    private var galleryRequest = false
    private lateinit var dialog: Dialog


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        NavigationUI.setupWithNavController(binding.toolbarProfile, findNavController())
        sharedElementEnterTransition = getEnterTransitionProfile()
        binding.fabPhotoEdit.scaleX = 0f
        binding.fabPhotoEdit.scaleY = 0f
        AnimUtils.scaleFab(binding.fabPhotoEdit)
//        viewModel.fetchUserData()
        observeViewModel()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()

    }

    private fun getEnterTransitionProfile(): TransitionSet {
        val transitionSet = TransitionSet()
        transitionSet.addTransition(
            TransitionInflater.from(requireContext())
                .inflateTransition(android.R.transition.move)
        )
        transitionSet.duration = 300
        transitionSet.startDelay = 100
        return transitionSet
    }


    private fun setListeners() {
        //Handle Animations on both back buttons
        binding.toolbarProfile.setNavigationOnClickListener {
            AnimUtils.unscaleFab(binding.fabPhotoEdit)
            Navigation.findNavController(it).navigateUp()
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            AnimUtils.unscaleFab(binding.fabPhotoEdit)
            Navigation.findNavController(binding.root).navigateUp()
        }

        binding.btnUpdateName.setOnClickListener {
            if (binding.editUserNameProfile.text.toString().trim().isNotEmpty()) {
                viewModel.updateUserData("userName", binding.editUserNameProfile.text.toString())
            } else {
                Toast.makeText(it.context, "Please write a name", Toast.LENGTH_SHORT).show()
            }
        }
        binding.btnUpdateState.setOnClickListener {
            viewModel.updateUserData("state", binding.editStateProfile.text.toString())

        }
        binding.fabPhotoEdit.setOnClickListener {
            showBottomSheetPhotoOptions()
        }

    }

    private fun showBottomSheetPhotoOptions() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val layoutBottomSheet = layoutInflater.inflate(R.layout.layout_edit_profile_photo, null)
        setButtonSheetListeners(layoutBottomSheet, bottomSheetDialog)
        bottomSheetDialog.setContentView(layoutBottomSheet)
        bottomSheetDialog.show()
    }

    private fun setButtonSheetListeners(
        layoutBottomSheet: View?,
        bottomSheetDialog: BottomSheetDialog
    ) {
        layoutBottomSheet!!
            .findViewById<FloatingActionButton>(R.id.fabDeletProfilePicture).setOnClickListener {
                if(userImgUrl != null && userImgUrl?.length!! > 0) {
                    viewModel.deleteImgFromStorage(userImgUrl!!)
                } else {
                    Toast.makeText(requireContext(), "There is no picture to delete", Toast.LENGTH_SHORT)
                        .show()
                }
                bottomSheetDialog.dismiss()
            }
        layoutBottomSheet
            .findViewById<FloatingActionButton>(R.id.fabGalleryPicture).setOnClickListener {
                galleryRequest = true
                (activity as MainActivity).checkGalleryPermission()
                bottomSheetDialog.dismiss()
            }
        layoutBottomSheet
            .findViewById<FloatingActionButton>(R.id.fabCameraPicture).setOnClickListener {
                Navigation.findNavController(binding.root).navigate(ProfileFragmentDirections
                    .actionPofileToCamera(null, CAMERA_ACTION_UPDATE_PICTURE))
                bottomSheetDialog.dismiss()
            }
    }



    fun onPermissionResult(permissionGranted: Boolean) {

        if (cameraRequest && permissionGranted) {
            cameraIntent()
        }
        if (galleryRequest && permissionGranted) {
            galleryIntent()
        }
    }

    private fun galleryIntent() {

        val intent = Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            .setType("image/*");
        startActivityForResult(intent, REQUEST_STORAGE);

    }

    private fun cameraIntent() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {

            null
        }
        photoFile?.also {
            val photoURI: Uri = FileProvider.getUriForFile(
                requireContext(),
                "com.example.android.fileprovider",
                it
            )
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            if (intent.resolveActivity(requireActivity().packageManager) != null) {
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
            } else {
                Toast.makeText(
                    requireContext(),
                    "An error ocurred while getting the camera",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_IMAGE_CAPTURE -> {
                cameraRequest = false
                if (resultCode == RESULT_OK) {
                    addPicToGallery(currentPhotoPath, requireContext())
//                    binding.profileImageRounded2.loadImageRounded(
//                        currentPhotoPath,
//                        getProgressDrawable(requireContext())
//                    )

                }
            }
            REQUEST_STORAGE -> {
                galleryRequest = false
                if (resultCode == RESULT_OK) {
                    try {
                        val imgFromGallery = data?.data
                        if (imgFromGallery != null) {
                            CropImage.imageCropFromFragment(imgFromGallery, requireContext(), this)
                        }
                        currentPhotoPath = data?.data.toString()
//                    binding.profileImageRounded2.loadImageRounded(
//                        data?.data.toString(),
//                        getProgressDrawable(requireContext())
//                    )
                    } catch (e: NullPointerException) {
                        e.cause
                    }
                }

            }
            REQUEST_CROP -> {
                if (resultCode == RESULT_OK) {
                    try {
                        val imgCrop = UCrop.getOutput(data!!)
                        if (imgCrop != null) {
                            viewModel.updateUserImageStorage(imgCrop)
                        }
                        dialog = createCustomDialog(requireContext(), null, null)
                            dialog.show()
                    } catch (e: NullPointerException) {
                        e.cause
                    }
                }
            }
        }
    }

    private fun observeViewModel() {
        viewModel.currentUser.observe(viewLifecycleOwner, {
            userImgUrl = it.imageUrl
            bindUserProfile(it)
        })
        viewModel.uploadProgress.observe(viewLifecycleOwner, {
            val messageDialogTextView = dialog.findViewById<TextView>(R.id.messageDialog)
            messageDialogTextView.text = "Upload progress: " + it.toInt() + "%"
        })
        viewModel.uploadSuccessful.observe(viewLifecycleOwner, {
            if (it) {
                dialog.dismiss()
                Toast.makeText(requireContext(), "Upload successful", Toast.LENGTH_SHORT).show()
            } else {
                dialog.dismiss()
                Toast.makeText(requireContext(), "Upload failed", Toast.LENGTH_SHORT).show()
            }
        })
        viewModel.onFailure.observe(viewLifecycleOwner, {
            if(it){
                Toast.makeText(requireContext(), "Uploading image to DB failed", Toast.LENGTH_SHORT).show()
            }
        })
        viewModel.isDatabaseDeleted.observe(viewLifecycleOwner, {
            if(it) {
                Toast.makeText(requireContext(), "Image was deleted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Failed to delete image", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun bindUserProfile(user: User) {
        binding.imgUser.loadImage(
            user.imageUrl,
            getProgressDrawable(binding.root.context)
        )
        binding.editUserNameProfile.setText(user.userName)
        binding.editStateProfile.setText(user.state)
    }


    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
        val storageDir: File? = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}