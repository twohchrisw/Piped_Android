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
        recyclerView.adapter = StandardRecyclerAdapter(this, StandardRecyclerAdapter.PipedTask.Chlorination, appGlobals.lastLat, appGlobals.lastLng, this)
    }

    override fun onResume() {
        super.onResume()
        recyclerView.adapter.notifyDataSetChanged()
    }

    fun locationReceived(lat: Double, lng: Double) {
        appGlobals.lastLat = lat
        appGlobals.lastLng = lng
        recyclerView.adapter = StandardRecyclerAdapter(this, StandardRecyclerAdapter.PipedTask.Chlorination, appGlobals.lastLat, appGlobals.lastLng, this)
    }

    override fun didRequestMainImage(fieldName: String) {
        requestCameraPermissions()
    }

    override fun didRequestNotes(fieldName: String) {
        Log.d("cobswab", "Did request notes")
        setNotes(appGlobals.activeProcess.pt_chlor_notes)
    }

    override fun didRequestFlowrate(position: Int) {
        appGlobals.currentFlowrateActivityType = AppGlobals.Companion.FlowrateViewType.Chlor

        var fr: EXLDChlorFlowrates
        if (position < 0)
        {
            fr = EXLDChlorFlowrates.createFlowrate(this, appGlobals.activeProcess.columnId)
        }
        else
        {
            fr = EXLDChlorFlowrates.getChlorFlowrates(this, appGlobals.activeProcess.columnId).get(position)
        }

        appGlobals.drillChlorFlowrate = fr
        val intent = Intent(this, FlowrateActivity::class.java)
        startActivityForResult(intent, 99)
    }

    override fun cameraPermissionsGranted() {
        super.cameraPermissionsGranted()

        val p = appGlobals.activeProcess
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

                runOnUiThread {
                    p.save(this)
                    recyclerView.adapter.notifyDataSetChanged()
                }
            })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val p = appGlobals.activeProcess

        if (requestCode == NOTES_REQUEST && data != null)
        {
            p.pt_chlor_notes = data!!.getStringExtra(NotesActivity.NOTES_EXTRA)
            runOnUiThread {
                p.save(this)
            }

        }

        if (requestCode == CAMERA_REQUEST_CAMERA)
        {
            val uuid = UUID.randomUUID().toString()
            val fileName = "chlor_${uuid}.jpg"
            saveImageToExternalStorage(fileName)
            p.pt_chlor_end_photo = fileName

            runOnUiThread {
                p.save(this)
            }

        }

        runOnUiThread {
            val myAdapter = recyclerView.adapter as StandardRecyclerAdapter
            myAdapter.updateTotalWater()
            myAdapter.notifyDataSetChanged()

            //recyclerView.adapter.notifyDataSetChanged()
        }
    }
}
