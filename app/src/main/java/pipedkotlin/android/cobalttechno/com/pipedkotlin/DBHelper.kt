package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.*

class DBHelper(ctx: Context) : ManagedSQLiteOpenHelper(ctx, "PipedDatabase", null, 1)
{
    companion object {
        private var instance: DBHelper? = null

        @Synchronized
        fun getInstance(ctx: Context): DBHelper {
            if (instance == null)
            {
                instance = DBHelper(ctx.applicationContext)
            }
            return instance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase?) {

        // Settings
        db!!.createTable(EXLDSettings.TABLE_NAME, true,
                EXLDSettings.COLUMN_ID to INTEGER + PRIMARY_KEY + UNIQUE,
                EXLDSettings.COLUMN_ALERT_EMAIL_ADDRESS to TEXT,
                EXLDSettings.COLUMN_ALERT_SMS_NUMBER to TEXT,
                EXLDSettings.COLUMN_ALLOW_AUDITING to TEXT,
                EXLDSettings.COLUMN_ALLOW_CHLOR to TEXT,
                EXLDSettings.COLUMN_ALLOW_DECHLOR to TEXT,
                EXLDSettings.COLUMN_ALLOW_DI to TEXT,
                EXLDSettings.COLUMN_ALLOW_FILLING to TEXT,
                EXLDSettings.COLUMN_ALLOW_FLUSH to TEXT,
                EXLDSettings.COLUMN_ALLOW_FLUSH2 to TEXT,
                EXLDSettings.COLUMN_ALLOW_PE to TEXT,
                EXLDSettings.COLUMN_ALLOW_SAMPLING to TEXT,
                EXLDSettings.COLUMN_ALLOW_SWABBING to TEXT,
                EXLDSettings.COLUMN_ALLOW_SURVEYING to TEXT,
                EXLDSettings.COLUMN_COMPANY_ID to TEXT)

        // List Items
        db!!.createTable(EXLDListItems.TABLE_NAME, true,
                EXLDListItems.COLUMN_ID to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
                EXLDListItems.COLUMN_COMPANY_ID to TEXT,
                EXLDListItems.COLUMN_LIST_NAME to TEXT,
                EXLDListItems.COLUMN_LIST_ITEM to TEXT)

        // Clients
        db!!.createTable(EXLDClients.TABLE_NAME, true,
                EXLDClients.COLUMN_ID to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
                EXLDClients.COLUMN_CLIENT_ID to TEXT,
                EXLDClients.COLUMN_CLIENT_NAME to TEXT,
                EXLDClients.COLUMN_COMPANY_ID to TEXT)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }
}

val Context.database: DBHelper get() = DBHelper.getInstance(applicationContext)
