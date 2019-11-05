package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import org.jetbrains.anko.db.classParser
import org.jetbrains.anko.db.parseList
import org.jetbrains.anko.db.select

class ListSelectionActivity : AppCompatActivity(), ListSelectionRecyclerAdapter.ListSelectionRecyclerClickListener {

    lateinit var recyclerView: RecyclerView

    companion object {
        val LIST_NAME_TECHNICIANS  = "Tech"
        val LIST_NAME_VEHICLES = "Vehicle"
        val LIST_NAME_INSTALLATION_TECH = "IT"
        val LIST_NAME_CLIENTS = "Client"
        val LIST_NAME_SCHEMES = "Scheme"
    }

    enum class ListContext(val value: Int)
    {
        technicians(0), vehicles(1), installTechs(2), clients(3), schemes(4), pipeType(5), pumpType(6), installTechOther(7)
    }
    var listContext = ListContext.technicians.value

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_selection)

        listContext = intent.getIntExtra("ListType", 0)
        assignOutlets()


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        if (listContext == ListContext.installTechs.value || listContext == ListContext.schemes.value)
        {
            val inflater = menuInflater
            inflater.inflate(R.menu.install_tech_other_menu, menu)
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when(item?.itemId)
        {
            R.id.mnuOther -> {
                val alertHelper = AlertHelper(this)
                if (listContext == ListContext.installTechs.value) {
                    alertHelper.dialogForTextInput("Other Installation Technique", appGlobals.activeProcess.pt_installation_tech_other, ::installTechOtherSelected)
                }
                if (listContext == ListContext.schemes.value)
                {
                    alertHelper.dialogForTextInput("Other Scheme Name", appGlobals.activeProcess.scheme_name, ::schemeOtherSelected)
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    fun installTechOtherSelected(value: String)
    {
        appGlobals.activeProcess.pt_installation_tech_other = value
        appGlobals.activeProcess.pt_installation_tech = "other"
        appGlobals.activeProcess.save(this)
        setResult(Activity.RESULT_OK)
        finish()
    }

    fun schemeOtherSelected(value: String)
    {
        appGlobals.activeProcess.scheme_name = value
        appGlobals.activeProcess.save(this)
        setResult(Activity.RESULT_OK)
        finish()
    }


    fun assignOutlets()
    {
        recyclerView = findViewById(R.id.listSelectionRecycler) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        if (listContext == ListContext.clients.value)
        {
            supportActionBar?.title = "Clients"
            val clients = database.use {
                select(EXLDClients.TABLE_NAME).whereArgs(EXLDClients.COLUMN_COMPANY_ID + " = '" + appGlobals.activeProcess.company_id + "'")
                        .orderBy(EXLDClients.COLUMN_CLIENT_NAME).exec {
                    parseList<EXLDClients>(classParser())
                }
            }
            recyclerView.adapter = ListSelectionRecyclerAdapter(listContext, null, clients, this)
        }
        else
        {
            var listName = LIST_NAME_INSTALLATION_TECH
            when (listContext)
            {
                ListContext.installTechs.value -> {
                    supportActionBar?.title = "Install Techs"
                    listName = LIST_NAME_INSTALLATION_TECH
                }

                ListContext.pipeType.value -> {
                    supportActionBar?.title = "Pipe Type"
                }

                ListContext.pumpType.value -> {
                    supportActionBar?.title = "Pump Type"
                }

                ListContext.schemes.value -> {
                    supportActionBar?.title = "Scheme Name"
                    listName = LIST_NAME_SCHEMES
                }

                ListContext.technicians.value -> {
                    supportActionBar?.title = "Technicians"
                    listName = LIST_NAME_TECHNICIANS
                }

                ListContext.vehicles.value -> {
                    supportActionBar?.title = "Vehicles"
                    listName = LIST_NAME_VEHICLES
                }
            }

            val listItems = database.use {
                select(EXLDListItems.TABLE_NAME).whereArgs(EXLDListItems.COLUMN_COMPANY_ID + " = '" + appGlobals.activeProcess.company_id + "' AND " + EXLDListItems.COLUMN_LIST_NAME + " = '" + listName + "'")
                        .orderBy(EXLDListItems.COLUMN_LIST_ITEM).exec {
                            parseList<EXLDListItems>(classParser())
                        }
            }
            recyclerView.adapter = ListSelectionRecyclerAdapter(listContext, listItems, null, this)
        }

    }

    // MARK: Click Listener

    override fun listItemClicked(listContext: Int, listItem: EXLDListItems) {
        // Now return the value back to the calling activity
        val intent = Intent()
        intent.putExtra("listId", listContext)
        intent.putExtra("listValue", listItem.listItem)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun clientItemClicked(client: EXLDClients) {
        val intent = Intent()
        intent.putExtra("listId", listContext)
        intent.putExtra("listValue", client.clientName)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun stringItemClicked(value: String) {
        val intent = Intent()
        intent.putExtra("listId", listContext)
        intent.putExtra("listValue", value)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}
