package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
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
        val LIST_NAME_PIPETYPE = "PipeType"
    }

    enum class ListContext(val value: Int)
    {
        technicians(0), vehicles(1), installTechs(2), clients(3), schemes(4), pipeType(5), pumpType(6)
    }
    var listContext = ListContext.technicians.value

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_selection)

        val listContext = intent.getIntExtra("ListType", 0)
        assignOutlets()
    }

    fun assignOutlets()
    {
        recyclerView = findViewById(R.id.listSelectionRecycler) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        if (listContext == ListContext.clients.value)
        {
            supportActionBar?.title = "Clients"
            val clients = database.use {
                select(EXLDClients.TABLE_NAME).whereArgs(EXLDClients.COLUMN_COMPANY_ID + " = '" + AppGlobals.instance.activeProcess.company_id + "'")
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
                    listName = LIST_NAME_PIPETYPE
                }

                ListContext.pumpType.value -> {
                    supportActionBar?.title = "Pump Type"
                    //TODO: What happens here
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
                select(EXLDListItems.TABLE_NAME).whereArgs(EXLDListItems.COLUMN_COMPANY_ID + " = '" + AppGlobals.instance.activeProcess.company_id + "' AND " + EXLDListItems.COLUMN_LIST_NAME + " = '" + listName + "'")
                        .orderBy(EXLDListItems.COLUMN_LIST_ITEM).exec {
                            parseList<EXLDListItems>(classParser())
                        }
            }
            recyclerView.adapter = ListSelectionRecyclerAdapter(listContext, listItems, null, this)
        }

    }

    // MARK: Click Listener

    override fun listItemClicked(listContext: Int, listItem: EXLDListItems) {
        Log.d("cobalt", "clicked " + listItem.listItem)
    }

    override fun clientItemClicked(client: EXLDClients) {
        Log.d("cobalt", "clicked " + client.clientName)
    }
}
