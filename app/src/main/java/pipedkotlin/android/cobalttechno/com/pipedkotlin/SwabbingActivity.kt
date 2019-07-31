package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView

class SwabbingActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_swabbing)
        title = "Swabbing"

        setupLocationClient()
        getCurrentLocation(::locationReceived)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = StandardRecyclerAdapter(this, StandardRecyclerAdapter.PipedTask.Swabbing, lastLat, lastLng)

    }

    fun locationReceived(lat: Double, lng: Double) {
        lastLat = lat
        lastLng = lng
        recyclerView.adapter = StandardRecyclerAdapter(this, StandardRecyclerAdapter.PipedTask.Swabbing, lastLat, lastLng)
    }
}
