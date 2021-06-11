package android.example.com.chatapp.viewModel

import android.example.com.chatapp.model.FbUser
import android.example.com.chatapp.model.FirebaseDBService
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

class SettingsViewModel: ViewModel() {

    val currentUser = Transformations.map(FirebaseDBService.fetchUserData(FbUser.getUserId()!!)) { it }
}