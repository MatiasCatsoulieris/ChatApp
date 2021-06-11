package android.example.com.chatapp.util

import android.app.Dialog
import android.content.Context
import android.example.com.chatapp.R
import android.graphics.Color
import android.media.MediaScannerConnection
import android.util.Patterns
import android.widget.*
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import java.io.File
import java.util.*

///////////
const val CHAT_FRAGMENT = "ChatFragment"
const val CHAT_FRAGMENT_CODE = 1
///////////INTENT CODES
const val PERMISSION_CAMERA = 1
const val PERMISSION_STORAGE = 2
const val REQUEST_IMAGE_CAPTURE = 10
const val REQUEST_STORAGE = 20
const val REQUEST_CROP = 3
//////CONTACT TYPE ACTION
const val TYPE_ACTION = "KeyTypeAction"
const val ACTION_CHAT = "CHAT"
const val ACTION_CALL = "CALL"
const val ACTION_SEND_MESSAGE = "SEND_MESSAGE"
const val ACTION_SEND_PHOTO_MESSAGE = "SEND_PHOTO_MESSAGE"
/////CONTACT PAGE SIZE
const val PAGE_SIZE = 15
/////CAMERA TYPE ACTIONS
const val CAMERA_TYPE_ACTION = "cameraTypeAction"
const val CAMERA_ACTION_MESSAGE = "actionMessage"
const val CAMERA_ACTION_STATE = "actionState"
const val CAMERA_ACTION_UPDATE_PICTURE = "actionUpdatePicture"
///////INTENT CODE MESSAGE TYPE
const val CODE_RECOGNIZE_SPEECH = 44
const val CODE_DOC_PDF = 22
const val CODE_VIDEO = 33
const val CODE_IMAGE = 55
const val CODE_AUDIO = 66

const val ONE_MEGABYTE = 1024 * 1024 * 5

const val UID = "KeyUID"
const val USERNAME = "KeyUsername"
const val IMG_URI = "KeyImg"

fun getProgressDrawable(context: Context): CircularProgressDrawable {
    return CircularProgressDrawable(context).apply {
        strokeWidth = 10f
        centerRadius = 50f
    }
}

fun String.isValidEmail() : Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun createCustomDialog (context: Context, title:String?, message: String?): Dialog {
    val dialog = Dialog(context)
    dialog.setContentView(R.layout.view_dialog)
    title?.let {
        val textViewTitle = dialog.findViewById<TextView>(R.id.titleDialog)
        textViewTitle.text = title
    }
    message?.let {
        val messageDialogTextView = dialog.findViewById<TextView>(R.id.messageDialog)
        messageDialogTextView.text = message
    }
    return dialog
}
fun addPicToGallery(currentPhotoPath: String, context: Context) {
    val f = File(currentPhotoPath)
    MediaScannerConnection.scanFile(context, arrayOf(f.toString()), null, null)
}

fun ImageView.loadImage(uri: String?, progressDrawable: CircularProgressDrawable) {
    val options = RequestOptions()
        .placeholder(progressDrawable)
        .error(R.drawable.android_default_icons)

    Glide.with(context)
        .setDefaultRequestOptions(options)
        .load(uri)
        .into(this)
}

fun getRandomColor(): Int {
    val random = Random()
    return Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256))
}

fun ImageView.loadImageRounded(uri: String?, progressDrawable: CircularProgressDrawable) {
    val options = RequestOptions()
        .placeholder(progressDrawable)
        .error(R.drawable.android_default_icons)
        .circleCrop()

    Glide.with(context)
        .setDefaultRequestOptions(options)
        .load(uri)
        .into(this)
}

fun ImageButton.enableButton(enable: Boolean) {
    this.isEnabled = enable
    this.isFocusable = enable
    this.isClickable = enable
}