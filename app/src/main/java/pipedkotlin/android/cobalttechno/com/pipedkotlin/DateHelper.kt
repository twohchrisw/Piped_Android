package pipedkotlin.android.cobalttechno.com.pipedkotlin

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.min

class DateHelper {
    companion object {

        val DB_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"
        val DISPLAY_DATE_FORMAT = "dd-MM-yyyy HH:mm"
        val DISPLAY_DATE_FORMAT_WITH_SECONDS = "dd-MM-yyyy HH:mm:ss"

        fun dbStringToDate(value: String, default: Date): Date
        {

            try {
                val date = SimpleDateFormat(DB_DATE_FORMAT).parse(value)
                return date
            }
            catch (e: Exception)
            {
                return  default
            }
        }

        fun dateFromValues(year: Int, month: Int, day: Int, hour: Int, minute: Int): Date
        {
            val c = Calendar.getInstance()
            c.set(year, month, day, hour, minute)
            val date = c.time
            return date
        }

        fun dateToDBString(date: Date): String
        {
            val format = SimpleDateFormat(DB_DATE_FORMAT)
            return format.format(date)
        }

        fun millisToDBString(millis: Long): String
        {
            val theDate = Date(millis)
            return dateToDBString(theDate)
        }

        fun dbDateStringFormatted(dateString: String): String
        {
            try {
                val date = SimpleDateFormat(DB_DATE_FORMAT).parse(dateString)
                val format = SimpleDateFormat(DISPLAY_DATE_FORMAT)
                return format.format(date)
            }
            catch (e: Exception)
            {
                return ""
            }
        }

        fun dbDateStringFormattedWithSeconds(dateString: String): String
        {
            try {
                val date = SimpleDateFormat(DB_DATE_FORMAT).parse(dateString)
                val format = SimpleDateFormat(DISPLAY_DATE_FORMAT_WITH_SECONDS)
                return format.format(date)
            }
            catch (e: Exception)
            {
                return ""
            }
        }

        fun dateIsValid(dateString: String): Boolean
        {
            try {
                val date = SimpleDateFormat(DB_DATE_FORMAT).parse(dateString)
                return true
            }
            catch (e: Exception)
            {
            }

            return false
        }

        fun timeDifferenceFormattedForCountdown(millis: Long): String
        {
            var seconds = millis / 1000
            var minutes = seconds / 60
            seconds = seconds % 60
            var hours = minutes / 60
            minutes = minutes % 60

            return String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }


    }
}