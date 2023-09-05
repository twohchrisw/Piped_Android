package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.startActivity
import java.util.*

class ProcessMenuActivity : AppCompatActivity(), ProcessMenuRecyclerAdapter.ProcessMenuRecyclerClickListener, SyncManager.SyncManagerDelegate {

    companion object {
        val MENU_MODE_MAIN = 0
        val MENU_MODE_TASKS = 1
        val MENU_MODE_KEY = "MENU_MODE_KEY"
    }

    lateinit var recyclerView: RecyclerView
    lateinit var headerProcess: TextView
    lateinit var headerAddress: TextView
    var menuMode = ProcessMenuActivity.MENU_MODE_MAIN

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_process_menu)
        appGlobals.processMenuActivity = this

        menuMode = MENU_MODE_MAIN
        if (appGlobals.processMenuShowingTasks)
        {
            menuMode = MENU_MODE_TASKS
        }

        assignOutlets()

        if (menuMode == ProcessMenuActivity.MENU_MODE_MAIN)
        {
            supportActionBar?.title = "Process Menu"
        }

        if (menuMode == ProcessMenuActivity.MENU_MODE_TASKS)
        {
            supportActionBar?.title = "Tasks"
        }

    }

    override fun onResume() {
        super.onResume()

        Log.d("cobsync", "Process Menu on Resume")

        appGlobals.activeProcess = EXLDProcess.processForId(MainApplication.applicationContext(), appGlobals.activeProcess.columnId)!!

        if (appGlobals.activeProcess.needsSync())
        {
            Log.d("cobsync", "initiating sync")
            val a = this
            doAsync {
                appGlobals.syncManager.syncProcess(appGlobals.activeProcess, a)
            }
        }
        else
        {
            Log.d("cobsync","Resume needs sync is false: Sync: ${appGlobals.activeProcess.last_sync_millis}  Update: ${appGlobals.activeProcess.last_update_millis}")
        }

        if (appGlobals.tibiisController.connectStatus == TibiisController.ConnectionStatus.connected)
        {
            Log.d("Cobalt", "TIBIIS IS CONNECTED")
        }

    }




    fun syncCompleted()
    {
        runOnUiThread {
            headerProcess.text = appGlobals.activeProcess.processNoDescription()
        }
    }

    fun assignOutlets()
    {
        recyclerView = findViewById(R.id.menuRecyclerView) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ProcessMenuRecyclerAdapter(menuMode, this)

        headerProcess = findViewById(R.id.tvHeaderProcess) as TextView
        headerAddress = findViewById(R.id.tvHeaderAddress) as TextView

        headerProcess.text = appGlobals.activeProcess.processNoDescription()
        headerAddress.text = appGlobals.activeProcess.address
    }

    fun loadTasksMenu()
    {
        val processMenuIntent = Intent(this, ProcessMenuActivity::class.java)
        appGlobals.processMenuShowingTasks = true
        startActivity(processMenuIntent)
    }

    fun loadProcessDetails()
    {
        val detailsIntent = Intent(this, DetailsActivity::class.java)
        startActivity(detailsIntent)
    }

    fun loadSwabbing()
    {
        val swabbingIntent = Intent(this, SwabbingActivity::class.java)
        startActivity(swabbingIntent)
    }

    fun loadFilling()
    {
        val fillingIntent = Intent(this, FillingActivity::class.java)
        startActivity(fillingIntent)
    }

    fun loadFlushing(flushType: Int)
    {
        appGlobals.currentFlushType = flushType
        val flushIntent = Intent(this, FlushingActivity::class.java)
        startActivity(flushIntent)
    }

    fun loadChlor()
    {
        val chlorIntent = Intent(this, ChlorActivity::class.java)
        startActivity(chlorIntent)
    }

    fun loadDec()
    {
        val decIntent = Intent(this, DecActivity::class.java)
        startActivity(decIntent)
    }

    fun loadSurveying()
    {
        val surveyIntent = Intent(this, SurveyNotesActivity::class.java)
        startActivity(surveyIntent)
    }

    fun loadEquipment()
    {
        val equipIntent = Intent(this, EquipmentActivity::class.java)
        startActivity(equipIntent)
    }

    fun loadConsumables()
    {
        val cons = Intent(this, ConsumablesActivity::class.java)
        startActivity(cons)
    }

    fun loadSampling()
    {
        val samp = Intent(this, SamplingActivity::class.java)
        startActivity(samp)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        appGlobals.processMenuShowingTasks = false
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.calc_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d("cobalt", "Finished")

        // To ensure we go back to the previous menu (i.e. we're in tasks mode, since we have a parent set for the main menu)
        if (item?.itemId == android.R.id.home)
        {
            if (appGlobals.tibiisController.connectStatus == TibiisController.ConnectionStatus.connected && menuMode == ProcessMenuActivity.MENU_MODE_MAIN)
            {
                // Prevent the user leaving the process if Tibiis is connected
                val alert = AlertHelper(this)
                alert.dialogForOKAlertNoAction("Tibiis Connected!!", "You cannot leave the current process whilst the Tibiis is still connected.")
            }
            else
            {
                finish()
            }

            return true
        }

        if (item?.itemId == R.id.mnuCalc)
        {
            appGlobals.calculatorTitle = "Process Menu"
            val pipeCalculatorIntent = Intent(this, PipeCalculatorActivity::class.java)
            startActivity(pipeCalculatorIntent)
        }

        return super.onOptionsItemSelected(item)
    }

    override fun listItemClicked(menuMode: Int, menuItem: Int) {

        if (menuMode == ProcessMenuActivity.MENU_MODE_MAIN)
        {
            val hasEnteredDetails = appGlobals.activeProcess.hasEnteredProcessDetails()

            when (menuItem)
            {
                ProcessMenuRecyclerAdapter.MainMenuItems.processDetails.value -> { loadProcessDetails() }
                ProcessMenuRecyclerAdapter.MainMenuItems.tasks.value -> {
                    if (hasEnteredDetails) {
                        loadTasksMenu()
                    }
                }
                ProcessMenuRecyclerAdapter.MainMenuItems.consumables.value -> {
                    if (hasEnteredDetails)
                    {
                        loadConsumables()
                    }
                }
                ProcessMenuRecyclerAdapter.MainMenuItems.equipment.value -> {
                    if (hasEnteredDetails)
                    {
                        loadEquipment()
                    }
                }
            }
        }

        if (menuMode == ProcessMenuActivity.MENU_MODE_TASKS)
        {
            when (menuItem)
            {
                ProcessMenuRecyclerAdapter.TaskMenuItems.peTest.value -> {
                    val testingIntent = Intent(this, TestingActivity::class.java)
                    testingIntent.putExtra(TestingActivity.TESTING_CONTEXT_EXTRA, "PE")
                    startActivity(testingIntent)
                }
                ProcessMenuRecyclerAdapter.TaskMenuItems.diTest.value -> {
                    val testingIntent = Intent(this, TestingActivity::class.java)
                    testingIntent.putExtra(TestingActivity.TESTING_CONTEXT_EXTRA, "DI")
                    startActivity(testingIntent)
                }
                ProcessMenuRecyclerAdapter.TaskMenuItems.swabbing.value -> {
                    loadSwabbing()
                }
                ProcessMenuRecyclerAdapter.TaskMenuItems.filling.value -> {
                    loadFilling()
                }
                ProcessMenuRecyclerAdapter.TaskMenuItems.flushing.value -> {
                    loadFlushing(1)
                }
                ProcessMenuRecyclerAdapter.TaskMenuItems.flushing2.value -> {
                    loadFlushing(2)
                }
                ProcessMenuRecyclerAdapter.TaskMenuItems.chlor.value -> {
                    loadChlor()
                }
                ProcessMenuRecyclerAdapter.TaskMenuItems.dechlor.value -> {
                    loadDec()
                }
                ProcessMenuRecyclerAdapter.TaskMenuItems.surveying.value -> {
                    loadSurveying()
                }
                ProcessMenuRecyclerAdapter.TaskMenuItems.sampling.value -> {
                    loadSampling()
                }
            }
        }
    }

    override fun processHasSynced(process: EXLDProcess) {

    }

    override fun processFailedToSync(process: EXLDProcess, errorMessage: String) {

    }
}
