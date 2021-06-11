package android.example.com.chatapp.viewModel

import android.example.com.chatapp.model.*
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class StatusWatchViewModel : ViewModel() {



    ///////Gets the number of spectators
    val statusId = MutableLiveData<String>()
    fun getSpectators(StatusId: String) {
        statusId.value = StatusId
    }
    val _spectatorNumberLiveData =
        Transformations.switchMap(statusId) { FirebaseDBService.getSpectatorsNumber(it) }

    /////Gets the user basic data for display when watching status
    val userIdLiveData = MutableLiveData<String>()
    fun getUserData(uid: String) {
        userIdLiveData.value = uid
    }
    val _userBasicLiveData = Transformations.switchMap(userIdLiveData) { FbUser.getUserBasicData(it) }

    /////Gets the list of Status of the selected user
    val idForStatusListLiveData = MutableLiveData<String>()
    fun getStatusList(uid: String) {
        idForStatusListLiveData.value = uid
    }
    val _statusLiveData = Transformations.switchMap(idForStatusListLiveData) { FirebaseDBService.getStatusList(it) }

    ////Adds visit to status watched
    fun addVisit(stateId: String) {
        val statusSpectator = StatusSpectator(FbUser.getUserId(), System.currentTimeMillis())
        FirebaseDBService.addVisit(stateId, statusSpectator)
    }

    ////Deletes selected status
    val deleteCompleted = MutableLiveData<Boolean>()
    fun deleteStatus(status: Status) {
        viewModelScope.launch {
            FirebaseStorage.deleteStatusFromStorage(status).collect {
                if (it) {
                    FirebaseDBService.deleteStatus(status).collect { isDeleted ->
                        deleteCompleted.value = isDeleted
                    }
                }
            }
        }
    }


}