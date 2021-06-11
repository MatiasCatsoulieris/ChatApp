package android.example.com.chatapp.model


import android.graphics.Bitmap
import android.widget.ImageView
import com.bumptech.glide.load.Transformation


data class Filter(
    var transformation: Transformation<Bitmap>?,
    var name: String?
        ) {

}