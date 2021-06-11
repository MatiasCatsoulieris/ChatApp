package android.example.com.chatapp.view

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.example.com.chatapp.databinding.FragmentSpectatorListBinding
import android.example.com.chatapp.model.StatusSpectator
import android.example.com.chatapp.model.User
import android.example.com.chatapp.view.adapters.SpectatorsAdapter
import android.example.com.chatapp.viewModel.SpectatorListViewModel
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI

class SpectatorListFragment : Fragment() {

    ////VIEW BINDING
    private var _binding: FragmentSpectatorListBinding? = null
    private val binding get() = _binding!!
    /////ViewModel
    private lateinit var viewModel: SpectatorListViewModel

    private var statusId : String? = null
    private var spectatorList = mutableListOf<StatusSpectator>()
    private val spectatorAdapter = SpectatorsAdapter(arrayListOf(), arrayListOf())


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSpectatorListBinding.inflate(inflater, container, false  )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(SpectatorListViewModel::class.java)
        NavigationUI.setupWithNavController(binding.toolbarSpectator, findNavController())
        getExtras()
        getSpectators()
        setAdapter()
        observeViewModel()
    }

    private fun setAdapter() {
        binding.recyclerViewSpectator.adapter = spectatorAdapter
    }


    private fun getExtras() {
        arguments?.let {
            statusId = requireArguments().getString("statusId")
        }
    }

    private fun getSpectators() {
//        viewModel.getSpectatorList(statusId!!)
          viewModel.getList(statusId!!)
    }

    private fun observeViewModel() {
        viewModel.spectatorsListLiveData.observe(viewLifecycleOwner, {
            it?.let{
                spectatorAdapter.updateSpectatorList(it as ArrayList<StatusSpectator>)
            }
        })
        viewModel.userSpectatorListLiveData.observe(viewLifecycleOwner, {
            it?.let{
                binding.txtSpectatorNumber.text = "(${it.size})"
                spectatorAdapter.updateUserSpectatorList(it as ArrayList<User>)
            }
        })
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}