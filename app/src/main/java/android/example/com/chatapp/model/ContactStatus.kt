package android.example.com.chatapp.model

data class ContactStatus (
    val timeStampLastStatus: Long? = 0,
        ) {
    var uid: String? = FbUser.getUserId()
    var spectatorsNotification: HashMap<String, Any> = HashMap()

    fun mergeUser(user:User): UiContactStatus {
        return UiContactStatus(user.userId, this.timeStampLastStatus, user.userName, user.imageUrl, user.state )
    }
}