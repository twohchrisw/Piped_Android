package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.content.Intent
import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import java.util.*

class SurveyNotesActivity : BaseActivity(), StandardRecyclerAdapter.StandardRecyclerSurveyNoteInterface {

    private lateinit var recyclerView: RecyclerView
    private var currentSurveyNote: EXLDSurveyNotes? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_swabbing)
        title = "Surveying"

        setupLocationClient()
        getCurrentLocation(::locationReceived)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = StandardRecyclerAdapter(this, StandardRecyclerAdapter.PipedTask.Surveying, AppGlobals.instance.lastLat, AppGlobals.instance.lastLng, null, this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.add_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item?.itemId)
        {
            R.id.mnuAdd -> {
                val fr = EXLDSurveyNotes.createFlowrate(this, AppGlobals.instance.activeProcess.columnId)

                runOnUiThread {
                    didRequestNote(fr)
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    fun locationReceived(lat: Double, lng: Double) {
        AppGlobals.instance.lastLat = lat
        AppGlobals.instance.lastLng = lng
        recyclerView.adapter = StandardRecyclerAdapter(this, StandardRecyclerAdapter.PipedTask.Surveying, AppGlobals.instance.lastLat, AppGlobals.instance.lastLng, null, this)
    }

    override fun didRequestNote(note: EXLDSurveyNotes) {
        currentSurveyNote = note
        setNotes(currentSurveyNote!!.sn_note)
        AppGlobals.instance.activeProcess.save(this)    // The updates forces a sync
    }

    override fun didRequestImage(note: EXLDSurveyNotes) {
        currentSurveyNote = note
        requestCameraPermissions()
        AppGlobals.instance.activeProcess.save(this)    // The updates forces a sync
    }


    override fun cameraPermissionsGranted() {
        super.cameraPermissionsGranted()

        if (currentSurveyNote!!.sn_photo.length < 2)
        {
            // Straight load
            choosePicFromCamera()
        }
        else
        {
            // Ask for user direction
            val alert = AlertHelper(this)
            alert.dialogForOKAlert("Delete Image", "Do you want to delete this image?", {
                currentSurveyNote!!.sn_photo = ""
                currentSurveyNote!!.save(this)
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
            currentSurveyNote!!.sn_note = data!!.getStringExtra(NotesActivity.NOTES_EXTRA)
            currentSurveyNote!!.sn_lat = AppGlobals.instance.lastLat
            currentSurveyNote!!.sn_long = AppGlobals.instance.lastLng
            currentSurveyNote!!.save(this)
        }

        if (requestCode == CAMERA_REQUEST_CAMERA)
        {
            val uuid = UUID.randomUUID().toString()
            val fileName = "survey_${uuid}.jpg"
            saveImageToExternalStorage(fileName)
            currentSurveyNote!!.sn_photo = fileName
            currentSurveyNote!!.save(this)
        }

        runOnUiThread {
            recyclerView.adapter.notifyDataSetChanged()
        }
    }
}
