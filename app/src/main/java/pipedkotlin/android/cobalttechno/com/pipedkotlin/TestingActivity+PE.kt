package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.content.Intent
import android.util.Log
import java.util.*
import kotlin.concurrent.schedule

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

    if (p.pt_pressurising_start != "" && DateHelper.dateIsValid(p.pt_pressurising_start) && !DateHelper.dateIsValid(p.pt_pressurising_finish))
    {
        // In the middle of pressurising
        testingSession.loggingMode = TestingSessionData.LoggingMode.pressurising
        testingSession.isPressurisingWithTibiis = true
        testingSession.isLoggingWithTibiis = false
        formatActionPanelForPressurising()
        beginCountupTimer()
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
    AppGlobals.instance.activeProcess.archivePETest(this)
}

fun TestingActivity.clearPEData()
{
    AppGlobals.instance.activeProcess.clearPEData(this)
    AppGlobals.instance.activeProcess.save(this)
    formatForReadyToPressurise()
}

fun TestingActivity.startPressurisingButtonPressed()
{
    val p = AppGlobals.instance.activeProcess
    isPressurisingPE = true

    if (p.tibsessLogNumberForReading1 > 0)
    {
        Log.d("petest", "CANNOT START PRESSURISIING WHILST WE HAVE A VALID LOG NUMBER FOR READING 1")
        return
    }

    testWillFailAlertIgnored = false
    testWillFailN1Ignored = false
    p.initialiseForPETest(this)

    p.pt_lat = AppGlobals.instance.lastLat
    p.pt_long = AppGlobals.instance.lastLng

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
        if (tibiisSession.startPressureReading != null)
        {
            val press = tibiisSession.startPressureReading!!.pressure / 1000.0
            p.pt_start_pressure = press
            p.save(this)
            recyclerView.adapter.notifyDataSetChanged()
        }

        tibiisStartPressurising()
        saveCalibrationDetails()
    }
    else
    {
        testingSession.isPressurisingWithTibiis = false
        beginPressurisation()
    }

    beginCountupTimer()

    airPrecentageTimer.scheduleAtFixedRate(TestingActivity.AirPrecentageTimerTask(this), 0, 2000)
}

fun TestingActivity.beginCountupTimer()
{
    Log.d("petest", "Begin count up timer")
    countUpTimer.cancel()
    countUpTimer = Timer()
    runOnUiThread {
        countUpTimer.scheduleAtFixedRate(TestingActivity.PressurisingCountupTimer(this), 0, 1000)
    }
}

