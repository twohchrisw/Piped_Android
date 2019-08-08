package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import kotlinx.android.synthetic.main.activity_swabbing.*

class FlushingActivity : BaseActivity(), StandardRecyclerAdapter.StandardRecyclerAdapterInterface {

    private lateinit var recyclerView: RecyclerView
    var photoField = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_swabbing)

        if (AppGlobals.instance.currentFlushType == 1) {
            title = "Flushing"
        }
        else {
            title = "Flushing 2"
        }

        setupLocationClient()
        getCurrentLocation(::locationReceived)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        if (AppGlobals.instance.currentFlushType == 1) {
            recyclerView.adapter = StandardRecyclerAdapter(this, StandardRecyclerAdapter.PipedTask.Flushing, lastLat, lastLng, this)
        }
        else {
            recyclerView.adapter = StandardRecyclerAdapter(this, StandardRecyclerAdapter.PipedTask.Flushing2, lastLat, lastLng, this)
        }
    }

    fun locationReceived(lat: Double, lng: Double) {
        lastLat = lat
        lastLng = lng

        if (AppGlobals.instance.currentFlushType == 1) {
            recyclerView.adapter = StandardRecyclerAdapter(this, StandardRecyclerAdapter.PipedTask.Flushing, lastLat, lastLng, this)
        }
        else {
            recyclerView.adapter = StandardRecyclerAdapter(this, StandardRecyclerAdapter.PipedTask.Flushing2, lastLat, lastLng, this)
        }
    }

    override fun didRequestMainImage(fieldName: String) {
    }

    override fun didRequestNotes(fieldName: String) {
        Log.d("cobswab", "Did request notes")

        if (AppGlobals.instance.currentFlushType == 1) {
            setNotes(AppGlobals.instance.activeProcess.pt_flush_notes)
        }
        else {
            setNotes(AppGlobals.instance.activeProcess.pt_flush_notes2)
        }

    }

    override fun didRequestFlowrate() {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val p = AppGlobals.instance.activeProcess

        if (requestCode == NOTES_REQUEST && data != null)
        {
            if (AppGlobals.instance.currentFlushType == 1) {
                p.pt_flush_notes = data!!.getStringExtra(NotesActivity.NOTES_EXTRA)
            }
            else {
                p.pt_flush_notes2 = data!!.getStringExtra(NotesActivity.NOTES_EXTRA)
            }

            p.save(this)
        }

        runOnUiThread {
            recyclerView.adapter.notifyDataSetChanged()
        }
    }
}
