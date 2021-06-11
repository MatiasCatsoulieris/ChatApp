package android.example.com.chatapp.viewModel

import android.example.com.chatapp.model.FbUser
import android.example.com.chatapp.model.FirebaseDBService
import android.example.com.chatapp.model.FirebaseStorage
import android.net.Uri
import androidx.lifecycle.*
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    val currentUser =
        Transformations.map(FirebaseDBService.fetchUserData(FbUser.getUserId()!!)) { it }
    val uploadProgress = Transformations.map(FirebaseStorage.uploadProgress) { it }
    val uploadSuccessful = Transformations.map(FirebaseStorage.uploadSuccessful) { it }
    val onFailure = Transformations.switchMap(FirebaseStorage.downloadUrl) {
        val map = HashMap<String, String>()
        map["imageUrl"] = it.toString()
        FirebaseDBService.updateUserImage(map, FbUser.getUserId()!!)
    }
    private val deleteSuccessful = MutableLiveData<Boolean>()
    val isDatabaseDeleted = MutableLiveData<Boolean>()

    fun updateUserData(key: String, value: String) {
        val map = HashMap<String, String>()
        map[key] = value
        FirebaseDBService.updateUser(map, FbUser.getUserId()!!)
    }

    fun updateUserImageStorage(uriCroppedImage: Uri) {
        FirebaseStorage.updateUserImageStorage(uriCroppedImage, FbUser.getUserId()!!)

    }


    fun deleteImgFromStorage(imgUrl: String) {
        viewModelScope.launch {
            val isSuccessful = FirebaseStorage.deleteImage(imgUrl)
            if (isSuccessful) {
                isDatabaseDeleted.value =
                    FirebaseDBService.deleteImageFromDatabase(FbUser.getUserId()!!)
            }
            deleteSuccessful.value = isSuccessful

        }


    }
}