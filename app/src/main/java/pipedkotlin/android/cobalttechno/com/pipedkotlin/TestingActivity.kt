package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
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
        setupLocationClient()
        assignOutlets()
        addListeners()
        loadData()
        getCurrentLocation(::locationReceived)

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

    // MARK: Passed functions to dialogs for row selections

    fun setSectionName(value: String)
    {
        if (testingSession.testingContext == TestingSessionData.TestingContext.pe)
        {
            AppGlobals.instance.activeProcess.pt_section_name = value
            AppGlobals.instance.activeProcess.pt_lat = lastLat
            AppGlobals.instance.activeProcess.pt_long = lastLng
        }
        else
        {
            AppGlobals.instance.activeProcess.pt_di_section_name = value
            AppGlobals.instance.activeProcess.di_lat = lastLat
            AppGlobals.instance.activeProcess.di_long = lastLng
        }
        AppGlobals.instance.activeProcess.save(this)
        loadData()
    }

    fun locationReceived(lat: Double, lng: Double) {
        lastLat = lat
        lastLng = lng
    }

    override fun locationPermissionsGranted()
    {
        getCurrentLocation(::locationReceived)
    }

    fun setSectionLength(value: String)
    {
        val p = AppGlobals.instance.activeProcess
        if (testingSession.testingContext == TestingSessionData.TestingContext.pe) {
            p.pt_section_length = value;
        }
        else
        {
            p.pt_di_section_length = value;
        }
        p.save(this)
        loadData()
    }

    fun setPipeDiameter(value: String)
    {
        if (testingSession.testingContext == TestingSessionData.TestingContext.pe) {
            AppGlobals.instance.activeProcess.pt_pe_pipe_diameter = value.toCobaltInt()
        }
        else
        {
            AppGlobals.instance.activeProcess.pt_di_pipe_diameter = value.toCobaltInt()
        }
        AppGlobals.instance.activeProcess.save(this)
        loadData()
    }

    fun setInstallTech()
    {
        val p = AppGlobals.instance.activeProcess

        //TODO: Install Tech List

        p.save(this)
        loadData()
    }

    fun setPumpSize()
    {
        val p = AppGlobals.instance.activeProcess
        if (testingSession.testingContext == TestingSessionData.TestingContext.pe) {
            //TODO: Pump Size List
        }
        else
        {
            //TODO: Pumpsize List
        }
        p.save(this)
    }

    fun setAllowedLoss()
    {
        //TODO: Allowed Loss Dialog
    }

    fun setLoggerDetails(value: String)
    {
        val p = AppGlobals.instance.activeProcess
        if (testingSession.testingContext == TestingSessionData.TestingContext.pe) {
            p.pt_pe_logger_details = value
        }
        else
        {
            p.pt_di_logger_details = value
        }
        p.save(this)
        loadData()
    }

    fun setStartPressure(value: String)
    {
        val p = AppGlobals.instance.activeProcess
        if (testingSession.testingContext == TestingSessionData.TestingContext.pe) {
            p.pt_start_pressure = value.toCobaltDouble()
        }
        else
        {
            p.pt_di_start_pressure = value.toCobaltDouble()
        }
        p.save(this)
        loadData()
    }

    fun setSTP(value: String)
    {
        val p = AppGlobals.instance.activeProcess
        if (testingSession.testingContext == TestingSessionData.TestingContext.pe) {
            p.pt_system_test_pressure = value.toCobaltDouble()
        }
        else
        {
            p.pt_di_stp = value.toCobaltDouble()
        }
        p.save(this)
        loadData()
    }

    fun setReading1(value: String)
    {
        val p = AppGlobals.instance.activeProcess
        p.pt_reading_1 = value.toCobaltDouble()
        p.save(this)
        loadData()
    }

    fun setReading2(value: String)
    {
        val p = AppGlobals.instance.activeProcess
        p.pt_reading_2 = value.toCobaltDouble()
        p.save(this)
        loadData()
    }

    fun setReading3(value: String)
    {
        val p = AppGlobals.instance.activeProcess
        p.pt_reading_3 = value.toCobaltDouble()
        p.save(this)
        loadData()
    }

    fun setReading15(value: String)
    {
        val p = AppGlobals.instance.activeProcess
        p.pt_di_r15_value = value.toCobaltDouble()
        p.save(this)
        loadData()
    }

    fun setReading60(value: String)
    {
        val p = AppGlobals.instance.activeProcess
        p.pt_di_r60_value = value.toCobaltDouble()
        p.save(this)
        loadData()
    }

    fun setNotes()
    {
        //TODO: Notes
    }


    // MARK: TestingRecyclerClickListener



    override fun didSelectPERow(row: Int) {
        val alert = AlertHelper(this)
        val p = AppGlobals.instance.activeProcess

        when (row)
        {
            TestingRecyclerAdapter.PERows.sectionName.value -> alert.dialogForTextInput("Test Section Name", p.pt_section_name, ::setSectionName)
            TestingRecyclerAdapter.PERows.sectionLength.value -> alert.dialogForTextInput("Section Length", p.pt_section_length.toString(), ::setSectionLength)
            TestingRecyclerAdapter.PERows.pipeDiameter.value -> alert.dialogForTextInput("Pipe Diameter", p.pt_pe_pipe_diameter.toString(), ::setPipeDiameter)
            TestingRecyclerAdapter.PERows.installTech.value -> setInstallTech()
            TestingRecyclerAdapter.PERows.pumpSize.value -> setPumpSize()
            TestingRecyclerAdapter.PERows.loggerDetails.value -> alert.dialogForTextInput("Logger Details", p.pt_pe_logger_details, ::setLoggerDetails)
            TestingRecyclerAdapter.PERows.startPressure.value -> alert.dialogForTextInput("Start Pressure", p.pt_start_pressure.toString(), ::setStartPressure)
            TestingRecyclerAdapter.PERows.stp.value -> alert.dialogForTextInput("System Test Pressure", p.pt_system_test_pressure.toString(), ::setSTP)
            TestingRecyclerAdapter.PERows.reading1.value -> alert.dialogForTextInput("Reading 1", p.pt_reading_1.toString(), ::setReading1)
            TestingRecyclerAdapter.PERows.reading2.value -> alert.dialogForTextInput("Reading 2", p.pt_reading_2.toString(), ::setReading2)
            TestingRecyclerAdapter.PERows.reading3.value -> alert.dialogForTextInput("Reading 3", p.pt_reading_3.toString(), ::setReading3)
            TestingRecyclerAdapter.PERows.notes.value -> setNotes()


        }
    }

    override fun didSelectDIRow(row: Int) {
        val alert = AlertHelper(this)
        val p = AppGlobals.instance.activeProcess

        when (row)
        {
            TestingRecyclerAdapter.DIRows.sectionName.value -> alert.dialogForTextInput("Test Section Name", p.pt_di_section_name, ::setSectionName)
            TestingRecyclerAdapter.DIRows.sectionLength.value -> alert.dialogForTextInput("Section Length", p.pt_di_section_length.toString(), ::setSectionLength)
            TestingRecyclerAdapter.DIRows.pipeDiameter.value -> alert.dialogForTextInput("Pipe Diameter", p.pt_di_pipe_diameter.toString(), ::setPipeDiameter)
            TestingRecyclerAdapter.DIRows.pumpSize.value -> setPumpSize()
            TestingRecyclerAdapter.DIRows.loggerDetails.value -> alert.dialogForTextInput("Logger Details", p.pt_di_logger_details, ::setLoggerDetails)
            TestingRecyclerAdapter.DIRows.startPressure.value -> alert.dialogForTextInput("Start Pressure", p.pt_di_start_pressure.toString(), ::setStartPressure)
            TestingRecyclerAdapter.DIRows.stp.value -> alert.dialogForTextInput("System Test Pressure", p.pt_di_stp.toString(), ::setSTP)
            TestingRecyclerAdapter.DIRows.reading15.value -> alert.dialogForTextInput("Reading 15m", p.pt_di_r15_value.toString(), ::setReading15)
            TestingRecyclerAdapter.DIRows.reading60.value -> alert.dialogForTextInput("Reading 60m", p.pt_di_r60_value.toString(), ::setReading60)
            TestingRecyclerAdapter.DIRows.notes.value -> setNotes()
        }
    }
}
