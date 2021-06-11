package android.example.com.chatapp.model

import android.example.com.chatapp.util.TimestampConverter
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class Message (
    val uidAuthor: String? = "",
    val uidReceiver: String? = "",
    val message: String? = "",
    val dataUrl: String? = "",
    val giphyMediaId: String? = "",
    val videoThumbnailUrl: String? = "",
    val messageType: MessageType? = MessageType.TYPE_TEXT
    ){
    @ServerTimestamp
    val timestamp: Date? = null

    override fun toString(): String {
        return "Message {" + "message=" + message + '\'' +
                ", timestamp" + TimestampConverter.getTimeStamp(timestamp!!.time) + "}"
    }
}

enum class MessageType(val type : Int) {

    TYPE_TEXT(1),
    TYPE_GIF(2),
    TYPE_PHOTO(3),
    TYPE_VIDEO(4),
    TYPE_AUDIO(5),
    TYPE_DOC_PDF(6)

}