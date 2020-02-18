package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*

import kotlinx.android.synthetic.main.activity_testing_acitivty.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import org.jetbrains.anko.startActivityForResult
import java.lang.Exception
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule

class TestingActivity : BaseActivity(), TestingRecyclerAdapter.TestingRecyclerClickListener, TibiisController.TibiisControllerDelegate, TBXDataController.TBXDataControllerDelegate {

    // Outlets
    lateinit var recyclerView: RecyclerView
    lateinit var linTestingActionPanel: LinearLayout
    lateinit var btnAction: Button
    lateinit var tvConnectStatus: TextView
    lateinit var btnConnect: Button
    lateinit var linPressurising: RelativeLayout // The section of the action panel that displays the pressurising spinner
    lateinit var tvPressurisingLabel: TextView
    lateinit var linWaitingForReading: LinearLayout // The section of the action panel that shows 'Waiting for Reading 1'
    lateinit var tvWaiting: TextView
    lateinit var linCountdown: LinearLayout
    lateinit var tvCountdown: TextView
    lateinit var progCountdown: ProgressBar
    lateinit var tvPressureValueLabel: TextView
    lateinit var pvActivity: ProgressBar
    lateinit var ivBattery: ImageView
    lateinit var tvBattery: TextView
    lateinit var pvDownloading: ProgressBar
    lateinit var tvDownloading: TextView

    // Vars
    var recyclerLayout = LinearLayoutManager(this)
    var tibiisSession = TibiisSessionData()
    var testingSession = TestingSessionData()
    lateinit var calcManager: TestingCalcs
    var timer = Timer()
    var liveLogTimer = Timer()
    var countUpTimer = Timer()  // Counting the pressurising seconds
    var airPrecentageTimer = Timer()
    var readingsHaveCompleted = false
    var lastLogReading: LogReading? = null
    var testWillFailAlertIgnored = false
    var testWillFailN1Ignored = false
    val numberOfSecondsEitherSideOfReadingForScreenOn = 15
    var shouldTurnScreenOnWithNextLog = false
    var shouldTurnScreenOffWithNextLog = false
    var isScreenOn = false
    val WATER_LITRES_PER_PULSE = 0.25
    val MAX_PREVIOUS_LOGS: Int = 16
    var lastPreviousLogRequired = -1
    var previousDownloadStartLog = -1
    var lastMaxLogNumber = 0
    var isDownloadingPreviousData = false
    var isCheckingIntegrity = false
    var hasCheckedIntegrity = false
    var preventDIAskingForLossValue = false
    var recyclerViewHasBeenSetup = false
    var adapter: TestingRecyclerAdapter? = null
    var isPressurisingPE = false

    // From TestingActionPanel
    var lastPreviousReading = Date()
    var isPressurisingDI = false
    var previousReadingTimer = Timer()

    // Constants
    val BUTTON_TEXT_START_PRESS = "Start Pressurising"
    val BUTTON_TEXT_STOP_PRESS = "Pressure Reached"
    val BUTTON_TEXT_CALCULATE = "Calculate"
    val BUTTON_TEXT_VIEW_CHART = "View Results"
    val BUTTON_TEXT_START_TEST = "Start Test"

    // For dev testing
    var prev_download_cycle_start: Long = 0
    var prev_cycle_marker1: Long = 0
    var prev_cycle_marker2: Long = 0
    var prev_cycle_marker3: Long = 0
    var prev_cycle_marker4: Long = 0

    var PREVIOUS_LOG_REQUEST_START_LOG = -1
    var PREVIOUS_LOG_REQUEST_NUMBER_LOGS = -1

    val REQUEST_ADD_NOTES = 1
    var plMissedCandidate = -1
    var safetyAppGlobals: AppGlobals? = null

    // Menu Items
    lateinit var menuZeroTibiis: MenuItem
    lateinit var menuEnableAutoPump: MenuItem
    lateinit var menuEnableConditioning: MenuItem
    lateinit var menuDisableAutoPump: MenuItem

    enum class ActivityRequestCodes(val value: Int) {
        peNotes(1), diNotes(2), listSelection(3), permissions(4), enableBluetooth(5)
    }

    companion object {
        val TESTING_CONTEXT_EXTRA = "TESTING_CONTEXT_EXTRA"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_testing_acitivty)

        Log.d("cobtimer", "onCreate isPressurisingPE: $isPressurisingPE")

        // Set the context for the tbxDataController so we can run commands on the main thread
        appGlobals.tibiisController.tbxDataController.context = this
        appGlobals.tibiisController.appContext = this
        safetyAppGlobals = appGlobals   // Keep a reference to app globals to try and stop it resetting

        // Assign the correct testing context
        val testingContext = intent.getStringExtra(TESTING_CONTEXT_EXTRA)
        if (testingContext == "DI")
        {
            testingSession.testingContext = TestingSessionData.TestingContext.di
            supportActionBar?.title = "Metallic Test"
        }
        else
        {
            testingSession.testingContext = TestingSessionData.TestingContext.pe
            supportActionBar?.title = "PE Testing"
        }


        // Setup the calculation manager
        calcManager = TestingCalcs(testingSession.testingContext, appGlobals.activeProcess)

        // Outlets and data

        setupLocationClient()
        assignOutlets()
        addListeners()
        loadData()
        getCurrentLocation(::locationReceived)
        formatForViewWillAppear()
        setupTibiis()

        if (testingSession.testingContext == TestingSessionData.TestingContext.di)
        {
            //TODO: createDiBackButton - to prevent the user exiting a test
        }

