package android.example.com.chatapp.view

import android.animation.ObjectAnimator
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.example.com.chatapp.R
import android.example.com.chatapp.databinding.HomeFragmentBinding
import android.example.com.chatapp.util.CAMERA_ACTION_MESSAGE
import android.example.com.chatapp.util.AnimUtils
import android.example.com.chatapp.util.CAMERA_ACTION_STATE
import android.example.com.chatapp.util.CAMERA_TYPE_ACTION
import android.example.com.chatapp.view.adapters.MyPagerAdapter
import android.example.com.chatapp.view.pagerFragments.ChatsFragment
import android.example.com.chatapp.view.pagerFragments.StatesFragment
import android.example.com.chatapp.view.pagerFragments.CallsFragment
import android.example.com.chatapp.view.pagerFragments.CameraFragment
import android.example.com.chatapp.viewModel.HomeViewModel
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.FragmentManager
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.lang.NullPointerException

class HomeFragment : Fragment() {

    //ViewBinding
    private var _binding: HomeFragmentBinding? = null
    private val binding get() = _binding!!

    //ViewModel
    private lateinit var viewModel: HomeViewModel

    private val fragmentList =
        arrayListOf(Fragment(), ChatsFragment(), StatesFragment(), CallsFragment())
    private lateinit var fragmentAdapter: MyPagerAdapter
    private var searchViewVisibility = false
    private var isCameraOpened = false
    private var lastTabSelected: Int? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = HomeFragmentBinding.inflate(inflater, container, false)
        hideCameraLayout(binding.frameCamera)
        setHasOptionsMenu(true)
        setViewPager()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        NavigationUI.setupWithNavController(
            binding.collapsingToolBarHome,
            binding.toolbarHome,
            findNavController()
        )
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        setListeners()
        binding.toolbarHome.inflateMenu(R.menu.menu)
        viewModel.setTokenNotification()
        viewModel.getNrUnreadMessages()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()

