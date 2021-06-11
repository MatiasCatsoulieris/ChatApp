package android.example.com.chatapp.viewModel

import android.example.com.chatapp.model.*
import androidx.lifecycle.*

class SpectatorListViewModel : ViewModel() {



    private val statusIdLiveData = MutableLiveData<String>()


    fun getList(statusId: String) {
        statusIdLiveData.value = statusId
    }
    val spectatorsListLiveData =
        Transformations.switchMap(statusIdLiveData) { FirebaseDBService.getSpectatorsList(it) }

    val userSpectatorListLiveData =
        Transformations.switchMap(spectatorsListLiveData) { FirebaseDBService.getListenerSpectatorBasicDataList(it) }


}

