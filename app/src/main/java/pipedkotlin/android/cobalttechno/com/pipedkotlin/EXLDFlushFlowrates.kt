package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.content.Context
import org.jetbrains.anko.db.*
import java.text.SimpleDateFormat
import java.util.*

class EXLDFlushFlowrates(var _id: Long = -1,
                         var flush_process_id: Long = -1,
                         var flush_created: String = "",
                         var flush_flowrate: Double = 0.0,
                         var flush_type: Int = 1) {

    companion object {
        val TABLE_NAME = "EXLDFlushFlowrates"
        val COLUMN_ID = "_id"
        val COLUMN_FLUSH_PROCESS_ID = "flush_process_id"
        val COLUMN_FLUSH_CREATED = "flush_created"
        val COLUMN_FLUSH_FLOWRATE = "flush_flowrate"
        val COLUMN_FLUSH_TYPE = "flush_type"

        fun getFlushFlowrates(ctx: Context, processId: Long, flushType: Int): List<EXLDFlushFlowrates>
        {
            return ctx.database.use {
                select(EXLDFlushFlowrates.TABLE_NAME)
                        .whereArgs("${EXLDFlushFlowrates.COLUMN_FLUSH_PROCESS_ID} = $processId AND ${EXLDFlushFlowrates.COLUMN_FLUSH_TYPE} == $flushType")
                        .orderBy(EXLDFillingFlowrates.COLUMN_ID, SqlOrderDirection.DESC)
                        .exec {
                            parseList<EXLDFlushFlowrates>(classParser())
                        }
            }
        }

        fun createFlowrate(ctx: Context, value: Double, processId: Long, flushType: Int): EXLDFlushFlowrates
        {
            var flowrate = EXLDFlushFlowrates()
            flowrate.flush_flowrate = value
            flowrate.flush_created = DateHelper.dateToDBString(Date())
            flowrate.flush_process_id = processId
            flowrate.flush_type = flushType
            flowrate.save(ctx)

            return flowrate
        }

        fun totalWaterVolume(ctx: Context, processId: Long, pauseString: String, flushType: Int): Double
        {
            val existingFlowrates = EXLDFlushFlowrates.getFlushFlowrates(ctx, processId, flushType)
            var flowrateBag = ArrayList<FlowrateBag>()
            for (fr in existingFlowrates)
            {
                val fr_date = DateHelper.dbStringToDateOrNull(fr.flush_created)
                var fr_value = fr.flush_flowrate

                if (fr_date != null)
                {
                    flowrateBag.add(FlowrateBag(fr_date!!, fr_value))
                }
            }

            val p = AppGlobals.instance.activeProcess
            var runStarted = DateHelper.dbStringToDateOrNull(p.pt_flush_started)
            var swabHome = DateHelper.dbStringToDateOrNull(p.pt_flush_completed)

            if (flushType == 2)
            {
                runStarted = DateHelper.dbStringToDateOrNull(p.pt_flush_started2)
                swabHome = DateHelper.dbStringToDateOrNull(p.pt_flush_completed2)
            }

            if (runStarted != null)
            {
                return FlowrateBag.totalWaterForFlowrates(ctx, processId, flowrateBag.toList(), runStarted!!, swabHome, pauseString)
            }

            return 0.0
        }

        fun deleteFlowrates(ctx: Context, processId: Long, flushType: Int)
        {
            ctx.database.use {
                delete(EXLDFlushFlowrates.TABLE_NAME, "${EXLDFlushFlowrates.COLUMN_FLUSH_PROCESS_ID} = $processId AND ${EXLDFlushFlowrates.COLUMN_FLUSH_TYPE} = $flushType")
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
                saveId = insert(TABLE_NAME,
                        COLUMN_FLUSH_PROCESS_ID to flush_process_id,
                        EXLDFlushFlowrates.COLUMN_FLUSH_CREATED to today,
                        EXLDFlushFlowrates.COLUMN_FLUSH_TYPE to flush_type,
                        EXLDFlushFlowrates.COLUMN_FLUSH_FLOWRATE to flush_flowrate)
                _id = saveId
            }
        }
        else
        {
            context.database.use {
                update(EXLDFlushFlowrates.TABLE_NAME, EXLDFlushFlowrates.COLUMN_FLUSH_FLOWRATE to flush_flowrate)
                        .whereArgs("${EXLDFlushFlowrates.COLUMN_ID} == $_id").exec()
            }
        }

        return  saveId
    }
}