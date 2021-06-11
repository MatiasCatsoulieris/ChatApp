package android.example.com.chatapp.model

import android.example.com.chatapp.util.TimestampConverter
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class ContactChat (
    var uid: String? = "",
    var isChatSeen: Boolean? = false,
        ) {
    @ServerTimestamp
    var timestamp: Date? = null
    var isActive: Boolean = true


    override fun toString(): String {
        return "ContactChat{" + "isActive =" + isActive +
                ",isChatSeen=" + isChatSeen + ",timestamp=" +
                TimestampConverter.getTimeStamp(timestamp!!.time) + '}'
    }

//    fun mergeToUiContactChat(user: User, message: Message): UiContactChat {
//        return UiContactChat(this.uid, user.userName, user.imageUrl, message.message,
//            message.messageType, this.timestamp, this.isChatSeen)
//    }

}

