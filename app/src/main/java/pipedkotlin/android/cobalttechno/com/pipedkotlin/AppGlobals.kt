package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.util.Log
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
    }

    var companyId = ""
    var userId = ""
    var activeProcess = EXLDProcess()
    var tibiisController = TibiisController()

}