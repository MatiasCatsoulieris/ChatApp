package android.example.com.chatapp.viewModel

import android.example.com.chatapp.model.FirebaseDBService
import android.example.com.chatapp.model.User
import androidx.lifecycle.*
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class UserProfileViewModel : ViewModel() {

    private val userId = MutableLiveData<String>()
    val userLiveData = Transformations.switchMap(userId) {
        FirebaseDBService.getContactInfo(it)
    }

    fun getUserData(uid: String) {
        userId.value = uid
    }

    fun getPhotoListChatQuery(uid: String): Query {
        return FirebaseDBService.getPhotoListChatQuery(uid)
    }
}