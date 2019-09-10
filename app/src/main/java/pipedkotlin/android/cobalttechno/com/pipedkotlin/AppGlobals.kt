package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.net.Uri
import android.os.Environment
import android.util.Log
import java.io.File

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
        val SERVICE_DOMAIN = "http://pipedapp-001-site1.dtempurl.com/"
        val FILE_UPLOAD_URL = "${SERVICE_DOMAIN}fileupload.php"


        fun uriForSavedImage(filename: String): Uri
        {
            val path = Environment.getExternalStorageDirectory().toString()
            val file = File(path, filename)
            return Uri.parse(file.absolutePath)
        }

        enum class FlowrateViewType {
            None, Chlor, DeChlor, Sampling
        }
    }

    var companyId = ""
    var userId = ""
    var activeProcess = EXLDProcess()
    var tibiisController = TibiisController()
    var syncManager = SyncManager()
    var peFailMessage = ""
    var peFailMessageAfterSync = false
    var sampleFailMessageAfterSync = ""
    var diFailMessageAfterSync = ""
    var excelPEReadings = PEReadings()
    var currentFlushType = 1
    var processMenuShowingTasks = false
    var currentFlowrateActivityType = FlowrateViewType.None
    var processListActivity: ProcessListActivity? = null
    var processMenuActivity: ProcessMenuActivity? = null

    var drillChlorFlowrate: EXLDChlorFlowrates? = null
    var drillDecFlowrate: EXLDDecFlowrates? = null
    var drillSamplFlorwate: EXLDSamplingData? = null

    var lastLat: Double = 0.0
    var lastLng: Double = 0.0

    val DI_TEST_MODE = true
    val DI_TESTING_ZERO_LOSS_VALUE = 0.00999
    val DI_TESTING_VALUE = 0.201
    val DI_15_MIN_MAXIMUM = 0.051

}