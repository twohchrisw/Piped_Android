package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.util.Log
import junit.framework.Test
import java.util.*


fun TestingActivity.startDITest()
{
    //TODO: Not Implemented
}

fun TestingActivity.calculateDIButtonPressed()
{
    //TODO: Not Implemented
}

fun TestingActivity.loadCheckDI()
{
    //TODO: Not implemented
}

fun TestingActivity.endDIPressurisation()
{
    //TODO: Not Implemented
}

fun TestingActivity.saveReading15(pr: LogReading)
{
    val press = pr.pressure / 1000.0
    AppGlobals.instance.activeProcess.pt_di_r15_value = press
    tibiisSession.setLogDIReading15(pr)
    loadData()
    saveCalibrationDetails()
}

fun TestingActivity.saveReading60(pr: LogReading)
{
    val press = pr.pressure / 1000.0
    AppGlobals.instance.activeProcess.pt_di_r60_value = press
    tibiisSession.setLogDIReading60(pr)
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