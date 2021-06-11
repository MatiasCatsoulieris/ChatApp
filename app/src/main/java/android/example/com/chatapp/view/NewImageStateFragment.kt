package android.example.com.chatapp.view

import android.app.Dialog
import android.example.com.chatapp.R
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.example.com.chatapp.databinding.FragmentNewImageStateBinding
import android.example.com.chatapp.model.Filter
import android.example.com.chatapp.util.*
import android.example.com.chatapp.view.adapters.FilterAdapter
import android.example.com.chatapp.viewModel.NewImageStatesViewModel
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import jp.wasabeef.glide.transformations.BlurTransformation
import jp.wasabeef.glide.transformations.ColorFilterTransformation
import jp.wasabeef.glide.transformations.GrayscaleTransformation
import jp.wasabeef.glide.transformations.gpu.*


class NewImageStateFragment : Fragment(), OnFilterListener {

    //ViewBinding
    private var _binding: FragmentNewImageStateBinding? = null
    private val binding get() = _binding!!

    //ViewModel
    private lateinit var viewModel: NewImageStatesViewModel

    var image: String? = null
    private lateinit var dialog: Dialog
    private lateinit var behavior: BottomSheetBehavior<LinearLayout>
    private lateinit var filterAdapter: FilterAdapter


    //FilterList
    val filterList = arrayListOf(
        Filter(null, "None"),
        Filter(BlurTransformation(10, 3), "Blur"),
        Filter(GrayscaleTransformation(), "Gray"),
        Filter(ColorFilterTransformation(0x50F0000), "Red"),
        Filter(VignetteFilterTransformation(), "Vignette"),
        Filter(InvertFilterTransformation(), "Invert"),
        Filter(PixelationFilterTransformation(), "Pixelation"),
        Filter(SepiaFilterTransformation(), "Sepia"),
        Filter(SketchFilterTransformation(), "Sketch")
    )

    override fun onFilterClicked(filter: com.bumptech.glide.load.Transformation<Bitmap>?) {
        if (filter == null) {
            binding.canvasImageNewState.loadImage(image, getProgressDrawable(requireContext()))
        } else {
            Glide.with(requireContext()).load(image).apply(RequestOptions.bitmapTransform(filter))
                .into(binding.canvasImageNewState)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
        //Sets Fullscreen
    ): View? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requireActivity().window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            requireActivity().window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        _binding = FragmentNewImageStateBinding.inflate(inflater, container, false)
        arguments?.let {
            image = it.getString("ImgUri", null)
            image?.let {
                binding.canvasImageNewState.loadImage(image, getProgressDrawable(requireContext()))
                filterAdapter = FilterAdapter(filterList, image!!, this)
            }
        }
        behavior = BottomSheetBehavior.from(binding.includeFilterLayout.bottomSheetFilters)
        viewModel = ViewModelProvider(this).get(NewImageStatesViewModel::class.java)
//        viewModel.getImgCurrentUser()


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()
        setAdapter()
        observeViewModel()
    }

    private fun setListeners() {
        requireActivity().onBackPressedDispatcher.addCallback {
            Log.e("ZZZ", "saliendo")
            if (behavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                behavior.state = BottomSheetBehavior.STATE_COLLAPSED
            } else if (binding.txtState.visibility == View.VISIBLE) {
                binding.txtState.visibility = View.GONE
            } else {
                Navigation.findNavController(binding.root).navigateUp()
                this.isEnabled = false

            }
        }
        binding.buttonAddText.setOnClickListener {
            binding.txtState.visibility = View.VISIBLE
        }
        binding.buttonChangePaintColor.setOnClickListener {
            binding.canvasImageNewState.setRandomColorPaint()
        }
        binding.buttonEnablePainting.setOnClickListener {
            binding.canvasImageNewState.enablePainting()
        }
        binding.buttonFinish.setOnClickListener {
            requireActivity().onBackPressed()
        }
        binding.fabAddNewImageState.setOnClickListener {
            val uri: Uri? = BitmapUtils.getUriforBitmap(
                BitmapUtils.getBitmapFromView(binding.canvasImageNewState),
                requireContext()
            )
            if (uri != null) {
                dialog =
                    createCustomDialog(requireContext(), null, "Publishing status")
                        dialog.show()
                viewModel.publishStateInStorage(uri)

            }
        }
        binding.includeFilterLayout.imgArrow.setOnClickListener { }
        binding.txtState.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val state = binding.txtState.text?.trim()
                if (state != null && state.isNotEmpty()) {
                    binding.canvasImageNewState.writeTextOnCanvas(state.toString())
                    binding.txtState.setText("")
                    binding.txtState.visibility = View.GONE
                }
            }
            return@setOnEditorActionListener false
        }
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        enableButtons(true)
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        enableButtons(false)
                        binding.canvasImageNewState.disablePainting()
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.includeFilterLayout.imgArrow.rotation = slideOffset * 180
                binding.canvasImageNewState.scaleX = 1f - (0.20 * slideOffset).toFloat()
                binding.canvasImageNewState.scaleY = 1f - (0.20 * slideOffset).toFloat()
                binding.fabAddNewImageState.alpha = 1 - slideOffset
                binding.buttonEnablePainting.alpha = 1 - slideOffset
                binding.buttonChangePaintColor.alpha = 1 - slideOffset
                binding.buttonAddText.alpha = 1 - slideOffset
            }

        })
    }

    private fun enableButtons(enabled: Boolean) {
        binding.fabAddNewImageState.isEnabled = enabled
        binding.buttonEnablePainting.isEnabled = enabled
        binding.buttonAddText.isEnabled = enabled
        binding.buttonChangePaintColor.isEnabled = enabled
    }

    private fun setAdapter() {
        if(this::filterAdapter.isInitialized) {
            binding.includeFilterLayout.recyclerViewFilters.adapter = filterAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.imgUserLiveData.observe(viewLifecycleOwner, {
            it?.let {
                binding.imgUserState.loadImage(it, getProgressDrawable(requireContext()))
            }
        })
        viewModel.uploadProgress.observe(viewLifecycleOwner, {
            it?.let{
                val messageDialogTextView = dialog.findViewById<TextView>(R.id.messageDialog)
                messageDialogTextView.text = "Publishing status: " + it.toInt() + "%"
            }
        })
        viewModel.uploadSuccessful.observe(viewLifecycleOwner, {
            if (!it) {
                dialog.dismiss()
                Toast.makeText(requireContext(), "Upload failed", Toast.LENGTH_SHORT).show()
            }
        })
        viewModel.taskSuccessful.observe(viewLifecycleOwner, {
            if(it) {
                Navigation.findNavController(binding.root).navigateUp()
                dialog.dismiss()
                Toast.makeText(requireContext(), "Your status was published", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}