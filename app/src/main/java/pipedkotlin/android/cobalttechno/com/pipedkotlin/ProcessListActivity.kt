package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat.startActivityForResult
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_process_list.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.startActivityForResult
import java.util.*

class ProcessListActivity : BaseActivity(), ProcessListRecyclerAdapter.ProcessListRecyclerViewClickListener, SyncManager.SyncManagerDelegate {

    // Outlets
    lateinit var recyclerView: RecyclerView
    lateinit var processes: List<EXLDProcess>


    // MARK: View Setup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_process_list)
        appGlobals.processListActivity = this  // For getting updates from the sync manager

        // Setup the location client
        setupLocationClient()
        getCurrentLocation(::locationReceived)

        // Set the context for the tbxDataController so we can run commands on the main thread
        appGlobals.tibiisController.tbxDataController.context = this

        // Get the processes
        processes = EXLDProcess.allProcesses(this)

        // Setup the view
        assignOutlets()
        addListeners()
        supportActionBar?.setTitle("Process List")
        //TODO: Ensure processes refresh after adding new and returning to this view

        //val readings = EXLDTibiisReading.getAllTibiisReadings(this)
        //Log.d("cobsync", "All tibiis readings count = ${readings.size}")
        //val x = 1
    }

    public fun updateRecycler()
    {
        processes = EXLDProcess.allProcesses(this)

        runOnUiThread {
            recyclerView.adapter = ProcessListRecyclerAdapter(processes, this)
        }
    }

    fun locationReceived(lat: Double, lng: Double) {
        appGlobals.lastLat = lat
        appGlobals.lastLng = lng
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
                if (AppGlobals.isOnline(this)) {
                    syncOutstandingProcesses()
                }
                else
                {
                    val alert = AlertHelper(this)
                    alert.dialogForOKAlertNoAction("Sync with Server", "Unable to Sync as your device is currently not connected to the internet!")
                }
            }

            R.id.mnuSignout -> {
                //TODO: Ensure all processes have synced before allowing this, see ProcessListViewController.signOutOfCompany #549

                val alert = AlertHelper(this)

                if (doAnyProcessesNeedSync())
                {
                    alert.dialogForOKAlertNoAction("Signout", "Please sync all processes before signing out")
                }
                else
                {
                    // Reset the login data
                    EXLDSettings.resetLoginToDefault(this)
                    this.finish()
                }
            }

            R.id.mnuAbout -> {
                val version = BuildConfig.VERSION_NAME
                val companyId = appGlobals.companyId

                val alert = AlertHelper(this)
                alert.dialogForOKAlertNoAction("About Piped", "Version: ${version}\r\nSigned in as '$companyId'")
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        return
    }

    fun syncOutstandingProcesses()
    {
        val ctx = this
        for (p in processes)
        {
            if (p.needsSync())
            {
                val a = this
                doAsync {
                    appGlobals.syncManager.syncProcess(p, ctx)
                }
            }
        }
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

            val alert = AlertHelper(this)
            alert.dialogForOKAlert("Create new Process?", "", {
                runOnUiThread {
                    addNewProcess()
                }
            })

        }
    }

    // Load the process menu
    fun loadMenuForProcess(process: EXLDProcess)
    {
        appGlobals.activeProcess = process
        val processMenuIntent = Intent(this, ProcessMenuActivity::class.java)
        processMenuIntent.putExtra(ProcessMenuActivity.MENU_MODE_KEY, ProcessMenuActivity.MENU_MODE_MAIN)
        appGlobals.processMenuShowingTasks = false
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
        Log.d("cobalt", "New process sets company id as " + appGlobals.companyId)
        process.company_id = appGlobals.companyId
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

    override fun processHasSynced(process: EXLDProcess) {
        Log.d("cobsync", "Process Has Synced")
        runOnUiThread {
            recyclerView.adapter.notifyDataSetChanged()
        }
    }

    override fun processFailedToSync(process: EXLDProcess, errorMessage: String) {

    }

    fun doAnyProcessesNeedSync(): Boolean
    {
        for (p in processes)
        {
            if (p.needsSync())
            {
                return true
            }
        }

        return false
    }

}
