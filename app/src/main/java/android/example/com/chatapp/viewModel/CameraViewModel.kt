package android.example.com.chatapp.viewModel

import android.example.com.chatapp.model.FbUser
import android.example.com.chatapp.model.FirebaseDBService
import android.example.com.chatapp.model.FirebaseStorage
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

class CameraViewModel : ViewModel() {


    val uploadProgress = Transformations.map(FirebaseStorage.uploadProgress) { it }
    val uploadSuccessful = Transformations.map(FirebaseStorage.uploadSuccessful) { it }
    val updateSuccessful = Transformations.switchMap(FirebaseStorage.downloadUrl) {
        val map = HashMap<String, String>()
        map["imageUrl"] = it.toString()
        FirebaseDBService.updateUserImage(map, FbUser.getUserId()!!)
    }


    fun updateUserImageStorage(uriCroppedImage: Uri) {
        FirebaseStorage.updateUserImageStorage(uriCroppedImage, FbUser.getUserId()!!)

    }


}