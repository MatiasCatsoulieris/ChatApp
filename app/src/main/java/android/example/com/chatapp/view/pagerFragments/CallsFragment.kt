package android.example.com.chatapp.view.pagerFragments

import android.example.com.chatapp.R
import android.example.com.chatapp.databinding.FragmentCallsBinding
import android.example.com.chatapp.util.CharsToIconUtil
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.example.com.chatapp.view.adapters.UsersAdapter
import android.example.com.chatapp.viewModel.CallsViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import java.util.ArrayList


class CallsFragment : Fragment() {

    //ViewBinding
    private var _binding: FragmentCallsBinding? = null
    private val binding get() = _binding!!

    //ViewModel
    private lateinit var viewmodel: CallsViewModel

    private var myAdapter = UsersAdapter(arrayListOf())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCallsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewmodel = ViewModelProvider(this).get(CallsViewModel::class.java)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = myAdapter
        }
        setIcon()
        viewmodel.refresh()
        observeViewModel()
    }

    private fun setIcon() {
        val listChars = arrayListOf("$")
        val listIcons = arrayListOf(R.drawable.ic_call_white)
        CharsToIconUtil.setIconInTxtView(binding.textInfoCalls, getString(R.string.txt_info_call)
        , listChars, listIcons, requireContext())
    }

    private fun observeViewModel() {
        viewmodel.usersData.observe(viewLifecycleOwner, {
            if (it != null) {
                myAdapter.updateList(it)
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}