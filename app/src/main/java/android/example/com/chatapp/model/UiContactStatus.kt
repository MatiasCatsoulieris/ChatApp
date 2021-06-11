package android.example.com.chatapp.model

data class UiContactStatus (
    var uid: String? = "",
    var timestampLastStatus: Long? = 0,
    var userName: String? = "",
    var imageUrl: String? = "",
    var state: String? = ""
        ) {
}