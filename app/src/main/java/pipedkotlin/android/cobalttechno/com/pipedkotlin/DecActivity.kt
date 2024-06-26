package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class DecActivity : BaseActivity(), StandardRecyclerAdapter.StandardRecyclerAdapterInterface {

    private lateinit var recyclerView: RecyclerView
    var photoField = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_swabbing)
        title = "DeChlorination"

        setupLocationClient()
        getCurrentLocation(::locationReceived)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = StandardRecyclerAdapter(this, StandardRecyclerAdapter.PipedTask.DeChlorination, appGlobals.lastLat, appGlobals.lastLng, this)
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
            appGlobals.calculatorTitle = "Dechlorination"
            val pipeCalculatorIntent = Intent(this, PipeCalculatorActivity::class.java)
            startActivity(pipeCalculatorIntent)
        }

        return super.onOptionsItemSelected(item)
    }

    fun locationReceived(lat: Double, lng: Double) {
        appGlobals.lastLat = lat
        appGlobals.lastLng = lng
        recyclerView.adapter = StandardRecyclerAdapter(this, StandardRecyclerAdapter.PipedTask.DeChlorination, appGlobals.lastLat, appGlobals.lastLng, this)
    }

    override fun didRequestMainImage(fieldName: String) {
    }

    override fun didRequestNotes(fieldName: String) {
        Log.d("cobswab", "Did request notes")
        setNotes(appGlobals.activeProcess.pt_dec_notes)
    }

    override fun didRequestFlowrate(position: Int) {
        appGlobals.currentFlowrateActivityType = AppGlobals.Companion.FlowrateViewType.DeChlor

        var fr: EXLDDecFlowrates
        if (position < 0)
        {
            fr = EXLDDecFlowrates.createFlowrate(this, appGlobals.activeProcess.columnId)
        }
        else
        {
            fr = EXLDDecFlowrates.getDecFlowrates(this, appGlobals.activeProcess.columnId).get(position)
        }

        appGlobals.drillDecFlowrate = fr
        val intent = Intent(this, FlowrateActivity::class.java)
        startActivityForResult(intent, 99)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val p = appGlobals.activeProcess

        if (requestCode == NOTES_REQUEST && data != null)
        {
            p.pt_dec_notes =
                data!!.getStringExtra(NotesActivity.NOTES_EXTRA)
                    .toString()
            p.save(this)
        }

        runOnUiThread {
            val myAdapter = recyclerView.adapter as StandardRecyclerAdapter
            myAdapter.updateTotalWater()
            myAdapter.notifyDataSetChanged()
        }
    }
}
