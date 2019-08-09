package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.content.Context
import org.jetbrains.anko.db.*
import java.text.SimpleDateFormat
import java.util.*

class EXLDSamplingData(var _id: Long = -1,
                       var samp_process_id: Long = -1,
                       var samp_chlor_free: Double = 0.0,
                       var samp_chlor_total: Double = 0.0,
                       var samp_desc: String = "",
                       var samp_failnotes: String = "",
                       var samp_lat: Double = 0.0,
                       var samp_lng: Double = 0.0,
                       var samp_sample_id: String = "",
                       var samp_location: String = "",
                       var samp_other_info: String = "",
                       var samp_photo: String = "",
                       var samp_test_status: Int = 0,
                       var samp_timestamp: String = "",
                       var samp_turbidity: Double = 0.0,
                       var samp_water_temp: Int = 0) {

    companion object {
        val TABLE_NAME = "EXLDSamplingData"
        val COLUMN_SAMP_ID = "_id"
        val COLUMN_PROCESS_ID = "sampl_process_id"
        val COLUMN_SAMP_CHLOR_FREE = "sampl_chlor_free"
        val COLUMN_SAMP_CHLOR_TOTAL = "sampl_chlor_total"
        val COLUMN_SAMP_DESC = "sampl_desc"
        val COLUMN_SAMP_FAILNOTES  = "sampl_failnotes"
        val COLUMN_SAMP_LAT = "sampl_lat"
        val COLUMN_SAMP_LNG = "sampl_long"
        val COLUMN_SAMP_SAMPLE_ID = "sampl_sample_id"
        val COLUMN_SAMP_LOCATION = "sampl_location"
        val COLUMN_SAMP_OTHER_INFO = "sampl_other_info"
        val COLUMN_SAMP_PHOTO = "sampl_photo"
        val COLUMN_SAMP_TEST_STATUS = "sampl_test_status"
        val COLUMN_SAMP_TIMESTAMP = "sampl_timestamp"
        val COLUMN_SAMP_TURBIDITY = "sampl_turbidity"
        val COLUMN_SAMP_WATER_TEMP = "sampl_water_temp"

        fun createFlowrate(ctx: Context, processId: Long): EXLDSamplingData
        {
            var flowrate = EXLDSamplingData()
            flowrate.samp_process_id = processId
            flowrate.save(ctx)

            return flowrate
        }

        fun getSamplingFlowrates(ctx: Context, processId: Long): List<EXLDSamplingData>
        {
            return ctx.database.use {
                select(EXLDSamplingData.TABLE_NAME)
                        .whereArgs("${EXLDSamplingData.COLUMN_PROCESS_ID} = $processId")
                        .orderBy(EXLDSamplingData.COLUMN_SAMP_ID, SqlOrderDirection.DESC)
                        .exec {
                            parseList<EXLDSamplingData>(classParser())
                        }
            }
        }
    }

    fun save(ctx: Context): Long
    {
        val sdf = SimpleDateFormat(DateHelper.DB_DATE_FORMAT)
        val today = sdf.format(Date())
        var saveId: Long = _id

        if (_id < 1)
        {
            ctx.database.use {
                saveId = insert(TABLE_NAME,
                        COLUMN_PROCESS_ID to samp_process_id,
                        COLUMN_SAMP_CHLOR_FREE to samp_chlor_free,
                        COLUMN_SAMP_CHLOR_TOTAL to samp_chlor_total,
                        COLUMN_SAMP_DESC to samp_desc,
                        COLUMN_SAMP_FAILNOTES to samp_failnotes,
                        COLUMN_SAMP_LAT to samp_lat,
                        COLUMN_SAMP_LNG to samp_lng,
                        COLUMN_SAMP_SAMPLE_ID to samp_sample_id,
                        COLUMN_SAMP_LOCATION to samp_location,
                        COLUMN_SAMP_OTHER_INFO to samp_other_info,
                        COLUMN_SAMP_PHOTO to samp_photo,
                        COLUMN_SAMP_TEST_STATUS to samp_test_status,
                        COLUMN_SAMP_TIMESTAMP to today,
                        COLUMN_SAMP_TURBIDITY to samp_turbidity,
                        COLUMN_SAMP_WATER_TEMP to samp_water_temp)
                _id = saveId
            }
        }
        else
        {
            ctx.database.use {
                update(TABLE_NAME,COLUMN_SAMP_CHLOR_FREE to samp_chlor_free,
                        COLUMN_SAMP_CHLOR_TOTAL to samp_chlor_total,
                        COLUMN_SAMP_DESC to samp_desc,
                        COLUMN_SAMP_FAILNOTES to samp_failnotes,
                        COLUMN_SAMP_LAT to samp_lat,
                        COLUMN_SAMP_LNG to samp_lng,
                        COLUMN_SAMP_SAMPLE_ID to samp_sample_id,
                        COLUMN_SAMP_LOCATION to samp_location,
                        COLUMN_SAMP_OTHER_INFO to samp_other_info,
                        COLUMN_SAMP_PHOTO to samp_photo,
                        COLUMN_SAMP_TEST_STATUS to samp_test_status,
                        COLUMN_SAMP_TIMESTAMP to today,
                        COLUMN_SAMP_TURBIDITY to samp_turbidity,
                        COLUMN_SAMP_WATER_TEMP to samp_water_temp)
                        .whereArgs("${EXLDSamplingData.COLUMN_SAMP_ID} == $_id").exec()
            }
        }

        return saveId
    }

}