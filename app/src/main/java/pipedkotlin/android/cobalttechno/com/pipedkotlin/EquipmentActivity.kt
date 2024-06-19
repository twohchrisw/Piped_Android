package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager

class EquipmentActivity : BaseActivity(), StandardRecyclerAdapter.StandardRecyclerAdapterInterface {

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_swabbing)
        title = "Equipment"

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = EquipmentRecycler(this)
    }

    override fun didRequestFlowrate(position: Int) {

    }

    override fun didRequestMainImage(fieldName: String) {

    }

    override fun didRequestNotes(fieldName: String) {

    }
}
