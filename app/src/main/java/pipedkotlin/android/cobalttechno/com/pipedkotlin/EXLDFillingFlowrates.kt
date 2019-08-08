package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.content.Context
import org.jetbrains.anko.db.*
import java.text.SimpleDateFormat
import java.util.*

class EXLDFillingFlowrates(var _id: Long = -1,
                           var filling_process_id: Long = -1,
                           var filling_created: String = "",
                           var filling_flowrate: Double = 0.0) {

    companion object {
        val COLUMN_ID = "_id"
        val TABLE_NAME = "EXLDFillingFlowrates"
        val COLUMN_FILLING_CREATED = "filling_created"
        val COLUMN_FILLING_FLOWRATE = "filling_flowrate"
        val COLUMN_FILLING_PROCESS_ID = "filling_process_id"

        fun getFillingFlowrates(ctx: Context, processId: Long): List<EXLDFillingFlowrates>
        {
            return ctx.database.use {
                select(EXLDFillingFlowrates.TABLE_NAME)
                        .whereArgs("${EXLDFillingFlowrates.COLUMN_FILLING_PROCESS_ID} = $processId")
                        .orderBy(EXLDFillingFlowrates.COLUMN_ID, SqlOrderDirection.DESC)
                        .exec {
                            parseList<EXLDFillingFlowrates>(classParser())
                        }
            }
        }

        fun createFlowrate(ctx: Context, value: Double, processId: Long): EXLDFillingFlowrates
        {
            var flowrate = EXLDFillingFlowrates()
            flowrate.filling_flowrate = value
            flowrate.filling_created = DateHelper.dateToDBString(Date())
            flowrate.filling_process_id = processId
            flowrate.save(ctx)

            return flowrate
        }

        fun totalWaterVolume(ctx: Context, processId: Long, pauseString: String): Double
        {
            val existingFlowrates = EXLDFillingFlowrates.getFillingFlowrates(ctx, processId)
            var flowrateBag = ArrayList<FlowrateBag>()
            for (fr in existingFlowrates)
            {
                val fr_date = DateHelper.dbStringToDateOrNull(fr.filling_created)
                var fr_value = fr.filling_flowrate

                if (fr_date != null)
                {
                    flowrateBag.add(FlowrateBag(fr_date!!, fr_value))
                }
            }

            val p = AppGlobals.instance.activeProcess
            val runStarted = DateHelper.dbStringToDateOrNull(p.filling_started)
            val swabHome = DateHelper.dbStringToDateOrNull(p.filling_stopped)
            if (runStarted != null)
            {
                return FlowrateBag.totalWaterForFlowrates(ctx, processId, flowrateBag.toList(), runStarted!!, swabHome, pauseString)
            }

            return 0.0
        }

        fun deleteFlowrates(ctx: Context, processId: Long)
        {
            ctx.database.use {
                delete(EXLDFillingFlowrates.TABLE_NAME, "${EXLDFillingFlowrates.COLUMN_FILLING_PROCESS_ID} = $processId")
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
                saveId = insert(EXLDFillingFlowrates.TABLE_NAME,
                        EXLDFillingFlowrates.COLUMN_FILLING_PROCESS_ID to filling_process_id,
                        EXLDFillingFlowrates.COLUMN_FILLING_CREATED to today,
                        EXLDFillingFlowrates.COLUMN_FILLING_FLOWRATE to filling_flowrate)
                _id = saveId
            }
        }
        else
        {
            context.database.use {
                update(EXLDFillingFlowrates.TABLE_NAME, EXLDFillingFlowrates.COLUMN_FILLING_FLOWRATE to filling_flowrate)
                        .whereArgs("${EXLDFillingFlowrates.COLUMN_ID} == $_id").exec()
            }
        }

        return  saveId
    }
}