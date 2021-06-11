package android.example.com.chatapp.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.*


data class User (
    var userName: String? = "",
    var userId: String = "",
    var imageUrl: String? = "",
    var state: String? = "",
    @ServerTimestamp
    var onRegisterTimeStamp: Date? = null
    ) {}

