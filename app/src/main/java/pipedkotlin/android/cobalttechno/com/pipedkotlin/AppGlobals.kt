package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.net.Uri
import android.os.Environment
import android.util.Log
import java.io.File
import java.util.logging.Logger

inline fun catchAll(message: String, action: () -> Unit) {
    try {
        action()
    } catch (t: Throwable) {
        Log.d("Cobalt","Failed to $message. ${t.message}", t)
    }
}

public class AppGlobals private constructor() {

    // Singleton setup
    private object Holder { val INSTANCE = AppGlobals() }

    companion object {
        val instance: AppGlobals by lazy { Holder.INSTANCE }

        fun uriForSavedImage(filename: String): Uri
        {
            val path = Environment.getExternalStorageDirectory().toString()
            val file = File(path, filename)
            return Uri.parse(file.absolutePath)
        }
    }

    var companyId = ""
    var userId = ""
    var activeProcess = EXLDProcess()
    var tibiisController = TibiisController()
    var peFailMessage = ""
    var peFailMessageAfterSync = false
    var sampleFailMessageAfterSync = ""
    var diFailMessageAfterSync = ""
    var excelPEReadings = PEReadings()

}