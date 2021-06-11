package android.example.com.chatapp.view.pagerFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.example.com.chatapp.R
import android.example.com.chatapp.databinding.FragmentStatesBinding
import android.example.com.chatapp.model.ContactStatus
import android.example.com.chatapp.model.UiContactStatus
import android.example.com.chatapp.model.User
import android.example.com.chatapp.util.CharsToIconUtil
import android.example.com.chatapp.view.adapters.StatusListAdapter
import android.example.com.chatapp.viewModel.StatusViewModel
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.firebase.ui.firestore.FirestoreRecyclerAdapter


class StatesFragment : Fragment() {

    //ViewBinding
    private var _binding: FragmentStatesBinding? = null
    private val binding get() = _binding!!
    //Firestore Adapter
    private lateinit var myAdapter: StatusListAdapter
    //ViewModel
    private lateinit var viewModel: StatusViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStatesBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(StatusViewModel::class.java)
        setAdapter()
        getContactStatus()
        setWSTextWithIcons()
        observeViewModel()
        return binding.root
    }

    private fun setAdapter() {

        myAdapter = StatusListAdapter(arrayListOf())
        binding.recyclerViewStatus.adapter = myAdapter
    }

    private fun observeViewModel() {
        viewModel.uiListLiveData.observe(viewLifecycleOwner, {
            it?.let {
                myAdapter.updateUserList(it as ArrayList<UiContactStatus>)
                if(it.isEmpty()) {
                 binding.txtNoStatus.visibility = View.VISIBLE
                } else {
                    binding.txtNoStatus.visibility = View.GONE
                }
            }
        })
    }

    private fun getContactStatus() {
//        viewModel.getUserStatusListener()
    }

    private fun setWSTextWithIcons() {
        val charsList = ArrayList<String>()
        charsList.add("$")
        charsList.add("%")
        val iconsList = ArrayList<Int>()
        iconsList.add(R.drawable.ic_edit_gray)
        iconsList.add(R.drawable.ic_action_camera)
        CharsToIconUtil.setIconInTxtView(binding.txtNoStatus, getString(R.string.txt_info_status),
        charsList, iconsList, requireContext())

    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}