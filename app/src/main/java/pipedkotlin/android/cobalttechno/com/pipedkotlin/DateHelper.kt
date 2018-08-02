package pipedkotlin.android.cobalttechno.com.pipedkotlin

import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

class DateHelper {
    companion object {

        val DB_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"
        val DISPLAY_DATE_FORMAT = "dd-MM-yyyy HH:mm"

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

    }
}