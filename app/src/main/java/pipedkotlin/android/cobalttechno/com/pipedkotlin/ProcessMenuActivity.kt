package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.MenuItem
import org.jetbrains.anko.startActivity

class ProcessMenuActivity : AppCompatActivity(), ProcessMenuRecyclerAdapter.ProcessMenuRecyclerClickListener {

    companion object {
        val MENU_MODE_MAIN = 0
        val MENU_MODE_TASKS = 1
        val MENU_MODE_KEY = "MENU_MODE_KEY"
    }

    lateinit var recyclerView: RecyclerView
    var menuMode = ProcessMenuActivity.MENU_MODE_MAIN

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_process_menu)
        supportActionBar?.title = AppGlobals.instance.activeProcess.processNoDescription()
        menuMode = intent.getIntExtra(MENU_MODE_KEY, MENU_MODE_MAIN)
        assignOutlets()
        Log.d("cobalt", "Menu Mode is " + menuMode.toString() + " Process Company Id is " + AppGlobals.instance.activeProcess.company_id)
    }

    fun assignOutlets()
    {
        recyclerView = findViewById(R.id.menuRecyclerView) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ProcessMenuRecyclerAdapter(menuMode, this)
    }

    fun loadTasksMenu()
    {
        val processMenuIntent = Intent(this, ProcessMenuActivity::class.java)
        processMenuIntent.putExtra(ProcessMenuActivity.MENU_MODE_KEY, ProcessMenuActivity.MENU_MODE_TASKS)
        startActivity(processMenuIntent)
    }

    fun loadProcessDetails()
    {
        val detailsIntent = Intent(this, DetailsActivity::class.java)
        startActivity(detailsIntent)
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
            when (menuItem)
            {
                ProcessMenuRecyclerAdapter.MainMenuItems.processDetails.value -> { loadProcessDetails() }
                ProcessMenuRecyclerAdapter.MainMenuItems.tasks.value -> { loadTasksMenu() }
                ProcessMenuRecyclerAdapter.MainMenuItems.consumables.value -> { }
                ProcessMenuRecyclerAdapter.MainMenuItems.equipment.value -> { }
            }
        }

        if (menuMode == ProcessMenuActivity.MENU_MODE_TASKS)
        {

        }
    }
}