        Log.d("cobalt4", "Process ID: ${appGlobals.activeProcess.internalId}")
    }

    override fun onResume() {
        super.onResume()

        /* Check that the appGlobals have not reverted to null */
        if (appGlobals.activeProcess.columnId < 0)
        {
            /* AppGlobals has reset itself, need to return to login menu */
            appGlobals = AppGlobals()
            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)

        }
        else {
            /* Refresh the view */
            Log.d("cobtimer", "onResume isPressurisingPE: $isPressurisingPE")
            recyclerView.adapter?.notifyDataSetChanged()
            formatForViewWillAppear()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.d("cobtimer", "onDestroy isPressurisingPE: $isPressurisingPE")
    }

    override fun onStart() {
        super.onStart()

        Log.d("cobtimer", "onStart isPressurisingPE: $isPressurisingPE")
    }

    override fun onRestart() {
        super.onRestart()

        Log.d("cobtimer", "onRestart isPressurisingPE: $isPressurisingPE")
    }

    override fun onStop() {
        super.onStop()

        if (appGlobals.activeProcess.columnId > -1) {
            appGlobals.activeProcess.save(this)
        }
        Log.d("cobtimer", "onStop isPressurisingPE: $isPressurisingPE")
    }

    override fun onPause() {
        super.onPause()

        if (appGlobals.activeProcess.columnId > -1) {
            appGlobals.activeProcess.save(this)
        }
        Log.d("cobtimer", "onPause isPressurisingPE: $isPressurisingPE")
    }

    fun loadData(scrollToReading: Int = 0)
    {
        runOnUiThread {
            if (!recyclerViewHasBeenSetup) {
                Log.d("cob2", "loadData()")
                recyclerLayout = recyclerView.layoutManager as LinearLayoutManager
                //val firstVisible = recyclerLayout.findFirstCompletelyVisibleItemPosition()

                adapter = TestingRecyclerAdapter(testingSession.testingContext, testingSession, this)
                recyclerView.adapter = adapter
                //recyclerLayout.scrollToPosition(firstVisible)
                recyclerViewHasBeenSetup = true
            }
            else
            {
                // Why does this work fine when not connected but not when connected to Tibiis
                // What is different??
                appGlobals.activeProcess.save(this)

                //recyclerView.adapter = null
                //recyclerView.layoutManager = null
                //recyclerView.adapter = adapter
                //recyclerView.layoutManager = recyclerLayout
                adapter!!.notifyDataSetChanged()
                //Log.d("petest", "Adapter requested update")
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater

        if (testingSession.testingContext == TestingSessionData.TestingContext.di)
        {
            inflater.inflate(R.menu.testing_menu_di, menu)
        }
        else {
            inflater.inflate(R.menu.testing_menu, menu)
        }

        //menuZeroTibiis = menu!!.findItem(R.id.mnuZeroTibiisSensors)
        //menuEnableAutoPump = menu!!.findItem(R.id.mnuEnableAutoPump)
        //menuDisableAutoPump = menu!!.findItem(R.id.mnuDisableAutoPump)
        //menuEnableConditioning = menu!!.findItem(R.id.mnuEnableAutoPumpConditioning)

        //formatOptionsMenuForContext(false)

        return true
    }

    fun formatForViewWillAppear()
    {
        formatActionPanelForDefault()

        if (testingSession.testingContext == TestingSessionData.TestingContext.pe)
        {
            loadCheckPE()

            if (arePEReadingsComplete())
            {
                formatActionPanelForCalculate()
            }
        }

        if (testingSession.testingContext == TestingSessionData.TestingContext.di)
        {
            loadCheckDI()

            if (DateHelper.dateIsValid(appGlobals.activeProcess.pt_di_r60_time))
            {
                formatActionPanelForCalculate()
            }
        }

        if (testingSession.testingContext == TestingSessionData.TestingContext.di && appGlobals.activeProcess.di_is_zero_loss == -1)
        {
            if (!preventDIAskingForLossValue)
            {
                requestDILossValue()
            }
        }

        preventDIAskingForLossValue = false
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        if (item != null)
        {
            didPressActionButton(item!!.itemId)
        }

        return super.onOptionsItemSelected(item)
    }

    fun assignOutlets()
    {
        recyclerView = findViewById(R.id.testingRecycler) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        linTestingActionPanel = findViewById(R.id.linTestingActionPanel) as LinearLayout
        btnAction = findViewById(R.id.btnAction) as Button
        tvConnectStatus = findViewById(R.id.tvConnectStatus) as TextView
        btnConnect = findViewById(R.id.btnConnect) as Button
        linPressurising = findViewById(R.id.linPressurising) as RelativeLayout
        tvPressurisingLabel = findViewById(R.id.tvPressurisingLabel) as TextView
        linWaitingForReading = findViewById(R.id.linWaitingForReading) as LinearLayout
        tvWaiting = findViewById(R.id.tvWaitingLabel) as TextView
        linCountdown = findViewById(R.id.linCountdown) as LinearLayout
        tvCountdown = findViewById(R.id.tvCountdown) as TextView
        progCountdown = findViewById(R.id.progCountdown) as ProgressBar
        tvPressureValueLabel = findViewById(R.id.tvPressureValueLabel) as TextView
        pvActivity = findViewById(R.id.pvActivity) as ProgressBar
        ivBattery = findViewById(R.id.ivBattery) as ImageView
        tvBattery = findViewById(R.id.tvBatteryText) as TextView
        pvDownloading = findViewById(R.id.pvDownloading) as ProgressBar
        tvDownloading = findViewById(R.id.tvDownloading) as TextView
    }

    fun addListeners()
    {
        btnAction.setOnClickListener { view -> actionButtonTapped() }
        btnConnect.setOnClickListener { view -> connectButtonTapped() }
    }




    // MARK: ROW SELECTIONS

    fun setSectionName(value: String)
    {
        if (testingSession.testingContext == TestingSessionData.TestingContext.pe)
        {
            appGlobals.activeProcess.pt_section_name = value
            appGlobals.activeProcess.pt_lat = appGlobals.lastLat
            appGlobals.activeProcess.pt_long = appGlobals.lastLng
        }
        else
        {
            appGlobals.activeProcess.pt_di_section_name = value
            appGlobals.activeProcess.di_lat = appGlobals.lastLat
            appGlobals.activeProcess.di_long = appGlobals.lastLng
        }
        appGlobals.activeProcess.save(this)
        loadData()
    }

    fun locationReceived(lat: Double, lng: Double) {
        appGlobals.lastLat = lat
        appGlobals.lastLng = lng
    }

    override fun locationPermissionsGranted()
    {
        getCurrentLocation(::locationReceived)
    }

    fun setSectionLength(value: String)
    {
        val p = appGlobals.activeProcess

        val intValue = value.toIntOrNull()
        if (intValue == null)
        {
            return
        }

        if (testingSession.testingContext == TestingSessionData.TestingContext.pe) {
            p.pt_section_length = intValue.toString()
        }
        else
        {
            p.pt_di_section_length = intValue.toString()
        }
        p.save(this)
        loadData()
    }

    fun setPipeDiameter(value: String)
    {
        if (testingSession.testingContext == TestingSessionData.TestingContext.pe) {
            appGlobals.activeProcess.pt_pe_pipe_diameter = value.toCobaltInt()
        }
        else
        {
            appGlobals.activeProcess.pt_di_pipe_diameter = value.toCobaltInt()
        }
        appGlobals.activeProcess.save(this)
        loadData()
    }

    fun setInstallTech()
    {
        val listIntent = Intent(this, ListSelectionActivity::class.java)
        listIntent.putExtra("ListType", ListSelectionActivity.ListContext.installTechs.value)
        startActivityForResult(listIntent, ActivityRequestCodes.listSelection.value)
    }

    fun setPumpSize()
    {
        val listIntent = Intent(this, ListSelectionActivity::class.java)
        listIntent.putExtra("ListType", ListSelectionActivity.ListContext.pumpType.value)
        startActivityForResult(listIntent, ActivityRequestCodes.listSelection.value)
    }

    fun setAllowedLoss()
    {
        //TODO: Allowed Loss Dialog
    }

    fun setLoggerDetails(value: String)
    {
        val p = appGlobals.activeProcess
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
        val p = appGlobals.activeProcess
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
        val p = appGlobals.activeProcess
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
        val p = appGlobals.activeProcess
        p.pt_reading_1 = value.toCobaltDouble()
        p.save(this)
        loadData()
    }

    fun setReading2(value: String)
    {
        val p = appGlobals.activeProcess
        p.pt_reading_2 = value.toCobaltDouble()
        p.save(this)
        loadData()
    }

    fun setReading3(value: String)
    {
        val p = appGlobals.activeProcess
        p.pt_reading_3 = value.toCobaltDouble()
        p.save(this)
        loadData()
    }

    fun setReading15(value: String)
    {
        val p = appGlobals.activeProcess
        p.pt_di_r15_value = value.toCobaltDouble()
        p.save(this)
        loadData()
    }

    fun setReading60(value: String)
    {
        val p = appGlobals.activeProcess
        p.pt_di_r60_value = value.toCobaltDouble()
        p.save(this)
        loadData()
    }

    // Display the notes dialog
    // Results handled in onActivityResult
    fun setNotes()
    {
        val notesIntent = Intent(this, NotesActivity::class.java)

        if (testingSession.testingContext == TestingSessionData.TestingContext.pe)
        {
            notesIntent.putExtra(NotesActivity.NOTES_EXTRA, appGlobals.activeProcess.pt_pe_notes)
            startActivityForResult(notesIntent, ActivityRequestCodes.peNotes.value)
        }
        else
        {
            notesIntent.putExtra(NotesActivity.NOTES_EXTRA, appGlobals.activeProcess.pt_di_notes)
            startActivityForResult(notesIntent, ActivityRequestCodes.diNotes.value)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == ActivityRequestCodes.peNotes.value && data != null)
        {
            appGlobals.activeProcess.pt_pe_notes = data!!.getStringExtra(NotesActivity.NOTES_EXTRA)
        }

        if (requestCode == ActivityRequestCodes.diNotes.value && data != null)
        {
            appGlobals.activeProcess.pt_di_notes = data!!.getStringExtra(NotesActivity.NOTES_EXTRA)
        }

        if (requestCode == ActivityRequestCodes.listSelection.value && data != null)
        {
            val listId = data!!.getIntExtra("listId", -1)
            val listItem = data!!.getStringExtra("listValue")

            when (listId)
            {
                ListSelectionActivity.ListContext.installTechs.value -> {
                    if (testingSession.testingContext == TestingSessionData.TestingContext.pe) {
                        appGlobals.activeProcess.pt_pe_it = listItem
                    }
                    else
                    {
                        // Nothing here for DI
                    }
                }
                ListSelectionActivity.ListContext.pumpType.value -> {
                    if (testingSession.testingContext == TestingSessionData.TestingContext.pe) {
                        appGlobals.activeProcess.pt_pe_pump_size = listItem
                    }
                    else
                    {
                        appGlobals.activeProcess.pt_di_pump_size = listItem
                    }
                }
            }
        }

        appGlobals.activeProcess.save(this)
        loadData()
    }

    fun requestDILossValue()
    {
        val choiceDialog = AlertDialog.Builder(this)
        choiceDialog.setTitle("Metallic Test Loss: Please select the allowed pressure loss value for this test")
        //choiceDialog.setMessage("Please select the allowed pressure loss value for this test")
        val choiceDialogItems = arrayOf("0.2 bar", "Zero Loss")
        choiceDialog.setItems(choiceDialogItems) { dialog, which ->
            when (which) {
                0 -> {
                    runOnUiThread {
                        appGlobals.activeProcess.di_is_zero_loss = 0
                        appGlobals.activeProcess.save(this)
                        loadData()
                    }
                }
                1 -> {
                    runOnUiThread {
                        appGlobals.activeProcess.di_is_zero_loss = 1
                        appGlobals.activeProcess.save(this)
                        loadData()
                    }
                }
            }
        }

        //choiceDialog.setNegativeButton("Cancel") { dialog, which ->
        //}

        val dialog = choiceDialog.create()
        dialog.show()

        //choiceDialog.create().show()
    }


    // MARK: TestingRecyclerClickListener

    override fun didSelectPERow(row: Int) {
        val alert = AlertHelper(this)
        val p = appGlobals.activeProcess

        when (row)
        {
            TestingRecyclerAdapter.PERows.sectionName.value -> alert.dialogForTextInput("Test Section Name", p.pt_section_name, ::setSectionName)
            TestingRecyclerAdapter.PERows.sectionLength.value -> alert.dialogForTextInput("Section Length", p.pt_section_length.toString(), ::setSectionLength)
            TestingRecyclerAdapter.PERows.pipeDiameter.value -> alert.dialogForIntegerInput("Pipe Diameter", p.pt_pe_pipe_diameter.toString(), ::setPipeDiameter)
            TestingRecyclerAdapter.PERows.installTech.value -> setInstallTech()
            TestingRecyclerAdapter.PERows.pumpSize.value -> setPumpSize()
            TestingRecyclerAdapter.PERows.loggerDetails.value -> alert.dialogForTextInput("Logger Details", p.pt_pe_logger_details, ::setLoggerDetails)
            TestingRecyclerAdapter.PERows.startPressure.value -> alert.dialogForDecimalInput("Start Pressure", p.pt_start_pressure.toString(), ::setStartPressure)
            TestingRecyclerAdapter.PERows.stp.value -> alert.dialogForDecimalInput("System Test Pressure", p.pt_system_test_pressure.toString(), ::setSTP)
            TestingRecyclerAdapter.PERows.reading1.value -> alert.dialogForDecimalInput("Reading 1", p.pt_reading_1.toString(), ::setReading1)
            TestingRecyclerAdapter.PERows.reading2.value -> alert.dialogForDecimalInput("Reading 2", p.pt_reading_2.toString(), ::setReading2)
            TestingRecyclerAdapter.PERows.reading3.value -> alert.dialogForDecimalInput("Reading 3", p.pt_reading_3.toString(), ::setReading3)
            TestingRecyclerAdapter.PERows.notes.value -> setNotes()
        }
    }

    override fun didSelectDIRow(row: Int) {
        val alert = AlertHelper(this)
        val p = appGlobals.activeProcess

        when (row)
        {
            TestingRecyclerAdapter.DIRows.sectionName.value -> alert.dialogForTextInput("Test Section Name", p.pt_di_section_name, ::setSectionName)
            TestingRecyclerAdapter.DIRows.sectionLength.value -> alert.dialogForIntegerInput("Section Length", p.pt_di_section_length.toString(), ::setSectionLength)
            TestingRecyclerAdapter.DIRows.pipeDiameter.value -> alert.dialogForIntegerInput("Pipe Diameter", p.pt_di_pipe_diameter.toString(), ::setPipeDiameter)
            TestingRecyclerAdapter.DIRows.pumpSize.value -> setPumpSize()
            TestingRecyclerAdapter.DIRows.loggerDetails.value -> alert.dialogForTextInput("Logger Details", p.pt_di_logger_details, ::setLoggerDetails)
            TestingRecyclerAdapter.DIRows.startPressure.value -> alert.dialogForDecimalInput("Start Pressure", p.pt_di_start_pressure.toString(), ::setStartPressure)
            TestingRecyclerAdapter.DIRows.stp.value -> alert.dialogForDecimalInput("System Test Pressure", p.pt_di_stp.toString(), ::setSTP)
            TestingRecyclerAdapter.DIRows.reading15.value -> alert.dialogForDecimalInput("Reading 15m", p.pt_di_r15_value.toString(), ::setReading15)
            TestingRecyclerAdapter.DIRows.reading60.value -> alert.dialogForDecimalInput("Reading 60m", p.pt_di_r60_value.toString(), ::setReading60)
            TestingRecyclerAdapter.DIRows.notes.value -> setNotes()
        }
    }


    // MARK: Timer Loops

    fun cancelAirPercentageTimer()
    {
        airPrecentageTimer.cancel()
        airPrecentageTimer = Timer()
    }

    fun airPercentageAlert()
    {
        runOnUiThread {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Test Will Fail")
            builder.setMessage("This test will fail, do you want to continue?")

            builder.setPositiveButton("Continue", { dialog, i ->
                testWillFailAlertIgnored = true
                dialog.dismiss()
            })

            builder.setNegativeButton("Abort", { dialog, i ->
                abortPETest()
                dialog.dismiss()
            })

            builder.create().show()
        }
    }

    class AirPrecentageTimerTask(val a: TestingActivity): TimerTask()
    {
        override fun run() {
            Log.d("petest", "Air Percentage Timer")
            val p = appGlobals.activeProcess
            a.loadData()

            if (a.testingSession.testingContext == TestingSessionData.TestingContext.pe && p.isPEPressurising() && a.testWillFailAlertIgnored == false)
            {
                var pstart = DateHelper.dbStringToDateOrNull(appGlobals.activeProcess.pt_pressurising_start)
                val airCalc = AirPressureCalc(p, TestingSessionData.TestingContext.pe)
                if (airCalc.isValid().first)
                {
                    val airPressureSeconds = airCalc.performCalc()
                    if (airPressureSeconds != null)
                    {
                        val calendar = Calendar.getInstance()
                        calendar.time = pstart
                        calendar.add(Calendar.SECOND, airPressureSeconds.fourPercent)
                        val cutOff4PercentSeconds = calendar.time

                        if (cutOff4PercentSeconds.time < Date().time)
                        {
                            Log.d("petest", "Over 4% Cut off")
                            a.airPercentageAlert()
                            a.cancelAirPercentageTimer()
                        }
                    }
                }

                // Failsafe to turn timer off
                if (p.pt_pressurising_finish.length > 1 && a.testingSession.testingContext == TestingSessionData.TestingContext.pe)
                {
                    a.cancelAirPercentageTimer()
                }
            }
        }
    }

    class TurnOffPreviousTask(val a: TestingActivity): TimerTask() {
        override fun run() {
            a.turnOffPreviousReadings()
        }
    }


    class LiveLogTimerTask(val a: TestingActivity): TimerTask()
    {
        var hasStarted = false

        override fun run() {

            if (appGlobals.tibiisController.connectStatus != TibiisController.ConnectionStatus.connected)
            {
                return
            }

            try {
                val now = Date()

                if (!a.isDownloadingPreviousData)
                {
                    appGlobals.tibiisController.tbxDataController.sendCommandLiveLog()

                    if (a.shouldTurnScreenOnWithNextLog)
                    {
                        Timer("screenOn", false).schedule(100) {
                            a.shouldTurnScreenOnWithNextLog = false
                            Log.d("Cobalt", "Turning Screen On")
                            appGlobals.tibiisController.tbxDataController.sendCommandScreenControl(true)
                        }
                    }

                    if (a.shouldTurnScreenOffWithNextLog)
                    {
                        Timer("screenOff", false).schedule(100) {
                            a.shouldTurnScreenOffWithNextLog = false
                            Log.d("Cobalt", "Turning Screen Off")
                            appGlobals.tibiisController.tbxDataController.sendCommandScreenControl(false)
                        }
                    }
                }
                else
                {
                    // There's a chance that the previous download log command has not been responded to
                    // Here we refire it
                    if (a.PREVIOUS_LOG_REQUEST_START_LOG > -1 && a.PREVIOUS_LOG_REQUEST_NUMBER_LOGS > -1) {

                        // We only refire if we have waiting more then a second for the data
                        val timeWaitedForResponse = Date().time - a.prev_download_cycle_start
                        if (timeWaitedForResponse > 800)
                        {
                            val startAt = a.PREVIOUS_LOG_REQUEST_START_LOG
                            val numberOfLogs = a.PREVIOUS_LOG_REQUEST_NUMBER_LOGS
                            //a.PREVIOUS_LOG_REQUEST_START_LOG = -1
                            //a.PREVIOUS_LOG_REQUEST_NUMBER_LOGS = -1
                            Log.d("zzz", "XX Refiring unresponded download command start log: ${startAt} number of logs: ${numberOfLogs} after waiting $timeWaitedForResponse")
                            a.prev_download_cycle_start = Date().time
                            appGlobals.tibiisController.tbxDataController.sendCommandFetchOldLogs(startAt, numberOfLogs)
                        }
                        else
                        {
                            //Log.d("zzz", "XX Refire Request NOT Refired only waited $timeWaitedForResponse")
                        }
                    }

                }
            }
            catch (e: Exception)
            {

            }
            hasStarted = true

        }
    }

    class PressurisingCountupTimer(val a: TestingActivity): TimerTask()
    {
        override fun run() {
            val now = Date()

            val defaultDate = DateHelper.date1970()
            val pressurisingStarted = DateHelper.dbStringToDate(appGlobals.activeProcess.pt_pressurising_start, defaultDate)

            // Don't update the timer if we haven't got a valid pressurising start date
            val cal = Calendar.getInstance()
            cal.time = pressurisingStarted
            if (cal.get(Calendar.YEAR) < 2000)
            {
                return
            }

            a.runOnUiThread {
                val diff = now.time - pressurisingStarted.time
                val countUpText = DateHelper.timeDifferenceFormattedForCountdown(diff)
                a.setPressurisingButtonText(countUpText)
            }

        }
    }

    class DITimerTask(val a: TestingActivity, val r15Time: Date, val r60Time: Date): TimerTask()
    {
        override fun run() {
            val now = Date()
            val p = appGlobals.activeProcess

            //Log.d("ditest", "DITimerTask TimerStage: ${a.testingSession.timerStage}")

            if (a.testingSession.timerStage == 0)
            {
                if (now.time < r15Time.time)
                {
                    waitingFor15mReading(now)
                }
                else
                {
                    Log.d("ditest", "HIT R15 Reading")

                    if (a.tibiisSession.lastReading != null)
                    {
                        Log.d("ditest", "SAVE R15 Tibiis Reading")
                        a.saveReading15(a.tibiisSession.lastReading!!)
                    }

                    // Are we conditioning
                    if (a.testingSession.getIsDITestConditioning())
                    {
                        // Is the test failing
                        Log.d("ditest", "Testing for conditioning fail")
                        var isFailing = false
                        val loss = p.getDIR15CalcResult()
                        if (p.di_is_zero_loss == 0)
                        {
                            if (loss > appGlobals.DI_15_MIN_MAXIMUM)
                            {
                                isFailing = true
                            }
                        }

                        if (p.di_is_zero_loss == 1)
                        {
                            if (loss > appGlobals.DI_TESTING_ZERO_LOSS_VALUE)
                            {
                                isFailing = true
                            }
                        }

                        if (isFailing)
                        {
                            Log.d("ditest", "DI TEST IS FAILING FOR CONDITIONING")
                            a.testingSession.timerStage = -1
                            //TODO: Work with pressure timer for DI Conditioning
                        }
                    }

                    a.testingSession.timerStage = 1
                    a.loadData()
                }
            }


            if (a.testingSession.timerStage == 1)
            {
                if (now.time < r60Time.time)
                {
                    waitingFor60mReading(now)
                }
                else
                {
                    Log.d("ditest", "HIT R60 Reading")
                    if (a.tibiisSession.lastReading != null)
                    {
                        Log.d("ditest", "Calling save for R60 reading")
                        a.saveReading60(a.tibiisSession.lastReading!!)
                    }

                    a.testingSession.timerStage = 2
                    a.loadData()
                }
            }

            if (a.testingSession.timerStage == 2)
            {
                //Log.d("ditest", "Formatting for Calculate")
                a.loadData()
                a.cancelPETimer()
                a.testingSession.timerStage = 99
            }

        }

        fun waitingFor15mReading(now: Date)
        {
            Log.d("ditest", "Waiting for 15m")
            val millisDiff = r15Time.time - now.time
            val countdownString = DateHelper.timeDifferenceFormattedForCountdown(millisDiff)
            a.formatActionPanelForCountdown(r15Time, DateHelper.dbStringToDate(appGlobals.activeProcess.pt_di_pressurising_started, Date()), countdownString, "Waiting for 15m Reading")
        }

        fun waitingFor60mReading(now: Date)
        {
            Log.d("ditest", "Waiting for 60m")
            val millisDiff = r60Time.time - now.time
            val countdownString = DateHelper.timeDifferenceFormattedForCountdown(millisDiff)
            a.formatActionPanelForCountdown(r60Time, r15Time, countdownString, "Waiting for 60m Reading")
        }
    }

    class PETimerTask(val a: TestingActivity, val r1Time: Date, val r2Time: Date, val r3Time: Date): TimerTask()
    {
        override fun run() {
            val now = Date()

            // Reading 1
            if (a.testingSession.timerStage == 0)
            {
                if (now.time < r1Time.time)
                {
                    waitingForReading1()
                }
                else
                {
                    Log.d("petest", "Reading 1 now.time ${now.time} r1Time.time ${r1Time.time}")
                    hitReading1()
                }
            }

            if (a.testingSession.timerStage == 1)
            {
                if (now.time < r2Time.time)
                {
                    waitingForReading2()
                }
                else
                {
                    hitReading2()
                }
            }

            if (a.testingSession.timerStage == 2)
            {
                if (now.time < r3Time.time)
                {
                    waitingForReading3()
                }
                else
                {
                    hitReading3()
                }
            }

            if (a.testingSession.timerStage == 3)
            {
                a.testingSession.timerStage = 4
                a.cancelPETimer()
            }
        }

        fun waitingForReading1()
        {
            val millisDiff = r1Time.time - Date().time
            val countdownString = DateHelper.timeDifferenceFormattedForCountdown(millisDiff)
            a.formatActionPanelForCountdown(r1Time, DateHelper.dbStringToDate(appGlobals.activeProcess.pt_pressurising_finish, Date()), countdownString, "Waiting for Reading 1")
        }

        fun waitingForReading2()
        {
            val millisDiff = r2Time.time - Date().time
            val countdownString = DateHelper.timeDifferenceFormattedForCountdown(millisDiff)
            a.formatActionPanelForCountdown(r2Time, r1Time, countdownString, "Waiting for Reading 2")
        }

        fun waitingForReading3()
        {
            val millisDiff = r3Time.time - Date().time
            val countdownString = DateHelper.timeDifferenceFormattedForCountdown(millisDiff)
            a.formatActionPanelForCountdown(r3Time, r2Time, countdownString, "Waiting for Reading 3")
        }

        fun hitReading1()
        {
            if (a.testingSession.isLoggingWithTibiis)
            {
                if (a.tibiisSession.lastReading != null)
                {
                    Log.d("petest", "Hit Reading 1 - saving reading")
                    a.saveReading1(a.tibiisSession.lastReading!!)
                }
                else
                {
                    Log.d("Cobalt", "Reading 1 Missed by Logger")
                }

                a.tibiisSession.numberOfReading = 1
            }

            a.testingSession.timerStage = 1
            a.loadData()
        }

        fun hitReading2()
        {
            if (a.testingSession.isLoggingWithTibiis)
            {
                if (a.testingSession.isLoggingWithTibiis)
                {
                    if (a.tibiisSession.lastReading != null)
                    {
                        a.saveReading2(a.tibiisSession.lastReading!!)
                    }
                    else
                    {
                        Log.d("Cobalt", "Reading 2 Missed by Logger")
                    }

                    a.tibiisSession.numberOfReading = 2
                }
            }

            a.testingSession.timerStage = 2
            a.loadData()
        }

        fun hitReading3()
        {
            if (a.testingSession.isLoggingWithTibiis)
            {
                if (a.testingSession.isLoggingWithTibiis)
                {
                    if (a.tibiisSession.lastReading != null)
                    {
                        a.saveReading3(a.tibiisSession.lastReading!!)
                    }
                    else
                    {
                        Log.d("Cobalt", "Reading 3 Missed by Logger")
                    }

                    a.tibiisSession.numberOfReading = 3
                }
            }

            a.testingSession.timerStage = 3
            a.loadData()
        }
    }

    // MARK: Tibiis Controller Delegate

    override fun tibiisConnected() {

        val tc = appGlobals.tibiisController
        val p = appGlobals.activeProcess

        formatOptionsMenuForContext(true)

        /* DI */

        if (tc.testingContext == TestingSessionData.TestingContext.di)
        {
            if (tibiisSession.getLogNumberForR15() > 0 || tc.tibiisHasBeenConnected)
            {
                Log.d("Cobalt", "DI Reconnection")
                tc.shouldCheckForMissingLogs = true
                tc.previousCommand = tc.currentCommand

                if (tc.currentCommand == TibiisController.CurrentCommand.none)
                {
                    tc.currentCommand = TibiisController.CurrentCommand.logger
                }

                formatTibiisForConnected()
            }
            else
            {
                tc.tibiisHasBeenConnected = true
                Log.d("Cobalt", "Start connection for DI")
                testingSession.loggingMode = TestingSessionData.LoggingMode.waiting
                testingSession.isPressurisingWithTibiis = false
                testingSession.isLoggingWithTibiis = false
                testingSession.isAmbientLoggingWithTibiis = false
                tc.startPressureSession()
                formatTibiisForConnected()
            }

            return
        }

        /* PE */

        // Is this a reconnection whilst logging
        if (tibiisSession.getLogNumberForReading1() > 0)
        {
            Log.d("petest", "PE Reconnect shouldCheckForMissingLogs = true")

            tc.shouldCheckForMissingLogs = true
            tc.previousCommand = tc.currentCommand

            if (tc.currentCommand == TibiisController.CurrentCommand.none)
            {
                tc.currentCommand = TibiisController.CurrentCommand.logger
            }
        }
        else
        {
            Log.d("petest", "PE Reconnect did not set shouldCheckForMissingLogs")
            if (!testingSession.isPressurisingWithTibiis)
            {
                testingSession.loggingMode = TestingSessionData.LoggingMode.waiting
                testingSession.isPressurisingWithTibiis = false
                testingSession.isLoggingWithTibiis = false
                testingSession.isAmbientLoggingWithTibiis = false
                tc.tibiisHasBeenConnected = true
                tc.startPressureSession()
            }
            else
            {
                Log.d("Cobalt", "Reconnection whilst pressurising")
            }
        }

        formatTibiisForConnected()
    }

    override fun tibiisDisconnected() {
        //val abortPressurising = testingSession.isPressurisingWithTibiis
        formatTibiisForNotConnected()
        formatOptionsMenuForContext(false)
        updatePressureGuageForZero()

        /*
        if (abortPressurising)
        {
            if (testingSession.testingContext == TestingSessionData.TestingContext.pe)
            {
                abortPETest()
                runOnUiThread {
                    //val alertHelper = AlertHelper(this)
                    //alertHelper.dialogForOKAlertNoAction("Test Aborted!", "The Bluetooth Connection was lost.  Current test has been aborted.  Please remember to reset your Tibiis device.")
                }
            }
        }

         */
    }

    override fun tibiisFailedToConnect() {
        val alert = AlertHelper(this)
        alert.dialogForOKAlertNoAction("Tibiis Not Found", "The Tibiis device could not be found.")
        formatTibiisForNotConnected()
    }

    // MARK: TBX Controller Delegate
    // This is TasksTesting+TBX in iOS

    override fun TbxDataControllerNoResponseToCommand() {
        Log.d("Cobalt", "TBXDataController - no response")
    }

    override fun TbxDataControllerPacketReceived(packet: TBXDataController.IncomingPacket) {
        //Log.d("Cobalt", "Received Packet")

        if (packet.command == null)
        {
            Log.d("Cobalt", "NULL Command in incoming packet")
            return
        }

        when (packet.command!!)
        {
            TBXDataController.Command.ProtocolVersion -> {
                Log.d("Cobalt", "Protocol Version: ${packet.parseDataAsProtocolVersion()})")
            }

            TBXDataController.Command.FetchLiveLog -> {
                if (packet.parseAsLogReading() != null)
                {
                    val logReading = packet.parseAsLogReading()!!
                    Log.d("LogReading", logReading.description())
                    saveLiveLog(logReading)

                    if (appGlobals.tibiisController.shouldCheckForMissingLogs)
                    {
                        Log.d("zzz", "shouldCheckForMissingLogs is true")
                        appGlobals.tibiisController.shouldCheckForMissingLogs = false
                        this.isDownloadingPreviousData= true
                        Log.d("zzz", "Downloading previous logs for lognumber: ${logReading.logNumber}")
                        downloadPreviousReadings(logReading.logNumber)
                    }
                }
            }

            TBXDataController.Command.FetchOldLogs -> {

                prev_cycle_marker1 = Date().time
                val previousLogData = packet.parseDataAsPreviousLogReadings()
                prev_cycle_marker2 = Date().time
                if (previousLogData != null)
                {
                    PREVIOUS_LOG_REQUEST_NUMBER_LOGS = -1
                    PREVIOUS_LOG_REQUEST_START_LOG = -1
                    plMissedCandidate = -1

                    val message = "Received Previous Logs: Start Log No: ${previousLogData.startLogNumber}, Number of Logs: ${previousLogData.numberOfLogs}"
                    Log.d("zzz", message)
                    for (log in previousLogData.logs)
                    {
                        Log.d("cobpr","Previous log: ${log.description()}")
                        saveLiveLog(log, true)
                    }

                    // Save the actual reading
                    saveLiveLog(previousLogData!!.liveLog)

                    prev_cycle_marker3 = Date().time
                    lastMaxLogNumber = previousLogData!!.maxLogNumber

                    if (lastMaxLogNumber == -1)
                    {
                        Log.d("Cobalt", "Negative Max Log")
                        lastMaxLogNumber = 1
                    }

                    Log.d("zzz", "Cycle: request start: $prev_download_cycle_start data back: $prev_cycle_marker1 data parsed: $prev_cycle_marker2 logs saved: $prev_cycle_marker3 number of logs: ${previousLogData.logs.size}")
                    continueProcessingPreviousLogs()

                }
                else
                {
                    Log.d("cobpr", "Previous log data is NULL")
                }
            }

            TBXDataController.Command.TimeSync -> {
                Log.d("Cobalt", "TIME SYNC RECEIVED")
            }

            TBXDataController.Command.GetOptionBytes -> {

                val serialNumber = packet.parseAsSerialNumber()
                val trimmedSerialNumber = serialNumber.trim()
                var actualSerialNumner = ""
                if (trimmedSerialNumber.length == 1)
                {
                    actualSerialNumner = "Tibiis0000${trimmedSerialNumber}"
                }
                if (trimmedSerialNumber.length == 2)
                {
                    actualSerialNumner = "Tibiis000${trimmedSerialNumber}"
                }
                if (trimmedSerialNumber.length == 3)
                {
                    actualSerialNumner = "Tibiis00${trimmedSerialNumber}"
                }
                if (trimmedSerialNumber.length == 4)
                {
                    actualSerialNumner = "Tibiis0${trimmedSerialNumber}"
                }

                if (tibiisSession.testingContext == TestingSessionData.TestingContext.pe)
                {
                    appGlobals.activeProcess.pt_pe_logger_details = actualSerialNumner
                }
                else
                {
                    appGlobals.activeProcess.pt_di_logger_details = actualSerialNumner
                }
                runOnUiThread {
                    Log.d("zzz", "Saving serial number ${actualSerialNumner}")
                    appGlobals.activeProcess.save(this)
                    loadData()
                }

            }

            TBXDataController.Command.GetCalibrationData -> {
                val calibResult = packet.parseAsCalibrationData()
                val p = appGlobals.activeProcess
                val result = calibResult.dataString

                when (calibResult.calibrationByte)
                {
                    TBXDataController.CalibrationData.Name -> {
                        Log.d("cobcalib", "Saving calib name: $result")
                        p.calib_name = result
                        p.saveCalibDetails(MainApplication.applicationContext())
                    }

                    TBXDataController.CalibrationData.Temp -> {
                        Log.d("cobcalib", "Saving calib temp: $result")
                        p.calib_temp = result
                        p.saveCalibDetails(MainApplication.applicationContext())
                    }

                    TBXDataController.CalibrationData.Date -> {
                        Log.d("cobcalib", "Saving calib date: $result")
                        p.calib_date = result
                        p.saveCalibDetails(MainApplication.applicationContext())
                    }

                    TBXDataController.CalibrationData.Time -> {
                        Log.d("cobcalib", "Saving calib time: $result")
                        p.calib_time = result
                        p.saveCalibDetails(MainApplication.applicationContext())
                    }

                    TBXDataController.CalibrationData.Pressure1 -> {
                        Log.d("cobcalib", "Saving calib p1: $result")
                        p.calib_p1 = result
                        p.saveCalibDetails(MainApplication.applicationContext())
                    }

                    TBXDataController.CalibrationData.Pressure2 -> {
                        Log.d("cobcalib", "Saving calib p2: $result")
                        p.calib_p2 = result
                        p.saveCalibDetails(MainApplication.applicationContext())
                    }

                    TBXDataController.CalibrationData.Pressure3 -> {
                        Log.d("cobcalib", "Saving calib p3: $result")
                        p.calib_p3 = result
                        p.saveCalibDetails(MainApplication.applicationContext())
                    }

                    TBXDataController.CalibrationData.Pressure4 -> {
                        Log.d("cobcalib", "Saving calib p4: $result")
                        p.calib_p4 = result
                        p.saveCalibDetails(MainApplication.applicationContext())
                    }

                    TBXDataController.CalibrationData.Pressure5 -> {
                        Log.d("cobcalib", "Saving calib p5: $result")
                        p.calib_p5 = result
                        p.saveCalibDetails(MainApplication.applicationContext())
                    }

                    TBXDataController.CalibrationData.Pressure6 -> {
                        Log.d("cobcalib", "Saving calib p6: $result")
                        p.calib_p6 = result
                        p.saveCalibDetails(MainApplication.applicationContext())
                    }
                }
            }


            //TODO: NEEDS COMPLETING
        }


    }



}
