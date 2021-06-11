package android.example.com.chatapp.util

import android.os.Build
import android.text.format.DateUtils
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*

class TimestampConverter {
    companion object{
        fun dateToLong(date:Date): Long {
            val formatter = SimpleDateFormat.getDateInstance(DateFormat.MEDIUM, Locale.ROOT);
            return formatter.format(date).toLong()
        }
        fun getTimeStamp(timestamp: Long): String {

            return DateUtils.getRelativeTimeSpanString(timestamp)
                .toString().toLowerCase(Locale.ROOT)
        }
    }
}