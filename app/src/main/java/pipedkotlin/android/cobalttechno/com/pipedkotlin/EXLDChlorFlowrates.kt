package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.content.Context
import org.jetbrains.anko.db.*
import java.text.SimpleDateFormat
import java.util.*

class EXLDChlorFlowrates(var chlor_id: Long = -1,
                         var chlor_process_id: Long = -1,
                         var chlor_flowrate: Double = 0.0,
                         var chlor_photo: String = "",
                         var chlor_strength: Double = 0.0,
                         var chlor_timestamp: String = "",
                         var uploaded: Int = 0) {

    companion object {
        val TABLE_NAME = "EXLDChlorFlowrates"
        val COLUMN_ID = "_id"
        val COLUMN_CHLOR_PROCESS_ID = "chlor_process_id"
        val COLUMN_CHLOR_FLOWRATE = "chlor_flowrate"
        val COLUMN_CHLOR_PHOTO = "chlor_photo"
        val COLUMN_CHLOR_STRENGTH = "chlor_strength"
        val COLUMN_CHLOR_TIMESTAMP = "chlor_timestamp"
        val COLUMN_UPLOADED = "uploaded"

        fun getChlorFlowrates(ctx: Context, processId: Long): List<EXLDChlorFlowrates>
        {
            return ctx.database.use {
                select(EXLDChlorFlowrates.TABLE_NAME)
                        .whereArgs("${EXLDChlorFlowrates.COLUMN_CHLOR_PROCESS_ID} = $processId")
                        .orderBy(EXLDChlorFlowrates.COLUMN_ID, SqlOrderDirection.DESC)
                        .exec {
                            parseList<EXLDChlorFlowrates>(classParser())
                        }
            }
        }

        fun getChlorFlowratesForUpload(ctx: Context, processId: Long): List<EXLDChlorFlowrates>
        {
            return ctx.database.use {
                select(EXLDChlorFlowrates.TABLE_NAME)
                        .whereArgs("${EXLDChlorFlowrates.COLUMN_CHLOR_PROCESS_ID} = $processId AND ${EXLDChlorFlowrates.COLUMN_UPLOADED} = 0")
                        .orderBy(EXLDChlorFlowrates.COLUMN_ID, SqlOrderDirection.DESC)
                        .exec {
                            parseList<EXLDChlorFlowrates>(classParser())
                        }
            }
        }

        fun markAsUploaded(ctx: Context, items: List<EXLDChlorFlowrates>)
        {
            for (c in items)
            {
                c.uploaded = 1
                c.save(ctx)
            }
        }

        fun createFlowrate(ctx: Context, processId: Long): EXLDChlorFlowrates
        {
            var flowrate = EXLDChlorFlowrates()
            flowrate.chlor_timestamp = DateHelper.dateToDBString(Date())
            flowrate.chlor_process_id = processId
            flowrate.save(ctx)

            return flowrate
        }

        fun totalWaterVolume(ctx: Context, processId: Long, pauseString: String): Double
        {
            val existingFlowrates = EXLDChlorFlowrates.getChlorFlowrates(ctx, processId)
            var flowrateBag = ArrayList<FlowrateBag>()
            for (fr in existingFlowrates)
            {
                val fr_date = DateHelper.dbStringToDateOrNull(fr.chlor_timestamp)
                var fr_value = fr.chlor_flowrate

                if (fr_date != null)
                {
                    flowrateBag.add(FlowrateBag(fr_date!!, fr_value))
                }
            }

            val p = AppGlobals.instance.activeProcess
            var runStarted = DateHelper.dbStringToDateOrNull(p.pt_chlor_start_time)
            var swabHome = DateHelper.dbStringToDateOrNull(p.pt_chlor_main_chlorinated)

            if (runStarted != null)
            {
                return FlowrateBag.totalWaterForFlowrates(ctx, processId, flowrateBag.toList(), runStarted!!, swabHome, pauseString)
            }

            return 0.0
        }

        fun deleteFlowrates(ctx: Context, processId: Long)
        {
            ctx.database.use {
                delete(EXLDChlorFlowrates.TABLE_NAME, "${EXLDChlorFlowrates.COLUMN_CHLOR_PROCESS_ID} = $processId")
            }
        }
    }

    fun save(context: Context): Long
    {
        val sdf = SimpleDateFormat(DateHelper.DB_DATE_FORMAT)
        val today = sdf.format(Date())
        var saveId: Long = chlor_id

        if (chlor_id < 1)
        {
            context.database.use {
                saveId = insert(EXLDChlorFlowrates.TABLE_NAME,
                        EXLDChlorFlowrates.COLUMN_CHLOR_PROCESS_ID to chlor_process_id,
                        EXLDChlorFlowrates.COLUMN_CHLOR_FLOWRATE to chlor_flowrate,
                        EXLDChlorFlowrates.COLUMN_CHLOR_PHOTO to chlor_photo,
                        EXLDChlorFlowrates.COLUMN_CHLOR_STRENGTH to chlor_strength,
                        EXLDChlorFlowrates.COLUMN_CHLOR_TIMESTAMP to today,
                        EXLDChlorFlowrates.COLUMN_UPLOADED to uploaded)
                chlor_id = saveId
            }
        }
        else
        {
            context.database.use {
                update(EXLDChlorFlowrates.TABLE_NAME, EXLDChlorFlowrates.COLUMN_CHLOR_FLOWRATE to chlor_flowrate,
                        EXLDChlorFlowrates.COLUMN_CHLOR_STRENGTH to chlor_strength,
                        EXLDChlorFlowrates.COLUMN_CHLOR_PHOTO to chlor_photo,
                        EXLDChlorFlowrates.COLUMN_UPLOADED to uploaded)
                        .whereArgs("${EXLDChlorFlowrates.COLUMN_ID} == $chlor_id").exec()
            }
        }

        return  saveId
    }

}