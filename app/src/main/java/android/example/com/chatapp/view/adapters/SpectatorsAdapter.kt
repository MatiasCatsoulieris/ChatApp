package android.example.com.chatapp.view.adapters

import android.example.com.chatapp.databinding.CardSpectatorBinding
import android.example.com.chatapp.model.Status
import android.example.com.chatapp.model.StatusSpectator
import android.example.com.chatapp.model.User
import android.example.com.chatapp.util.TimestampConverter
import android.example.com.chatapp.util.getProgressDrawable
import android.example.com.chatapp.util.loadImage
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class SpectatorsAdapter(val spectatorsList: ArrayList<StatusSpectator>, val userSpectatorList: ArrayList<User> )
    : RecyclerView.Adapter<SpectatorsAdapter.SpectatorsViewHolder>() {
    class SpectatorsViewHolder (val view: CardSpectatorBinding): RecyclerView.ViewHolder ( view.root)
    {}

    fun updateSpectatorList(newSpectatorList: ArrayList<StatusSpectator>) {
        spectatorsList.clear()
        spectatorsList.addAll(newSpectatorList)
    }

    fun updateUserSpectatorList(newUserSpectatorList: ArrayList<User>) {
        userSpectatorList.clear()
        userSpectatorList.addAll(newUserSpectatorList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpectatorsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = CardSpectatorBinding.inflate(inflater, parent, false)
        return SpectatorsViewHolder(view)
    }

    override fun onBindViewHolder(holder: SpectatorsViewHolder, position: Int) {
        holder.view.spectatorImg.
        loadImage(userSpectatorList[position].imageUrl, getProgressDrawable(holder.view.root.context))
        holder.view.spectatorName.text = userSpectatorList[position].userName
        holder.view.txtSeenStatus.text = TimestampConverter.getTimeStamp(spectatorsList[position].timestamp!!)
    }

    override fun getItemCount(): Int = userSpectatorList.size

}