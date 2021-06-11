package android.example.com.chatapp.viewModel

import android.example.com.chatapp.model.FirebaseDBService
import android.example.com.chatapp.model.FirebaseStorage
import android.net.Uri
import androidx.lifecycle.*
import kotlinx.coroutines.launch

class NewStatesViewModel : ViewModel() {


    val _uploadProgress = Transformations.map(FirebaseStorage.stateUploadProgress) { it }
    val _uploadSuccessful = Transformations.map(FirebaseStorage.stateUploadSuccessful) { it }
    val _taskSuccessful = Transformations.switchMap(FirebaseStorage.stateDownloadUrl) {
        FirebaseDBService.publishStatusInDB(it.toString())

    }

    fun publishStateInStorage(statusUri: Uri) {
        viewModelScope.launch {
            FirebaseStorage.publishStateInStorage(statusUri)
        }
    }


}