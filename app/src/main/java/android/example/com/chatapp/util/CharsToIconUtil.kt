package android.example.com.chatapp.util

import android.content.Context
import android.text.Spannable
import android.text.style.ImageSpan
import android.widget.TextView

class CharsToIconUtil {

    companion object {
        fun setIconInTxtView(txtView: TextView, text: String, listChars: ArrayList<String>,
                             listIcons: ArrayList<Int>, context: Context) {
            if(listChars.size == listIcons.size) {
                val spannable = Spannable.Factory.getInstance().newSpannable(text)

                for (i in 0 until listChars.size) {
                    val index = text.indexOf(listChars[i])
                    spannable.setSpan(ImageSpan(context, listIcons[i]), index, index + 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                txtView.text = spannable
            } else {

            }
        }
    }
}