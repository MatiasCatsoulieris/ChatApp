package android.example.com.chatapp.viewModel

import android.example.com.chatapp.model.FbUser
import android.example.com.chatapp.model.FirebaseDBService
import android.example.com.chatapp.model.FirebaseStorage
import android.net.Uri
import androidx.lifecycle.*
import kotlinx.coroutines.launch

class NewImageStatesViewModel : ViewModel() {

    val imgUserLiveData = Transformations.map(FbUser.getUserImage()) { it }
    val uploadProgress = Transformations.map(FirebaseStorage.stateUploadProgress) { it }
    val uploadSuccessful = Transformations.map(FirebaseStorage.stateUploadSuccessful) { it }
    val taskSuccessful = Transformations.switchMap(FirebaseStorage.stateDownloadUrl) {
        FirebaseDBService.publishStatusInDB(it.toString())
    }


    fun publishStateInStorage(statusUri: Uri) {
        viewModelScope.launch {
            FirebaseStorage.publishStateInStorage(statusUri)
            }
    }

}