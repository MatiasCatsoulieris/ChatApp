package android.example.com.chatapp.view.adapters

import android.example.com.chatapp.R
import android.example.com.chatapp.databinding.ChatItemLeftBinding
import android.example.com.chatapp.databinding.ChatItemRightBinding
import android.example.com.chatapp.model.Message
import android.example.com.chatapp.util.getProgressDrawable
import android.example.com.chatapp.util.loadImage
import android.example.com.chatapp.util.loadImageRounded
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

class MessagesAdapter(val chatList: ArrayList<Message>) : RecyclerView.Adapter<MessagesAdapter.BaseViewHolder>() {


    val MSG_TYPE_LEFT = 0
    val MSG_TYPE_RIGHT = 1

    abstract class BaseViewHolder(view: View)
        :  RecyclerView.ViewHolder(view)

    class LeftViewHolder(view: ChatItemLeftBinding) : BaseViewHolder(view.root) {

    }
    class RightViewHolder(view: ChatItemRightBinding) : BaseViewHolder(view.root){}

    fun updateChat(newList: List<Message>) {
        chatList.clear()
        chatList.addAll(newList)
        notifyDataSetChanged()
    }

    fun addMessage( message: Message) {
        chatList.add(message)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if(viewType == MSG_TYPE_RIGHT) {
            val view = ChatItemRightBinding.inflate(inflater,
                parent, false)
            RightViewHolder(view)
        } else {
            val view = ChatItemLeftBinding.inflate(inflater,
                parent, false)
            LeftViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        if (holder == LeftViewHolder::class.java) {
            holder.itemView.findViewById<TextView>(R.id.messageTextView).text = chatList[position].message
            holder.itemView.findViewById<ImageView>(R.id.profile_image)
                .loadImageRounded(null, getProgressDrawable(holder.itemView.context))
        } else {
            holder.itemView.findViewById<TextView>(R.id.messageTextView).text = chatList[position].message
            holder.itemView.findViewById<ImageView>(R.id.profile_image)
                .loadImageRounded(null, getProgressDrawable(holder.itemView.context))
        }



    }


    override fun getItemCount(): Int = chatList.size

    override fun getItemViewType(position: Int): Int {



        return MSG_TYPE_LEFT
    }


}