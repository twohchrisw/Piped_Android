package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.content.Context
import org.jetbrains.anko.db.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class EXLDSwabFlowrates(var _id: Long = -1,
                        var swab_created: String = "",
                        var swab_flowrate: Double = 0.0,
                        var swab_process_id: Long = -1) {

    companion object {
        var COLUMN_ID = "_id"
        var TABLE_NAME = "EXLDSwabFlowrates"
        var COLUMN_SWAB_CREATED = "swab_created"
        var COLUMN_SWAB_FLOWRATE = "swab_flowrate"
        var COLUMN_PROCESS_ID = "swab_process_id"

        fun getSwabbingFlowrates(ctx: Context, processId: Long): List<EXLDSwabFlowrates>
        {
             return ctx.database.use {
                 select(EXLDSwabFlowrates.TABLE_NAME)
                         .whereArgs("${EXLDSwabFlowrates.COLUMN_PROCESS_ID} = $processId")
                         .orderBy(EXLDSwabFlowrates.COLUMN_ID)
                         .exec {
                             parseList<EXLDSwabFlowrates>(classParser())
                         }
             }
        }

        fun createFlowrate(ctx: Context, value: Double, processId: Long): EXLDSwabFlowrates
        {
            var flowrate = EXLDSwabFlowrates()
            flowrate.swab_flowrate = value
            flowrate.swab_created = DateHelper.dateToDBString(Date())
            flowrate.swab_process_id = processId
            flowrate.save(ctx)

            return flowrate
        }

        fun totalWaterVolume(ctx: Context, processId: Long, pauseString: String): Double
        {
            val existingFlowrates = getSwabbingFlowrates(ctx, processId)
            var flowrateBag = ArrayList<FlowrateBag>()
            for (fr in existingFlowrates)
            {
                val fr_date = DateHelper.dbStringToDateOrNull(fr.swab_created)
                var fr_value = fr.swab_flowrate

                if (fr_date != null)
                {
                    flowrateBag.add(FlowrateBag(fr_date!!, fr_value))
                }
            }

            val p = AppGlobals.instance.activeProcess
            val runStarted = DateHelper.dbStringToDateOrNull(p.swab_run_started)
            val swabHome = DateHelper.dbStringToDateOrNull(p.swab_home)
            if (runStarted != null)
            {
                return FlowrateBag.totalWaterForFlowrates(ctx, processId, flowrateBag.toList(), runStarted!!, swabHome, pauseString)
            }

            return 0.0
        }

        fun deleteFlowrates(ctx: Context, processId: Long)
        {
            ctx.database.use {
                delete(EXLDSwabFlowrates.TABLE_NAME, "${EXLDSwabFlowrates.COLUMN_PROCESS_ID} = $processId")
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
                        COLUMN_PROCESS_ID to swab_process_id,
                        COLUMN_SWAB_CREATED to today,
                        COLUMN_SWAB_FLOWRATE to swab_flowrate)
                _id = saveId
            }
        }
        else
        {
            context.database.use {
                update(TABLE_NAME, COLUMN_SWAB_FLOWRATE to swab_flowrate)
                        .whereArgs("$COLUMN_ID == $_id").exec()
            }
        }

        return  saveId
    }

}