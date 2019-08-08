package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
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

        // Set the context for the tbxDataController so we can run commands on the main thread
        AppGlobals.instance.tibiisController.tbxDataController.context = this

        // Get the processes
        processes = EXLDProcess.allProcesses(this)

        // Setup the view
        assignOutlets()
        addListeners()
        supportActionBar?.setTitle("Process List")
        //TODO: Ensure processes refresh after adding new and returning to this view
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_process_list, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item?.itemId)
        {
            R.id.mnuSyncWithServer -> {
                val alert = AlertHelper(this)
                alert.dialogForOKAlertNoAction("Sync With Server", "In development")
            }

            R.id.mnuSignout -> {
                //TODO: Ensure all processes have synced before allowing this, see ProcessListViewController.signOutOfCompany #549
                val alert = AlertHelper(this)
                alert.dialogForOKAlertNoAction("Signout", "In development")
            }

            R.id.mnuAbout -> {
                val version = BuildConfig.VERSION_NAME
                val companyId = AppGlobals.instance.companyId

                val alert = AlertHelper(this)
                alert.dialogForOKAlertNoAction("About Piped", "Version: ${version}\r\nSigned in as '$companyId'")
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        return
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
        AppGlobals.instance.processMenuShowingTasks = false
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
