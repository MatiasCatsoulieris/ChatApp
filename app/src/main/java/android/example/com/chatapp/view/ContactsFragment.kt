package android.example.com.chatapp.view

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.example.com.chatapp.databinding.ContactsFragmentBinding
import android.example.com.chatapp.model.FbUser
import android.example.com.chatapp.model.User
import android.example.com.chatapp.util.EndlessRecyclerOnScrollListener
import android.example.com.chatapp.util.PAGE_SIZE
import android.example.com.chatapp.util.UID
import android.example.com.chatapp.view.adapters.ContactsAdapter
import android.example.com.chatapp.viewModel.ContactsViewModel
import android.util.Log
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ContactsFragment : Fragment() {


    //ViewBinding
    private var _binding: ContactsFragmentBinding? = null
    private val binding get() = _binding!!

    //ViewModel
    private lateinit var viewModel: ContactsViewModel

    //Adapter
    private lateinit var contactAdapter: ContactsAdapter

    private var isLoading = false
    private var isLastPage = false
    private var lastDocument: User? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ContactsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(ContactsViewModel::class.java)
        arguments?.let {
            it.getString("TYPE_ACTION")?.let { typeAction ->
                contactAdapter = ContactsAdapter(arrayListOf(), typeAction, this)
            }
        }
        NavigationUI.setupWithNavController(binding.toolbar, findNavController())
        binding.recyclerViewContacts.adapter = contactAdapter
        setListeners()
        getContactsList(null)
        viewModel.getNumberOfContacts()
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.numberOfContacts.observe(viewLifecycleOwner, {
            it?.let {
                val text = "$it contacts"
                binding.txtContactNumbers.text = text
            }
        })
        viewModel.contactListLiveData.observe(viewLifecycleOwner, {
            it?.let {
                if (it.isNotEmpty()) {
                lastDocument = it.last()
                it.forEach { user ->
                    contactAdapter.addContact(user)
                }
                }
            }
        })
        viewModel.isLastPage.observe(viewLifecycleOwner, {
            it?.let {
                isLoading = false
                isLastPage = it
            }
        })
    }


    private fun setListeners() {
        binding.recyclerViewContacts.addOnScrollListener(object :
            EndlessRecyclerOnScrollListener() {
            override fun onLoadMore() {
                getContactsList(lastDocument!!)
            }

        })
    }

    private fun getContactsList(lastUser: User?) {
        isLoading = true
        viewModel.getContacts(lastUser)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}