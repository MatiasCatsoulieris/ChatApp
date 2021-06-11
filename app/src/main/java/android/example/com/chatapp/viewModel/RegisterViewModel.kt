package android.example.com.chatapp.viewModel
import android.example.com.chatapp.model.FirebaseDBService
import android.example.com.chatapp.model.User
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch
import java.util.*

class RegisterViewModel : ViewModel() {

    val loginResult = MutableLiveData<Boolean>()
    val register = MutableLiveData<Boolean>()
    val error = MutableLiveData<String>()
    //Firebase Auth
    private var authorization: FirebaseAuth = FirebaseAuth.getInstance()


    fun customRegister (username: String, email: String, password: String) {
        authorization.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    register.value = true
                    val firebaseUser: FirebaseUser = authorization.currentUser!!
                    val userId = firebaseUser.uid
                    val timeStamp = Date(firebaseUser.metadata.creationTimestamp)
                    val appUser = User(username, userId, null, null, timeStamp)
                    postUser(appUser)
                } else {
                    register.value = false
                    val message = it.exception!!.message!!
                    error.value = message
                }
            }
    }

    private fun postUser(user: User) {
        viewModelScope.launch {
            loginResult.value = FirebaseDBService.postNewUser(user)
        }
    }
}