package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.content.Context
import org.jetbrains.anko.db.insert
import java.text.SimpleDateFormat
import java.util.*

data class EXLDTibiisReading(var _id: Long = -1,
                             var battery: Int = -1,
                             var createdOn: String = "",
                             var flowRate: Int = -1,
                             var logNumber: Int = -1,
                             var pressure: Int = -1,
                             var processId: Long = -1,
                             var readingType: String = "",
                             var testType: String = "",
                             var uploaded: Int = 0)
{
    companion object {
        val COLUMN_ID = "id"
        val TABLE_NAME = "EXLDTibiisReadings"
        val COLUMN_BATTERY = "battery"
        val COLUMN_CREATED_ON = "createdOn"
        val COLUMN_FLOW_RATE = "flowRate"
        val COLUMN_LOG_NUMBER = "logNumber"
        val COLUMN_PRESSURE = "pressure"
        val COLUMN_PROCESS_ID = "processId"
        val COLUMN_READING_TYPE = "readingType"
        val COLUMN_TEST_TYPE = "testType"
        val COLUMN_UPLOADED = "uploaded"
    }

    fun save(context: Context): Long
    {
        val sdf = SimpleDateFormat(DateHelper.DB_DATE_FORMAT)
        val today = sdf.format(Date())
        var saveId: Long = -1

        if (_id < 1)
        {
            context.database.use {
                saveId = insert(TABLE_NAME,
                        COLUMN_BATTERY to battery,
                        COLUMN_CREATED_ON to today,
                        COLUMN_FLOW_RATE to flowRate,
                        COLUMN_LOG_NUMBER to logNumber,
                        COLUMN_PRESSURE to pressure,
                        COLUMN_PROCESS_ID to processId,
                        COLUMN_READING_TYPE to readingType,
                        COLUMN_TEST_TYPE to testType,
                        COLUMN_UPLOADED to uploaded)
            }
        }

        // No need for an update for readings

        return saveId
    }

}

