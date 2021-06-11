package android.example.com.chatapp.view.adapters

import android.example.com.chatapp.databinding.CardStatusBinding
import android.example.com.chatapp.model.UiContactStatus
import android.example.com.chatapp.util.TimestampConverter
import android.example.com.chatapp.util.getProgressDrawable
import android.example.com.chatapp.util.loadImage
import android.example.com.chatapp.view.HomeFragmentDirections
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView

class StatusListAdapter(private val userList: ArrayList<UiContactStatus>): RecyclerView.Adapter<StatusListAdapter.StatusListViewHolder>() {
    class StatusListViewHolder(var view: CardStatusBinding) : RecyclerView.ViewHolder(view.root) {}

    fun updateUserList(newUserList: ArrayList<UiContactStatus>) {
        userList.clear()
        userList.addAll(newUserList)

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatusListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = CardStatusBinding.inflate(inflater, parent, false)
        return StatusListViewHolder(view)
    }

    override fun onBindViewHolder(holder: StatusListViewHolder, position: Int) {

        holder.view.txtUserNameStatus.text = userList[position].userName

        holder.view.imgUserStatus.loadImage(
            userList[position].imageUrl, getProgressDrawable(holder.view.root.context)
        )
        holder.view.timestampStatus.text =
            TimestampConverter.getTimeStamp(userList[position].timestampLastStatus!!)
        holder.itemView.setOnClickListener {
            Navigation.findNavController(holder.view.root).navigate(HomeFragmentDirections.actionHomeToStatusWatch(
                userList[position].uid!!))
        }
    }


    override fun getItemCount(): Int = userList.size
}