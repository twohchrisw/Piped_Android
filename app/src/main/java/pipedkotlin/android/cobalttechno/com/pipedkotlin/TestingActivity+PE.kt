package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.util.Log
import java.util.*

fun TestingActivity.loadCheckPE()
{
    val p = AppGlobals.instance.activeProcess
    val tc = AppGlobals.instance.tibiisController

    if (tibiisSession.getLogNumberForStart() == -1)
    {
        tibiisSession.setLogNumberForStart(p.tibsessDILogNumberForStart)
    }
    if (tibiisSession.getLogNumberForReading1() == -1)
    {
        tibiisSession.setLogNumberForReading1(p.tibsessLogNumberForReading1)
    }
    if (tibiisSession.getLogNumberForReading2() == -1)
    {
        tibiisSession.setLogNumberForReading2(p.tibsessLogNumberForReading2)
    }
    if (tibiisSession.getLogNumberForReading3() == -1)
    {
        tibiisSession.setLogNumberForReading3(p.tibsessLogNumberForReading3)
    }

    val storedStartLoggingDate = DateHelper.dbStringToDateOrNull(p.testsessStartLoggingTime)
    val storedLastLoggingDate = DateHelper.dbStringToDateOrNull(p.testsessLastLoggingTime)
    val storedFirstLoggingDate = DateHelper.dbStringToDateOrNull(p.testsessFirstLogReadingDate)
    if (testingSession.getStartLoggingTime() == null && storedStartLoggingDate != null)
    {
        testingSession.setStartLoggingTime(storedStartLoggingDate)
    }
    if (testingSession.getLastLoggingTime() == null && storedLastLoggingDate != null)
    {
        testingSession.setLastLoggingTime(storedLastLoggingDate)
    }
    if (testingSession.getFirstLogReading() == null && storedFirstLoggingDate != null)
    {
        testingSession.setFirstLogReading(storedFirstLoggingDate)
    }


    if (!DateHelper.dateIsValid(p.pt_pressurising_start) && !DateHelper.dateIsValid(p.pt_pressurising_finish))
    {
        // No test in progress
        testingSession.loggingMode = TestingSessionData.LoggingMode.waiting
        testingSession.isPressurisingWithTibiis = true
        testingSession.isLoggingWithTibiis = true
        tc.resetController()
        return
    }

    if (DateHelper.dateIsValid(p.pt_pressurising_start) && !DateHelper.dateIsValid(p.pt_pressurising_finish))
    {
        // In the middle of pressurising
        testingSession.loggingMode = TestingSessionData.LoggingMode.pressurising
        testingSession.isPressurisingWithTibiis = true
        testingSession.isLoggingWithTibiis = false
        formatActionPanelForPressurising()
        return
    }

    /* Taking readings */

    val readingsCount = p.pt_pe_readings_count
    val now = Date()
    testingSession.loggingMode = TestingSessionData.LoggingMode.logging
    testingSession.isLoggingWithTibiis = true
    testingSession.isPressurisingWithTibiis = true
    testingSession.isAmbientLoggingWithTibiis = false
    addMissedReadings()

    if (p.pe_test_has_calculated == 1)
    {
        testingSession.hasCalculatedPETest = true
        tibiisSession.hasCalculated = true
        formatActionPanelForCalculate()
        loadData()
        return
    }
    else
    {
        testingSession.hasCalculatedPETest = false
        tibiisSession.hasCalculated = false
    }

    // If the current time is earlier than the reading 3 log time, set the timer up appropriately and continue
    if (DateHelper.dateIsValid(p.pt_reading3_time))
    {
        val r3Time = DateHelper.dbStringToDate(p.pt_reading3_time, Date())
        if (now.time < r3Time.time)
        {
            resumePETimer()
            return
        }

        if (readingsCount == 3)
        {
            testingSession.timerStage = 4
        }
    }

    p.save(this)
}

