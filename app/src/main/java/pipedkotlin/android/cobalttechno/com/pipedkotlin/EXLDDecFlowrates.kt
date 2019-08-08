package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.content.Context
import org.jetbrains.anko.db.*
import java.text.SimpleDateFormat
import java.util.*

class EXLDDecFlowrates(var _id: Long = -1,
                       var dec_process_id: Long = -1,
                       var dec_discharge: Double = 0.0,
                       var dec_flowrate: Double = 0.0,
                       var dec_photo: String = "",
                       var dec_strength: Double = 0.0,
                       var dec_timestamp: String = "")
{
    companion object {
        val TABLE_NAME = "EXLDDecFlowrates"
        val COLUMN_ID = "_id"
        val COLUMN_DEC_PROCESS_ID = "dec_process_id"
        val COLUMN_DEC_DISCHARGE = "dec_discharge"
        val COLUMN_DEC_FLOWRATE = "dec_flowrate"
        val COLUMN_DEC_PHOTO = "dec_photo"
        val COLUMN_DEC_STRENGTH = "dec_strength"
        val COLUMN_DEC_TIMESTAMP = "dec_timestamp"

        fun getDecFlowrates(ctx: Context, processId: Long): List<EXLDDecFlowrates>
        {
            return ctx.database.use {
                select(EXLDDecFlowrates.TABLE_NAME)
                        .whereArgs("${EXLDDecFlowrates.COLUMN_DEC_PROCESS_ID} = $processId")
                        .orderBy(EXLDDecFlowrates.COLUMN_ID, SqlOrderDirection.DESC)
                        .exec {
                            parseList<EXLDDecFlowrates>(classParser())
                        }
            }
        }

        fun createFlowrate(ctx: Context, processId: Long): EXLDDecFlowrates
        {
            var flowrate = EXLDDecFlowrates()
            flowrate.dec_timestamp = DateHelper.dateToDBString(Date())
            flowrate.dec_process_id = processId
            flowrate.save(ctx)

            return flowrate
        }

        fun totalWaterVolume(ctx: Context, processId: Long, pauseString: String): Double
        {
            val existingFlowrates = EXLDDecFlowrates.getDecFlowrates(ctx, processId)
            var flowrateBag = ArrayList<FlowrateBag>()
            for (fr in existingFlowrates)
            {
                val fr_date = DateHelper.dbStringToDateOrNull(fr.dec_timestamp)
                var fr_value = fr.dec_flowrate

                if (fr_date != null)
                {
                    flowrateBag.add(FlowrateBag(fr_date!!, fr_value))
                }
            }

            val p = AppGlobals.instance.activeProcess
            var runStarted = DateHelper.dbStringToDateOrNull(p.pt_dec_start)
            var swabHome = DateHelper.dbStringToDateOrNull(p.pt_dec_dechlorinated)

            if (runStarted != null)
            {
                return FlowrateBag.totalWaterForFlowrates(ctx, processId, flowrateBag.toList(), runStarted!!, swabHome, pauseString)
            }

            return 0.0
        }

        fun deleteFlowrates(ctx: Context, processId: Long)
        {
            ctx.database.use {
                delete(EXLDDecFlowrates.TABLE_NAME, "${EXLDDecFlowrates.COLUMN_DEC_PROCESS_ID} = $processId")
            }
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
                saveId = insert(EXLDDecFlowrates.TABLE_NAME,
                        EXLDDecFlowrates.COLUMN_DEC_PROCESS_ID to dec_process_id,
                        EXLDDecFlowrates.COLUMN_DEC_DISCHARGE to dec_discharge,
                        EXLDDecFlowrates.COLUMN_DEC_FLOWRATE to dec_flowrate,
                        EXLDDecFlowrates.COLUMN_DEC_PHOTO to dec_photo,
                        EXLDDecFlowrates.COLUMN_DEC_STRENGTH to dec_strength,
                        EXLDDecFlowrates.COLUMN_DEC_TIMESTAMP to today)
                _id = saveId
            }
        }
        else
        {
            context.database.use {
                update(EXLDDecFlowrates.TABLE_NAME,
                        EXLDDecFlowrates.COLUMN_DEC_DISCHARGE to dec_discharge,
                        EXLDDecFlowrates.COLUMN_DEC_FLOWRATE to dec_flowrate,
                        EXLDDecFlowrates.COLUMN_DEC_PHOTO to dec_photo,
                        EXLDDecFlowrates.COLUMN_DEC_STRENGTH to dec_strength)
                        .whereArgs("${EXLDDecFlowrates.COLUMN_ID} == $_id").exec()
            }
        }

        return  saveId
    }
}