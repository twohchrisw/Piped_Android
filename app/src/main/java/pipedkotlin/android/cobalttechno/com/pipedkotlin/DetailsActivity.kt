package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import org.jetbrains.anko.db.INTEGER
import org.jetbrains.anko.startActivityForResult

class DetailsActivity : AppCompatActivity(), DetailsRecyclerAdapter.DetailsRecyclerClickListener {

    lateinit var recyclerView: RecyclerView

    enum class ActivityRequestCodes(val value: Int)
    {
        listSelection(0), addressSelection(1)
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

    fun savePipeLength(value: String)
    {
        val p = AppGlobals.instance.activeProcess
        val intValue = value.toIntOrNull()
        if (intValue != null)
        {
            p.pipe_length = intValue
            p.save(this)
            assignOutlets()
        }
    }

    fun savePipeDiameter(value: String)
    {
        val p = AppGlobals.instance.activeProcess
        val intValue = value.toIntOrNull()
        if (intValue != null) {
            p.pipe_diameter = intValue
            p.save(this)
            assignOutlets()
        }
    }


    // One of the main details rows has been selected
    override fun listItemClicked(menuItem: Int) {

        val listIntent = Intent(this, ListSelectionActivity::class.java)

        when (menuItem)
        {
            DetailsRecyclerAdapter.MenuItems.technician.value -> {
                listIntent.putExtra("ListType", ListSelectionActivity.ListContext.technicians.value)
                startActivityForResult(listIntent, ActivityRequestCodes.listSelection.value)
            }
            DetailsRecyclerAdapter.MenuItems.vehicle.value -> {
                listIntent.putExtra("ListType", ListSelectionActivity.ListContext.vehicles.value)
                startActivityForResult(listIntent, ActivityRequestCodes.listSelection.value)
            }
            DetailsRecyclerAdapter.MenuItems.installationTech.value -> {
                listIntent.putExtra("ListType", ListSelectionActivity.ListContext.installTechs.value)
                startActivityForResult(listIntent, ActivityRequestCodes.listSelection.value)
            }
            DetailsRecyclerAdapter.MenuItems.client.value -> {
                listIntent.putExtra("ListType", ListSelectionActivity.ListContext.clients.value)
                startActivityForResult(listIntent, ActivityRequestCodes.listSelection.value)
            }
            DetailsRecyclerAdapter.MenuItems.scheme.value -> {
                listIntent.putExtra("ListType", ListSelectionActivity.ListContext.schemes.value)
                startActivityForResult(listIntent, ActivityRequestCodes.listSelection.value)
            }
            DetailsRecyclerAdapter.MenuItems.address.value -> {
                val addressIntent = Intent(this, GetAddressActivity::class.java)
                startActivityForResult(addressIntent, ActivityRequestCodes.addressSelection.value)
            }
            DetailsRecyclerAdapter.MenuItems.pipeType.value -> {
                listIntent.putExtra("ListType", ListSelectionActivity.ListContext.pipeType.value)
                startActivityForResult(listIntent, ActivityRequestCodes.listSelection.value)
            }
            DetailsRecyclerAdapter.MenuItems.pipeLength.value -> {
                val alertHelper = AlertHelper(this)
                alertHelper.dialogForTextInput("Pipe Length", AppGlobals.instance.activeProcess.pipe_length.toString(), ::savePipeLength)
            }
            DetailsRecyclerAdapter.MenuItems.pipeDiameter.value -> {
                val alertHelper = AlertHelper(this)
                alertHelper.dialogForTextInput("Pipe Length", AppGlobals.instance.activeProcess.pipe_diameter.toString(), ::savePipeDiameter)
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        val p = AppGlobals.instance.activeProcess

        // A list selection has been made
        if (requestCode == ActivityRequestCodes.listSelection.value && data != null)
        {
            val listId = data!!.getIntExtra("listId", -1)
            val listItem = data!!.getStringExtra("listValue")

            when (listId)
            {
                ListSelectionActivity.ListContext.technicians.value -> p.technician_name = listItem
                ListSelectionActivity.ListContext.vehicles.value -> p.vehicle_name = listItem
                ListSelectionActivity.ListContext.installTechs.value -> p.pt_installation_tech = listItem
                ListSelectionActivity.ListContext.clients.value -> p.client = listItem
                ListSelectionActivity.ListContext.pipeType.value -> p.pipe_description = listItem
            }
        }

        if (requestCode == ActivityRequestCodes.addressSelection.value && data != null)
        {
            AppGlobals.instance.activeProcess.address = data!!.getStringExtra("address")
        }

        p.save(this)
        assignOutlets()
    }



}
