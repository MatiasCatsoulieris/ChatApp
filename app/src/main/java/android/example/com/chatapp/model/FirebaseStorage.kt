package android.example.com.chatapp.model

import android.example.com.chatapp.util.FirebaseConstants
import android.example.com.chatapp.util.ONE_MEGABYTE
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.*

object FirebaseStorage {

    private val firebaseStorage = FirebaseStorage.getInstance()
    private val storageReference = firebaseStorage.reference
    val uploadProgress = MutableLiveData<Double>()
    val uploadSuccessful = MutableLiveData<Boolean>()
    val downloadUrl = MutableLiveData<Uri>()

    val stateUploadProgress = MutableLiveData<Double>()
    val stateUploadSuccessful = MutableLiveData<Boolean>()
    val stateDownloadUrl = MutableLiveData<Uri>()

    //Function uploads the image and returns de uri of the uploaded file

    fun updateUserImageStorage(uri: Uri, userId: String) {
        val reference = storageReference.child(FirebaseConstants.STORAGE_USERS_IMG_PROFILE)
            .child("$userId.jpg")
        reference.putFile(uri).addOnSuccessListener {
            uploadSuccessful.value = true
            reference.downloadUrl.addOnSuccessListener {
                downloadUrl.value = it
            }
        }.addOnFailureListener {
            uploadSuccessful.value = false
        }.addOnProgressListener {
            uploadProgress.value = (100.0 * it.bytesTransferred / it.totalByteCount)
        }
    }

    suspend fun deleteImage(imgUrl: String): Boolean {
        var isSuccessful = false
        val imgReference = firebaseStorage.getReferenceFromUrl(imgUrl)
        imgReference.delete().addOnCompleteListener {
            isSuccessful = it.isSuccessful
        }.await()
        return isSuccessful

    }

    fun publishStateInStorage(statusUri: Uri) {
        val filePath = storageReference.child(FirebaseConstants.STATES)
            .child(FbUser.getUserId()!!)
            .child(UUID.randomUUID().toString() + "jpg")

        filePath.putFile(statusUri).addOnSuccessListener {
            filePath.downloadUrl.addOnSuccessListener {
                stateDownloadUrl.value = it
            }
        }.addOnFailureListener {
            stateUploadSuccessful.value = false
        }.addOnProgressListener {
            stateUploadProgress.value = (100.0 * it.bytesTransferred / it.totalByteCount)
        }
    }

    fun deleteStatusFromStorage(status: Status): Flow<Boolean> = flow {
        Log.e("BBB", "OnDeleteStorage")
        var deletedFromStorage: Boolean = false
        FirebaseStorage.getInstance().getReferenceFromUrl(status.imgUrl!!)
            .delete().addOnCompleteListener {
                deletedFromStorage = it.isSuccessful
            }.await()
        emit(deletedFromStorage)
    }

    val docFileMessage = MutableLiveData<Message>()
    val docFileUploadProgress = MutableLiveData<Double>()
    val docFileUploadSuccessful = MutableLiveData<Boolean>()

    fun sendFileMessage(
        uidReceiver: String, uriData: Uri, message: String, mediaFile: String,
        extensionFile: String, type: MessageType
    ) {
        val filePath = storageReference.child(FirebaseConstants.CHAT_CHANNELS + "Media")
            .child(FbUser.getUserId()!!)
            .child(FirebaseConstants.CHATS)
            .child(uidReceiver)
            .child(mediaFile)
            .child(UUID.randomUUID().toString() + extensionFile)

        filePath.putFile(uriData).addOnSuccessListener {
            filePath.downloadUrl.addOnSuccessListener {
                docFileUploadSuccessful.value = true
                val url = it.toString()
                val newMessage = Message(FbUser.getUserId(), uidReceiver,message,url,null,null,type)
                docFileMessage.value = newMessage
            }

        }.addOnFailureListener {
            docFileUploadSuccessful.value = false
        }.addOnProgressListener {
            docFileUploadProgress.value = (100.0 * it.bytesTransferred / it.totalByteCount)
        }
    }

    val pdfFile = MutableLiveData<ByteArray>()
    val downloadFailed = MutableLiveData<Boolean>()
    fun downloadPdf(url: String) {
        FirebaseStorage.getInstance().reference.storage
            .getReferenceFromUrl(url)
            .getBytes(ONE_MEGABYTE.toLong())
            .addOnSuccessListener {
                pdfFile.value = it
            }.addOnFailureListener {
                downloadFailed.value = true
            }
    }

    val uploadThumbnailSuccessful = MutableLiveData<Boolean>()
    val thumbnailUrlLiveData = MutableLiveData<String>()
    fun sendThumbnailToMedia(uidReceiver: String, videoUri: Uri, thumbnailUri: Uri) {
        val folderName = UUID.randomUUID().toString()
        val fileName = UUID.randomUUID().toString()
        val filePath = storageReference.child(FirebaseConstants.CHAT_CHANNELS + "Media")
            .child(FbUser.getUserId()!!)
            .child(FirebaseConstants.CHATS)
            .child(uidReceiver)
            .child("video")
            .child(folderName)
            .child("$fileName.jpg")

        filePath.putFile(thumbnailUri).addOnSuccessListener {
            filePath.downloadUrl.addOnSuccessListener {
                thumbnailUrlLiveData.value = it.toString()
                uploadThumbnailSuccessful.value = true
                sendVideoToMedia(videoUri, it,uidReceiver, folderName, fileName)
            }
        }.addOnFailureListener {
            uploadThumbnailSuccessful.value = false
        }
    }

    val uploadVideoSuccessful = MutableLiveData<Boolean>()
    val videoUrlLiveData = MutableLiveData<String>()
    val videoUploadProgress = MutableLiveData<Double>()
    private fun sendVideoToMedia(videoUri: Uri, thumbnailStorageUri: Uri?, uidReceiver: String, folderName: String, fileName: String) {
        val filePath = storageReference.child(FirebaseConstants.CHAT_CHANNELS + "Media")
            .child(FbUser.getUserId()!!)
            .child(FirebaseConstants.CHATS)
            .child(uidReceiver)
            .child("video")
            .child(folderName)
            .child("$fileName.mp4")

        filePath.putFile(videoUri).addOnSuccessListener {
            filePath.downloadUrl.addOnSuccessListener {
                uploadVideoSuccessful.value = true
                videoUrlLiveData.value = it.toString()
            }
        }.addOnFailureListener {
            uploadVideoSuccessful.value = false
        }.addOnProgressListener {
            videoUploadProgress.value = (100.0 * it.bytesTransferred / it.totalByteCount)
        }
    }

}