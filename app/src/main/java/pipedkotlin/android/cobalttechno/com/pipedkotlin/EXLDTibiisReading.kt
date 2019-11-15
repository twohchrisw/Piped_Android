package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.content.Context
import org.jetbrains.anko.db.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

data class EXLDTibiisReading(var _id: Long = -1,
                             var battery: Int = -1,
                             var createdOn: String = "",
                             var flowRate: Int = -1,
                             var lognumber: Int = -1,
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

        fun getAllTibiisReadings(ctx: Context): List<EXLDTibiisReading>
        {
            return ctx.database.use {
                select(EXLDTibiisReading.TABLE_NAME)
                        .orderBy(EXLDTibiisReading.COLUMN_LOG_NUMBER)
                        .exec {
                            parseList<EXLDTibiisReading>(classParser())
                        }
            }
        }

        fun getTibiisReadingsForUpload(ctx: Context, processId: Long, testType: String): List<EXLDTibiisReading>
        {
            return ctx.database.use {
                select(EXLDTibiisReading.TABLE_NAME)
                        .whereArgs("${EXLDTibiisReading.COLUMN_PROCESS_ID} = $processId AND ${COLUMN_TEST_TYPE} = '$testType' AND ${COLUMN_UPLOADED} == 0 AND ${COLUMN_LOG_NUMBER} > 0 AND ${COLUMN_READING_TYPE} != 'After Logging'")
                        .orderBy(EXLDTibiisReading.COLUMN_LOG_NUMBER)
                        .exec {
                            parseList<EXLDTibiisReading>(classParser())
                        }
            }
        }

        fun getTibiisReadingForLogNumber(ctx: Context, processId: Long, logNumber: Int, testType: String): EXLDTibiisReading?
        {
            val readings =  ctx.database.use {
                select(EXLDTibiisReading.TABLE_NAME)
                        .whereArgs("${EXLDTibiisReading.COLUMN_PROCESS_ID} = $processId AND ${COLUMN_TEST_TYPE} = '$testType' AND ${COLUMN_LOG_NUMBER} == $logNumber")
                        .orderBy(EXLDTibiisReading.COLUMN_LOG_NUMBER)
                        .exec {
                            parseList<EXLDTibiisReading>(classParser())
                        }
            }

            return readings.firstOrNull()
        }

        fun getTibiisReadingsForPressurising(ctx: Context, processId: Long, testType: String): List<EXLDTibiisReading>
        {
            return ctx.database.use {
                select(EXLDTibiisReading.TABLE_NAME)
                        .whereArgs("${EXLDTibiisReading.COLUMN_PROCESS_ID} = $processId AND ${COLUMN_TEST_TYPE} = '$testType' AND ${COLUMN_READING_TYPE} = 'Pressurising'")
                        .orderBy(EXLDTibiisReading.COLUMN_LOG_NUMBER)
                        .exec {
                            parseList<EXLDTibiisReading>(classParser())
                        }
            }
        }


    }

    var txNumber = 0
    var logNumber = -1
    var flowrate = -1

    init {
        logNumber = lognumber   // Needed for sync
        flowrate = flowRate
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
                        COLUMN_LOG_NUMBER to lognumber,
                        COLUMN_PRESSURE to pressure,
                        COLUMN_PROCESS_ID to processId,
                        COLUMN_READING_TYPE to readingType,
                        COLUMN_TEST_TYPE to testType,
                        COLUMN_UPLOADED to uploaded)
            }

            _id = saveId
        }
        else
        {
            context.database.use {
                update(TABLE_NAME, COLUMN_UPLOADED to uploaded)
                        .whereArgs("$COLUMN_ID = $_id").exec()
            }
        }


        return saveId
    }

}

