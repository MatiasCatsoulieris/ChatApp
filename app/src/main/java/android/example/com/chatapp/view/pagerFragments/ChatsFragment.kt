package android.example.com.chatapp.view.pagerFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.example.com.chatapp.R
import android.example.com.chatapp.databinding.CardChatContactBinding
import android.example.com.chatapp.databinding.FragmentChatsBinding
import android.example.com.chatapp.model.ContactChat
import android.example.com.chatapp.model.UiContactChat
import android.example.com.chatapp.util.CharsToIconUtil
import android.example.com.chatapp.view.HomeFragment
import android.example.com.chatapp.view.MainActivity
import android.example.com.chatapp.view.adapters.ChatListAdapter
import android.example.com.chatapp.view.adapters.chat.ChatListViewHolder
import android.example.com.chatapp.viewModel.ChatsViewModel
import android.view.*
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.ExperimentalCoroutinesApi


class ChatsFragment : Fragment() {

    //ViewBinding
    private var _binding: FragmentChatsBinding? = null
    private val binding get() = _binding!!

    //ViewModel
    private lateinit var viewModel: ChatsViewModel

    //Adapter
    private lateinit var chatsAdapter: ChatListAdapter
    var chatsUnread = mutableMapOf<String, Boolean>()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(ChatsViewModel::class.java)
        setIconWSTxtInfo()
        setAdapter()
        observeViewModel()
    }

    private fun setIconWSTxtInfo() {
        val charList = ArrayList<String>()
        charList.add("$")
        val iconList = ArrayList<Int>()
        iconList.add(R.drawable.ic_contacts_grey)
        CharsToIconUtil.setIconInTxtView(
            binding.txtNoContactsChat, getString(R.string.txt_info_chats),
            charList, iconList, requireContext()
        )
    }

    private fun setAdapter() {
        chatsAdapter = ChatListAdapter(arrayListOf(), requireActivity(), actionModeCallback)
        binding.chatRecyclerView.adapter = chatsAdapter
    }


    @ExperimentalCoroutinesApi
    private fun observeViewModel() {
        viewModel.liveDataContacts.observe(viewLifecycleOwner, {
            it?.let {
                if (it.isNotEmpty()) {
                    binding.txtNoContactsChat.visibility = View.GONE
                } else {
                    binding.txtNoContactsChat.visibility = View.VISIBLE
                }
                it.forEach { contact ->
                    if (contact.isChatSeen!!) {
                        chatsUnread[contact.uid!!] = true
                    } else {
                        chatsUnread.remove(contact.uid!!)
                    }
                }
                chatsAdapter.updateList(it as ArrayList<UiContactChat>)
                            }
        })
    }

    private val actionModeCallback: ActionMode.Callback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            mode?.menuInflater?.inflate(R.menu.menu_action_mode, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            when(item?.itemId) {
                R.id.actionMenuDelete -> {
                    mode?.finish()
                    return true
                }
                R.id.actionMenuArchive -> {
                    mode?.finish()
                    return true
                }
                R.id.menuSelectAll -> {
                    for (i in 0 until chatsAdapter.itemCount) {
                        val uid = chatsAdapter.uiContactList[i].uid
                        val holder = binding.chatRecyclerView.findViewHolderForAdapterPosition(i)  as ChatListViewHolder
                        chatsAdapter.onItemSelect(uid!! , holder, true)
                    }
                    return true
                }
                else -> return false
            }
        }

        override fun onDestroyActionMode(mode: android.view.ActionMode?) {
            chatsAdapter.destroyActionMode()
            chatsAdapter.deselectContacts()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}