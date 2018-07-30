package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.content.Context
import android.util.Log
import org.jetbrains.anko.db.*

data class EXLDClients(val columnId: Int,
                       val clientId: String?,
                       val clientName: String?,
                       val companyId: String?)
{
    companion object {
        val COLUMN_ID = "ID"
        val TABLE_NAME = "EXLDClients"
        val COLUMN_CLIENT_ID = "clientId"
        val COLUMN_CLIENT_NAME = "clientName"
        val COLUMN_COMPANY_ID = "companyId"

        fun deleteAll(context: Context)
        {
            context.database.use {
                delete(EXLDClients.TABLE_NAME)
            }
        }

        fun addNew(context: Context, clientId: String, clientName: String, companyId: String)
        {
            context.database.use {
                insert(EXLDClients.TABLE_NAME, EXLDClients.COLUMN_CLIENT_ID to clientId,
                        EXLDClients.COLUMN_CLIENT_NAME to clientName,
                        EXLDClients.COLUMN_COMPANY_ID to companyId)
            }
        }

        fun listAll(context: Context)
        {
            val listItems = context.database.use {
                select(EXLDClients.TABLE_NAME).exec {
                    parseList<EXLDClients>(classParser())
                }
            }

            Log.d("cobalt", "Listing " + listItems.size.toString() + " Clients")
            for (item in listItems)
            {
                Log.d("cobalt", "ColumnId: " + item.columnId.toString() + ", ClientId: " + item.clientId + ", ClientName: " + item.clientName + ", CompanyId: " + item.companyId)
            }
        }

    }
}