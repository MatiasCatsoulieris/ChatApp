package android.example.com.chatapp.util

import android.graphics.Bitmap


interface OnFilterListener {
    fun onFilterClicked(filter: com.bumptech.glide.load.Transformation<Bitmap>?) {}
}