package android.example.com.chatapp.model

data class UiContactChat (
    var uid: String? = "",
    var userName : String? = "",
    var imageUrl: String? = "",
    var lastMessage: String? = "",
    var typeLastMessage: MessageType? = MessageType.TYPE_TEXT,
    var timestamp: Long? = 0,
    var isSeenByUser: Boolean?,
    var isChatSeen: Boolean?

) {}