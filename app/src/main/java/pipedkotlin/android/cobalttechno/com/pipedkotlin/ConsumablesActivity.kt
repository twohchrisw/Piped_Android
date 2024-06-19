package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class ConsumablesActivity : BaseActivity(), StandardRecyclerAdapter.StandardRecyclerAdapterInterface {

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_swabbing)
        title = "Consumables"

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = StandardRecyclerAdapter(this, StandardRecyclerAdapter.PipedTask.Consumables, appGlobals.lastLat, appGlobals.lastLng, this)
    }

    override fun didRequestNotes(fieldName: String) {
        setNotes(appGlobals.activeProcess.consum_notes)
    }

    override fun didRequestMainImage(fieldName: String) {

    }

    override fun didRequestFlowrate(position: Int) {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val p = appGlobals.activeProcess

        if (requestCode == NOTES_REQUEST && data != null)
        {
            p.consum_notes =
                data!!.getStringExtra(NotesActivity.NOTES_EXTRA)
                    .toString()
            p.save(this)
        }

        runOnUiThread {
            recyclerView.adapter?.notifyDataSetChanged()
        }
    }
}
