package android.example.com.chatapp.util

import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan

class TxtUtils {

    companion object {
        fun setTitleSubtitleButton(title: String, subtitle: String): SpannableString {
            val finalTxt = title + subtitle
            val spannableString = SpannableString.valueOf(finalTxt)
            spannableString.setSpan(
                ForegroundColorSpan(Color.GRAY), title.length, finalTxt.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannableString.setSpan(
                RelativeSizeSpan(0.8f), title.length, finalTxt.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            return spannableString
        }
    }
}