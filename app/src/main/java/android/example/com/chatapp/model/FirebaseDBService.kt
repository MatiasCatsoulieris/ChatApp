package android.example.com.chatapp.model

import android.example.com.chatapp.util.FirebaseConstants
import android.example.com.chatapp.util.PAGE_SIZE
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import java.lang.NullPointerException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

object FirebaseDBService {

    private var lastDocumentVisible: String? = null
    val isLastPage = MutableLiveData<Boolean>()
    val isSuccessful = MutableLiveData<Boolean>()


    suspend fun postNewUser(user: User): Boolean {
        var result: Boolean = false
        val database = FirebaseFirestore.getInstance()
        database.collection(FirebaseConstants.USERS).document(user.userId)
            .set(user, SetOptions.merge()).addOnCompleteListener {
                result = it.isSuccessful
            }.await()
        return result
    }

    fun fetchUserData(userId: String): MutableLiveData<User> {
        val currentUser = MutableLiveData<User>()
        val database = FirebaseFirestore.getInstance()
        database.collection(FirebaseConstants.USERS).document(userId)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (value != null) {
                    val userName = value.getString("userName")
                    val imageUrl = value.getString("imageUrl")
                    val state = value.getString("state")
                    val user = User(userName!!, userId, imageUrl, state, null)
                    currentUser.value = user
                }
            }
        return currentUser
    }

    fun retrieveUsers(): MutableLiveData<List<User>> {
        val usersData = MutableLiveData<List<User>>()
        val usersList = mutableListOf<User>()
        val database = FirebaseFirestore.getInstance()
        database.collection(FirebaseConstants.USERS).addSnapshotListener { value, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            if (value != null) {
                val documents = value.documents
                documents.forEach {
                    val userData = User(
                        it.getString("userName")!!, it.getString("userId")!!,
                        it.getString("imageUrl"), it.getString("state"), null
                    )
                    //Remove current user from users list
                    if (userData.userId != FirebaseAuth.getInstance().currentUser!!.uid) {
                        usersList.add(userData)
                    }
                }
                usersData.value = usersList
            }
        }
        return usersData
    }

    fun publishStatusInDB(imgStatus: String): MutableLiveData<Boolean> {
        val taskSuccessful = MutableLiveData<Boolean>()
        val statusId =
            FirebaseFirestore.getInstance().collection(FirebaseConstants.STATES).document().id
        val timestamp = System.currentTimeMillis()
        val contactStatus = ContactStatus(timestamp)
        val status = Status(FbUser.getUserId(), imgStatus, statusId, timestamp)

        val db = FirebaseFirestore.getInstance()
        val writeBatch = db.batch()

        val statusDocument = db.collection(FirebaseConstants.STATES)
            .document(FbUser.getUserId()!!)
            .collection((FirebaseConstants.STATES))
            .document(statusId)

        val contactStatusDocument = db.collection(FirebaseConstants.CONTACT_STATES)
            .document(FbUser.getUserId()!!)

        writeBatch.set(statusDocument, status)
        writeBatch.set(contactStatusDocument, contactStatus)
        writeBatch.commit().addOnCompleteListener {
            taskSuccessful.value = it.isSuccessful
        }
        return taskSuccessful
    }

    fun observeNewStatus(): MutableLiveData<Boolean> {
        val newContactStatus = MutableLiveData<Boolean>()
        var newStatus: Boolean
        FirebaseFirestore.getInstance().collection(FirebaseConstants.CONTACT_STATES)
            .addSnapshotListener { value, error ->
                newStatus = false
                if (value != null) {
                    val contactList = value.documents
                    if (contactList.size > 0) {
                        for (contact in contactList) {
                            if (contact.id != FbUser.getUserId()
                                && contact.contains("spectatorsNotification")
                            ) {
                                val contactStatus =
                                    ContactStatus(contact.getLong("timeStampLastStatus"))
                                contactStatus.spectatorsNotification =
                                    contact.get("spectatorsNotification") as HashMap<String, Any>
                                val hashMap = contactStatus.spectatorsNotification
                                if (!hashMap.containsKey(FbUser.getUserId())) {
                                    newStatus = true
                                }
                            }
                        }
                        newContactStatus.value = newStatus
                    }
                }
            }
        return newContactStatus
    }

    fun updateNotificationNewStatus() {
        val reference = FirebaseFirestore.getInstance().collection(FirebaseConstants.CONTACT_STATES)
        reference.get().addOnCompleteListener {
            try {
                if (it.isSuccessful && it.result != null) {
                    val list = it.result!!.documents
                    if (list.size > 0) {
                        for (contact in list) {
                            if (contact.id != FbUser.getUserId()) {
                                val newContactStatus =
                                    ContactStatus(contact.getLong("timeStampLastStatus"))
                                val map = HashMap<String, Any>()
                                map[FbUser.getUserId()!!] = true

                                val newMap = HashMap<String, Any>()
                                newMap["spectatorsNotification"] = map
                                reference.document(contact.id).set(newMap, SetOptions.merge())
                            }
                        }
                    }
                }

            } catch (e: Exception) {
                e.cause
            }
        }
    }

    val contactStatusList = arrayListOf<ContactStatus>()

    @ExperimentalCoroutinesApi
    fun getQuery(): Flow<List<ContactStatus>> = callbackFlow<List<ContactStatus>> {
        val reference = FirebaseFirestore.getInstance().collection(FirebaseConstants.CONTACT_STATES)
            .orderBy("timeStampLastStatus", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                contactStatusList.clear()
                if (value != null) {
                    for (document in value.documents) {
                        val newContactStatus =
                            ContactStatus(document.getLong("timeStampLastStatus"))
                        newContactStatus.uid = document.id
                        contactStatusList.add(newContactStatus)
                    }
                    offer(contactStatusList)
                }
            }
        awaitClose { reference.remove() }
    }

    fun getListenerUserBasicDataList(list: List<ContactStatus>): MutableLiveData<List<User>> {
        val userListLiveData = MutableLiveData<List<User>>()
        val userList = arrayListOf<User>()
        userList.clear()
        for (contact in list) {
            try {
                FirebaseFirestore.getInstance().collection(FirebaseConstants.USERS)
                    .document(contact.uid!!).addSnapshotListener { value, error ->
                        if (value != null) {
                            val user = value.toObject(User::class.java)
                            userList.removeAll { it.userId == user!!.userId }
                            userList.add(user!!)
                            if (userList.size == list.size) {
                                userListLiveData.value = userList
                            }
                        }
                    }
            } catch (e: Exception) {
                e.cause
            }
        }
        return userListLiveData
    }

    fun getListenerSpectatorBasicDataList(list: List<StatusSpectator>): MutableLiveData<List<User>> {
        val userListLiveData = MutableLiveData<List<User>>()
        val userList = arrayListOf<User>()
        userList.clear()
        for (contact in list) {
            try {
                FirebaseFirestore.getInstance().collection(FirebaseConstants.USERS)
                    .document(contact.uid!!).addSnapshotListener { value, error ->
                        if (value != null) {
                            val user = value.toObject(User::class.java)
                            userList.removeAll { it.userId == user!!.userId }
                            userList.add(user!!)
                            if (userList.size == list.size) {
                                userListLiveData.value = userList
                            }
                        }
                    }
            } catch (e: Exception) {
                e.cause
            }
        }
        return userListLiveData
    }


    fun getStatusList(uid: String): MutableLiveData<List<Status>> {
        val statusLiveData = MutableLiveData<List<Status>>()
        FirebaseFirestore.getInstance().collection(FirebaseConstants.STATES).document(uid)
            .collection(FirebaseConstants.STATES)
            .orderBy("timeStamp", Query.Direction.DESCENDING)
            .get().addOnCompleteListener {
                if (it.isSuccessful && it.result != null) {
                    statusLiveData.value = it.result!!.toObjects(Status::class.java)
                }
            }
        return statusLiveData
    }

    fun addVisit(statusId: String, spectator: StatusSpectator) {
        FirebaseFirestore.getInstance()
            .collection(FirebaseConstants.SPECTATOR_STATES)
            .document(statusId)
            .collection(FirebaseConstants.SPECTATOR_STATES)
            .document(FbUser.getUserId()!!)
            .set(spectator, SetOptions.merge())
    }

    fun getSpectatorsNumber(statusId: String): MutableLiveData<Int> {
        val spectatorNumberLiveData = MutableLiveData<Int>()
        FirebaseFirestore.getInstance()
            .collection(FirebaseConstants.SPECTATOR_STATES)
            .document(statusId)
            .collection(FirebaseConstants.SPECTATOR_STATES)
            .get().addOnCompleteListener {
                try {
                    if (it.isSuccessful && it.result != null) {
                        val listSize = it.result!!.documents.size
                        if (listSize > 0) {
                            spectatorNumberLiveData.value = listSize
                        }
                    }

                } catch (e: NullPointerException) {
                    e.cause
                }
            }
        return spectatorNumberLiveData
    }

    fun deleteStatus(status: Status) = flow<Boolean> {
        var isDeletedFromFirestore = false
        FirebaseFirestore.getInstance()
            .collection(FirebaseConstants.STATES)
            .document(FbUser.getUserId()!!)
            .collection(FirebaseConstants.STATES)
            .document(status.statusId!!)
            .delete().addOnCompleteListener {
                isDeletedFromFirestore = it.isSuccessful
            }.await()
        emit(isDeletedFromFirestore)
    }

    fun getSpectatorsList(statusId: String): MutableLiveData<List<StatusSpectator>> {
        val spectatorsLiveData = MutableLiveData<List<StatusSpectator>>()
        val spectatorsList = arrayListOf<StatusSpectator>()
        FirebaseFirestore.getInstance().collection(FirebaseConstants.SPECTATOR_STATES)
            .document(statusId)
            .collection(FirebaseConstants.SPECTATOR_STATES)
            .addSnapshotListener { value, error ->
                spectatorsList.clear()
                if (value != null) {
                    for (spectator in value.documents) {
                        val newSpectator =
                            StatusSpectator(
                                spectator.getString("uid"),
                                spectator.getLong("timestamp")
                            )
                        spectatorsList.add(newSpectator)

                    }
                    spectatorsLiveData.value = spectatorsList
                }
            }
        return spectatorsLiveData
    }

    fun addUserToContacts(uidReceiver: String, message: Message) = flow<Boolean> {
        var isContactAdded = false
        val senderContactsReference =
            FirebaseFirestore.getInstance().collection(FirebaseConstants.CHAT_CONTACTS)
                .document(FbUser.getUserId()!!).collection(FirebaseConstants.ACTIVE_CONTACTS)
                .document(uidReceiver)

        val receiverContactsReference =
            FirebaseFirestore.getInstance().collection(FirebaseConstants.CHAT_CONTACTS)
                .document(uidReceiver).collection(FirebaseConstants.ACTIVE_CONTACTS)
                .document(FbUser.getUserId()!!)

        val db = FirebaseFirestore.getInstance()

        val batch = db.batch()


        val chatContactSender = ContactChat(uidReceiver, true)
        val chatContactReceiver = ContactChat(FbUser.getUserId()!!, false)

        batch.set(senderContactsReference, chatContactSender, SetOptions.merge())
        batch.set(receiverContactsReference, chatContactReceiver, SetOptions.merge())
        batch.commit().addOnCompleteListener {

            isContactAdded = it.isSuccessful
        }.await()
        emit(isContactAdded)
    }

    fun sendMessage(uidReceiver: String, message: Message) = flow<Boolean> {
        var isMessageSent = false
        val messageId = FirebaseFirestore.getInstance().collection(FirebaseConstants.CHATS)
            .document().id
        val senderMessageReference =
            FirebaseFirestore.getInstance().collection(FirebaseConstants.CHAT_CHANNELS)
                .document(FbUser.getUserId()!!).collection(FirebaseConstants.CHATS)
                .document(uidReceiver)
                .collection(FirebaseConstants.MESSAGES).document(messageId)
        val receiverMessageReference =
            FirebaseFirestore.getInstance().collection(FirebaseConstants.CHAT_CHANNELS)
                .document(uidReceiver).collection(FirebaseConstants.CHATS)
                .document(FbUser.getUserId()!!)
                .collection(FirebaseConstants.MESSAGES).document(messageId)
        val db = FirebaseFirestore.getInstance()
        val batch = db.batch()

        batch.set(senderMessageReference, message, SetOptions.merge())
        batch.set(receiverMessageReference, message, SetOptions.merge())
        batch.commit().addOnCompleteListener {
            isMessageSent = it.isSuccessful
        }.await()
        emit(isMessageSent)
    }

    fun getQueryForMessages(uidReceiver: String): Query = FirebaseFirestore.getInstance()
        .collection(FirebaseConstants.CHAT_CHANNELS).document(FbUser.getUserId()!!)
        .collection(FirebaseConstants.CHATS).document(uidReceiver)
        .collection(FirebaseConstants.MESSAGES)
        .orderBy("timestamp", Query.Direction.ASCENDING)

    ////

    fun updateUser(map: Map<String, String>, userId: String) {
        val reference = FirebaseFirestore.getInstance().collection(FirebaseConstants.USERS)
            .document(userId)
        reference.set(map, SetOptions.merge()).addOnCompleteListener {
            isSuccessful.value = it.isSuccessful
        }
    }

    fun updateUserImage(map: Map<String, String>, userId: String): MutableLiveData<Boolean> {
        val updateSuccessful = MutableLiveData<Boolean>()
        val reference = FirebaseFirestore.getInstance().collection(FirebaseConstants.USERS)
            .document(userId)
        reference.update(map).addOnCompleteListener {
            if (it.isSuccessful) {
                updateSuccessful.value = true
            }
        }
        return updateSuccessful
    }

    suspend fun deleteImageFromDatabase(userId: String): Boolean {
        var isDeleted = false
        val reference = FirebaseFirestore.getInstance().collection(FirebaseConstants.USERS)
            .document(userId)
        val map = HashMap<String, String>()
        map["imageUrl"] = ""
        reference.set(map, SetOptions.merge()).addOnCompleteListener {
            if (it.isSuccessful) {
                isDeleted = true
            }
        }.await()
        return isDeleted
    }

    fun getNumberOfContacts() = flow<Int> {
        var numberOfContacts = 0
        FirebaseFirestore.getInstance().collection(FirebaseConstants.USERS)
            .get().addOnCompleteListener {
                try {
                    if (it.isSuccessful && it.result != null) {
                        numberOfContacts = it.result!!.size() - 1
                    }
                } catch (e: NullPointerException) {
                    e.cause
                }

            }.await()
        emit(numberOfContacts)
    }

    fun getContacts(lastUser: User?) = flow<List<User>> {
        isLastPage.value = false
        lastDocumentVisible = lastUser?.userName
        val usersList = mutableListOf<User>()
        val query = FirebaseFirestore.getInstance().collection(FirebaseConstants.USERS)
            .orderBy("userName", Query.Direction.ASCENDING).limit(PAGE_SIZE.toLong())
        if (lastDocumentVisible == null) {
            usersList.clear()
            query.get().addOnCompleteListener {
                try {
                    if (it.isSuccessful && it.result != null) {
                        if (it.result!!.documents.size > 0) {
                            for (document in it.result!!.documents) {
                                val userData = document.toObject(User::class.java)
                                //Remove current user from users list
                                if (userData!!.userId != FirebaseAuth.getInstance().currentUser!!.uid) {
                                    usersList.add(userData)
                                }
                            }
                        } else {
                            isLastPage.value = true
                        }
                    } else {
                        Log.e("ContactsQuery", "Error retrieving contactList")
                    }
                } catch (e: NullPointerException) {
                    e.cause
                }
            }.await()
            emit(usersList)
        } else {
            query.startAfter(lastDocumentVisible).get().addOnCompleteListener {
                usersList.clear()
                try {
                    if (it.isSuccessful && it.result != null) {
                        if (it.result!!.documents.size > 0) {
                            for (document in it.result!!.documents) {
                                val userData = document.toObject(User::class.java)

                                //Remove current user from users list
                                if (userData!!.userId != FirebaseAuth.getInstance().currentUser!!.uid) {
                                    usersList.add(userData)
                                }
                            }
                        } else {
                            isLastPage.value = true
                        }
                    } else {
                        Log.e("ContactsQuery", "Error retrieving contactList")
                    }
                } catch (e: NullPointerException) {
                    e.cause
                }
            }.await()
            emit(usersList)
        }
    }

    fun getContactInfo(uid: String): MutableLiveData<User> {
        val user = MutableLiveData<User>()
        FirebaseFirestore.getInstance().collection(FirebaseConstants.USERS)
            .document(uid).get().addOnCompleteListener {
                if (it.isSuccessful && it.result != null) {
                    try {
                        user.value = it.result!!.toObject(User::class.java)
                    } catch (e: NullPointerException) {
                        e.cause
                    }
                }
            }
        return user
    }

    fun getPhotoListChatQuery(uidReceiver: String): Query {
        return FirebaseFirestore.getInstance()
            .collection(FirebaseConstants.CHAT_CHANNELS)
            .document(FbUser.getUserId()!!)
            .collection(FirebaseConstants.CHATS)
            .document(uidReceiver)
            .collection(FirebaseConstants.MESSAGES)
            .whereEqualTo("messageType", MessageType.TYPE_PHOTO)
            .orderBy("timestamp", Query.Direction.DESCENDING)
    }

    @ExperimentalCoroutinesApi
    fun getQueryForChatList() = callbackFlow<List<ContactChat>> {
        val listContactChat = ArrayList<ContactChat>()
        val reference = FirebaseFirestore.getInstance()
            .collection(FirebaseConstants.CHAT_CONTACTS)
            .document(FbUser.getUserId()!!)
            .collection(FirebaseConstants.ACTIVE_CONTACTS)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                listContactChat.clear()
                if(value != null) {
                    for(contact in value.documents) {
                        val newContactChat = contact.toObject(ContactChat::class.java)!!
                        listContactChat.add(newContactChat)
                    }
                    listContactChat.sortByDescending { it.timestamp }
                    offer(listContactChat)
                }
            }
        awaitClose { reference.remove() }
    }

    fun getUserBasicDataListForChat(list: List<ContactChat>): MutableLiveData<List<User>> {
        val userListLiveData = MutableLiveData<List<User>>()
        val userList = arrayListOf<User>()
        userList.clear()
        for (contact in list) {
            try {
                FirebaseFirestore.getInstance().collection(FirebaseConstants.USERS)
                    .document(contact.uid!!).addSnapshotListener { value, error ->
                        if (value != null) {
                            val user = value.toObject(User::class.java)
                            userList.removeAll { it.userId == user!!.userId }
                            userList.add(user!!)
                            if (userList.size == list.size) {
                                userListLiveData.value = userList
                            }
                        }
                    }
            } catch (e: Exception) {
                e.cause
            }
        }
        return userListLiveData
    }

    fun lastMessageQuery(contactList: List<ContactChat>): MutableLiveData<List<Message>> {
        val liveDataLastMessage = MutableLiveData<List<Message>>()
        val messageList = arrayListOf<Message>()
        contactList.forEach { contact->
            val query = FirebaseFirestore.getInstance()
                .collection(FirebaseConstants.CHAT_CHANNELS)
                .document(FbUser.getUserId()!!)
                .collection(FirebaseConstants.CHATS)
                .document(contact.uid!!)
                .collection(FirebaseConstants.MESSAGES)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .limitToLast(1)
            query.addSnapshotListener { value, error ->
                try {
                    if (value != null) {
                        val newMessage = value.documents[0].toObject(Message::class.java)
                        messageList.removeAll { message ->
                            message.uidAuthor == contact.uid
                                    || message.uidReceiver == contact.uid
                        }
                        messageList.add(newMessage!!)
                        if (messageList.size == contactList.size) {
                            liveDataLastMessage.value = messageList
                        }
                    }

                } catch (e: Exception) {
                    e.cause
                }
            }
        }
        return liveDataLastMessage
    }

    fun checkReceiverChatSeen(contactList: List<ContactChat>) : MutableLiveData<List<ContactChat>> {
        val liveDataReceiverContactList = MutableLiveData<List<ContactChat>>()
        val receiverContactList = arrayListOf<ContactChat>()
        contactList.forEach { contact ->
            FirebaseFirestore.getInstance()
                .collection(FirebaseConstants.CHAT_CONTACTS)
                .document(contact.uid!!)
                .collection(FirebaseConstants.ACTIVE_CONTACTS)
                .document(FbUser.getUserId()!!)
                .addSnapshotListener { value, error ->
                    try {
                        if (value != null) {
                            val newContactChat = value.toObject(ContactChat::class.java)!!
                            newContactChat.uid = contact.uid
                            receiverContactList
                                .removeAll { contact -> contact.uid == newContactChat.uid }
                            receiverContactList.add(newContactChat)
                            if(receiverContactList.size == contactList.size){
                                liveDataReceiverContactList.value = receiverContactList
                            }
                        }

                    } catch (e: NullPointerException) {
                        e.cause
                    }
                }
        }
        return liveDataReceiverContactList
    }

    fun setChatAsSeen(uidReceiver: String) {
        val map = HashMap<String, Any>()
        map["chatSeen"] = true
        FirebaseFirestore.getInstance()
            .collection(FirebaseConstants.CHAT_CONTACTS)
            .document(FbUser.getUserId()!!)
            .collection(FirebaseConstants.ACTIVE_CONTACTS)
            .document(uidReceiver)
            .update(map)
    }

}