package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem

class FlushingActivity : BaseActivity(), StandardRecyclerAdapter.StandardRecyclerAdapterInterface {

    private lateinit var recyclerView: RecyclerView
    var photoField = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_swabbing)

        if (appGlobals.currentFlushType == 1) {
            title = "Flushing"
        }
        else {
            title = "Flushing 2"
        }

        setupLocationClient()
        getCurrentLocation(::locationReceived)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        if (appGlobals.currentFlushType == 1) {
            recyclerView.adapter = StandardRecyclerAdapter(this, StandardRecyclerAdapter.PipedTask.Flushing, appGlobals.lastLat, appGlobals.lastLng, this)
        }
        else {
            recyclerView.adapter = StandardRecyclerAdapter(this, StandardRecyclerAdapter.PipedTask.Flushing2, appGlobals.lastLat, appGlobals.lastLng, this)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.calc_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item?.itemId == R.id.mnuCalc)
        {
            if (appGlobals.currentFlushType == 1)
            {
                appGlobals.calculatorTitle = "Flushing"
            }
            else
            {
                appGlobals.calculatorTitle = "Flushing 2"
            }

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

        if (appGlobals.currentFlushType == 1) {
            recyclerView.adapter = StandardRecyclerAdapter(this, StandardRecyclerAdapter.PipedTask.Flushing, appGlobals.lastLat, appGlobals.lastLng, this)
        }
        else {
            recyclerView.adapter = StandardRecyclerAdapter(this, StandardRecyclerAdapter.PipedTask.Flushing2, appGlobals.lastLat, appGlobals.lastLng, this)
        }
    }

    override fun didRequestMainImage(fieldName: String) {
    }

    override fun didRequestNotes(fieldName: String) {
        Log.d("cobswab", "Did request notes")

        if (appGlobals.currentFlushType == 1) {
            setNotes(appGlobals.activeProcess.pt_flush_notes)
        }
        else {
            setNotes(appGlobals.activeProcess.pt_flush_notes2)
        }

    }

    override fun didRequestFlowrate(position: Int) {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val p = appGlobals.activeProcess

        if (requestCode == NOTES_REQUEST && data != null)
        {
            if (appGlobals.currentFlushType == 1) {
                p.pt_flush_notes = data!!.getStringExtra(NotesActivity.NOTES_EXTRA).toString()
            }
            else {
                p.pt_flush_notes2 = data!!.getStringExtra(NotesActivity.NOTES_EXTRA).toString()
            }

            runOnUiThread {
                p.save(this)
            }

        }

        runOnUiThread {
            val myAdapter = recyclerView.adapter as StandardRecyclerAdapter
            myAdapter.updateTotalWater()
            myAdapter.notifyDataSetChanged()
        }
    }
}
