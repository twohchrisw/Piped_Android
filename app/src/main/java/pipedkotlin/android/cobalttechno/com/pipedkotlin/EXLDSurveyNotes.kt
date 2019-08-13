package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.content.Context
import org.jetbrains.anko.db.*
import java.text.SimpleDateFormat
import java.util.*

class EXLDSurveyNotes(var _id: Long = -1,
                      var sn_process_id: Long = -1,
                      var sn_lat: Double = 0.0,
                      var sn_long: Double = 0.0,
                      var sn_note: String = "",
                      var sn_photo: String = "",
                      var sn_timestamp: String = "",
                      var uploaded: Int = 0) {

    companion object {
        val TABLE_NAME = "EXLDSurveyNotes"
        val COLUMN_ID = "_id"
        val COLUMN_SN_PROCESS_ID = "sn_process_id"
        val COLUMN_SN_LAT = "sn_lat"
        val COLUMN_SN_LONG = "sn_long"
        val COLUMN_SN_NOTE = "sn_note"
        val COLUMN_SN_PHOTO = "sn_photo"
        val COLUMN_SN_TIMESTAMP = "sn_timestamp"
        val COLUMN_UPLOADED = "uploaded"

        fun getSurveyNotes(ctx: Context, processId: Long): List<EXLDSurveyNotes>
        {
            return ctx.database.use {
                select(EXLDSurveyNotes.TABLE_NAME)
                        .whereArgs("${EXLDSurveyNotes.COLUMN_SN_PROCESS_ID} = $processId")
                        .orderBy(EXLDSurveyNotes.COLUMN_ID, SqlOrderDirection.DESC)
                        .exec {
                            parseList<EXLDSurveyNotes>(classParser())
                        }
            }
        }

        fun getSurveyNotesForUpload(ctx: Context, processId: Long): List<EXLDSurveyNotes>
        {
            return ctx.database.use {
                select(EXLDSurveyNotes.TABLE_NAME)
                        .whereArgs("${EXLDSurveyNotes.COLUMN_SN_PROCESS_ID} = $processId AND ${COLUMN_UPLOADED} = 0")
                        .orderBy(EXLDSurveyNotes.COLUMN_ID, SqlOrderDirection.DESC)
                        .exec {
                            parseList<EXLDSurveyNotes>(classParser())
                        }
            }
        }

        fun markAsUploaded(ctx: Context, items: List<EXLDSurveyNotes>)
        {
            for (s in items)
            {
                s.uploaded = 1
                s.save(ctx)
            }
        }

        fun createFlowrate(ctx: Context, processId: Long): EXLDSurveyNotes
        {
            var flowrate = EXLDSurveyNotes()
            flowrate.sn_timestamp = DateHelper.dateToDBString(Date())
            flowrate.sn_process_id = processId
            flowrate.save(ctx)

            return flowrate
        }
    }

    fun delete(context: Context)
    {
        context.database.use {
            delete(EXLDSurveyNotes.TABLE_NAME, "${EXLDSurveyNotes.COLUMN_ID} = $_id")
        }
    }

    fun save(context: Context): Long
    {
        val sdf = SimpleDateFormat(DateHelper.DB_DATE_FORMAT)
        val today = sdf.format(Date())
        var saveId: Long = _id

        if (_id < 1)
        {
            context.database.use {
                saveId = insert(EXLDSurveyNotes.TABLE_NAME,
                        EXLDSurveyNotes.COLUMN_SN_PROCESS_ID to sn_process_id,
                        COLUMN_SN_LAT to sn_lat,
                        COLUMN_SN_LONG to sn_long,
                        COLUMN_SN_NOTE to sn_note,
                        COLUMN_SN_PHOTO to sn_photo,
                        COLUMN_SN_TIMESTAMP to today,
                        COLUMN_UPLOADED to uploaded)
                _id = saveId
            }
        }
        else
        {
            context.database.use {
                update(EXLDSurveyNotes.TABLE_NAME, COLUMN_SN_NOTE to sn_note,
                        COLUMN_SN_PHOTO to sn_photo,
                        COLUMN_SN_LAT to sn_lat,
                        COLUMN_SN_LONG to sn_long,
                        COLUMN_UPLOADED to uploaded)
                        .whereArgs("${EXLDSurveyNotes.COLUMN_ID} == $_id").exec()
            }
        }

        return  saveId
    }
}