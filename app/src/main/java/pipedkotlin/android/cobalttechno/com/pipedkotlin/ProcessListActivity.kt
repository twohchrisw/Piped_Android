package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_process_list.*

class ProcessListActivity : AppCompatActivity() {

    // Outlets
    lateinit var recyclerView: RecyclerView


    // MARK: View Setup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_process_list)
        assignOutlets()
        addListeners()

        //TODO: Set title

    }

    fun assignOutlets()
    {
        recyclerView = findViewById(R.id.processListRecyclerView) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ProcessListRecyclerAdapter(EXLDProcess.allProcesses(this))
    }

    fun addListeners()
    {
        // FAB Button
        fab.setOnClickListener { view ->
            addNewProcess()
        }
    }

    // MARK: Data Operations

    private fun addNewProcess()
    {

    }
}
