package android.example.com.chatapp.viewModel

import android.example.com.chatapp.model.FbUser
import android.example.com.chatapp.model.FirebaseDBService
import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class HomeViewModel : ViewModel() {


    val _contactNewStatus = Transformations.map(FirebaseDBService.observeNewStatus()) { it }

    private val _contactNewMessage = MutableLiveData<Int>()
    val contactNewMessage: LiveData<Int> get() = _contactNewMessage
    val logOutSuccessful = MutableLiveData<Boolean>()

    fun getNrUnreadMessages() {
        viewModelScope.launch {
            FirebaseDBService.getQueryForChatList().collect { list ->
                _contactNewMessage.value = list.filterNot { contact -> contact.isChatSeen == true }.size
            }
        }
    }

    fun checkNewStatus() {
        FirebaseDBService.updateNotificationNewStatus()
    }

    fun logOut() {
        viewModelScope.launch {
            FbUser.deleteTokenNotification().collect {
                if (it) {
                    FirebaseAuth.getInstance().signOut()
                    logOutSuccessful.value = it
                    } else {
                    logOutSuccessful.value = it
                }
            }
        }

    }

    fun setTokenNotification() {
        FbUser.getInstanceId()
    }


}