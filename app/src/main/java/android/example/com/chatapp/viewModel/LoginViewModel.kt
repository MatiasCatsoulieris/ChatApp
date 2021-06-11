package android.example.com.chatapp.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginViewModel : ViewModel() {

    private val authorization = FirebaseAuth.getInstance()
    val login = MutableLiveData<Boolean>()
    val error = MutableLiveData<String>()

    fun customLogin(mail: String, password: String) {
        authorization.signInWithEmailAndPassword(mail, password).addOnCompleteListener {
            if (it.isSuccessful) {
                login.value = true
            } else {
                login.value = false
                error.value = it.exception?.message
            }
        }
    }
    fun checkSession(): FirebaseUser? {
        return authorization.currentUser
    }
}