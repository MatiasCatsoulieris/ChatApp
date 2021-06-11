package android.example.com.chatapp.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class RecoverPasswordViewModel: ViewModel() {

    val recoverTask = MutableLiveData<Boolean>()

    fun recoverPassword(email: String) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener {
            recoverTask.value = it.isSuccessful
            }
        }

}