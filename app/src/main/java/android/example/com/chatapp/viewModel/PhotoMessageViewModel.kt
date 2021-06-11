package android.example.com.chatapp.viewModel

import android.example.com.chatapp.model.FirebaseDBService
import android.example.com.chatapp.model.FirebaseStorage
import android.example.com.chatapp.model.Message
import android.example.com.chatapp.model.MessageType
import android.net.Uri
import androidx.lifecycle.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class PhotoMessageViewModel: ViewModel() {

    private val receiverUid = MutableLiveData<String>()
    val isMessageSent = MutableLiveData<Boolean>()

    fun getReceiverData(uid: String) {
        receiverUid.value = uid
    }
    //////////Send message to user
    fun addUserAndSendMessage(uidReceiver: String, message: Message) {
        viewModelScope.launch {
            FirebaseDBService.addUserToContacts(uidReceiver, message).collect {
                if (it) {
                    FirebaseDBService.sendMessage(uidReceiver, message).collect { isSent ->
                        isMessageSent.value = isSent
                    }
                } else {
                    isMessageSent.value = false
                }
            }
        }
    }

    ////////////////// Sending file message
    val fileUploadProgress = Transformations.map(FirebaseStorage.docFileUploadProgress) { it }
    val fileUploadSuccessful = Transformations.map(FirebaseStorage.docFileUploadSuccessful) { it }


    private val downloadUrlObserver = Observer<Message>() {
        addUserAndSendMessage(receiverUid.value!!,it)

    }

    fun sendFileMessage(uriData: Uri, uidReceiver: String, message: String, mediaFile: String,
                        extensionFile: String, messageType: MessageType
    ) {
        FirebaseStorage.sendFileMessage(uidReceiver, uriData, message, mediaFile, extensionFile, messageType)
        FirebaseStorage.docFileMessage.observeForever(downloadUrlObserver)
    }
}