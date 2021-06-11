package android.example.com.chatapp.model

import android.app.Activity
import android.example.com.chatapp.util.FirebaseConstants
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.lang.NullPointerException

class FbUser {
    companion object {
        fun getUserId(): String? {
            return FirebaseAuth.getInstance().currentUser?.uid
        }

        fun getUserImage(): MutableLiveData<String> {
            val userImage = MutableLiveData<String>()
            FirebaseFirestore.getInstance().collection(FirebaseConstants.USERS)
                .document(getUserId()!!).get().addOnCompleteListener {
                    if (it.isSuccessful && it.result != null) {
                        val userImg = it.result!!.data?.get("imageUrl")
                        userImage.value = userImg.toString()
                    }
                }
            return userImage
        }

        fun getUserBasicData(uid: String): MutableLiveData<User> {
            val user = MutableLiveData<User>()
            FirebaseFirestore.getInstance().collection(FirebaseConstants.USERS)
                .document(uid).get().addOnCompleteListener {
                    if (it.isSuccessful && it.result != null) {
                        user.value = it.result!!.toObject(User::class.java)
                    }
                }
            return user
        }

        fun getInstanceId() {
            FirebaseMessaging.getInstance().token.addOnCompleteListener {
                if(it.isSuccessful && it.result != null) {
                    val notificationToken = it.result!!
                    val tokenMap = mutableMapOf<String, Any>()
                    tokenMap["tokenNotification"] = notificationToken
                    FirebaseFirestore.getInstance().collection(FirebaseConstants.USERS)
                        .document(getUserId()!!).set(tokenMap, SetOptions.merge())
                }
            }
        }
        fun deleteTokenNotification()= flow<Boolean> {
            var deleteSuccessful: Boolean = false
            val tokenMap = mutableMapOf<String, Any>()
            tokenMap["tokenNotification"] = ""
            FirebaseFirestore.getInstance().collection(FirebaseConstants.USERS)
                .document(getUserId()!!).update(tokenMap).addOnCompleteListener {
                    deleteSuccessful = it.isSuccessful
                }.await()
            emit(deleteSuccessful)
        }


    }
}