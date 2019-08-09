package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.content.Intent
import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import java.util.*

class SwabbingActivity : BaseActivity(), StandardRecyclerAdapter.StandardRecyclerAdapterInterface {

    private lateinit var recyclerView: RecyclerView
    var photoField = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_swabbing)
        title = "Swabbing"

        setupLocationClient()
        getCurrentLocation(::locationReceived)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = StandardRecyclerAdapter(this, StandardRecyclerAdapter.PipedTask.Swabbing, AppGlobals.instance.lastLat, AppGlobals.instance.lastLng, this)

    }

    fun locationReceived(lat: Double, lng: Double) {
        AppGlobals.instance.lastLat = lat
        AppGlobals.instance.lastLng = lng
        recyclerView.adapter = StandardRecyclerAdapter(this, StandardRecyclerAdapter.PipedTask.Swabbing, AppGlobals.instance.lastLat, AppGlobals.instance.lastLng, this)
    }

    override fun didRequestMainImage(fieldName: String) {
        photoField = ""
        Log.d("cobswab", "Picture Requested")
        requestCameraPermissions()
    }

    override fun didRequestNotes(fieldName: String) {
        Log.d("cobswab", "Did request notes")
        setNotes(AppGlobals.instance.activeProcess.swab_notes)
    }

    override fun didRequestFlowrate(position: Int) {

    }

    override fun cameraPermissionsGranted() {
        super.cameraPermissionsGranted()

        val p = AppGlobals.instance.activeProcess
        if (p.swab_photo.length < 2)
        {
            // Straight load
            choosePicFromCamera()
        }
        else
        {
            // Ask for user direction
            val alert = AlertHelper(this)
            alert.dialogForOKAlert("Delete Image", "Do you want to delete this image?", {
                p.swab_photo = ""
                p.save(this)
                runOnUiThread {
                    recyclerView.adapter.notifyDataSetChanged()
                }
            })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val p = AppGlobals.instance.activeProcess

        if (requestCode == CAMERA_REQUEST_CAMERA)
        {
            val uuid = UUID.randomUUID().toString()
            val fileName = "survey_${uuid}.jpg"

            if (data != null)
            {
                val bitmap = data!!.extras.get("data") as Bitmap
                saveImageToExternalStorage(bitmap, fileName)
                p.swab_photo = fileName
                p.save(this)
            }
        }

        if (requestCode == NOTES_REQUEST && data != null)
        {
            p.swab_notes = data!!.getStringExtra(NotesActivity.NOTES_EXTRA)
        }

        runOnUiThread {
            recyclerView.adapter.notifyDataSetChanged()
        }
    }

}
