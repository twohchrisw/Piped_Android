package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.content.Context
import android.provider.SyncStateContract.Helpers.insert
import android.provider.SyncStateContract.Helpers.update
import org.jetbrains.anko.db.*

data class EXLDSettings(val settingsID: Int,
                        val alertEmailAddress: String?,
                        val alertSMSNumber: String?,
                        val allowAuditing: String?,
                        val allowChlor: String?,
                        val allowDechlor: String?,
                        val allowDI: String?,
                        val allowFilling: String?,
                        val allowFlush: String?,
                        val allowFlush2: String?,
                        val allowPE: String?,
                        val allowSampling: String?,
                        val allowSwabbing: String?,
                        val allowSurveying: String?,
                        val companyID: String?)
{
    companion object {
        val COLUMN_ID = "settingsID"
        val TABLE_NAME = "EXLDSettings"
        val COLUMN_ALERT_EMAIL_ADDRESS = "alertEmailAddress"
        val COLUMN_ALERT_SMS_NUMBER = "alertSMSNumber"
        val COLUMN_ALLOW_AUDITING = "allowAuditing"
        val COLUMN_ALLOW_CHLOR = "allowChlor"
        val COLUMN_ALLOW_DECHLOR = "allowDechlor"
        val COLUMN_ALLOW_DI = "allowDI"
        val COLUMN_ALLOW_FILLING = "allowFilling"
        val COLUMN_ALLOW_FLUSH = "allowFlush"
        val COLUMN_ALLOW_FLUSH2 = "allowFlush2"
        val COLUMN_ALLOW_PE = "allowPE"
        val COLUMN_ALLOW_SURVEYING = "allowSurveying"
        val COLUMN_ALLOW_SAMPLING = "allowSampling"
        val COLUMN_ALLOW_SWABBING = "allowSwabbing"
        val COLUMN_COMPANY_ID = "companyId"

        fun getExistingCompanyId(context: Context): String {

            val settings = context.database.use {
                select(EXLDSettings.TABLE_NAME).whereArgs("settingsID = 1").exec {
                    parseList<EXLDSettings>(classParser())
                }
            }

            if (settings.size == 0)
            {
                return ""
            }
            else
            {
                val firstRecord = settings.get(0)
                return firstRecord.companyID ?: ""
            }
        }

        fun writeCompanyIdToDatabase(context: Context, companyId: String)
        {
            val settings = context.database.use {
                select(EXLDSettings.TABLE_NAME).whereArgs("settingsID == 1").exec {
                    parseList<EXLDSettings>(classParser())
                }
            }

            if (settings.size == 0)
            {
                // Insert
                context.database.use {
                    insert(EXLDSettings.TABLE_NAME, EXLDSettings.COLUMN_ID to 1,
                            EXLDSettings.COLUMN_COMPANY_ID to companyId
                    )
                }
            }
            else
            {
                // Update
                context.database.use {
                    update(EXLDSettings.TABLE_NAME, EXLDSettings.COLUMN_COMPANY_ID to companyId).whereArgs(EXLDSettings.COLUMN_ID + " = 1").exec()
                }
            }
        }

        fun writePermissions(context: Context, surveying: String, filling: String, pe: String, di: String, chlor: String, dechlor: String, flush: String, flush2: String, sampling: String, swabbing: String, auditing: String)
        {
            context.database.use {
                update(EXLDSettings.TABLE_NAME, EXLDSettings.COLUMN_ALLOW_SURVEYING to surveying,
                        EXLDSettings.COLUMN_ALLOW_FILLING to filling,
                        EXLDSettings.COLUMN_ALLOW_PE to pe,
                        EXLDSettings.COLUMN_ALLOW_DI to di,
                        EXLDSettings.COLUMN_ALLOW_CHLOR to chlor,
                        EXLDSettings.COLUMN_ALLOW_DECHLOR to dechlor,
                        EXLDSettings.COLUMN_ALLOW_FLUSH to flush,
                        EXLDSettings.COLUMN_ALLOW_FLUSH2 to flush2,
                        EXLDSettings.COLUMN_ALLOW_SAMPLING to sampling,
                        EXLDSettings.COLUMN_ALLOW_SWABBING to swabbing,
                        EXLDSettings.COLUMN_ALLOW_AUDITING to auditing).whereArgs(EXLDSettings.COLUMN_ID + " = 1").exec()
            }
        }
    }
}