fun TestingActivity.addMissedReadings()
{
    Log.d("Cobalt", "Add missed readings")
    val p = AppGlobals.instance.activeProcess
    var reading1: LogReading? = null
    var reading2: LogReading? = null
    var reading3: LogReading? = null

    if (tibiisSession.getLogNumberForReading1() > -1)
    {
        reading1 = tibiisSession.getReadingForLogNumber(tibiisSession.getLogNumberForReading1())
        Log.d("Cobalt", "Add missed reading 1: Log: ${reading1?.logNumber} Pressure: ${reading1?.pressure}")
    }

    if (tibiisSession.getLogNumberForReading2() > -1)
    {
        reading2 = tibiisSession.getReadingForLogNumber(tibiisSession.getLogNumberForReading2())
        Log.d("Cobalt", "Add missed reading 2: Log: ${reading2?.logNumber} Pressure: ${reading2?.pressure}")
    }

    if (tibiisSession.getLogNumberForReading3() > -1)
    {
        reading3 = tibiisSession.getReadingForLogNumber(tibiisSession.getLogNumberForReading3())
        Log.d("Cobalt", "Add missed reading 3: Log: ${reading3?.logNumber} Pressure: ${reading3?.pressure}")
    }

    var tableNeedsReload = false

    if (p.pt_reading_1 == 0.0 && reading1 != null)
    {
        val press = reading1!!.pressure.toDouble() / 1000.0
        p.pt_reading_1 = press
        tableNeedsReload = true
    }

    if (p.pt_reading_2 == 0.0 && reading2 != null)
    {
        val press = reading2!!.pressure.toDouble() / 1000.0
        p.pt_reading_2 = press
        tableNeedsReload = true
    }

    if (p.pt_reading_3 == 0.0 && reading3 != null)
    {
        val press = reading3!!.pressure.toDouble() / 1000.0
        p.pt_reading_3 = press
        tableNeedsReload = true
    }

    p.save(this)
    if (tableNeedsReload)
    {
        reloadTable()
    }
}

fun TestingActivity.calculatePETestResults()
{
    calcManager.calculatePETestResults()
    AppGlobals.instance.activeProcess.save(this)
    formatActionPanelForCalculate()
}

fun TestingActivity.arePEReadingsComplete(): Boolean
{
    val p = AppGlobals.instance.activeProcess

    if (testingSession.timerStage > 6)
    {
        return true
    }

    if (p.pt_pe_readings_count == 3 && testingSession.timerStage > 3)
    {
        return true
    }

    if (p.pt_pe_readings_count == 4 && testingSession.timerStage > 4)
    {
        return true
    }

    val r3Time = DateHelper.dbStringToDate(p.pt_reading3_time, Date())
    if (p.pt_reading_1 > 0 && p.pt_reading_2 > 0 && p.pt_reading_3 > 0 && r3Time.time < Date().time)
    {
        return true
    }

    return false
}

fun TestingActivity.archivePETest()
{
    //TODO: Needs completing
}

fun TestingActivity.clearPEData()
{
    AppGlobals.instance.activeProcess.clearPEData(this)
    formatForReadyToPressurise()
}

fun TestingActivity.startPressurisingButtonPressed()
{
    val p = AppGlobals.instance.activeProcess

    if (p.tibsessLogNumberForReading1 > 0)
    {
        Log.d("Cobalt", "CANNOT START PRESSURISIING WHILST WE HAVE A VALID LOG NUMBER FOR READING 1")
        return
    }

    testWillFailAlertIgnored = false
    testWillFailN1Ignored = false
    p.initialiseForPETest(this)

    p.pt_lat = lastLat
    p.pt_long = lastLng

    // Start the timer
    testingSession.timerStage = 0

    if (tibiisSession.lastReading == null)
    {
        Log.d("Cobalt", "Last Tibiis Reading was Null")
    }

    tibiisSession.startPressureReading = tibiisSession.lastReading
    Log.d("Cobalt", "Set start reading ${tibiisSession.startPressureReading?.logNumber}")
    formatActionPanelForPressurising()
    testingSession.isPressurisingWithTibiis = true
    p.save(this)

    if (AppGlobals.instance.tibiisController.connectStatus == TibiisController.ConnectionStatus.connected)
    {
        tibiisStartPressurising()
    }
    else
    {
        beginPressurisation()
    }

    //TODO: Air Pressurisation Fail Timer
}

// Begin Manual PE Pressurisation
fun TestingActivity.beginPressurisation()
{
    Log.d("cobalt", "Begin Pressurisation")
    // Reset the timer
    timer = Timer()

    AppGlobals.instance.activeProcess.pt_pressurising_start = DateHelper.dateToDBString(Date())
    AppGlobals.instance.activeProcess.needs_server_sync = 1
    AppGlobals.instance.activeProcess.save(this)
    loadData()
}