        lastTabSelected?.let {
            if(it == 0) {
                isCameraOpened = showCameraFragment()
                /////For enabling camera (useless animator)
                val objectAnimatorSet = ObjectAnimator.ofFloat(binding.frameCamera,"x", 0f)
                objectAnimatorSet.start()

            } else {
                removeCameraFragment()
            }
            setFabIcon(it)
            hideMenuItems(it, binding.toolbarHome.menu)
            AnimUtils.showFabAddState(
                it,
                binding.floatingEditStateMain,
                requireContext()
            )

        }
    }


    private fun setListeners() {
        if (binding.tablayout.selectedTabPosition == 1) {
            binding.toolbarHome.navigationIcon = null
        }
        ///////////////BackPressedListener
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (searchViewVisibility) {
                searchViewVisibility = false
                AnimUtils.hideSearchView(binding.searchViewMain, binding.appBarLayoutMain)
            } else {
                if (binding.tablayout.selectedTabPosition == 1) {
                    if (isEnabled) {
                        isEnabled = false
                        requireActivity().finish()
                    }
                } else {
                    if (binding.tablayout.selectedTabPosition == 0) {
                        hideBottomSheet()

                    } else {
                        binding.pager.currentItem = 1
                    }
                }
            }
        }
        ////////// PAGER  SCROLL LISTENER
        binding.pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                if (position == 0) {
                    binding.floatingMain.translationX = -
                    (1 - positionOffset) * (2 - binding.floatingMain.height * 1.5f)
                    binding.floatingEditStateMain.translationX = -
                    (1 - positionOffset) * (3 - binding.floatingMain.height * 1.5f)
                    binding.appBarLayoutMain.translationY =
                        (1 - positionOffset) * -binding.appBarLayoutMain.height
                    binding.frameCamera.translationX = (-binding.frameCamera.width) * positionOffset
                    if (!isCameraOpened) {
                        isCameraOpened = showCameraFragment()
                    }
                } else {
                    if (isCameraOpened) {
                        removeCameraFragment()
                    }
                }
            }
        })

        binding.tablayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                Log.e("DDD", binding.pager.currentItem.toString())
                val tabPosition = tab!!.position
                lastTabSelected = tabPosition
                AnimUtils.showFabAddState(
                    tabPosition,
                    binding.floatingEditStateMain,
                    requireContext()
                )
                setFabIcon(tabPosition)
                hideMenuItems(tabPosition, binding.toolbarHome.menu)
                if (searchViewVisibility) {
                    searchViewVisibility = false
                    AnimUtils.hideSearchView(binding.searchViewMain, binding.appBarLayoutMain)
                }
                when (tabPosition) {
                    0 -> {
                    }
                    1 -> {
                        AnimUtils.showFabAddState(
                            tabPosition,
                            binding.floatingEditStateMain,
                            requireContext()
                        )
                    }
                    2 -> {
                        viewModel.checkNewStatus()
                    }
                }
                if (tabPosition == 0) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        activity!!.window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                    }, 200)
                } else {
                    activity!!.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        })
        binding.floatingMain.setOnClickListener {
            when (binding.tablayout.selectedTabPosition) {
                1 -> {
                    Navigation.findNavController(it)
                        .navigate(HomeFragmentDirections.actionHomeToContacts("CHAT"))

                }
                2 -> {
                    Navigation.findNavController(it)
                        .navigate(
                            HomeFragmentDirections.actionHomeToCamera(
                                null,
                                CAMERA_ACTION_STATE
                            )
                        )

                }
                3 -> {
                    Navigation.findNavController(it)
                        .navigate(HomeFragmentDirections.actionHomeToContacts("CALL"))

                }
            }
        }
        binding.floatingEditStateMain.setOnClickListener {
            when (binding.tablayout.selectedTabPosition) {
                2 -> Navigation.findNavController(binding.root)
                    .navigate(HomeFragmentDirections.actionHomeToState())
                3 -> Toast.makeText(context, "Create video", Toast.LENGTH_SHORT).show()
            }
        }
        binding.toolbarHome.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menuSearch -> {
                    searchViewVisibility = true
                    binding.appBarLayoutMain.setExpanded(false, true)
                    AnimUtils.showSearchView(binding.searchViewMain)
                    return@setOnMenuItemClickListener false
                }
                R.id.itemNewGroup -> {
                    Toast.makeText(requireContext(), "New group", Toast.LENGTH_SHORT).show()
                    return@setOnMenuItemClickListener false
                }
                R.id.itemSettings -> {
                    Navigation.findNavController(binding.root)
                        .navigate(HomeFragmentDirections.actionHomeToSettings())
                    return@setOnMenuItemClickListener false
                }
                R.id.itemLogout -> {
                    viewModel.logOut()

                    return@setOnMenuItemClickListener false
                }
                else -> {
                    return@setOnMenuItemClickListener false
                }
            }
        }
    }

    private fun setBadgeForNewMessages(nrNewChats: Int) {
        try {
            val chatBadge = binding.tablayout.getTabAt(1)?.orCreateBadge
            chatBadge?.isVisible = nrNewChats != 0
            chatBadge?.number = nrNewChats

        } catch (e: NullPointerException) {
            e.message
        }
    }

    private fun observeViewModel() {
        viewModel._contactNewStatus.observe(viewLifecycleOwner, {
            it?.let {
                binding.tablayout.getTabAt(2)?.orCreateBadge?.isVisible = it
            }
        })
        viewModel.contactNewMessage.observe(viewLifecycleOwner, {
            setBadgeForNewMessages(it)
        })
        viewModel.logOutSuccessful.observe(viewLifecycleOwner, {
            if(it) {
                Navigation.findNavController(binding.root)
                    .navigate(HomeFragmentDirections.actionHomeToLogin())
            }
        })
    }

    private fun hideBottomSheet() {
        val cameraFragment =
            requireActivity().supportFragmentManager.findFragmentById(R.id.frameCamera) as CameraFragment
        if (cameraFragment.behavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
            binding.pager.currentItem = 1
        } else {
            cameraFragment.behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }


    private fun hideCameraLayout(cameraLayout: FrameLayout) {
        cameraLayout.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                cameraLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                cameraLayout.translationX = (-cameraLayout.width * 1.0f)
            }
        })
    }

    private fun hideMenuItems(position: Int, menu: Menu) {
        when (position) {
            1 -> {
                setMenuItemVisibility(R.id.itemNewGroup, true, menu)
            }
            2 -> {
                setMenuItemVisibility(R.id.itemNewGroup, false, menu)
            }
            3 -> {
                setMenuItemVisibility(R.id.itemNewGroup, false, menu)
            }
        }
    }

    private fun setMenuItemVisibility(menuItemId: Int, visible: Boolean, menu: Menu) {
        val menuItem = menu.findItem(menuItemId)
        menuItem.isVisible = visible
    }

    private fun setViewPager() {
        fragmentAdapter = MyPagerAdapter(fragmentList, requireActivity())
        binding.pager.adapter = fragmentAdapter
        TabLayoutMediator(binding.tablayout, binding.pager) { tab, position ->
            tab.text = when (position) {
                0 -> ""
                1 -> "Chats"
                2 -> "States"
                3 -> "Calls"
                else -> ""
            }
            if (position == 0) {
                tab.setIcon(R.drawable.ic_action_camera)
            }

        }.attach()

        binding.pager.setCurrentItem(1, false)
        binding.pager.offscreenPageLimit = fragmentList.size
        /////Sets camera tab width to Wrap Content
        val layout = (binding.tablayout.getChildAt(0) as LinearLayout).getChildAt(0)
        val layoutParams = layout.layoutParams as LinearLayout.LayoutParams
        layoutParams.weight = 0f
        layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
        layout.layoutParams = layoutParams

    }

    ////Changes Floating Action Button Icons depending on selected tab
    private fun setFabIcon(position: Int) {
        when (position) {
            1 -> binding.floatingMain.setImageResource(R.drawable.ic_contacts)
            2 -> {
                binding.floatingMain.setImageResource(R.drawable.ic_action_camera)
                binding.floatingEditStateMain.setImageResource(R.drawable.ic_edit)
            }
            3 -> {
                binding.floatingMain.setImageResource(R.drawable.ic_call_white)
                binding.floatingEditStateMain.setImageResource(R.drawable.ic_video)
            }
        }
    }


    /////////CAMERA TAB
    private fun removeCameraFragment() {
        val fragmentManager = activity?.supportFragmentManager
        if (fragmentManager!!.findFragmentById(R.id.frameCamera) != null) {
            fragmentManager.popBackStack(
                CameraFragment::class.java.simpleName,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
            isCameraOpened = false
        }
    }

    private fun showCameraFragment(): Boolean {
        val fragmentManager = activity?.supportFragmentManager
        if (fragmentManager?.findFragmentByTag(CameraFragment::class.java.simpleName) == null) {
            val bundle = Bundle()
            bundle.putString(CAMERA_TYPE_ACTION, CAMERA_ACTION_MESSAGE)
            val cameraFragment = CameraFragment()
            cameraFragment.arguments = bundle

            fragmentManager!!.beginTransaction()
                .add(R.id.frameCamera, cameraFragment)
                .addToBackStack(CameraFragment::class.java.simpleName)
                .commit()
        }

        return true
    }

    override fun onPause() {
        super.onPause()
        removeCameraFragment()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}