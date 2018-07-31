package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import kotlinx.android.synthetic.main.activity_process_list.*
import org.jetbrains.anko.startActivityForResult

class ProcessListActivity : AppCompatActivity(), ProcessListRecyclerAdapter.ProcessListRecyclerViewClickListener {

    // Outlets
    lateinit var recyclerView: RecyclerView
    lateinit var processes: List<EXLDProcess>


    // MARK: View Setup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_process_list)

        // Get the processes
        processes = EXLDProcess.allProcesses(this)

        // Setup the view
        assignOutlets()
        addListeners()
        supportActionBar?.setTitle("Process List")
        //TODO: Ensure processes refresh after adding new and returning to this view
    }

    fun assignOutlets()
    {
        recyclerView = findViewById(R.id.processListRecyclerView) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ProcessListRecyclerAdapter(processes, this)
    }

    fun addListeners()
    {
        // FAB Button
        fab.setOnClickListener { view ->
            addNewProcess()
        }
    }

    // Load the process menu
    fun loadMenuForProcess(process: EXLDProcess)
    {
        AppGlobals.instance.activeProcess = process
        val processMenuIntent = Intent(this, ProcessMenuActivity::class.java)
        processMenuIntent.putExtra(ProcessMenuActivity.MENU_MODE_KEY, ProcessMenuActivity.MENU_MODE_MAIN)
        startActivityForResult(processMenuIntent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Refresh the list on return
        processes = EXLDProcess.allProcesses(this)
        recyclerView.adapter = ProcessListRecyclerAdapter(processes, this)
    }

    override fun listItemClicked(process: EXLDProcess) {
        loadMenuForProcess(process)
    }

    // MARK: Data Operations

    private fun addNewProcess()
    {
        // Create a new process
        var process = EXLDProcess()
        Log.d("cobalt", "New process sets company id as " + AppGlobals.instance.companyId)
        process.company_id = AppGlobals.instance.companyId
        val newProcessId = process.save(this)

        // Retrieve the just created process - we can't use the process we just created as the values haven't populated
        val newProcess = EXLDProcess.processForId(this, newProcessId)
        if (newProcess != null)
        {
            loadMenuForProcess(newProcess)
        }
        else
        {
            Log.d("cobalt", "ERROR: No process after create")
        }
    }
}
