package android.example.com.chatapp.viewModel

import android.example.com.chatapp.model.*
import android.net.Uri
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.firebase.ui.firestore.ClassSnapshotParser
import com.firebase.ui.firestore.FirestoreArray
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    val receiverUid = MutableLiveData<String>()
    val receiverLiveData: LiveData<User> get() = _receiverLiveData
    val isMessageSent = MutableLiveData<Boolean>()
    private lateinit var query : Query
    lateinit var array: FirestoreArray<Message>

    fun getReceiverData(uid: String) {
        receiverUid.value = uid
        initializeQuery()
    }

    private val _receiverLiveData = Transformations.switchMap(receiverUid) { FbUser.getUserBasicData(it) }

    //////////Query for the Firestore RecyclerView Adapter
    private fun initializeQuery() {
        query = FirebaseDBService.getQueryForMessages(receiverUid.value!!)
        array = FirestoreArray(query, ClassSnapshotParser(Message::class.java))
    }

    //////////checkChatAsSeen
    fun checkChatAsSeen(uidReceiver: String) {
        FirebaseDBService.setChatAsSeen(uidReceiver)
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
    val fileUploadProgress = MutableLiveData<Double>()
    val fileUploadSuccessful = MutableLiveData<Boolean>()
    private val upLoadProgressObserver = Observer<Double>() {
        it?.let {
            fileUploadProgress.value = it
        }
    }
    private val uploadSuccessfulObserver = Observer<Boolean> {
        fileUploadSuccessful.value = it
        FirebaseStorage.docFileUploadProgress.removeObserver(upLoadProgressObserver)

    }
    private val downloadUrlObserver = Observer<Message>() {
        FirebaseStorage.docFileUploadSuccessful.removeObserver(uploadSuccessfulObserver)
        addUserAndSendMessage(receiverUid.value!!,it)
    }

    fun sendFileMessage(uriData: Uri, uidReceiver: String, message: String, mediaFile: String,
     extensionFile: String, messageType: MessageType) {
        FirebaseStorage.sendFileMessage(uidReceiver, uriData, message, mediaFile, extensionFile, messageType)
        FirebaseStorage.docFileUploadProgress.observeForever(upLoadProgressObserver)
        FirebaseStorage.docFileUploadSuccessful.observeForever(uploadSuccessfulObserver)
        FirebaseStorage.docFileMessage.observeForever(downloadUrlObserver)
    }

    /////////Send thumbnail and video
    fun sendThumbnailAndVideoToMedia(uirReceiver: String, videoUri: Uri, thumbnailUri: Uri) {
        FirebaseStorage.sendThumbnailToMedia(uirReceiver,videoUri,thumbnailUri)
    }
    val videoUploadProgress = Transformations.map(FirebaseStorage.videoUploadProgress) { it }

    val isVideoSent = MediatorLiveData<Boolean>().apply{
        fun update() {
            val isThumbnailUploaded = FirebaseStorage.uploadThumbnailSuccessful.value ?: return
            val thumbnailUrl = FirebaseStorage.thumbnailUrlLiveData.value ?: return
            val isVideoUploaded = FirebaseStorage.uploadVideoSuccessful.value ?: return
            val videoUrl = FirebaseStorage.videoUrlLiveData.value ?: return

            value = if(isThumbnailUploaded && isVideoUploaded) {
                val newMessage = Message(FbUser.getUserId(), receiverUid.value, "Video",
                    videoUrl, null, thumbnailUrl, MessageType.TYPE_VIDEO)
                addUserAndSendMessage(receiverUid.value!! ,newMessage)
                true
            } else {
                false
            }

        }
        addSource(FirebaseStorage.uploadThumbnailSuccessful) { update() }
        addSource(FirebaseStorage.thumbnailUrlLiveData) { update() }
        addSource(FirebaseStorage.uploadVideoSuccessful) { update() }
        addSource(FirebaseStorage.videoUrlLiveData) { update() }
    }



    override fun onCleared() {
        if(FirebaseStorage.docFileUploadProgress.hasActiveObservers()) {
            FirebaseStorage.docFileUploadProgress.removeObserver(upLoadProgressObserver)
        }
        if(FirebaseStorage.docFileUploadSuccessful.hasActiveObservers()) {
            FirebaseStorage.docFileUploadSuccessful.removeObserver(uploadSuccessfulObserver)
        }
        if(FirebaseStorage.docFileMessage.hasActiveObservers()) {
            FirebaseStorage.docFileMessage.removeObserver(downloadUrlObserver)
        }
        super.onCleared()
    }




}