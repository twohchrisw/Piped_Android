package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.core.app.ActivityCompat
import android.util.Log
import android.view.Menu
import android.view.MenuItem
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
        recyclerView.adapter = StandardRecyclerAdapter(this, StandardRecyclerAdapter.PipedTask.Swabbing, appGlobals.lastLat, appGlobals.lastLng, this)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.calc_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item?.itemId == R.id.mnuCalc)
        {
            appGlobals.calculatorTitle = "Swabbing"
            val pipeCalculatorIntent = Intent(this, PipeCalculatorActivity::class.java)
            startActivity(pipeCalculatorIntent)
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        recyclerView.adapter?.notifyDataSetChanged()
    }

    fun locationReceived(lat: Double, lng: Double) {
        appGlobals.lastLat = lat
        appGlobals.lastLng = lng
        recyclerView.adapter = StandardRecyclerAdapter(this, StandardRecyclerAdapter.PipedTask.Swabbing, appGlobals.lastLat, appGlobals.lastLng, this)
    }

    override fun didRequestMainImage(fieldName: String) {
        photoField = ""
        Log.d("cobswab", "Picture Requested")
        requestCameraPermissions()
    }

    override fun didRequestNotes(fieldName: String) {
        Log.d("cobswab", "Did request notes")
        setNotes(appGlobals.activeProcess.swab_notes)
    }

    override fun didRequestFlowrate(position: Int) {

    }

    override fun cameraPermissionsGranted() {
        super.cameraPermissionsGranted()

        val p = appGlobals.activeProcess
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

                runOnUiThread {
                    p.save(this)
                    recyclerView.adapter?.notifyDataSetChanged()
                }
            })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val p = appGlobals.activeProcess

        if (requestCode == CAMERA_REQUEST_CAMERA)
        {
            val uuid = UUID.randomUUID().toString()
            val fileName = "survey_${uuid}.jpg"
            saveImageToExternalStorage(fileName)
            p.swab_photo = fileName

            runOnUiThread {
                p.save(this)
            }
        }

        if (requestCode == NOTES_REQUEST && data != null)
        {
            p.swab_notes = data!!.getStringExtra(NotesActivity.NOTES_EXTRA).toString()
        }

        runOnUiThread {
            val myAdapter = recyclerView.adapter as StandardRecyclerAdapter
            myAdapter.updateTotalWater()
            myAdapter.notifyDataSetChanged()
        }
    }

}
