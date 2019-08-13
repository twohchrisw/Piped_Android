package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class SyncManager: FileUploadManager.FileUploadManagerDelegate {

    var processBeingSynced: EXLDProcess?  = null
    var startLog = 0
    var totalLogs = 0
    var currentFilename = ""
    var isFirst = 0
    var isLast = 0

    interface SyncManagerDelegate
    {
        fun processHasSynced(process: EXLDProcess)
        fun processFailedToSync(process: EXLDProcess, errorMessage: String)
    }

    fun syncProcess(process: EXLDProcess)
    {
        if (processBeingSynced != null)
        {
            Log.d("cobsync", "Sync manager busy syncing, sync request ignored")
            return
        }

        Log.d("cobsync", "Syncing Process")
        processBeingSynced = process
        processBeingSynced!!.company_user_id = AppGlobals.instance.userId

        syncProcessHeader()
    }

    fun syncProcessHeader()
    {
        if (processBeingSynced == null)
        {
            return
        }

        // Add the flowrates
        processBeingSynced!!.chlorFlowrates = ArrayList(EXLDChlorFlowrates.getChlorFlowrates(MainApplication.applicationContext(), processBeingSynced!!.columnId))
        processBeingSynced!!.decFlowrates = ArrayList(EXLDDecFlowrates.getDecFlowrates(MainApplication.applicationContext(), processBeingSynced!!.columnId))
        processBeingSynced!!.pauseSessions = ArrayList(EXLDPauseSessions.pauseSessionForUpload(MainApplication.applicationContext(), processBeingSynced!!.columnId))
        processBeingSynced!!.equipmentExtra = ArrayList(EXLDEquipmentExtra.getExtrasForUpload(MainApplication.applicationContext(), processBeingSynced!!.columnId))
        processBeingSynced!!.fillingFlowrates = ArrayList(EXLDFillingFlowrates.getFillingFlowrates(MainApplication.applicationContext(), processBeingSynced!!.columnId))
        processBeingSynced!!.flushingFlowrates = ArrayList(EXLDFlushFlowrates.getFlushFlowratesForUpload(MainApplication.applicationContext(), processBeingSynced!!.columnId))
        processBeingSynced!!.samplingData = ArrayList(EXLDSamplingData.getSamplingFlowrates(MainApplication.applicationContext(), processBeingSynced!!.columnId))
        processBeingSynced!!.swabFlowrates = ArrayList(EXLDSwabFlowrates.getSwabbingFlowrates(MainApplication.applicationContext(), processBeingSynced!!.columnId))
        processBeingSynced!!.surveyNotes = ArrayList(EXLDSurveyNotes.getSurveyNotes(MainApplication.applicationContext(), processBeingSynced!!.columnId))


        //TODO: Get the count of tibiis readings, see iOS
        startLog = 0
        totalLogs = 0
        isFirst = 1
        isLast = 1

        syncBatch(startLog, isFirst, isLast)
    }

    fun syncBatch(startLog: Int, isFirst: Int, isLast: Int)
    {
        val gson = Gson()
        var processJson = gson.toJson(processBeingSynced!!)
        currentFilename = "k_${processBeingSynced!!.server_process_id}_${Date().time}.json"
        val uploadUrl = AppGlobals.FILE_UPLOAD_URL

        val uploadManager = FileUploadManager()
        Log.d("cobsync", "uploading ${currentFilename} to ${uploadUrl}")
        uploadManager.uploadTextFile(processJson.toString(), uploadUrl, currentFilename, MainApplication.applicationContext(), this)
    }

    /* Fileupload Manager Delegate */

    override fun uploadSuccess() {
        // Prod the server to process the file
        val urlString = "${AppGlobals.SERVICE_DOMAIN}ExcelSync.asmx/JSONSync2?filename=$currentFilename&first=$isFirst&last=$isLast"
        val queue = Volley.newRequestQueue(MainApplication.applicationContext())

        var stringRequest = StringRequest(Request.Method.GET, urlString, Response.Listener<String> { response ->

            if (isFirst == 1)
            {
                // Server Process ID
                val serverProcessId = getServerProcessIdFromXml(response.toString())
                Log.d("cobsync", "Server Process ID: $serverProcessId")
                processBeingSynced!!.server_process_id = serverProcessId
                processBeingSynced!!.save(MainApplication.applicationContext())

                uploadProcessImages()
            }

            if (isLast == 1)
            {
                updateForSuccess()
            }

        }, Response.ErrorListener {
            Log.d("cobsync", "Failure prodding server: ${urlString}")
            updateForFailure("message")
        })

        queue.add(stringRequest)
    }

    override fun uploadFailed() {
        Log.d("cobsync", "Upload Failed")
    }

    override fun imageUploadSuccess(filename: String?) {
        if (filename != null)
        {
            var tinyDb = TinyDB(MainApplication.applicationContext())
            var uploadedImages = tinyDb.getListString("uploadedImages")
            uploadedImages.add(filename!!)
            tinyDb.putListString("uploadedImages", uploadedImages)
            Log.d("cobsync", "Success uploading image ${filename!!}")
        }
    }

    override fun imageUploadFail() {

    }

    fun getServerProcessIdFromXml(xmlString: String): Int
    {
        val startIndex = xmlString.indexOf("XXX")
        val endIndex = xmlString.indexOf("ZZZ")

        if (startIndex > 0 && endIndex > 0)
        {
            val spidText = xmlString.substring(startIndex + 3, endIndex)
            val spid = spidText.toIntOrNull()
            if (spid != null)
            {
                return spid
            }
        }

        return 0
    }

    fun uploadProcessImages()
    {
        val tinyDB = TinyDB(MainApplication.applicationContext())
        val alreadyUploaded = tinyDB.getListString("uploadedImages")
        val processImages = imagesForUpload()

        for (img in processImages)
        {
            if (!alreadyUploaded.contains(img))
            {
                uploadImage(img)
            }
        }
    }

    fun uploadImage(imageName: String)
    {
        val path = Environment.getExternalStorageDirectory().toString()
        val file = File(path, imageName)
        val bitmap = BitmapFactory.decodeFile(file.path)
        val fileuploadManager = FileUploadManager()

        if (bitmap != null)
        {
            fileuploadManager.uploadImage(bitmap, AppGlobals.FILE_UPLOAD_URL, imageName, MainApplication.applicationContext(), this)
        }
    }


    fun imagesForUpload(): ArrayList<String>
    {
        var imagesToUpload = ArrayList<String>()
        val p = processBeingSynced!!

        if (p.pt_chlor_end_photo.length > 2)
        {
            imagesToUpload.add(p.pt_chlor_end_photo)
        }
        if (p.pt_chlor_start_photo.length > 2)
        {
            imagesToUpload.add(p.pt_chlor_start_photo)
        }
        if (p.pt_dec_photo.length > 2)
        {
            imagesToUpload.add(p.pt_dec_photo)
        }
        if (p.swab_photo.length > 2)
        {
            imagesToUpload.add(p.swab_photo)
        }

        for (i in EXLDChlorFlowrates.getChlorFlowrates(MainApplication.applicationContext(), p.columnId))
        {
            if (i.chlor_photo.length > 2)
            {
                imagesToUpload.add(i.chlor_photo)
            }
        }

        for (i in EXLDDecFlowrates.getDecFlowrates(MainApplication.applicationContext(), p.columnId))
        {
           if (i.dec_photo.length > 2)
           {
               imagesToUpload.add(i.dec_photo)
           }
        }

        for (i in EXLDSamplingData.getSamplingFlowrates(MainApplication.applicationContext(), p.columnId))
        {
            if (i.sampl_photo.length > 2)
            {
                imagesToUpload.add(i.sampl_photo)
            }
        }

        for (i in EXLDSurveyNotes.getSurveyNotes(MainApplication.applicationContext(), p.columnId))
        {
            if (i.sn_photo.length > 2)
            {
                imagesToUpload.add(i.sn_photo)
            }
        }

        return imagesToUpload
    }



    fun updateForSuccess()
    {
        if (processBeingSynced == null)
        {
            Log.d("cobsync", "process or context is null on updateDelegatesForSuccess")
            return
        }

        MainApplication.applicationContext().run {
            processBeingSynced!!.last_sync_millis = Date().time + 2000  // To ensure the sync time is greater than the update time
            processBeingSynced!!.save(MainApplication.applicationContext())
        }

        Log.d("cobsync", "Process being synced is now set to null")
        AppGlobals.instance.processListActivity?.updateRecycler()
        AppGlobals.instance.processMenuActivity?.syncCompleted()
        processBeingSynced = null

    }

    fun updateForFailure(errorMessage: String)
    {
        if (processBeingSynced == null)
        {
            Log.d("cobsync", "process is nulll on updateDelegatesForFailure")
            return
        }

    }


}