package android.example.com.chatapp.util

import android.content.Context

class PixelsToDpUtil {

    companion object {
        fun convertPixelsToDp(dp: Int, context: Context): Float {
            return dp * context.resources.displayMetrics.density
        }
    }
}