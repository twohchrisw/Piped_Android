package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.content.Intent
import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import java.util.*

class SamplingActivity : BaseActivity(), StandardRecyclerAdapter.StandardRecyclerAdapterInterface {

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_swabbing)
        title = "Sampling"

        setupLocationClient()
        getCurrentLocation(::locationReceived)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = StandardRecyclerAdapter(this, StandardRecyclerAdapter.PipedTask.Sampling, appGlobals.lastLat, appGlobals.lastLng, this)
    }

    override fun onResume() {
        super.onResume()
        recyclerView.adapter?.notifyDataSetChanged()
    }

    fun locationReceived(lat: Double, lng: Double) {
        appGlobals.lastLat = lat
        appGlobals.lastLng = lng
        recyclerView.adapter = StandardRecyclerAdapter(this, StandardRecyclerAdapter.PipedTask.Sampling, appGlobals.lastLat, appGlobals.lastLng, this)
    }

    override fun didRequestMainImage(fieldName: String) {

    }

    override fun didRequestNotes(fieldName: String) {
        setNotes(appGlobals.activeProcess.pt_sampl_notes)
    }

    override fun didRequestFlowrate(position: Int) {
        appGlobals.currentFlowrateActivityType = AppGlobals.Companion.FlowrateViewType.Sampling

        var fr: EXLDSamplingData
        if (position < 0)
        {
            fr = EXLDSamplingData.createFlowrate(this, appGlobals.activeProcess.columnId)
        }
        else
        {
            fr = EXLDSamplingData.getSamplingFlowrates(this, appGlobals.activeProcess.columnId).get(position)
        }

        appGlobals.drillSamplFlorwate = fr
        val intent = Intent(this, FlowrateActivity::class.java)
        startActivityForResult(intent, 99)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val p = appGlobals.activeProcess

        if (requestCode == NOTES_REQUEST && data != null)
        {
            p.pt_sampl_notes = data!!.getStringExtra(NotesActivity.NOTES_EXTRA)
            runOnUiThread {
                p.save(this)
            }
        }

        runOnUiThread {
            recyclerView.adapter?.notifyDataSetChanged()
        }
    }
}
