package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.content.Context
import org.jetbrains.anko.db.*

class EXLDEquipmentExtra(var ee_id: Long = -1,
                         var ee_process_id: Long = -1,
                         var ee_desc: String = "",
                         var ee_type: Int = 0) {

    public enum class EquipSection(val value: Int)
    {
        Dosing(0), PTP(1), Leak(2), Measure(3), Other(4)
    }

    companion object {

        val TABLE_NAME = "EXLDEquipmentExtra"
        val COLUMN_EE_ID = "ee_id"
        val COLUMN_EE_PROCESS_ID = "ee_process_id"
        val COLUMN_EE_DESC = "ee_desc"
        val COLUMN_EE_TYPE = "ee_type"

        fun getExtrasForSection(ctx: Context, pid: Long, section: Int): List<EXLDEquipmentExtra>
        {

            return ctx.database.use {
                select(EXLDEquipmentExtra.TABLE_NAME)
                        .whereArgs("${EXLDEquipmentExtra.COLUMN_EE_PROCESS_ID} = $pid AND ${EXLDEquipmentExtra.COLUMN_EE_TYPE} == $section")
                        .orderBy(EXLDEquipmentExtra.COLUMN_EE_ID)
                        .exec {
                            parseList<EXLDEquipmentExtra>(classParser())
                        }
            }
        }

        fun getExtrasForUpload(ctx: Context, pid: Long): List<EXLDEquipmentExtra>
        {
            return ctx.database.use {
                select(EXLDEquipmentExtra.TABLE_NAME)
                        .whereArgs("${EXLDEquipmentExtra.COLUMN_EE_PROCESS_ID} = $pid")
                        .orderBy(EXLDEquipmentExtra.COLUMN_EE_ID)
                        .exec {
                            parseList<EXLDEquipmentExtra>(classParser())
                        }
            }
        }

        fun addExtra(ctx: Context, pid: Long, desc: String, section: Int)
        {
            ctx.database.use {
                val id = insert(TABLE_NAME,
                        COLUMN_EE_PROCESS_ID to pid,
                        COLUMN_EE_DESC to desc,
                        COLUMN_EE_TYPE to section)
            }
        }
    }
}