package android.example.com.chatapp.view

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.example.com.chatapp.R
import android.example.com.chatapp.databinding.FragmentNewStateBinding
import android.example.com.chatapp.util.BitmapUtils
import android.example.com.chatapp.util.createCustomDialog
import android.example.com.chatapp.viewModel.NewStatesViewModel
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import java.util.*


class NewStateFragment : Fragment() {

    //ViewBinding
    private var _binding: FragmentNewStateBinding? = null
    private val binding get() = _binding!!
    //viewModel
    private lateinit var viewModel: NewStatesViewModel

    private var positionTypeface = 1
    private lateinit var dialog: Dialog

    //TextWatcher
    private val textWatcher = object: TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val state = binding.stateText.text?.trim()
            state?.let {
                if (state.isNotEmpty()) {
                    binding.fabAddNewState.show()
                } else {
                    binding.fabAddNewState.hide()
                }
            }
        }

        override fun afterTextChanged(s: Editable?) {

        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNewStateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(NewStatesViewModel::class.java)
        setListeners()
        observeViewModel()
        binding.fabAddNewState.hide()
    }


    private fun setListeners() {
        binding.stateText.addTextChangedListener(textWatcher)
        binding.fabAddNewState.setOnClickListener {
            binding.canvasState.writeTextOnCanvas(binding.stateText.text.toString())
            binding.stateText.setText("")
            binding.stateText.hint = ""

            publishNewState()
        }
        binding.buttonChangeBackGround.setOnClickListener {
            binding.canvasState.setBackgroundColor(getRandomColor())
        }
        binding.buttonChangeFont.setOnClickListener {
            val typeface = getTypeFace()
            binding.buttonChangeFont.typeface = typeface
            binding.stateText.typeface = typeface
            binding.canvasState.changeTypeFace(typeface)

            if(positionTypeface == 2) {
                positionTypeface = 0
            } else {
                positionTypeface++
            }
        }
    }

    private fun publishNewState() {
        val uriState : Uri? = BitmapUtils.getUriforBitmap(
            BitmapUtils.getBitmapFromView(binding.canvasState),binding.root.context)
        if (uriState != null) {
            dialog = createCustomDialog(requireContext(), "Publishing status", null)
            dialog.show()
            viewModel.publishStateInStorage(uriState)
        }
    }
    private fun observeViewModel() {
        viewModel._uploadProgress.observe(viewLifecycleOwner, {
            it?.let{
                val messageDialogTextView = dialog.findViewById<TextView>(R.id.messageDialog)
                messageDialogTextView.text = "Publishing status: " + it.toInt() + "%"


            }
        })
        viewModel._uploadSuccessful.observe(viewLifecycleOwner, {
            if (!it) {

    dialog.dismiss()
                Toast.makeText(requireContext(), "Upload failed", Toast.LENGTH_SHORT).show()
            }
        })
        viewModel._taskSuccessful.observe(viewLifecycleOwner, {
            if(it) {
                Navigation.findNavController(binding.root).navigateUp()
                dialog.dismiss()
                Toast.makeText(requireContext(), "Your status was published", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }


    private fun getRandomColor(): Int {
        val random = Random()
        return Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256))
    }

    private fun getTypeFace() : Typeface {
        return when (positionTypeface) {
            1 -> ResourcesCompat.getFont(binding.root.context, R.font.jacksilver)!!
            2 -> ResourcesCompat.getFont(binding.root.context, R.font.retrowmentho)!!
            else -> Typeface.SANS_SERIF
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}