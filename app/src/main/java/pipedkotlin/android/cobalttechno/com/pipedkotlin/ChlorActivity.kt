package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.content.Intent
import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import java.util.*

class ChlorActivity : BaseActivity(), StandardRecyclerAdapter.StandardRecyclerAdapterInterface {

    private lateinit var recyclerView: RecyclerView
    var photoField = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_swabbing)
        title = "Chlorination"

        setupLocationClient()
        getCurrentLocation(::locationReceived)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = StandardRecyclerAdapter(this, StandardRecyclerAdapter.PipedTask.Chlorination, AppGlobals.instance.lastLat, AppGlobals.instance.lastLng, this)
    }

    fun locationReceived(lat: Double, lng: Double) {
        AppGlobals.instance.lastLat = lat
        AppGlobals.instance.lastLng = lng
        recyclerView.adapter = StandardRecyclerAdapter(this, StandardRecyclerAdapter.PipedTask.Chlorination, AppGlobals.instance.lastLat, AppGlobals.instance.lastLng, this)
    }

    override fun didRequestMainImage(fieldName: String) {
        requestCameraPermissions()
    }

    override fun didRequestNotes(fieldName: String) {
        Log.d("cobswab", "Did request notes")
        setNotes(AppGlobals.instance.activeProcess.pt_chlor_notes)
    }

    override fun didRequestFlowrate(position: Int) {
        AppGlobals.instance.currentFlowrateActivityType = AppGlobals.Companion.FlowrateViewType.Chlor

        var fr: EXLDChlorFlowrates
        if (position < 0)
        {
            fr = EXLDChlorFlowrates.createFlowrate(this, AppGlobals.instance.activeProcess.columnId)
        }
        else
        {
            fr = EXLDChlorFlowrates.getChlorFlowrates(this, AppGlobals.instance.activeProcess.columnId).get(position)
        }

        AppGlobals.instance.drillChlorFlowrate = fr
        val intent = Intent(this, FlowrateActivity::class.java)
        startActivityForResult(intent, 99)
    }

    override fun cameraPermissionsGranted() {
        super.cameraPermissionsGranted()

        val p = AppGlobals.instance.activeProcess
        if (p.pt_chlor_end_photo.length < 2)
        {
            // Straight load
            choosePicFromCamera()
        }
        else
        {
            // Ask for user direction
            val alert = AlertHelper(this)
            alert.dialogForOKAlert("Delete Image", "Do you want to delete this image?", {
                p.pt_chlor_end_photo = ""
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

        if (requestCode == NOTES_REQUEST && data != null)
        {
            p.pt_chlor_notes = data!!.getStringExtra(NotesActivity.NOTES_EXTRA)
            p.save(this)
        }

        if (requestCode == CAMERA_REQUEST_CAMERA)
        {
            val uuid = UUID.randomUUID().toString()
            val fileName = "chlor_${uuid}.jpg"
            saveImageToExternalStorage(fileName)
            p.pt_chlor_end_photo = fileName
            p.save(this)
        }

        runOnUiThread {
            val myAdapter = recyclerView.adapter as StandardRecyclerAdapter
            myAdapter.updateTotalWater()
            myAdapter.notifyDataSetChanged()

            //recyclerView.adapter.notifyDataSetChanged()
        }
    }
}