fun TestingActivity.cancelCountupTimer()
{
    countUpTimer.cancel()
    countUpTimer = Timer()
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
    Log.d("petest", "stopPressurisingButtonPressed")
    if (testingSession.testingContext == TestingSessionData.TestingContext.pe)
    {
        cancelCountupTimer()
        isPressurisingPE = false

        if (DateHelper.dateIsValid(AppGlobals.instance.activeProcess.pt_pressurising_start))
        {
            AppGlobals.instance.activeProcess.pt_pressurising_finish = DateHelper.dateToDBString(Date())

            if (testingSession.isPressurisingWithTibiis && tibiisSession.countReadingOfType(TibiisSessionData.TibiisReadingType.pressurising, AppGlobals.instance.activeProcess.columnId) > 0)
            {
                shouldTurnScreenOnWithNextLog = true
                updatePressurisingDataFromTibiisSession()
            }

            var airPercentage = 0
            var pstart = DateHelper.dbStringToDateOrNull(AppGlobals.instance.activeProcess.pt_pressurising_start)
            var pend = DateHelper.dbStringToDateOrNull(AppGlobals.instance.activeProcess.pt_pressurising_finish)
            if (pstart != null && pend != null)
            {
                val pressurisingSeconds = (pend.time - pstart.time) / 1000
                Log.d("petest", "Pressurising seconds: $pressurisingSeconds")
                val airCalc = AirPressureCalc(AppGlobals.instance.activeProcess, TestingSessionData.TestingContext.pe)
                if (airCalc.isValid().first)
                {
                    val airPressureSeconds = airCalc.performCalc()
                    if (airPressureSeconds != null)
                    {
                        if (pressurisingSeconds >= airPressureSeconds!!.onePercent && pressurisingSeconds < airPressureSeconds!!.twoPrecent)
                        {
                            airPercentage = 1
                        }
                        if (pressurisingSeconds >= airPressureSeconds!!.twoPrecent && pressurisingSeconds < airPressureSeconds!!.threePercent)
                        {
                            airPercentage = 2
                        }
                        if (pressurisingSeconds >= airPressureSeconds!!.threePercent && pressurisingSeconds < airPressureSeconds!!.fourPercent)
                        {
                            airPercentage = 3
                        }
                        if (pressurisingSeconds >= airPressureSeconds.fourPercent)
                        {
                            airPercentage = 4
                        }
                    }
                }
            }

            AppGlobals.instance.activeProcess.pt_reading_5 = airPercentage.toDouble()
            AppGlobals.instance.activeProcess.save(this)
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
        return
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
        return
    }

    val p = AppGlobals.instance.activeProcess


    if (DateHelper.dateIsValid(p.pt_reading1_time) && DateHelper.dateIsValid(p.pt_reading2_time) && DateHelper.dateIsValid(p.pt_reading3_time))
    {
        tibiisSession.hasCalculated = true

        timer.cancel()
        timer = Timer()
        liveLogTimer = Timer()

        calculatePETestResults()
        //saveCalibrationDetails()
        loadData()
        loadPEGraph()

        tibiisStopPressurising()

        Timer("stopTest", false).schedule(1000) {
            if (AppGlobals.instance.tibiisController.connectStatus == TibiisController.ConnectionStatus.connected) {
                AppGlobals.instance.tibiisController.disconnectTibiis()
            }
        }
        /*
        runOnUiThread {
            val alert = AlertHelper(this)
            alert.dialogForOKAlertNoAction("Test Results", "In development")
        }
        */
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

fun TestingActivity.loadPEGraph()
{
    val peGraphIntent = Intent(this, PEGraphActivity::class.java)
    //peGraphIntent.putExtra(ProcessMenuActivity.MENU_MODE_KEY, ProcessMenuActivity.MENU_MODE_TASKS)
    startActivity(peGraphIntent)
}

fun TestingActivity.saveCalibrationDetails()
{
    val tc = AppGlobals.instance.tibiisController
    val p = AppGlobals.instance.activeProcess

    Log.d("cobcalib", "saveCalibrationDetails()")
    Log.d("cobcalib", p.calibDetailsDescription())

    if (tc.connectStatus == TibiisController.ConnectionStatus.connected) {


        if (p.calib_name.length == 0) {
            Log.d("cobcalib", "Requesting Calib name")
            runOnUiThread {
                tc.tbxDataController.sendCommandGetCalibrationData(TBXDataController.CalibrationData.Name)
            }
        }

        if (p.calib_name.length == 0) {
            Timer("calib11", false).schedule(125) {
                runOnUiThread {
                    Log.d("cobcalib", "Requesting Calib temp")
                    tc.tbxDataController.sendCommandGetCalibrationData(TBXDataController.CalibrationData.Name)
                }
            }
        }

        if (p.calib_name.length == 0) {
            Timer("calib111", false).schedule(250) {
                runOnUiThread {
                    Log.d("cobcalib", "Requesting Calib temp")
                    tc.tbxDataController.sendCommandGetCalibrationData(TBXDataController.CalibrationData.Name)
                }
            }
        }

        if (p.calib_name.length == 0) {
            Timer("calib111", false).schedule(375) {
                runOnUiThread {
                    Log.d("cobcalib", "Requesting Calib temp")
                    tc.tbxDataController.sendCommandGetCalibrationData(TBXDataController.CalibrationData.Name)
                }
            }
        }

        if (p.calib_temp.length == 0) {
            Timer("calib1a", false).schedule(525) {
                runOnUiThread {
                    Log.d("cobcalib", "Requesting Calib temp")
                    tc.tbxDataController.sendCommandGetCalibrationData(TBXDataController.CalibrationData.Temp)
                }
            }
        }

        if (p.calib_temp.length == 0) {
            Timer("calib1b", false).schedule(675) {
                runOnUiThread {
                    Log.d("cobcalib", "Requesting Calib temp")
                    tc.tbxDataController.sendCommandGetCalibrationData(TBXDataController.CalibrationData.Temp)
                }
            }
        }

        if (p.calib_temp.length == 0) {
            Timer("calib1b", false).schedule(900) {
                runOnUiThread {
                    Log.d("cobcalib", "Requesting Calib temp")
                    tc.tbxDataController.sendCommandGetCalibrationData(TBXDataController.CalibrationData.Temp)
                }
            }
        }

        if (p.calib_temp.length == 0) {
            Timer("calib1c", false).schedule(1025) {
                runOnUiThread {
                    Log.d("cobcalib", "Requesting Calib temp")
                    tc.tbxDataController.sendCommandGetCalibrationData(TBXDataController.CalibrationData.Temp)
                }
            }
        }

        if (p.calib_date.length == 0) {
            Timer("calib2", false).schedule(1175) {
                runOnUiThread {
                    Log.d("cobcalib", "Requesting Calib date")
                    tc.tbxDataController.sendCommandGetCalibrationData(TBXDataController.CalibrationData.Date)
                }
            }
        }

        if (p.calib_date.length == 0) {
            Timer("calib2a", false).schedule(1325) {
                runOnUiThread {
                    Log.d("cobcalib", "Requesting Calib date")
                    tc.tbxDataController.sendCommandGetCalibrationData(TBXDataController.CalibrationData.Date)
                }
            }
        }

        if (p.calib_date.length == 0) {
            Timer("calib2b", false).schedule(1475) {
                runOnUiThread {
                    Log.d("cobcalib", "Requesting Calib date")
                    tc.tbxDataController.sendCommandGetCalibrationData(TBXDataController.CalibrationData.Date)
                }
            }
        }

        if (p.calib_date.length == 0) {
            Timer("calib2b", false).schedule(1675) {
                runOnUiThread {
                    Log.d("cobcalib", "Requesting Calib date")
                    tc.tbxDataController.sendCommandGetCalibrationData(TBXDataController.CalibrationData.Date)
                }
            }
        }

        if (p.calib_date.length == 0) {
            Timer("calib2b", false).schedule(1450) {
                runOnUiThread {
                    Log.d("cobcalib", "Requesting Calib date")
                    tc.tbxDataController.sendCommandGetCalibrationData(TBXDataController.CalibrationData.Date)
                }
            }
        }

        if (p.calib_date.length == 0) {
            Timer("calib2b", false).schedule(1600) {
                runOnUiThread {
                    Log.d("cobcalib", "Requesting Calib date")
                    tc.tbxDataController.sendCommandGetCalibrationData(TBXDataController.CalibrationData.Date)
                }
            }
        }

        if (p.calib_time.length == 0) {
            Timer("calib3", false).schedule(1725) {
                runOnUiThread {
                    Log.d("cobcalib", "Requesting Calib time")
                    tc.tbxDataController.sendCommandGetCalibrationData(TBXDataController.CalibrationData.Time)
                }
            }
        }

        if (p.calib_time.length == 0) {
            Timer("calib3", false).schedule(1850) {
                runOnUiThread {
                    Log.d("cobcalib", "Requesting Calib time")
                    tc.tbxDataController.sendCommandGetCalibrationData(TBXDataController.CalibrationData.Time)
                }
            }
        }

        if (p.calib_time.length == 0) {
            Timer("calib3", false).schedule(2025) {
                runOnUiThread {
                    Log.d("cobcalib", "Requesting Calib time")
                    tc.tbxDataController.sendCommandGetCalibrationData(TBXDataController.CalibrationData.Time)
                }
            }
        }

        if (p.calib_time.length == 0) {
            Timer("calib3", false).schedule(2200) {
                runOnUiThread {
                    Log.d("cobcalib", "Requesting Calib time")
                    tc.tbxDataController.sendCommandGetCalibrationData(TBXDataController.CalibrationData.Time)
                }
            }
        }

        if (p.calib_p1.length == 0) {
            Timer("calib4", false).schedule(2325) {
                runOnUiThread {
                    Log.d("cobcalib", "Requesting Calib p1")
                    tc.tbxDataController.sendCommandGetCalibrationData(TBXDataController.CalibrationData.Pressure1)
                }
            }
        }

        if (p.calib_p1.length == 0) {
            Timer("calib5", false).schedule(2450) {
                runOnUiThread {
                    Log.d("cobcalib", "Requesting Calib p1")
                    tc.tbxDataController.sendCommandGetCalibrationData(TBXDataController.CalibrationData.Pressure1)
                }
            }
        }

        if (p.calib_p1.length == 0) {
            Timer("calib5", false).schedule(2600) {
                runOnUiThread {
                    Log.d("cobcalib", "Requesting Calib p1")
                    tc.tbxDataController.sendCommandGetCalibrationData(TBXDataController.CalibrationData.Pressure1)
                }
            }
        }

        if (p.calib_p1.length == 0) {
            Timer("calib5", false).schedule(2725) {
                runOnUiThread {
                    Log.d("cobcalib", "Requesting Calib p1")
                    tc.tbxDataController.sendCommandGetCalibrationData(TBXDataController.CalibrationData.Pressure1)
                }
            }
        }

        if (p.calib_p2.length == 0) {
            Timer("calib6", false).schedule(2975) {
                runOnUiThread {
                    Log.d("cobcalib", "Requesting Calib p2")
                    tc.tbxDataController.sendCommandGetCalibrationData(TBXDataController.CalibrationData.Pressure2)
                }
            }
        }

        if (p.calib_p2.length == 0) {
            Timer("calib6", false).schedule(3130) {
                runOnUiThread {
                    Log.d("cobcalib", "Requesting Calib p2")
                    tc.tbxDataController.sendCommandGetCalibrationData(TBXDataController.CalibrationData.Pressure2)
                }
            }
        }

        if (p.calib_p2.length == 0) {
            Timer("calib6", false).schedule(3220) {
                runOnUiThread {
                    Log.d("cobcalib", "Requesting Calib p2")
                    tc.tbxDataController.sendCommandGetCalibrationData(TBXDataController.CalibrationData.Pressure2)
                }
            }
        }

        if (p.calib_p2.length == 0) {
            Timer("calib6", false).schedule(3390) {
                runOnUiThread {
                    Log.d("cobcalib", "Requesting Calib p2")
                    tc.tbxDataController.sendCommandGetCalibrationData(TBXDataController.CalibrationData.Pressure2)
                }
            }
        }

        if (p.calib_p3.length == 0) {
            Timer("calib7", false).schedule(3510) {
                runOnUiThread {
                    Log.d("cobcalib", "Requesting Calib p3")
                    tc.tbxDataController.sendCommandGetCalibrationData(TBXDataController.CalibrationData.Pressure3)
                }
            }
        }

        if (p.calib_p3.length == 0) {
            Timer("calib7", false).schedule(3690) {
                runOnUiThread {
                    Log.d("cobcalib", "Requesting Calib p3")
                    tc.tbxDataController.sendCommandGetCalibrationData(TBXDataController.CalibrationData.Pressure3)
                }
            }
        }

        if (p.calib_p3.length == 0) {
            Timer("calib7", false).schedule(3810) {
                runOnUiThread {
                    Log.d("cobcalib", "Requesting Calib p3")
                    tc.tbxDataController.sendCommandGetCalibrationData(TBXDataController.CalibrationData.Pressure3)
                }
            }
        }

        if (p.calib_p3.length == 0) {
            Timer("calib7", false).schedule(3990) {
                runOnUiThread {
                    Log.d("cobcalib", "Requesting Calib p3")
                    tc.tbxDataController.sendCommandGetCalibrationData(TBXDataController.CalibrationData.Pressure3)
                }
            }
        }

        if (p.calib_p4.length == 0) {
            Timer("calib8", false).schedule(4125) {
                runOnUiThread {
                    Log.d("cobcalib", "Requesting Calib p4")
                    tc.tbxDataController.sendCommandGetCalibrationData(TBXDataController.CalibrationData.Pressure4)
                }
            }
        }

        if (p.calib_p4.length == 0) {
            Timer("calib8", false).schedule(4250) {
                runOnUiThread {
                    Log.d("cobcalib", "Requesting Calib p4")
                    tc.tbxDataController.sendCommandGetCalibrationData(TBXDataController.CalibrationData.Pressure4)
                }
            }
        }

        if (p.calib_p4.length == 0) {
            Timer("calib8", false).schedule(4400) {
                runOnUiThread {
                    Log.d("cobcalib", "Requesting Calib p4")
                    tc.tbxDataController.sendCommandGetCalibrationData(TBXDataController.CalibrationData.Pressure4)
                }
            }
        }

        if (p.calib_p4.length == 0) {
            Timer("calib8", false).schedule(4590) {
                runOnUiThread {
                    Log.d("cobcalib", "Requesting Calib p4")
                    tc.tbxDataController.sendCommandGetCalibrationData(TBXDataController.CalibrationData.Pressure4)
                }
            }
        }

        if (p.calib_p5.length == 0) {
            Timer("calib5", false).schedule(4710) {
                runOnUiThread {
                    Log.d("cobcalib", "Requesting Calib p5")
                    tc.tbxDataController.sendCommandGetCalibrationData(TBXDataController.CalibrationData.Pressure5)
                }
            }
        }

        if (p.calib_p5.length == 0) {
            Timer("calib5", false).schedule(4890) {
                runOnUiThread {
                    Log.d("cobcalib", "Requesting Calib p5")
                    tc.tbxDataController.sendCommandGetCalibrationData(TBXDataController.CalibrationData.Pressure5)
                }
            }
        }

        if (p.calib_p5.length == 0) {
            Timer("calib5", false).schedule(5125) {
                runOnUiThread {
                    Log.d("cobcalib", "Requesting Calib p5")
                    tc.tbxDataController.sendCommandGetCalibrationData(TBXDataController.CalibrationData.Pressure5)
                }
            }
        }

        if (p.calib_p5.length == 0) {
            Timer("calib5", false).schedule(5290) {
                runOnUiThread {
                    Log.d("cobcalib", "Requesting Calib p5")
                    tc.tbxDataController.sendCommandGetCalibrationData(TBXDataController.CalibrationData.Pressure5)
                }
            }
        }

        if (p.calib_p6.length == 0) {
            Timer("calib6", false).schedule(5430) {
                runOnUiThread {
                    Log.d("cobcalib", "Requesting Calib p6")
                    tc.tbxDataController.sendCommandGetCalibrationData(TBXDataController.CalibrationData.Pressure6)
                }
            }
        }

        if (p.calib_p6.length == 0) {
            Timer("calib6", false).schedule(5590) {
                runOnUiThread {
                    Log.d("cobcalib", "Requesting Calib p6")
                    tc.tbxDataController.sendCommandGetCalibrationData(TBXDataController.CalibrationData.Pressure6)
                }
            }
        }

        if (p.calib_p6.length == 0) {
            Timer("calib6", false).schedule(5710) {
                runOnUiThread {
                    Log.d("cobcalib", "Requesting Calib p6")
                    tc.tbxDataController.sendCommandGetCalibrationData(TBXDataController.CalibrationData.Pressure6)
                }
            }
        }

        if (p.calib_p6.length == 0) {
            Timer("calib6", false).schedule(5890) {
                runOnUiThread {
                    Log.d("cobcalib", "Requesting Calib p6")
                    tc.tbxDataController.sendCommandGetCalibrationData(TBXDataController.CalibrationData.Pressure6)
                }
            }
        }
    }
}

fun TestingActivity.loadTestResultsView()
{
    //TODO: Not implemented
}

fun TestingActivity.resetPETest()
{
    // Resetting the timers

    timer.cancel()
    timer = Timer()
    liveLogTimer = Timer()
    cancelAirPercentageTimer()
    AppGlobals.instance.activeProcess.pt_reading3_time = ""     // This stops the pe timer from resuming

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
    //formatForReadyToPressurise()
    formatActionPanelForDefault()

    timer.cancel()
    timer = Timer()
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
    Log.d("ditest", "cancelPETimer()")
    turnOutputPumpOff()
    timer.cancel()
    timer = Timer()
    formatActionPanelForCalculate()
    loadData()
    formatActionPanelForCalculate()
}

















