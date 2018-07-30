package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.content.Context
import android.util.Log
import org.jetbrains.anko.db.*

data class EXLDListItems(val columnId: Int,
                         val company_id: String?,
                         val listName: String?,
                         val listItem: String?)
{
    companion object {
        val COLUMN_ID = "ID"
        val TABLE_NAME = "EXLDListItems"
        val COLUMN_COMPANY_ID = "companyId"
        val COLUMN_LIST_ITEM = "listItem"
        val COLUMN_LIST_NAME = "listName"

        fun deleteAll(context: Context)
        {
            context.database.use {
                delete(EXLDListItems.TABLE_NAME)
            }
        }

        fun addNew(context: Context, companyId: String, listItem: String, listName: String)
        {
            context.database.use {
                insert(EXLDListItems.TABLE_NAME, EXLDListItems.COLUMN_COMPANY_ID to companyId,
                        EXLDListItems.COLUMN_LIST_NAME to listName,
                        EXLDListItems.COLUMN_LIST_ITEM to listItem)
            }
        }

        fun listAll(context: Context)
        {
            val listItems = context.database.use {
                select(EXLDListItems.TABLE_NAME).exec {
                    parseList<EXLDListItems>(classParser())
                }
            }

            Log.d("cobalt", "Listing " + listItems.size.toString() + " ListItems")
            for (item in listItems)
            {
                Log.d("cobalt", "ColumnId: " + item.columnId.toString() + ", CompanyId: " + item.company_id + ", ListName: " + item.listName + " ListValue: " + item.listItem)
            }
        }
    }
}