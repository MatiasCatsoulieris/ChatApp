package android.example.com.chatapp.util

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.WindowManager
import java.io.File
import java.util.*

class CameraUtils {

    companion object {
        fun getNewFile(): File {
            return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                val root = Environment.getStorageDirectory()
                val myDir = File("$root/ChatApp")
                if (!myDir.exists()) {
                    myDir.mkdirs()
                }
                File(myDir, UUID.randomUUID().toString() + ".jpg")
            } else {
                val root =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        .toString()
                val myDir = File("$root/ChatApp")
                if (!myDir.exists()) {
                    myDir.mkdirs()
                }
                File(myDir, UUID.randomUUID().toString() + ".jpg")
            }

        }

        fun getDisplayRotation(context: Context): Int {
            return if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.R) {
                (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
                    .defaultDisplay.rotation
            } else {
                context.display!!.rotation

            }
        }

        fun getImagesFromgGallery(context: Context): MutableList<String> {
            val listOfAllImages: MutableList<String> = mutableListOf()
            val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(MediaStore.Images.Media._ID)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val bundle = Bundle().apply {
                    putInt(ContentResolver.QUERY_ARG_LIMIT, 100)
                    putString(
                        ContentResolver.QUERY_ARG_SORT_COLUMNS,
                        MediaStore.Files.FileColumns.DATE_MODIFIED
                    )
                    putInt(
                        ContentResolver.QUERY_ARG_SORT_DIRECTION,
                        ContentResolver.QUERY_SORT_DIRECTION_DESCENDING
                    )
                }
                val cursor = context.contentResolver
                    .query(uri, projection, bundle, null)
                if (cursor != null) {
                    val columnIndexData = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)

                    while (cursor.moveToNext()) {
                        val absolutePath = cursor.getString(columnIndexData)
                        val uriImage = Uri.withAppendedPath(uri, "" + absolutePath)
                        listOfAllImages.add(uriImage.toString())
                    }
                    cursor.close()
                }
            } else {

                val cursor = context.contentResolver
                    .query(uri, projection, null, null, "datetaken DESC LIMIT 100")
                if (cursor != null) {
                    val columnIndexData = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)

                    while (cursor.moveToNext()) {
                        val absolutePath = cursor.getString(columnIndexData)
                        val uriImage = Uri.withAppendedPath(uri, "" + absolutePath)
                        listOfAllImages.add(uriImage.toString())
                    }
                    cursor.close()
                }
            }
                return listOfAllImages
            }
        }
    }