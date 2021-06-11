package android.example.com.chatapp.view.adapters

import android.app.Activity
import android.content.res.Resources
import android.example.com.chatapp.R
import android.example.com.chatapp.databinding.CardChatContactBinding
import android.example.com.chatapp.model.MessageType
import android.example.com.chatapp.model.UiContactChat
import android.example.com.chatapp.util.*
import android.example.com.chatapp.view.HomeFragmentDirections
import android.example.com.chatapp.view.adapters.chat.ChatListViewHolder
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.view.ActionMode
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import java.lang.NullPointerException

class ChatListAdapter (val uiContactList: ArrayList<UiContactChat>,val activity: Activity, val callback: android.view.ActionMode.Callback): RecyclerView.Adapter<ChatListViewHolder>() {

    private var actionMode: android.view.ActionMode? = null
    private val mapActionMode = mutableMapOf<String, ChatListViewHolder>()

    fun updateList(newList: ArrayList<UiContactChat>) {
        uiContactList.clear()
        uiContactList.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = CardChatContactBinding.inflate(inflater)
        return ChatListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatListViewHolder, position: Int) {
        holder.view.txtContactName.text = uiContactList[position].userName
        holder.view.txtTimestampLastMessage.text = TimestampConverter.getTimeStamp(uiContactList[position].timestamp!!)
        holder.view.imgUserChatContact.loadImage(uiContactList[position].imageUrl,
            getProgressDrawable(holder.view.root.context))

        if(uiContactList[position].isSeenByUser!!) {
            holder.view.imgMessageSeen.setImageResource(R.drawable.ic_chat_seen)

        } else {
            holder.view.imgMessageSeen.setImageResource(R.drawable.ic_message_not_seen)
        }
        if(uiContactList[position].isChatSeen!!) {
            holder.view.imgBadgeNewMessage.visibility = View.GONE
        } else {
            holder.view.imgBadgeNewMessage.visibility = View.VISIBLE
        }
        when (uiContactList[position].typeLastMessage!!) {
            MessageType.TYPE_TEXT -> {
                holder.view.imgTypeMessage.visibility = View.GONE
                holder.view.txtLastMessage.text = uiContactList[position].lastMessage
            }
            MessageType.TYPE_GIF -> {
                holder.view.imgTypeMessage.visibility = View.VISIBLE
                holder.view.imgTypeMessage.background =
                    ContextCompat.getDrawable(holder.view.root.context, R.drawable.ic_gif)
            }
            MessageType.TYPE_PHOTO -> {
                if(uiContactList[position].lastMessage!!.isNotEmpty()) {
                    holder.view.txtLastMessage.text = uiContactList[position].lastMessage!!
                } else {
                    holder.view.txtLastMessage.text = "PHOTO"
                }
                holder.view.imgTypeMessage.visibility = View.VISIBLE
                holder.view.imgTypeMessage.background =
                    ContextCompat.getDrawable(holder.view.root.context, R.drawable.ic_photo_gallery_grey)
            }
            MessageType.TYPE_VIDEO -> {
                holder.view.imgTypeMessage.visibility = View.VISIBLE
                holder.view.imgTypeMessage.background =
                    ContextCompat.getDrawable(holder.view.root.context, R.drawable.ic_action_camera)
            }
            MessageType.TYPE_AUDIO -> {
                holder.view.imgTypeMessage.visibility = View.VISIBLE
                holder.view.imgTypeMessage.background =
                    ContextCompat.getDrawable(holder.view.root.context, R.drawable.ic_mic_gray)
            }
            MessageType.TYPE_DOC_PDF -> {
                holder.view.imgTypeMessage.visibility = View.VISIBLE
                holder.view.imgTypeMessage.background =
                    ContextCompat.getDrawable(holder.view.root.context, R.drawable.ic_file_gray)
            }

        }
        holder.itemView.setOnClickListener {
            if(actionMode == null) {
                Navigation.findNavController(holder.view.root)
                    .navigate(HomeFragmentDirections.actionHomeFragmentToChatFragment(uiContactList[position].uid!!))
            } else {
                onItemSelect(uiContactList[position].uid!!, holder, false)
            }
        }
        holder.itemView.setOnLongClickListener {
            onItemSelect(uiContactList[position].uid!!, holder, false)
            return@setOnLongClickListener true
        }

    }

    override fun getItemCount() = uiContactList.size

    fun onItemSelect(contactId: String, holder: ChatListViewHolder, allChatsSelected: Boolean) {
        toggleSelection(contactId, holder, allChatsSelected)
        if(mapActionMode.isNotEmpty() && actionMode == null) {
            try {
                actionMode = activity.startActionMode(callback)
            } catch (e: NullPointerException) {
                e.cause
            }
        } else if (mapActionMode.isEmpty() && actionMode!=null) {
            actionMode!!.finish()
            deselectContacts()

        }
        showNumberSelected()
        showMenuItem()
    }

    private fun showMenuItem() {
        actionMode?.let {
            actionMode!!.menu.getItem(0).isVisible = mapActionMode.size <= 1
        }
    }

    private fun showNumberSelected() {
        actionMode?.let {
            actionMode!!.title = mapActionMode.size.toString()
        }
    }

    fun deselectContacts() {
        for (contact in mapActionMode.keys) {
            val holder = mapActionMode[contact]!!.view.imgSelectedContact
            AnimUtils.scaleView(holder, 150, 0f)
        }
        mapActionMode.clear()
    }

    private fun toggleSelection(
        contactId: String,
        holder: ChatListViewHolder,
        allChatsSelected: Boolean
    ) {
        if(allChatsSelected) {
            AnimUtils.scaleView(holder.view.imgSelectedContact, 150, 1f)
            mapActionMode[contactId] = holder
        } else {
            if(mapActionMode.containsKey(contactId)) {
                AnimUtils.scaleView(holder.view.imgSelectedContact, 150, 0f)
                mapActionMode.remove(contactId)
            } else {
                AnimUtils.scaleView(holder.view.imgSelectedContact, 150, 1f)
                mapActionMode[contactId] = holder
            }
        }
    }

    fun destroyActionMode() {
        actionMode = null
    }


}