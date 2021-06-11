package android.example.com.chatapp.viewModel

import android.app.Application
import android.example.com.chatapp.model.*
import android.util.Log
import androidx.lifecycle.*

import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect

class ChatsViewModel(application: Application) : AndroidViewModel(application) {


    @ExperimentalCoroutinesApi
    val liveDataContactChat = liveData<List<ContactChat>>(Dispatchers.IO) {
        FirebaseDBService.getQueryForChatList().collect {
            emit(it)
        }
    }
    @ExperimentalCoroutinesApi
    val liveDataUserContact = Transformations.switchMap(liveDataContactChat) {
        FirebaseDBService.getUserBasicDataListForChat(it)
    }

    @ExperimentalCoroutinesApi
    val liveDataUserLastMessage = Transformations.switchMap(liveDataContactChat) {
        FirebaseDBService.lastMessageQuery(it)
    }

    @ExperimentalCoroutinesApi
    val liveDataContactForChatSeen = Transformations.switchMap(liveDataContactChat) {
        FirebaseDBService.checkReceiverChatSeen(it)
    }

    ////Transforms all the data received to a Data Class designed for the UI
    @ExperimentalCoroutinesApi
    val liveDataContacts = MediatorLiveData<List<UiContactChat>>().apply {
        fun update() {

            val contactChatList = liveDataContactChat.value ?: return
            val userContactList = liveDataUserContact.value ?: return
            val messageList = liveDataUserLastMessage.value ?: return
            val chatSeenContactList = liveDataContactForChatSeen.value ?: return
            Log.d("DDD", "${contactChatList.size}, ${userContactList.size}, ${messageList.size}, ${chatSeenContactList.size}")
            if(userContactList.size != contactChatList.size || messageList.size != contactChatList.size
                || chatSeenContactList.size != contactChatList.size) {
                return
            } else {
                value =
                    mergeData(contactChatList, userContactList, messageList, chatSeenContactList)
            }
        }
        addSource(liveDataContactChat) { update() }
        addSource(liveDataUserContact) { update() }
        addSource(liveDataUserLastMessage) { update() }
        addSource(liveDataContactForChatSeen) { update() }
    }

    private fun mergeData(contactList: List<ContactChat>, userList: List<User>,
                          messageList: List<Message>, chatSeenContactList: List<ContactChat>) : List<UiContactChat> {
        val uiList = mutableListOf<UiContactChat>()
        for(contact in contactList) {
            val user = userList.find { it.userId == contact.uid }
            val message = messageList.find {it.uidReceiver == contact.uid || it.uidAuthor == contact.uid}
            val isContactChatSeen = chatSeenContactList.find { it.uid == contact.uid}

            val newUiUser = UiContactChat(
                user?.userId, user?.userName, user?.imageUrl,
                message?.message, message?.messageType, contact.timestamp?.time, isContactChatSeen?.isChatSeen, contact.isChatSeen)
            uiList.add(newUiUser)
        }

        uiList.sortByDescending { it.timestamp }
        return uiList
    }



}