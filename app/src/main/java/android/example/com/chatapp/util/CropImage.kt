package android.example.com.chatapp.util

import android.content.Context
import android.example.com.chatapp.R
import android.graphics.Bitmap
import android.net.Uri
import androidx.fragment.app.Fragment
import com.yalantis.ucrop.UCrop
import java.io.File
import java.lang.NullPointerException
import java.util.*

class CropImage {
    companion object {
        fun imageCropFromFragment(uri: Uri, context: Context, fragment: Fragment) {
            try {
                val fileName = UUID.randomUUID().toString() + ".jpg"
                val uCrop = UCrop.of(uri, Uri.fromFile(File(context.cacheDir, fileName)))
                uCrop.withAspectRatio(1F,1F)
                uCrop.withMaxResultSize(500,500)

                val options = UCrop.Options()
                options.setCompressionQuality(90)
                options.setCompressionFormat(Bitmap.CompressFormat.JPEG)
//                options.setStatusBarColor(context.resources.getColor(R.color.colorPrimaryDark, null))
//                options.setToolbarColor(context.resources.getColor(R.color.colorPrimary, null))
                options.setToolbarTitle("Crop Picture")

                uCrop.withOptions(options)
                uCrop.start(context, fragment, REQUEST_CROP)

            } catch (e: NullPointerException) {
                e.cause
            }
        }
    }
}