fun TestingActivity.stopPressurisingButtonPressed()
{
    if (testingSession.testingContext == TestingSessionData.TestingContext.pe)
    {
        if (DateHelper.dateIsValid(AppGlobals.instance.activeProcess.pt_pressurising_start))
        {
            AppGlobals.instance.activeProcess.pt_pressurising_finish = DateHelper.dateToDBString(Date())

            if (testingSession.isPressurisingWithTibiis && tibiisSession.countReadingOfType(TibiisSessionData.TibiisReadingType.pressurising, AppGlobals.instance.activeProcess.columnId) > 0)
            {
                shouldTurnScreenOnWithNextLog = true
                updatePressurisingDataFromTibiisSession()
            }

            //TODO: Air percentage stuff here

            setPETestFinishedPressurisingAndBeginLogging()
        }
        else
        {
            Log.d("cobalt", "Invalid pressurising start date!!!")
        }
    }

    AppGlobals.instance.activeProcess.needs_server_sync = 1
    AppGlobals.instance.activeProcess.save(this)
    testingSession.isPressurisingWithTibiis = false
}

fun TestingActivity.downloadingPreviousReadings(): Boolean
{
    return isDownloadingPreviousData
}

fun TestingActivity.calculatePEButtonPressed()
{
    if (downloadingPreviousReadings() == true)
    {
        val alert = AlertHelper(this)
        runOnUiThread {
            alert.dialogForOKAlertNoAction("Downloading Data", "Please wait until all previous logs have been downloaded before using Calculate")
        }
    }

    if (!hasCheckedIntegrity)
    {
        if (AppGlobals.instance.tibiisController.connectStatus == TibiisController.ConnectionStatus.connected)
        {
            checkLogIntegrity()
        }
    }

    if (isCheckingIntegrity)
    {
        val alert = AlertHelper(this)
        runOnUiThread {
            alert.dialogForOKAlertNoAction("Checking Log Integrity", "Please wait until all previous logs have been downloaded before using Calculate")
        }
    }

    val p = AppGlobals.instance.activeProcess


    if (DateHelper.dateIsValid(p.pt_reading1_time) && DateHelper.dateIsValid(p.pt_reading2_time) && DateHelper.dateIsValid(p.pt_reading3_time))
    {
        tibiisSession.hasCalculated = true
        calculatePETestResults()
        saveCalibrationDetails()
        loadData()
        //TODO: Load Test results view
        runOnUiThread {
            val alert = AlertHelper(this)
            alert.dialogForOKAlertNoAction("Test Results", "In development")
        }
    }
    else
    {
        val alert = AlertHelper(this)
        runOnUiThread {
            alert.dialogForOKAlertNoAction("Calculate", "You need to pressurise and enter data before calculating!")
        }

        p.needs_server_sync = 1
    }

    p.save(this)
}

fun TestingActivity.saveCalibrationDetails()
{
    //TODO: Not Implemented
    Log.d("Cobalt", "saveCalibrationDetails not implemented")
}

fun TestingActivity.loadTestResultsView()
{
    //TODO: Not implemented
}

fun TestingActivity.resetPETest()
{
    tibiisStopPressurising()
    testingSession.timerStage = 0
    archivePETest()
    clearPEData()  // Wire this i
    reloadTable()
    formatForViewWillAppear()
    AppGlobals.instance.activeProcess.initialiseForPETest(this)
    AppGlobals.instance.tibiisController.resetController()

    testingSession.resetTestingSession()
    tibiisSession.resetTibiisSesssionData()
    calcManager = TestingCalcs(testingSession.testingContext, AppGlobals.instance.activeProcess)
    formatForReadyToPressurise()

    // Resetting the timers
    liveLogTimer = Timer()
}

fun TestingActivity.resumePETimer()
{
    timer.cancel()
    timer = Timer()
    testingSession.timerStage = 0
    val r1 = DateHelper.dbStringToDate(AppGlobals.instance.activeProcess.pt_reading1_time, Date())
    val r2 = DateHelper.dbStringToDate(AppGlobals.instance.activeProcess.pt_reading2_time, Date())
    val r3 = DateHelper.dbStringToDate(AppGlobals.instance.activeProcess.pt_reading3_time, Date())
    timer.scheduleAtFixedRate(TestingActivity.PETimerTask(this, r1, r2, r3), 0, 1000)
}

fun TestingActivity.resumePETimerWithoutSettingStage()
{
    timer.cancel()
    timer = Timer()
    val r1 = DateHelper.dbStringToDate(AppGlobals.instance.activeProcess.pt_reading1_time, Date())
    val r2 = DateHelper.dbStringToDate(AppGlobals.instance.activeProcess.pt_reading2_time, Date())
    val r3 = DateHelper.dbStringToDate(AppGlobals.instance.activeProcess.pt_reading3_time, Date())
    timer.scheduleAtFixedRate(TestingActivity.PETimerTask(this, r1, r2, r3), 0, 1000)
}


fun TestingActivity.cancelPETimer()
{
    timer.cancel()
    timer = Timer()
    formatActionPanelForCalculate()
    loadData()
}
















