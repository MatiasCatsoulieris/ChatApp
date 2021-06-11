package android.example.com.chatapp.util

import android.content.Context
import android.content.res.Resources
import android.example.com.chatapp.R
import android.example.com.chatapp.model.FbUser
import android.graphics.Color
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat


fun showMessageTimestamp(message: String, timestamp: Long): SpannableString {
    var txtFinal: String =
        if (message.isNotEmpty()) message + "  " + TimestampConverter.getTimeStamp(timestamp)
        else TimestampConverter.getTimeStamp(timestamp)
    val spannable = SpannableString(txtFinal)
    spannable.setSpan(RelativeSizeSpan(0.7f), message.length, txtFinal.length, 0)
    spannable.setSpan(ForegroundColorSpan(Color.GRAY), message.length, txtFinal.length, 0)
    return spannable
}

fun changeMessageDesign(authorMessageId: String, cardView: CardView, textView: TextView, context: Context) {
    if(authorMessageId == FbUser.getUserId()) {
        textView.textAlignment = View.TEXT_ALIGNMENT_TEXT_END
        val paramsEnd = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT)
        paramsEnd.addRule(RelativeLayout.ALIGN_PARENT_END)
        paramsEnd.setMargins(PixelsToDpUtil.convertPixelsToDp(56,context).toInt(),0,0,0)
        cardView.layoutParams = paramsEnd
        cardView.background = ResourcesCompat.getDrawable(context.resources, R.drawable.shape_message_self, null)
    } else {
        textView.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
        val paramsStart = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT)
        paramsStart.addRule(RelativeLayout.ALIGN_PARENT_START)
        paramsStart.setMargins(0,0,PixelsToDpUtil.convertPixelsToDp(56,context).toInt(),0)
        cardView.layoutParams = paramsStart
        cardView.background = ContextCompat.getDrawable(context, R.drawable.shape_message)
    }
}