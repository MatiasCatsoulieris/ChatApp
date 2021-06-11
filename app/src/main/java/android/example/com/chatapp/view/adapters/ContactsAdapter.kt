package android.example.com.chatapp.view.adapters

import android.example.com.chatapp.R
import android.example.com.chatapp.databinding.CardStateContactBinding
import android.example.com.chatapp.model.User
import android.example.com.chatapp.util.*
import android.example.com.chatapp.view.ContactsFragmentDirections
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.recyclerview.widget.RecyclerView

class ContactsAdapter(val userList: ArrayList<User>, val typeAction: String, val fragment: Fragment) :
    RecyclerView.Adapter<ContactsAdapter.ContactsViewHolder>() {


    class ContactsViewHolder(var binding: CardStateContactBinding) :
        RecyclerView.ViewHolder(binding.root) {
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = DataBindingUtil.inflate<CardStateContactBinding>(
            inflater,
            R.layout.card_state_contact, parent, false
        )
        return ContactsViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactsViewHolder, position: Int) {
        holder.binding.user = userList[position]
        if (userList[position].state != null && userList[position].state!!.isNotEmpty()) {
            holder.binding.txtContactState.text = userList[position].state
        } else {
            holder.binding.txtContactState.visibility = View.GONE
        }
        holder.binding.contactImg.loadImage(
            userList[position].imageUrl,
            getProgressDrawable(holder.binding.root.context)
        )
        when (typeAction) {
            ACTION_CHAT -> {
                holder.itemView.setOnClickListener {
                    Navigation.findNavController(holder.binding.root)
                        .navigate(ContactsFragmentDirections.actionContactToChat(userList[position].userId))
                }
            }
            ACTION_CALL -> {
                holder.binding.btnContactCall.visibility = View.VISIBLE
                holder.binding.btnContactVideo.visibility = View.VISIBLE
            }
            ACTION_SEND_MESSAGE -> {

            }
            ACTION_SEND_PHOTO_MESSAGE -> {
                holder.itemView.setOnClickListener {
                    findNavController(fragment).previousBackStackEntry?.savedStateHandle?.set(
                        UID,
                        userList[position].userId
                    )
                    findNavController(fragment).previousBackStackEntry?.savedStateHandle?.set(
                        USERNAME,
                        userList[position].userName
                    )
                    Navigation.findNavController(holder.binding.root).navigateUp()
                }
            }
        }
    }

    override fun getItemCount(): Int = userList.size

    fun updateList(newList: List<User>) {
        userList.clear()
        userList.addAll(newList)
        notifyDataSetChanged()
    }

    fun addContact(user: User) {
        userList.add(user)
        notifyItemInserted(userList.lastIndex)
    }

}