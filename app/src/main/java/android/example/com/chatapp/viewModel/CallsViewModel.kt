package android.example.com.chatapp.viewModel

import android.app.Application
import android.example.com.chatapp.model.FirebaseDBService
import android.example.com.chatapp.model.User
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase


class CallsViewModel(application: Application) : AndroidViewModel(application) {

    private val _usersData = MutableLiveData<List<User>>()
    val usersData: LiveData<List<User>> get() = _usersData

    val usersDataObserver = Observer<List<User>> {
        _usersData.value = it
    }

    fun refresh(){
        readUsers()
    }

    private fun readUsers() {
        FirebaseDBService.retrieveUsers().observeForever(usersDataObserver)
    }

    override fun onCleared() {
        if(FirebaseDBService.retrieveUsers().hasActiveObservers()) {
            FirebaseDBService.retrieveUsers().removeObserver(usersDataObserver)

        }
        super.onCleared()

    }
}