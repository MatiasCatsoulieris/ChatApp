package android.example.com.chatapp.util

import android.content.Context
import android.example.com.chatapp.BuildConfig
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.view.View
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class BitmapUtils {
    companion object {
        fun getBitmapFromView(view: View) : Bitmap {
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            view.draw(canvas)
            return bitmap
        }

        fun getUriforBitmap(bitmap:Bitmap, context: Context): Uri {
            val cachePath = File(context.externalCacheDir, "my_images/")
            cachePath.mkdirs()
            val file = File(cachePath, UUID.randomUUID().toString()+".jpg")
            try{
                val fileOutputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
                fileOutputStream.flush()
                fileOutputStream.close()
            }
            catch(e: IOException) {
                e.cause
            }
            return FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID +".fileprovider", file)
        }
    }

}