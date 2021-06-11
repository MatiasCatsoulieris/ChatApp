package android.example.com.chatapp.view.pagerFragments

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Intent
import android.example.com.chatapp.R
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.example.com.chatapp.databinding.FragmentCameraBinding
import android.example.com.chatapp.util.*
import android.example.com.chatapp.view.adapters.GalleryAdapter
import android.example.com.chatapp.viewModel.CameraViewModel
import android.net.Uri
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.common.util.concurrent.ListenableFuture
import com.yalantis.ucrop.UCrop
import java.io.File
import java.lang.Exception
import java.lang.NullPointerException


class CameraFragment : Fragment() {

    //ViewBinding
    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    //ViewModel
    private lateinit var viewModel : CameraViewModel
    private var typeAction: String? = null
    private var uidReceiver: String? = null

    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var imageCapture: ImageCapture
    private var lensFacingType = CameraSelector.LENS_FACING_BACK
    private var flashMode = ImageCapture.FLASH_MODE_OFF
    private lateinit var galleryAdapterHorizontal: GalleryAdapter
    private lateinit var galleryAdapterGrid: GalleryAdapter
    lateinit var behavior: BottomSheetBehavior<CoordinatorLayout>
    var collapsed = true
    private lateinit var dialog : Dialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(CameraViewModel::class.java)
        behavior = BottomSheetBehavior.from(binding.includeLayoutCameraGallery.bottomSheetGallery)
        getExtras(savedInstanceState)
        startPreviewCamera()
        setListeners()
        getImages()
        observeViewModel()
        return binding.root
    }



    private fun getExtras(savedInstanceState: Bundle?) {
        try{
            this.typeAction = arguments?.getString(CAMERA_TYPE_ACTION, null)
            if(typeAction == null) {
            this.typeAction = requireArguments().getString("typeAction")
            }
            this.uidReceiver = arguments?.getString("UID", null)
        }catch (e: NullPointerException) {
            e.cause
        }
    }

    private fun setListeners() {
        if(findNavController().previousBackStackEntry?.destination?.id == R.id.profileFragment) {
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
                if (behavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                    behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                } else {
                    Navigation.findNavController(binding.cameraPreview).navigateUp()
                }
            }
        }
        binding.includeLayoutCamera.buttonSwitchFlash.setOnClickListener {
            when(flashMode) {
                ImageCapture.FLASH_MODE_OFF -> {
                    flashMode = ImageCapture.FLASH_MODE_ON
                    AnimUtils.changeSwitchFlashButton(
                        binding.includeLayoutCamera.buttonSwitchFlash, R.drawable.ic_flash_on, requireContext())
                }
                ImageCapture.FLASH_MODE_ON -> {
                    flashMode = ImageCapture.FLASH_MODE_AUTO
                    AnimUtils.changeSwitchFlashButton(
                        binding.includeLayoutCamera.buttonSwitchFlash, R.drawable.ic_flash_auto, requireContext())
                }
                ImageCapture.FLASH_MODE_AUTO -> {
                    flashMode = ImageCapture.FLASH_MODE_OFF
                    AnimUtils.changeSwitchFlashButton(
                        binding.includeLayoutCamera.buttonSwitchFlash, R.drawable.ic_flash_off, requireContext())
                }
            }
        }
        binding.includeLayoutCamera.buttonTakePhoto.setOnClickListener {
            binding.includeLayoutCamera.buttonTakePhoto.isEnabled = false
            AnimUtils.animateBtnTakePhoto(binding.includeLayoutCamera.buttonTakePhoto)
            val file = CameraUtils.getNewFile()
            val outPutFileOptions = ImageCapture.OutputFileOptions.Builder(file).build()
            imageCapture.targetRotation = (CameraUtils.getDisplayRotation(requireContext()))
            imageCapture.flashMode = flashMode
            imageCapture.takePicture(outPutFileOptions, ContextCompat.getMainExecutor(requireContext()),
                object: ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        addPicToGallery(file.toString(), requireContext())
                        binding.includeLayoutCamera.buttonTakePhoto.isEnabled = true
                        when(typeAction) {
                            CAMERA_ACTION_MESSAGE -> {
                                Navigation.findNavController(binding.root)
                                    .navigate(CameraFragmentDirections.actionCameraToPhotoMessage(uidReceiver, file.path))
                            }
                            CAMERA_ACTION_STATE -> {
                                Navigation.findNavController(binding.root)
                                    .navigate(CameraFragmentDirections.actionCameraToImageState(file.path))
                            }
                            CAMERA_ACTION_UPDATE_PICTURE -> {
                                CropImage.imageCropFromFragment(Uri.fromFile(File(file.path)),
                                    requireContext(), this@CameraFragment)
                            }

                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        binding.includeLayoutCamera.buttonTakePhoto.isEnabled = true
                        Toast.makeText(requireContext(), "Error taking picture", Toast.LENGTH_SHORT)
                            .show()
                    }
                } )
        }

        binding.includeLayoutCamera.buttonSwitchCamera.setOnClickListener {
            lensFacingType = if (lensFacingType == CameraSelector.LENS_FACING_BACK) {
                CameraSelector.LENS_FACING_FRONT
            } else {
                CameraSelector.LENS_FACING_BACK
            }
            startPreviewCamera()
            AnimUtils.rotateSwitchCameraButton(binding.includeLayoutCamera.buttonSwitchCamera)
        }

        binding.includeLayoutCameraGallery.buttonHideBottomSheet.setOnClickListener {
            if (behavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                behavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
        behavior.addBottomSheetCallback(object: BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when(newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        galleryAdapterHorizontal.isItemClickable(true)
                        binding.includeLayoutCameraGallery.toolbarGallery.isFocusableInTouchMode = false
                        galleryAdapterGrid.isItemClickable(false)
                        binding.includeLayoutCamera.buttonSwitchCamera.enableButton(true)
                        binding.includeLayoutCamera.buttonSwitchFlash.enableButton(true)
                        binding.includeLayoutCamera.buttonTakePhoto.enableButton(true)
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        collapsed = false
                        galleryAdapterHorizontal.isItemClickable(false)
                        binding.includeLayoutCameraGallery.toolbarGallery.isFocusableInTouchMode = true
                        galleryAdapterGrid.isItemClickable(true)
                        binding.includeLayoutCamera.buttonSwitchCamera.enableButton(false)
                        binding.includeLayoutCamera.buttonSwitchFlash.enableButton(false)
                        binding.includeLayoutCamera.buttonTakePhoto.enableButton(false)
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                //Hide
                binding.includeLayoutCameraGallery.recyclerViewGalleryHorizontal.alpha = 1 - slideOffset
                binding.includeLayoutCamera.layoutCamera.alpha = 1 - slideOffset
                //Show
                binding.includeLayoutCameraGallery.recyclerViewGalleryGrid.alpha = slideOffset
                binding.includeLayoutCameraGallery.toolbarGallery.alpha = slideOffset
            }
        }
        )

    }
    private fun getImages() {
        val listImages : MutableList<String> = CameraUtils.getImagesFromgGallery(requireContext())
        galleryAdapterHorizontal = GalleryAdapter(listImages, R.layout.card_gallery_small, typeAction!!,
        true, uidReceiver, this, "Horizontal")
        galleryAdapterGrid = GalleryAdapter(listImages, R.layout.card_gallery, typeAction!!,
            false, uidReceiver, this, "Grid")

        binding.includeLayoutCameraGallery.recyclerViewGalleryHorizontal.adapter = galleryAdapterHorizontal
        binding.includeLayoutCameraGallery.recyclerViewGalleryGrid.adapter = galleryAdapterGrid
        binding.includeLayoutCameraGallery.textNrViewGalleryPictures.text =
            "${listImages.size} Photos"

    }

    private fun startPreviewCamera() {
        val cameraProviderFuture : ListenableFuture<ProcessCameraProvider> =
            ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                cameraProvider.unbindAll()

                val preview = Preview.Builder().build()
                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(lensFacingType).build()
                imageCapture = ImageCapture.Builder().build()
                cameraProvider.bindToLifecycle(this@CameraFragment, cameraSelector,
                    imageCapture, preview)
                preview.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
            } catch (e: Exception) {
                e.cause
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }


    @SuppressLint("RestrictedApi")
    private fun stopPreviewCamera() {
        if(CameraX.isInitialized()) {
            CameraX.shutdown()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            REQUEST_CROP -> {
                if(resultCode == RESULT_OK){
                    try{
                        val imgCrop: Uri? = UCrop.getOutput(data!!)
                        if(imgCrop != null) {
                            viewModel.updateUserImageStorage(imgCrop)
                            dialog =
                                createCustomDialog(requireContext(), "Updating profile", null)
                                    dialog.show()
                        }
                    }
                    catch (e: NullPointerException) {
                        e.cause
                    }
                }
            }
        }
    }

    private fun observeViewModel() {
        viewModel.uploadProgress.observe(viewLifecycleOwner, {
            if(this::dialog.isInitialized) {
                val messageDialogTextView = dialog.findViewById<TextView>(R.id.messageDialog)
                messageDialogTextView.text = "Upload progress: " + it.toInt() + "%"
            }
        })
        viewModel.uploadSuccessful.observe(viewLifecycleOwner, {
            if(!it){
                if(this::dialog.isInitialized) {
                    dialog.dismiss()
                    Toast.makeText(requireContext(), "Upload failed", Toast.LENGTH_SHORT).show()
                }
            }
        })
        viewModel.updateSuccessful.observe(viewLifecycleOwner, {
            if(it) {
                if(this::dialog.isInitialized) {
                    dialog.dismiss()
                    Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()
                }
                Navigation.findNavController(binding.root).popBackStack()
            }
        })
    }



    override fun onDetach() {
        super.onDetach()
        stopPreviewCamera()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPreviewCamera()
        _binding = null
    }
}