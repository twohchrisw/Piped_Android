package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import org.jetbrains.anko.startActivity

class ProcessMenuActivity : AppCompatActivity(), ProcessMenuRecyclerAdapter.ProcessMenuRecyclerClickListener {

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

        menuMode = MENU_MODE_MAIN
        if (AppGlobals.instance.processMenuShowingTasks)
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

    fun assignOutlets()
    {
        recyclerView = findViewById(R.id.menuRecyclerView) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ProcessMenuRecyclerAdapter(menuMode, this)

        headerProcess = findViewById(R.id.tvHeaderProcess) as TextView
        headerAddress = findViewById(R.id.tvHeaderAddress) as TextView

        headerProcess.text = AppGlobals.instance.activeProcess.processNoDescription()
        headerAddress.text = AppGlobals.instance.activeProcess.address
    }

    fun loadTasksMenu()
    {
        val processMenuIntent = Intent(this, ProcessMenuActivity::class.java)
        AppGlobals.instance.processMenuShowingTasks = true
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
        AppGlobals.instance.currentFlushType = flushType
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

    override fun onBackPressed() {
        super.onBackPressed()
        AppGlobals.instance.processMenuShowingTasks = false
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        Log.d("cobalt", "Finished")

        // To ensure we go back to the previous menu (i.e. we're in tasks mode, since we have a parent set for the main menu)
        if (item?.itemId == android.R.id.home)
        {
            finish()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun listItemClicked(menuMode: Int, menuItem: Int) {

        if (menuMode == ProcessMenuActivity.MENU_MODE_MAIN)
        {
            val hasEnteredDetails = AppGlobals.instance.activeProcess.hasEnteredProcessDetails()

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
            }
        }
    }
}
