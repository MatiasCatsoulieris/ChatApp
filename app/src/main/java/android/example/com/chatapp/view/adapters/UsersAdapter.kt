package android.example.com.chatapp.view.adapters

import android.example.com.chatapp.R
import android.example.com.chatapp.databinding.ItemUserBinding
import android.example.com.chatapp.model.User
import android.example.com.chatapp.util.getProgressDrawable
import android.example.com.chatapp.util.loadImage
import android.example.com.chatapp.view.HomeFragmentDirections
import android.example.com.chatapp.view.OnUserClickListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView

class UsersAdapter(val usersList: ArrayList<User>) : RecyclerView.Adapter<UsersAdapter.UsersViewHolder>(),
    OnUserClickListener {

    class UsersViewHolder(var view : ItemUserBinding) : RecyclerView.ViewHolder(view.root) {

    }
    fun updateList(newList: List<User>) {
        usersList.clear()
        usersList.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = ItemUserBinding.inflate(inflater, parent, false)
        return UsersViewHolder(view)
    }

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        holder.view.user = usersList[position]
        holder.view.userImage.loadImage(usersList[position].imageUrl, getProgressDrawable(holder.view.root.context))
        holder.view.listener = this

    }

    override fun getItemCount(): Int = usersList.size

    override fun onUserClicked(view: View) {
        val imageUrl = view.findViewById<TextView>(R.id.imageUrl).text.toString()
        val id = view.findViewById<TextView>(R.id.userId).text.toString()
        val userName = view.findViewById<TextView>(R.id.usernameTextView).text.toString()

        super.onUserClicked(view)

    }

}