package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.util.Log
import java.util.*

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

fun TestingActivity.startPressurisingButtonPressed()
{
    val p = AppGlobals.instance.activeProcess
    p.initialiseForPETest(this)
    p.pt_lat = lastLat
    p.pt_long = lastLng
    testingSession.timerStage = 0
    formatActionPanelForPressurising()
    //TODO: tibiisSession.startPressureReading = tibiisSession.lastReading

    p.save(this)

    if (AppGlobals.instance.tibiisController.connectStatus == TibiisController.ConnectionStatus.connected)
    {
        //TODO: tibiisStartPressurising
    }
    else
    {
        beginPressurisation()
    }
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

            if (testingSession.isPressurisingWithTibiis) //TODO: && tibiisSession.countOfReadingType(readingType: .pressurising) > 0
            {
                //TODO: updatePressurisingDataFromTibiisSession
            }

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

fun TestingActivity.setPETestFinishedPressurisingAndBeginLogging()
{

    AppGlobals.instance.activeProcess.calculatePEReadingTimes(this)
    loadData()

    if (AppGlobals.instance.tibiisController.connectStatus == TibiisController.ConnectionStatus.connected)
    {
        //TODO: tibiisStartLogging
    }

    // Assign the timer function
    timer.cancel()
    timer = Timer()
    val r1 = DateHelper.dbStringToDate(AppGlobals.instance.activeProcess.pt_reading1_time, Date())
    val r2 = DateHelper.dbStringToDate(AppGlobals.instance.activeProcess.pt_reading2_time, Date())
    val r3 = DateHelper.dbStringToDate(AppGlobals.instance.activeProcess.pt_reading3_time, Date())
    timer.scheduleAtFixedRate(TestingActivity.PETimerTask(this, r1, r2, r3), 0, 1000)
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


fun TestingActivity.calculatePEButtonPressed()
{
    //TODO: Tibiis check for downloading previous readings

    val p = AppGlobals.instance.activeProcess


    if (DateHelper.dateIsValid(p.pt_reading1_time) && DateHelper.dateIsValid(p.pt_reading2_time) && DateHelper.dateIsValid(p.pt_reading3_time))
    {
        //TODO: Tibiis Session.hasCalculated = true
        calculatePETestResults()
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
        alert.dialogForOKAlertNoAction("Calculate", "You need to pressurise and enter data before calculating!")
        p.needs_server_sync = 1
    }

    p.save(this)
}

fun TestingActivity.calculatePETestResults()
{
    calcManager.calculatePETestResults()
    AppGlobals.instance.activeProcess.save(this)
    formatActionPanelForCalculate()
    //TODO: tibiisStopPressurising
    //TODO: disconnect Tibiis
}

fun TestingActivity.loadCheckPE()
{
    //TODO: Tibiis Stuff

    val p = AppGlobals.instance.activeProcess

    if (!DateHelper.dateIsValid(p.pt_pressurising_start) && !DateHelper.dateIsValid(p.pt_pressurising_finish))
    {
        // No test in progress
        testingSession.loggingMode = TestingSessionData.LoggingMode.waiting
        testingSession.isPressurisingWithTibiis = true
        testingSession.isLoggingWithTibiis = true
        //TODO: Reset tibiis controller
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
    //TODO: Add missed readings from tibiis

    if (p.pe_test_has_calculated == 1)
    {
        testingSession.hasCalculatedPETest = true
        //TODO: TibiisSession.hasCalculated = true
        formatActionPanelForCalculate()
        loadData()
        return
    }
    else
    {
        testingSession.hasCalculatedPETest = false
        //TODO: TibiisSession.hasCalculated = false
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

}

fun TestingActivity.saveCalibrationDetails()
{
    //TODO: Not Implemented
    Log.d("Cobalt", "saveCalibrationDetails not implemented")
}



