package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView

class DetailsActivity : AppCompatActivity(), DetailsRecyclerAdapter.DetailsRecyclerClickListener {

    lateinit var recyclerView: RecyclerView

    enum class ActivityRequestCodes(val value: Int)
    {
        listSelection(0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
        supportActionBar?.title = AppGlobals.instance.activeProcess.processNoDescription()
        assignOutlets()
    }

    fun assignOutlets()
    {
        recyclerView = findViewById(R.id.detailsRecycler) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = DetailsRecyclerAdapter(this)
    }

    override fun listItemClicked(menuItem: Int) {
        when (menuItem)
        {
            DetailsRecyclerAdapter.MenuItems.technician.value -> {
                //TODO: Load the list selection view the interface of which should provide (menuItem int, selectionId int, selectionText text)
                val listIntent = Intent(this, ListSelectionActivity::class.java)
                listIntent.putExtra("ListType", ListSelectionActivity.ListContext.technicians.value)
                startActivityForResult(listIntent, ActivityRequestCodes.listSelection.value)
            }
        }
    }

}
