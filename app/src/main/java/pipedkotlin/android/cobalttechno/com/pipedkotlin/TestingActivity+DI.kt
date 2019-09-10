package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.app.Activity
import android.util.Log
import junit.framework.Test
import java.util.*
import kotlin.concurrent.schedule


fun TestingActivity.startDITest(pumpEnabled: Boolean = true)
{
    if (AppGlobals.instance.tibiisController.connectStatus == TibiisController.ConnectionStatus.connected) {
        Log.d("ditest", "Begin DI Tibiis Logging")
        testingSession.loggingMode = TestingSessionData.LoggingMode.logging
        testingSession.isPressurisingDI = false
        tibiisSession.lcdOnTimeSet = true
        //TODO: Need to get serial number here

        if (testingSession.isAutoPumpAutomaticStopDIEnabled)
        {
            beginDIPressurisation()
            return
        }

        if (pumpEnabled)
        {
            turnOutputPumpOn()
        }
        else
        {
            turnOutputPumpOff()
        }

        beginStartDITest()
        tibiisStartLogging()
        tibiisStartPressurising()
    }
    else
    {
        beginStartDITest()
    }
}

fun TestingActivity.beginStartDITest()
{
    val now = Date()
    val p = AppGlobals.instance.activeProcess
    AppGlobals.instance.activeProcess.pt_di_r15_value = 0.0
    AppGlobals.instance.activeProcess.pt_di_r60_value = 0.0

    if (AppGlobals.instance.DI_TEST_MODE)
    {
        val r15Time = now.time + (10 * 1000)
        val r60Time = now.time + (30 * 1000)
        p.pt_di_r15_time = DateHelper.millisToDBString(r15Time)
        p.pt_di_r60_time = DateHelper.millisToDBString(r60Time)
    }
    else
    {
        val r15Time = now.time + (60 * 15 * 1000)
        val r60Time = now.time + (60 * 60 * 1000)
        p.pt_di_r15_time = DateHelper.millisToDBString(r15Time)
        p.pt_di_r60_time = DateHelper.millisToDBString(r60Time)
    }

    val r15Date = DateHelper.dbDateStringFormattedWithSeconds(p.pt_di_r15_time)
    val r60Date = DateHelper.dbDateStringFormattedWithSeconds(p.pt_di_r60_time)

    Log.d("ditest", "R15: $r15Date R60: $r60Date")

    p.pt_di_pressurising_started = DateHelper.dateToDBString(now)
    testingSession.timerStage = 0

    if (!testingSession.getIsDITestConditioning())
    {
        if (tibiisSession.lastReading != null)
        {
            val press = tibiisSession.lastReading!!.pressure / 1000
            p.pt_di_stp = press.toDouble()
            p.pt_di_start_pressure = press.toDouble()
        }
    }

    p.di_lat = AppGlobals.instance.lastLat
    p.di_long = AppGlobals.instance.lastLng
    p.needs_server_sync = 1
    reloadTable()
    p.save(this)

    timer.cancel()
    timer = Timer()
    val r15 = DateHelper.dbStringToDate(p.pt_di_r15_time, Date())
    val r60 = DateHelper.dbStringToDate(p.pt_di_r60_time, Date())
    val r3 = DateHelper.dbStringToDate(AppGlobals.instance.activeProcess.pt_reading3_time, Date())

    //timer.scheduleAtFixedRate(TestingActivity.PETimerTask(this, r1, r2, r3), 0, 1000)
    timer.scheduleAtFixedRate(TestingActivity.DITimerTask(this, r15, r60), 0, 1000)
}

fun TestingActivity.calculateDIButtonPressed()
{
    val p = AppGlobals.instance.activeProcess

    /*
    // This isn't reliable, came on when not downloading previous readings
    if (downloadingPreviousReadings())
    {
        val alert = AlertHelper(this)
        alert.dialogForOKAlertNoAction("Downloading Data", "Please wait until all previous logs have been downloaded before using Calculate")
        return
    }
     */

    if (DateHelper.dateIsValid(p.pt_di_r60_time))
    {
        val r60Time = DateHelper.dbStringToDate(p.pt_di_r60_time, Date())
        val r15Time = DateHelper.dbStringToDate(p.pt_di_r15_time, Date())

        p.di_test_has_calculated = 1

        val now = Date()
        if (now.time < r60Time.time)
        {
            val alert = AlertHelper(this)
            alert.dialogForOKAlertNoAction("Calculate", "Please wait for the reading time to complete before calculating.")
        }
        else
        {
            var alertTitle = ""
            var alertMessage = ""

            if (p.pt_di_r15_value > 0.0 && p.pt_di_r60_value > 0.0)
            {
                testingSession.hasCalculatedPETest = true
                tibiisSession.hasCalculated = true
                formatActionPanelForCalculate()
                saveCalibrationDetails()
                tibiisStopPressurising()

                Timer("stopTest", false).schedule(1000) {
                    if (AppGlobals.instance.tibiisController.connectStatus == TibiisController.ConnectionStatus.connected) {
                        AppGlobals.instance.tibiisController.disconnectTibiis()
                    }
                }

                var lossValue = AppGlobals.instance.DI_TESTING_VALUE
                if (p.di_is_zero_loss == 1)
                {
                    lossValue = AppGlobals.instance.DI_TESTING_ZERO_LOSS_VALUE
                }

                if (p.getDIR60CalcResult() < lossValue)
                {
                    testingSession.timerStage = 0
                    alertTitle = "Test Passed!"
                    alertMessage = "Test passed with a 60m calc result of ${p.getDIR60CalcResult().formatForDecPlaces(4)}"
                }
                else
                {
                    testingSession.timerStage = 0
                    alertTitle = "Test Failed!"
                    alertMessage = "Test failed with a 60m calc result of ${p.getDIR60CalcResult().formatForDecPlaces(4)}"

                }

                val alert = AlertHelper(this)
                alert.dialogForOKAlertNoAction(alertTitle, alertMessage)
            }
            else
            {
                val alert = AlertHelper(this)
                alert.dialogForOKAlertNoAction("Calculate", "Please enter the 15 and 60 minute values before calculating.")
            }
        }
    }

    p.needs_server_sync = 1
    p.save(this)
}

