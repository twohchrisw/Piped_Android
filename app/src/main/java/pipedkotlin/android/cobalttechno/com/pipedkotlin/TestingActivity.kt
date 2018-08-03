package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

import kotlinx.android.synthetic.main.activity_testing_acitivty.*

class TestingActivity : BaseActivity(), TestingRecyclerAdapter.TestingRecyclerClickListener {

    // Outlets
    lateinit var recyclerView: RecyclerView
    lateinit var linTestingActionPanel: LinearLayout
    lateinit var btnAction: Button
    lateinit var tvConnectStatus: TextView
    lateinit var btnConnect: Button

    // Vars
    var tibiisSession = TibiisSessionData()
    var testingSession = TestingSessionData()
    lateinit var calcManager: TestingCalcs

    companion object {
        val TESTING_CONTEXT_EXTRA = "TESTING_CONTEXT_EXTRA"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_testing_acitivty)

        // Assign the correct testing context
        val testingContext = intent.getStringExtra(TESTING_CONTEXT_EXTRA)
        if (testingContext == "PE")
        {
            testingSession.testingContext = TestingSessionData.TestingContext.pe
            supportActionBar?.title = "PE Testing"
        }
        else
        {
            testingSession.testingContext = TestingSessionData.TestingContext.di
            supportActionBar?.title = "DI Testing"
        }

        // Setup the calculation manager
        calcManager = TestingCalcs(testingSession.testingContext, AppGlobals.instance.activeProcess)

        // Outlets and data
        assignOutlets()
        addListeners()
        loadData()

        //TODO: setupTestingActionPanel
        //TODO: setupTibiis
    }

    fun assignOutlets()
    {
        recyclerView = findViewById(R.id.testingRecycler) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        linTestingActionPanel = findViewById(R.id.linTestingActionPanel) as LinearLayout
        btnAction = findViewById(R.id.btnAction) as Button
        tvConnectStatus = findViewById(R.id.tvConnectStatus) as TextView
        btnConnect = findViewById(R.id.btnConnect) as Button
    }

    fun addListeners()
    {
        btnAction.setOnClickListener { view -> actionButtonTapped() }
        btnConnect.setOnClickListener { view -> connectButtonTapped() }
    }

    fun loadData()
    {
        recyclerView.adapter = TestingRecyclerAdapter(testingSession.testingContext, this)
    }


    // MARK: TestingRecyclerClickListener

    override fun didSelectDIRow(row: TestingRecyclerAdapter.DIRows) {

    }

    override fun didSelectPERow(row: TestingRecyclerAdapter.PERows) {

    }
}
