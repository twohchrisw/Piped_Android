package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.util.Log
import java.util.*
import kotlin.concurrent.schedule

fun TestingActivity.updatePressurisingDataFromTibiisSession()
{
    val p = AppGlobals.instance.activeProcess

    if (testingSession.getStartLoggingTime() != null && testingSession.getLastLoggingTime() != null)
    {
        val firstLogDate = testingSession.getStartLoggingTime()!!
        val lastLogDate = testingSession.getLastLoggingTime()!!

        p.pt_pressurising_start = DateHelper.dateToDBString(firstLogDate)
        p.pt_pressurising_finish = DateHelper.dateToDBString(lastLogDate)

        if (tibiisSession.getHighestLogData() != null)
        {
            p.pt_system_test_pressure = tibiisSession.getHighestLogData()!!.logReading.pressure.toDouble() / 1000.0
        }
        else
        {
            p.pt_system_test_pressure = tibiisSession.getMaxPressurisingValue(p.columnId)
        }

        val startP = tibiisSession.startPressureReading!!
        val startPressure = startP.pressure.toDouble() / 1000.0
        p.pt_start_pressure = startPressure
        p.save(this)
    }
    else
    {
        Log.d("Cobalt", "Error getting start and end log dates")
    }
}

fun TestingActivity.turnOutputPumpOff()
{
    val tc = AppGlobals.instance.tibiisController
    if (tc.connectStatus != TibiisController.ConnectionStatus.connected)
    {
        return
    }

    Timer("outputControlOn4", false).schedule(100) {
        runOnUiThread {
            tc.tbxDataController.sendCommandOutputControl(false)

        }
    }

    Timer("outputControlOn4", false).schedule(200) {
        runOnUiThread {
            tc.tbxDataController.sendCommandOutputControl(false)

        }
    }

    Timer("outputControlOn4", false).schedule(300) {
        runOnUiThread {
            tc.tbxDataController.sendCommandOutputControl(false)

        }
    }

    Timer("outputControlOn4", false).schedule(400) {
        runOnUiThread {
            tc.tbxDataController.sendCommandOutputControl(false)

        }
    }
}

fun TestingActivity.turnOutputPumpOn()
{
    val tc = AppGlobals.instance.tibiisController
    if (tc.connectStatus != TibiisController.ConnectionStatus.connected)
    {
        return
    }

    Timer("outputControlOn4", false).schedule(100) {
        runOnUiThread {
            tc.tbxDataController.sendCommandOutputControl(true)

        }
    }

    Timer("outputControlOn4", false).schedule(200) {
        runOnUiThread {
            tc.tbxDataController.sendCommandOutputControl(true)

        }
    }

    Timer("outputControlOn4", false).schedule(300) {
        runOnUiThread {
            tc.tbxDataController.sendCommandOutputControl(true)

        }
    }

    Timer("outputControlOn4", false).schedule(400) {
        runOnUiThread {
            tc.tbxDataController.sendCommandOutputControl(true)

        }
    }
}

fun TestingActivity.setPETestFinishedPressurisingAndBeginLogging()
{

    AppGlobals.instance.activeProcess.calculatePEReadingTimes(this)
    loadData()

    if (AppGlobals.instance.tibiisController.connectStatus == TibiisController.ConnectionStatus.connected)
    {
        turnOutputPumpOff()
    }

    tibiisStartLogging()

    // Assign the timer function
    timer.cancel()
    timer = Timer()
    val r1 = DateHelper.dbStringToDate(AppGlobals.instance.activeProcess.pt_reading1_time, Date())
    val r2 = DateHelper.dbStringToDate(AppGlobals.instance.activeProcess.pt_reading2_time, Date())
    val r3 = DateHelper.dbStringToDate(AppGlobals.instance.activeProcess.pt_reading3_time, Date())
    timer.scheduleAtFixedRate(TestingActivity.PETimerTask(this, r1, r2, r3), 0, 1000)
}