fun TestingActivity.loadCheckDI()
{
    val p = AppGlobals.instance.activeProcess

    if (tibiisSession.getLogNumberForStartDI() < 1)
    {
        tibiisSession.setLogNumberForStartDI(p.tibsessDILogNumberForStart)
    }

    if (tibiisSession.getLogNumberForR15() < 1)
    {
        tibiisSession.setLogNumberForR15(p.tibsessLogR15)
    }

    if (tibiisSession.getLogNumberForR60() < 1)
    {
        tibiisSession.setLogNumberForR60(p.tibsessLogR60)
    }

    if (testingSession.getStartLoggingTime() == null)
    {
        val d = DateHelper.dbStringToDateOrNull(p.testSessDIStartLoggingTime)
        if (d != null)
        {
            testingSession.setStartLoggingTime(d!!)
        }
    }

    if (testingSession.getLastLoggingTime() == null)
    {
        val d = DateHelper.dbStringToDateOrNull(p.testSessDILastLoggingTime)
        if (d != null)
        {
            testingSession.setLastLoggingTime(d!!)
        }
    }

    if (testingSession.getFirstLogReading() == null)
    {
        val d = DateHelper.dbStringToDateOrNull(p.testSessDIFirstLogReadingDate)
        if (d != null)
        {
            testingSession.setFirstLogReading(d!!)
        }
    }

    if (tibiisSession.getLogNumberForR15() > 0)
    {
        testingSession.loggingMode = TestingSessionData.LoggingMode.logging
        testingSession.isLoggingWithTibiis = true
        testingSession.isPressurisingWithTibiis = false
        testingSession.isAmbientLoggingWithTibiis = false
    }
    else
    {
        testingSession.loggingMode = TestingSessionData.LoggingMode.waiting
        testingSession.isLoggingWithTibiis = false
        testingSession.isPressurisingWithTibiis = false
        testingSession.isAmbientLoggingWithTibiis = false
    }

    if (p.di_test_has_calculated == 1)
    {
        testingSession.hasCalculatedPETest = true
        tibiisSession.hasCalculated = true
    }
    else
    {
        testingSession.hasCalculatedPETest = false
        tibiisSession.hasCalculated = false
    }

}



fun TestingActivity.saveReading15(pr: LogReading)
{
    val press = pr.pressure / 1000.0
    AppGlobals.instance.activeProcess.pt_di_r15_value = press
    Log.d("ditest", "Saving reading 15 as $press")
    AppGlobals.instance.activeProcess.save(this)
    tibiisSession.setLogDIReading15(pr)
    loadData()
    saveCalibrationDetails()
}

fun TestingActivity.saveReading60(pr: LogReading)
{
    val press = pr.pressure / 1000.0
    AppGlobals.instance.activeProcess.pt_di_r60_value = press
    tibiisSession.setLogDIReading60(pr)
    Log.d("ditest", "Saving reading 60 as $press")
    AppGlobals.instance.activeProcess.save(this)
    loadData()
    saveCalibrationDetails()
}

fun TestingActivity.resetDITest()
{
    testingSession.timerStage = 0
    isPressurisingDI = false
    archiveDITest()
    clearDIData()
    reloadTable()
    formatForViewWillAppear()
    AppGlobals.instance.tibiisController.resetController()
    testingSession.resetTestingSession()
    tibiisSession = TibiisSessionData()
    tibiisSession.testingContext = TestingSessionData.TestingContext.di
    calcManager = TestingCalcs(testingSession.testingContext, AppGlobals.instance.activeProcess)
    formatForStartTest()
    liveLogTimer = Timer()
}

fun TestingActivity.archiveDITest()
{
    //TODO: Needs completing
}

fun TestingActivity.clearDIData()
{
    val p = AppGlobals.instance.activeProcess
    p.pt_di_start_pressure = 0.0
    p.pt_di_stp = 0.0
    p.pt_di_r15_value = 0.0
    p.pt_di_r60_value = 0.0
    p.pt_di_r15_time = ""
    p.pt_di_r60_time = ""
    p.di_lat = -1000.0
    p.di_long = -1000.0
    p.pt_di_notes = ""
    p.pt_di_pump_size = ""
    p.pt_di_pipe_diameter = 0
    p.pt_di_logger_details = ""
    p.di_test_has_calculated = 0
    p.di_is_zero_loss = -1

    AppGlobals.instance.activeProcess.clearDIData(this)
    formatForStartTest()
}

fun TestingActivity.beginDIPressurisation()
{
    //TODO: Needs completing
}

fun TestingActivity.endDIPressurisation()
{
    //TODO: Not Implemented
}

fun TestingActivity.prepareForDIConditioningPass(lossValue: Double)
{
    //TODO: Needs completing
}

fun TestingActivity.initialiseConditioningData()
{
    //TODO: Needs completing
}

