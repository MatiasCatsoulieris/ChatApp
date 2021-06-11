package android.example.com.chatapp.viewModel

import android.example.com.chatapp.model.FirebaseDBService
import android.example.com.chatapp.model.User
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ContactsViewModel : ViewModel() {

    val numberOfContacts = MutableLiveData<Int>()

    val contactListLiveData = MutableLiveData<List<User>>()
    val isLastPage = MutableLiveData<Boolean>()

    /////Observers
    val lastPageObserver = Observer<Boolean> { lastPage ->
            isLastPage.value = lastPage
        }


    fun getNumberOfContacts() {
        viewModelScope.launch {
            FirebaseDBService.getNumberOfContacts().collect {
                numberOfContacts.value = it
            }
        }
    }

    fun getContacts(lastUser: User?) {
        viewModelScope.launch {
            FirebaseDBService.getContacts(lastUser).collect {
                contactListLiveData.value = it
            }
            FirebaseDBService.isLastPage.observeForever(lastPageObserver)
        }
    }

    override fun onCleared() {
        if(FirebaseDBService.isLastPage.hasActiveObservers()) {
            FirebaseDBService.isLastPage.removeObserver(lastPageObserver)
        }
        super.onCleared()
    }
}