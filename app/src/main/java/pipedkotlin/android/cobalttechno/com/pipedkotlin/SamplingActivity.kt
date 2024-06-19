package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.calc_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item?.itemId == R.id.mnuCalc)
        {
            appGlobals.calculatorTitle = "Sampling"
            val pipeCalculatorIntent = Intent(this, PipeCalculatorActivity::class.java)
            startActivity(pipeCalculatorIntent)
        }

        return super.onOptionsItemSelected(item)
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
            p.pt_sampl_notes = data!!.getStringExtra(NotesActivity.NOTES_EXTRA).toString()
            runOnUiThread {
                p.save(this)
            }
        }

        runOnUiThread {
            recyclerView.adapter?.notifyDataSetChanged()
        }
    }
}
