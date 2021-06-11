package android.example.com.chatapp.view.status

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.example.com.chatapp.R
import android.example.com.chatapp.databinding.FragmentStatusWatchBinding
import android.example.com.chatapp.model.FbUser
import android.example.com.chatapp.model.Status
import android.example.com.chatapp.util.*
import android.example.com.chatapp.viewModel.StatusWatchViewModel
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import jp.shts.android.storiesprogressview.StoriesProgressView
import java.lang.IndexOutOfBoundsException


class StatusWatchFragment : Fragment(), StoriesProgressView.StoriesListener {

    ////VIEWBINDING
    private var _binding: FragmentStatusWatchBinding? = null
    private val binding get() = _binding!!

    /////VIEWMODEL
    private lateinit var viewModel: StatusWatchViewModel

    private var uid: String? = null
    private var pressTime: Long = 0
    private var limit: Long = 500
    private var counterPosition = 0
    private var statusList = mutableListOf<Status>()
    private lateinit var alertDialog: Dialog


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStatusWatchBinding.inflate(inflater, container, false)
        getFullscreen()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(StatusWatchViewModel::class.java)
        getExtras()
        setListeners()
        checkStatusAuthor()
        observeViewModel()
    }


    private fun getFullscreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requireActivity().window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            requireActivity().window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }

    private fun removeFullscreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requireActivity().window.insetsController?.show(WindowInsets.Type.statusBars())
        } else {
            requireActivity().window.clearFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }

    private fun getExtras() {
        arguments?.let {
            uid = requireArguments().getString("UID")
            getStatusList(uid!!)

        }
    }

    private fun getStatusList(uid: String) {
        viewModel.getUserData(uid)
        viewModel.getStatusList(uid)
    }

    private fun setListeners() {
        binding.btnFinish.setOnClickListener {
            Navigation.findNavController(it).navigateUp()
        }
        binding.buttonNext.setOnTouchListener(onTouchListener)
        binding.buttonNext.setOnClickListener {
            binding.storiesProgress.skip()
        }
        binding.buttonReverse.setOnTouchListener(onTouchListener)
        binding.buttonReverse.setOnClickListener {
            binding.storiesProgress.reverse()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private val onTouchListener = View.OnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                pressTime = System.currentTimeMillis()
                binding.storiesProgress.pause()
                AnimUtils.showView(binding.topBar, 350, 0f)
                AnimUtils.showView(binding.storiesProgress, 350, 0f)
                if (uid == FbUser.getUserId()) {
                    AnimUtils.showView(binding.layoutSpectators, 350, 0f)
                }
                return@OnTouchListener false
            }
            MotionEvent.ACTION_UP -> {

                val now = System.currentTimeMillis()
                binding.storiesProgress.resume()
                AnimUtils.showView(binding.topBar, 150, 1f)
                AnimUtils.showView(binding.storiesProgress, 150, 1f)
                if (uid == FbUser.getUserId()) {
                    AnimUtils.showView(binding.layoutSpectators, 150, 1f)
                }
                return@OnTouchListener limit < now - pressTime
            }
            else -> return@OnTouchListener false
        }
    }

    private fun observeViewModel() {
        viewModel._userBasicLiveData.observe(viewLifecycleOwner, {
            it?.let {
                binding.imgUserStatus.loadImage(it.imageUrl, getProgressDrawable(requireContext()))
                binding.txtUserNameStatus.text = it.userName

            }
        })
        viewModel._statusLiveData.observe(viewLifecycleOwner, {
            it?.let {
                if (it.isNotEmpty()) {
                    statusList = it as MutableList<Status>
                    getStatus(counterPosition)
                    configStoriesProgress(it.size)
                    startStories()
                }
            }
        })
        viewModel._spectatorNumberLiveData.observe(viewLifecycleOwner, {
            it?.let {
                if (it > 0) {
                    Log.e("AAb", "Fragment: $it")
                    binding.layoutSpectators.visibility = View.VISIBLE
                    binding.txtNrSpectatorsStatus.text = it.toString()
                }
            }
        })
        viewModel.deleteCompleted.observe(viewLifecycleOwner, {
            if (it) {
                alertDialog.dismiss()
                Toast.makeText(requireContext(), "Status deleted", Toast.LENGTH_SHORT).show()
                Navigation.findNavController(binding.root).navigateUp()
            }
        })
    }

    private fun startStories() {
        binding.storiesProgress.startStories()
    }

    private fun configStoriesProgress(size: Int) {
        binding.storiesProgress.setStoriesCount(size)
        binding.storiesProgress.setStoryDuration(3000)
        binding.storiesProgress.setStoriesListener(this)
    }

    private fun getStatus(position: Int) {
        try {
            binding.txtTimeStampStatus.text =
                TimestampConverter.getTimeStamp(statusList[position].timeStamp!!)
            Glide.with(requireContext()).load(statusList[position].imgUrl)
                .transition(DrawableTransitionOptions().crossFade()).into(binding.imgStatus)

            if (statusList[position].uid != FbUser.getUserId()) {
                viewModel.addVisit(statusList[position].statusId!!)
            } else {
                viewModel.getSpectators(statusList[position].statusId!!)
//                viewModel.getSpectators(statusList[position].statusId!!)
            }
        } catch (e: IndexOutOfBoundsException) {
            e.cause
        }
    }

    private fun checkStatusAuthor() {
        if (uid == FbUser.getUserId()) {
            binding.btnOptionsStatus.visibility = View.VISIBLE
            binding.btnOptionsStatus.setOnClickListener {
                showStatusSettings()
            }
            binding.layoutSpectators.visibility = View.VISIBLE
            binding.layoutSpectators.setOnClickListener {
                removeFullscreen()
                Navigation.findNavController(it).navigate(
                    StatusWatchFragmentDirections.actionStatusWatchToSpectatorList(statusList[counterPosition].statusId!!)
                )
            }
        } else {
            binding.btnOptionsStatus.visibility = View.GONE
            binding.layoutSpectators.visibility = View.GONE
        }
    }

    private fun showStatusSettings() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val layoutBottomSheet = layoutInflater.inflate(R.layout.layout_status_options, null)
        setBottomSheetListeners(layoutBottomSheet, bottomSheetDialog)
        bottomSheetDialog.setContentView(layoutBottomSheet)
        bottomSheetDialog.show()
        binding.storiesProgress.pause()
    }

    private fun setBottomSheetListeners(bottomLayout: View, bottomDialog: BottomSheetDialog) {
        bottomLayout.findViewById<FloatingActionButton>(R.id.fabDeleteStatus)?.setOnClickListener {
            alertDialog = createCustomDialog(requireContext(), "Deleting status...", null)
            alertDialog.show()
            viewModel.deleteStatus(statusList[counterPosition])
        }
        bottomDialog.setOnCancelListener {
            binding.storiesProgress.resume()
        }
    }

    //////STORIES LISTENER IMPLEMENTATION
    override fun onNext() {

        counterPosition += 1
        getStatus(counterPosition)

    }

    override fun onPrev() {
        if (counterPosition > 0) {
            counterPosition -= 1
            getStatus(counterPosition)
        }
    }

    override fun onComplete() {
        Navigation.findNavController(binding.root).navigateUp()
    }

    /////HANDLE STORIES ON FRAGMENT LIFECYCLE
    override fun onPause() {
        super.onPause()
        binding.storiesProgress.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        removeFullscreen()
        binding.storiesProgress.destroy()
    }

    override fun onResume() {
        super.onResume()
        Handler(Looper.getMainLooper()).postDelayed(object : Runnable {
            override fun run() {
                binding.storiesProgress.resume()
            }

        }, 500)
    }
}