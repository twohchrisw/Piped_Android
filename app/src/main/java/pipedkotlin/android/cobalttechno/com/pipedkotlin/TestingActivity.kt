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
import org.jetbrains.anko.find
import org.jetbrains.anko.startActivityForResult
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
    lateinit var linWaitingForReading: LinearLayout // The sectkon of the action panel that shows 'Waiting for Reading 1'
    lateinit var tvWaiting: TextView
    lateinit var linCountdown: LinearLayout
    lateinit var tvCountdown: TextView
    lateinit var progCountdown: ProgressBar
    lateinit var tvPressureValueLabel: TextView
    lateinit var pvActivity: ProgressBar
    lateinit var ivBattery: ImageView
    lateinit var tvBattery: TextView

    // Vars
    var tibiisSession = TibiisSessionData()
    var testingSession = TestingSessionData()
    lateinit var calcManager: TestingCalcs
    var timer = Timer()
    var liveLogTimer = Timer()
    var readingsHaveCompleted = false
    var lastLogReading: LogReading? = null
    var testWillFailAlertIgnored = false
    var testWillFailN1Ignored = false
    val numberOfSecondsEitherSideOfReadingForScreenOn = 15
    var shouldTurnScreenOnWithNextLog = false
    var shouldTurnScreenOffWithNextLog = false
    var isScreenOn = false
    val WATER_LITRES_PER_PULSE = 0.25
    val MAX_PREVIOUS_LOGS = 30
    var lastPreviousLogRequired = -1
    var lastMaxLogNumber = 0
    var isDownloadingPreviousData = false
    var isCheckingIntegrity = false
    var hasCheckedIntegrity = false
    var preventDIAskingForLossValue = false

    // From TestingActionPanel
    var lastPreviousReading = Date()
    var isPressurisingDI = false
    var previousReadingTimer: Timer? = null

    // Constants
    val BUTTON_TEXT_START_PRESS = "Start Pressurising"
    val BUTTON_TEXT_STOP_PRESS = "Pressure Reached"
    val BUTTON_TEXT_CALCULATE = "Calculate"
    val BUTTON_TEXT_VIEW_CHART = "View Results"
    val BUTTON_TEXT_START_TEST = "Start Test"

    enum class ActivityRequestCodes(val value: Int) {
        peNotes(1), diNotes(2), listSelection(3), permissions(4), enableBluetooth(5)
    }

    companion object {
        val TESTING_CONTEXT_EXTRA = "TESTING_CONTEXT_EXTRA"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_testing_acitivty)

        // Set the context for the tbxDataController so we can run commands on the main thread
        AppGlobals.instance.tibiisController.tbxDataController.context = this

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
        formatForViewWillAppear()
        setupTibiis()

        if (testingSession.testingContext == TestingSessionData.TestingContext.di)
        {
            //TODO: createDiBackButton - to prevent the user exiting a test
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.testing_menu, menu)
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

            if (DateHelper.dateIsValid(AppGlobals.instance.activeProcess.pt_di_r60_time))
            {
                formatActionPanelForCalculate()
            }
        }

        if (testingSession.testingContext == TestingSessionData.TestingContext.di && AppGlobals.instance.activeProcess.di_is_zero_loss == -1)
        {
            if (!preventDIAskingForLossValue)
            {
                requestDILossValue()
            }
        }

        preventDIAskingForLossValue = false
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        if (AppGlobals.instance.tibiisController != null)
        {
            if (item?.itemId == R.id.mnuTestCommand)
            {
                    runOnUiThread {
                        AppGlobals.instance.tibiisController.tbxDataController.sendCommandGetCalibrationData(TBXDataController.CalibrationData.Name)
                    }

                    Timer("c1", false).schedule(100) {
                        runOnUiThread {
                            AppGlobals.instance.tibiisController.tbxDataController.sendCommandGetCalibrationData(TBXDataController.CalibrationData.Pressure1)
                        }
                    }
                    Timer("c2", false).schedule(200) {
                        runOnUiThread {
                            AppGlobals.instance.tibiisController.tbxDataController.sendCommandGetCalibrationData(TBXDataController.CalibrationData.Pressure2)
                        }
                    }
                    Timer("c3", false).schedule(300) {
                        runOnUiThread {
                            AppGlobals.instance.tibiisController.tbxDataController.sendCommandGetCalibrationData(TBXDataController.CalibrationData.Pressure3)
                        }
                    }
                    Timer("c4", false).schedule(400) {
                        runOnUiThread {
                            AppGlobals.instance.tibiisController.tbxDataController.sendCommandGetCalibrationData(TBXDataController.CalibrationData.Pressure4)
                        }

                    }
                    Timer("c5", false).schedule(500) {
                        runOnUiThread {
                            AppGlobals.instance.tibiisController.tbxDataController.sendCommandGetCalibrationData(TBXDataController.CalibrationData.Pressure5)
                        }

                    }
                    Timer("c6", false).schedule(600) {
                        runOnUiThread {
                            AppGlobals.instance.tibiisController.tbxDataController.sendCommandGetCalibrationData(TBXDataController.CalibrationData.Pressure6)
                        }

                    }
                    Timer("c7", false).schedule(700) {
                        runOnUiThread {
                            AppGlobals.instance.tibiisController.tbxDataController.sendCommandGetCalibrationData(TBXDataController.CalibrationData.Date)
                        }

                    }
                    Timer("c9", false).schedule(900) {
                        runOnUiThread {
                            AppGlobals.instance.tibiisController.tbxDataController.sendCommandGetCalibrationData(TBXDataController.CalibrationData.Time)
                        }

                    }
            }

            if (item?.itemId == R.id.mnuTestCommandFetchLiveLog)
            {
                //AppGlobals.instance.tibiisController.sendTestCommandFetchLiveLog()

                AppGlobals.instance.tibiisController.tbxDataController.sendCommandLiveLog()
            }

            if (item?.itemId == R.id.mnuTestCommandBacklight)
            {
                //AppGlobals.instance.tibiisController.tbxDataController.sendCommandOutputControl(false)
                AppGlobals.instance.tibiisController.tbxDataController.sendCommandTimeSync()
            }

            if (item?.itemId == R.id.mnuTestCommandStartTest)
            {
                runOnUiThread {
                    AppGlobals.instance.tibiisController.commandStartLogger(true)
                }
            }

            if (item?.itemId == R.id.mnuTestCommandStopTest)
            {
                runOnUiThread {
                    AppGlobals.instance.tibiisController.commandStopLogger()
                }
            }

            if (item?.itemId == R.id.mnuTestCommandFetchOldLogs)
            {
                Log.d("Cobalt", "*** FETCH OLD LOGS TAPPED")
                runOnUiThread {
                    AppGlobals.instance.tibiisController.tbxDataController.sendCommandFetchOldLogs(2, 12)
                }

            }
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
    }

    fun addListeners()
    {
        btnAction.setOnClickListener { view -> actionButtonTapped() }
        btnConnect.setOnClickListener { view -> connectButtonTapped() }
    }

    fun loadData(scrollToReading: Int = 0)
    {
        runOnUiThread {
            val recyclerLayout = recyclerView.layoutManager as LinearLayoutManager
            val firstVisible = recyclerLayout.findFirstCompletelyVisibleItemPosition()
            recyclerView.adapter = TestingRecyclerAdapter(testingSession.testingContext, arePEReadingsComplete(), this)
            recyclerLayout.scrollToPosition(firstVisible)
        }
    }

    // MARK: ROW SELECTIONS

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

    // Display the notes dialog
    // Results handled in onActivityResult
    fun setNotes()
    {
        val notesIntent = Intent(this, NotesActivity::class.java)

        if (testingSession.testingContext == TestingSessionData.TestingContext.pe)
        {
            notesIntent.putExtra(NotesActivity.NOTES_EXTRA, AppGlobals.instance.activeProcess.pt_pe_notes)
            startActivityForResult(notesIntent, ActivityRequestCodes.peNotes.value)
        }
        else
        {

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == ActivityRequestCodes.peNotes.value && data != null)
        {
            AppGlobals.instance.activeProcess.pt_pe_notes = data!!.getStringExtra(NotesActivity.NOTES_EXTRA)
        }

        if (requestCode == ActivityRequestCodes.diNotes.value && data != null)
        {
            AppGlobals.instance.activeProcess.pt_di_notes = data!!.getStringExtra(NotesActivity.NOTES_EXTRA)
        }

        if (requestCode == ActivityRequestCodes.listSelection.value && data != null)
        {
            val listId = data!!.getIntExtra("listId", -1)
            val listItem = data!!.getStringExtra("listValue")

            when (listId)
            {
                ListSelectionActivity.ListContext.installTechs.value -> {
                    if (testingSession.testingContext == TestingSessionData.TestingContext.pe) {
                        AppGlobals.instance.activeProcess.pt_pe_it = listItem
                    }
                    else
                    {
                        // Nothing here for DI
                    }
                }
                ListSelectionActivity.ListContext.pumpType.value -> {
                    if (testingSession.testingContext == TestingSessionData.TestingContext.pe) {
                        AppGlobals.instance.activeProcess.pt_pe_pump_size = listItem
                    }
                    else
                    {
                        AppGlobals.instance.activeProcess.pt_di_pump_size = listItem
                    }
                }
            }
        }

        AppGlobals.instance.activeProcess.save(this)
        loadData()
    }

    fun requestDILossValue()
    {
        val choiceDialog = AlertDialog.Builder(this)
        choiceDialog.setTitle("Metallic Test Loss")
        choiceDialog.setMessage("Please select the allowed pressure loss value for this test")
        val choiceDialogItems = arrayOf("0.2 bar", "Zero Loss")
        choiceDialog.setItems(choiceDialogItems) { dialog, which ->
            when (which) {
                0 -> {
                    runOnUiThread {
                        AppGlobals.instance.activeProcess.di_is_zero_loss = 0
                    }
                }
                1 -> {
                    runOnUiThread {
                        AppGlobals.instance.activeProcess.di_is_zero_loss = 1
                    }
                }
            }
        }
        choiceDialog.setNegativeButton("Cancel") { dialog, which ->
        }
        choiceDialog.show()
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


    // MARK: Timer Loops

    class LiveLogTimerTask(val a: TestingActivity): TimerTask()
    {
        override fun run() {
            val now = Date()

            if (!a.isDownloadingPreviousData)
            {
                AppGlobals.instance.tibiisController.tbxDataController.sendCommandLiveLog()

                if (a.shouldTurnScreenOnWithNextLog)
                {
                    Timer("screenOn", false).schedule(100) {
                        a.shouldTurnScreenOnWithNextLog = false
                        Log.d("Cobalt", "Turning Screen On")
                        AppGlobals.instance.tibiisController.tbxDataController.sendCommandScreenControl(true)
                    }
                }

                if (a.shouldTurnScreenOffWithNextLog)
                {
                    Timer("screenOff", false).schedule(100) {
                        a.shouldTurnScreenOffWithNextLog = false
                        Log.d("Cobalt", "Turning Screen Off")
                        AppGlobals.instance.tibiisController.tbxDataController.sendCommandScreenControl(false)
                    }
                }
            }
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
            a.formatActionPanelForCountdown(r1Time, DateHelper.dbStringToDate(AppGlobals.instance.activeProcess.pt_pressurising_finish, Date()), countdownString, "Waiting for Reading 1")
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
                //TODO: Tibiis Stuff
            }

            a.testingSession.timerStage = 1
            a.loadData()
        }

        fun hitReading2()
        {
            if (a.testingSession.isLoggingWithTibiis)
            {
                //TODO: Tibiis Stuff
            }

            a.testingSession.timerStage = 2
            a.loadData()
        }

        fun hitReading3()
        {
            if (a.testingSession.isLoggingWithTibiis)
            {
                //TODO: Tibiis Stuff
            }

            a.testingSession.timerStage = 3
            a.loadData()
        }
    }

    // MARK: Tibiis Controller Delegate

    override fun tibiisConnected() {
        Log.d("cobalt", "TestingActivity.tibiisConnected()")
        formatTibiisForConnected()
    }

    override fun tibiisDisconnected() {
        Log.d("cobalt", "TestingActivity.tibiisNotConnected()")
        formatTibiisForNotConnected()
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
                    Log.d("Cobalt", logReading.description())
                    if (AppGlobals.instance.tibiisController.shouldCheckForMissingLogs)
                    {
                        AppGlobals.instance.tibiisController.shouldCheckForMissingLogs = false
                        this.isDownloadingPreviousData= true
                        Log.d("Cobalt", "Downloading previous logs for lognumber: ${logReading.logNumber}")
                        downloadPreviousReadings(logReading.logNumber)
                    }

                    saveLiveLog(logReading)
                }
            }

            TBXDataController.Command.FetchOldLogs -> {

                //TODO: Needs completing properly
                val previousLogData = packet.parseDataAsPreviousLogReadings()
                if (previousLogData != null)
                {
                    val message = "Previous Logs: Start Log No: ${previousLogData.startLogNumber}, Number of Logs: ${previousLogData.numberOfLogs}"
                    Log.d("Cobalt", message)
                    val alert = AlertHelper(this)
                    runOnUiThread {
                        alert.dialogForOKAlertNoAction("Old Logs", message)
                    }
                }
            }

            TBXDataController.Command.TimeSync -> {
                Log.d("Cobalt", "TIME SYNC RECEIVED")
            }

            TBXDataController.Command.GetCalibrationData -> {
                val calibResult = packet.parseAsCalibrationData()
                //TODO: Needs completing properly
                when (calibResult.calibrationByte)
                {
                    TBXDataController.CalibrationData.Name -> { Log.d("Cobalt", "Name: ${calibResult.dataString}") }
                    TBXDataController.CalibrationData.Pressure1 -> { Log.d("Cobalt", "P1: ${calibResult.dataString}") }
                    TBXDataController.CalibrationData.Pressure2 -> { Log.d("Cobalt", "P2: ${calibResult.dataString}") }
                    TBXDataController.CalibrationData.Pressure3 -> { Log.d("Cobalt", "P3: ${calibResult.dataString}") }
                    TBXDataController.CalibrationData.Pressure4 -> { Log.d("Cobalt", "P4: ${calibResult.dataString}") }
                    TBXDataController.CalibrationData.Pressure5 -> { Log.d("Cobalt", "P5: ${calibResult.dataString}") }
                    TBXDataController.CalibrationData.Pressure6 -> { Log.d("Cobalt", "P6: ${calibResult.dataString}") }
                    TBXDataController.CalibrationData.Date -> { Log.d("Cobalt", "Date: ${calibResult.dataString}") }
                    TBXDataController.CalibrationData.Time -> { Log.d("Cobalt", "Time: ${calibResult.dataString}") }
                }
            }


            //TODO: NEEDS COMPLETING
        }
    }



}
