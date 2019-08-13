package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.content.Context
import android.widget.TableLayout
import org.jetbrains.anko.db.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CountDownLatch

class EXLDPauseSessions(var _id: Long = -1,
                        var pause_duration_seconds: Long = 0,
                        var pause_end: String = "",
                        var pause_flowrate: Double = 0.0,
                        var pause_process_id: Long = -1,
                        var pause_start: String = "",
                        var pause_type: String = "" ,
                        var uploaded: Int = 0) {

    companion object {
        val PAUSE_TYPE_CHLOR = "chlor"
        val PAUSE_TYPE_DECHLOR = "dechlor"
        val PAUSE_TYPE_FILLING = "filling"
        val PAUSE_TYPE_SWABBING = "swabbing"
        val PAUSE_TYPE_FLUSH = "flush"
        val PAUSE_TYPE_FLUSH2 = "flush2"

        val TABLE_NAME = "EXLDPauseSessions"
        val COLUMN_ID = "_id"
        val COLUMN_PAUSE_END = "pause_end"
        val COLUMN_PAUSE_FLOWRATE = "pause_flowrate"
        val COLUMN_PAUSE_PROCESS_ID = "pause_process_id"
        val COLUMN_PAUSE_START = "pause_start"
        val COLUMN_PAUSE_TYPE = "pause_type"
        val COLUMN_PAUSE_DURATION_SECONDS = "pause_duration_seconds"
        val COLUMN_PAUSE_UPLOADED = "uploaded"

        fun pauseSessions(ctx: Context, processId: Long, pauseType: String): List<EXLDPauseSessions>
        {
            return ctx.database.use {
                select(EXLDPauseSessions.TABLE_NAME)
                        .whereArgs("${EXLDPauseSessions.COLUMN_PAUSE_PROCESS_ID} = $processId AND ${EXLDPauseSessions.COLUMN_PAUSE_TYPE} = '$pauseType'")
                        .orderBy(EXLDPauseSessions.COLUMN_ID)
                        .exec {
                            parseList<EXLDPauseSessions>(classParser())
                        }
            }
        }

        fun pauseSessionForUpload(ctx: Context, processId: Long): List<EXLDPauseSessions>
        {
            return ctx.database.use {
                select(EXLDPauseSessions.TABLE_NAME)
                        .whereArgs("${EXLDPauseSessions.COLUMN_PAUSE_PROCESS_ID} = $processId")
                        .orderBy(EXLDPauseSessions.COLUMN_ID)
                        .exec {
                            parseList<EXLDPauseSessions>(classParser())
                        }
            }
        }

        fun markAsUploaded(ctx: Context, items: List<EXLDPauseSessions>)
        {
            for (p in items)
            {
                p.uploaded = 1
                p.save(ctx)
            }
        }
    }

    fun totalPauseSeconds(): Long
    {
        val start = DateHelper.dbStringToDateOrNull(pause_start)
        val end = DateHelper.dbStringToDateOrNull(pause_end)

        if (start != null && end != null)
        {
            return (end.time - start.time) / 1000
        }

        return 0
    }

    fun save(ctx: Context): Long
    {
        val sdf = SimpleDateFormat(DateHelper.DB_DATE_FORMAT)
        val today = sdf.format(Date())
        var saveId: Long = pause_process_id

        if (_id < 1)
        {
            ctx.database.use {
                saveId = insert(TABLE_NAME,
                        COLUMN_PAUSE_END to pause_end,
                        COLUMN_PAUSE_START to pause_start,
                        COLUMN_PAUSE_FLOWRATE to pause_flowrate,
                        COLUMN_PAUSE_PROCESS_ID to pause_process_id,
                        COLUMN_PAUSE_TYPE to pause_type,
                        COLUMN_PAUSE_DURATION_SECONDS to pause_duration_seconds,
                        COLUMN_PAUSE_END to pause_end,
                        COLUMN_PAUSE_UPLOADED to uploaded)
                _id = saveId
            }
        }
        else
        {
            ctx.database.use {
                update(TABLE_NAME,
                        COLUMN_PAUSE_END to pause_end,
                        COLUMN_PAUSE_START to pause_start,
                        COLUMN_PAUSE_FLOWRATE to pause_flowrate,
                        COLUMN_PAUSE_PROCESS_ID to pause_process_id,
                        COLUMN_PAUSE_TYPE to pause_type,
                        COLUMN_PAUSE_DURATION_SECONDS to pause_duration_seconds,
                        COLUMN_PAUSE_END to pause_end,
                        COLUMN_PAUSE_UPLOADED to uploaded)
                        .whereArgs("$COLUMN_ID = $_id")
                        .exec()
            }
        }

        return saveId
    }
}