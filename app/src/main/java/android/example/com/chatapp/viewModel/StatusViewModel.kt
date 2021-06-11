package android.example.com.chatapp.viewModel

import android.example.com.chatapp.model.ContactStatus
import android.example.com.chatapp.model.FirebaseDBService
import android.example.com.chatapp.model.UiContactStatus
import android.example.com.chatapp.model.User
import android.util.Log
import androidx.lifecycle.*
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.lang.Exception

class StatusViewModel : ViewModel() {

    @ExperimentalCoroutinesApi
    val _statusListLiveData = liveData<List<ContactStatus>>(Dispatchers.IO) {
        FirebaseDBService.getQuery().collect { list ->
            emit(list)
        }
}
    @ExperimentalCoroutinesApi
    val _userStatusListLiveData = Transformations.switchMap(_statusListLiveData) {
        FirebaseDBService.getListenerUserBasicDataList(it)
    }

    @ExperimentalCoroutinesApi
    val uiListLiveData = MediatorLiveData<List<UiContactStatus>>().apply {
        fun update() {
            val statusLiveData = _statusListLiveData.value ?: return
            val userStatusListLiveData = _userStatusListLiveData.value ?: return

            value = mergeData(statusLiveData, userStatusListLiveData)
        }
        addSource(_statusListLiveData) {
            update()
        }
        addSource(_userStatusListLiveData) {
            update()
        }
    }

    private fun mergeData(contactStatusList: List<ContactStatus>,
                          userStatusList: List<User>): List<UiContactStatus> {
        val uiList = mutableListOf<UiContactStatus>()
        for(contactStatus in contactStatusList) {
            val user = userStatusList.find { it.userId == contactStatus.uid }
            val newUiContact = contactStatus.mergeUser(user!!)
            uiList.add(newUiContact)

        }
        uiList.sortByDescending { it.timestampLastStatus }
        return  uiList
    }
}
