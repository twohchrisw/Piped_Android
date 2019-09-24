package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.util.Log
import android.view.View
import junit.framework.Test
import org.jetbrains.anko.runOnUiThread
import java.lang.Exception
import java.util.*
import kotlin.concurrent.schedule

fun TestingActivity.setupTibiis()
{
    //TODO: Not fully implemented
    AppGlobals.instance.tibiisController.delegate = this
    AppGlobals.instance.tibiisController.testingContext = testingSession.testingContext
    AppGlobals.instance.tibiisController.appContext = this
    AppGlobals.instance.tibiisController.tbxDataController.delegate = this
    tibiisSession.testingContext = testingSession.testingContext

    if (AppGlobals.instance.tibiisController.connectStatus == TibiisController.ConnectionStatus.connected)
    {
        formatTibiisForConnected()
    }
    else
    {
        formatTibiisForNotConnected()
    }
}

fun TestingActivity.formatTibiisForConnected()
{
    Log.d("Cobalt", "formatTibiisForConnected()")
    runOnUiThread {
        tvConnectStatus.text = "Tibiis Connected"
        btnConnect.setText("Disconnect")
    }

    // Get the calibration details
    runOnUiThread {
        formatOptionsMenuForContext(true)
    }

    saveCalibrationDetails()

    // Set the log interval
    AppGlobals.instance.tibiisController.tbxDataController.sendCommandSetLogInterval(1)

    // Start the live log request loop after a 100ms delay
    Timer("LiveLogRequestLoop", false).schedule(100) {
        startTibiisLiveLogRequestLoop()

    }

    Timer("SaveCalib1", false).schedule(300) {
        saveCalibrationDetails()
    }

    Timer("SveCalib2", false).schedule(700) {
        saveCalibrationDetails()
    }

    //saveCalibrationDetails()
}

fun TestingActivity.reconnectTibiis()
{
    formatTibiisForConnected()
    AppGlobals.instance.tibiisController.commandSendAck()
}

fun TestingActivity.formatTibiisForNotConnected()
{
    Log.d("Cobalt", "formatTibiisForNotConnected")
    tvConnectStatus.text = "Tibiis Not Connected"
    btnConnect.setText("Connect")
    tibiisSession.lastReading = null
    updatePressureGuageForZero()
    AppGlobals.instance.tibiisController.hasSendDateSync = false
    hasCheckedIntegrity = false
    isCheckingIntegrity = false
    isDownloadingPreviousData = false

}

fun TestingActivity.formatTibiisForConnecting()
{
    tvConnectStatus.text = "Finding Tibiis . . ."
    btnConnect.setText("Disconnect")
}

fun TestingActivity.startTibiisLiveLogRequestLoop()
{
    Log.d("Cobalt", "Starting Live Log Request Loop")
    liveLogTimer.scheduleAtFixedRate(TestingActivity.LiveLogTimerTask(this), 0, 1000)
}

fun TestingActivity.tibiisStartPressurising()
{

    val tc = AppGlobals.instance.tibiisController
    if (tc.connectStatus == TibiisController.ConnectionStatus.connected)
    {
        Log.d("Cobalt", "[Start Pressurising]")

        tibiisSession.resetMissedReadingFlags()
        testingSession.loggingMode = TestingSessionData.LoggingMode.pressurising


        runOnUiThread {
            tc.tbxDataController.sendCommandTimeSync()

        }

        Timer("startTest1", false).schedule(100) {
            runOnUiThread {
                tc.tbxDataController.sendCommandTimeSync()

            }
        }

        Timer("startTest2", false).schedule(200) {
            runOnUiThread {
                tc.tbxDataController.sendCommandStartTest()

            }
        }

        Timer("startTest3", false).schedule(300) {
            runOnUiThread {
                tc.tbxDataController.sendCommandStartTest()

            }
        }

        Timer("outputControlOn4", false).schedule(400) {
            runOnUiThread {
                tc.tbxDataController.sendCommandOutputControl(true)

            }
        }

        Timer("outputControlOn5", false).schedule(500) {
            runOnUiThread {
                tc.tbxDataController.sendCommandOutputControl(true)

            }
        }

        Timer("screenOn", false).schedule(600) {
            runOnUiThread {
                tc.tbxDataController.sendCommandSetLCDOnTime(20)

            }
        }

        Timer("screenOn", false).schedule(700) {
            runOnUiThread {
                tc.tbxDataController.sendCommandSetLCDOnTime(20)

            }
        }

        //TODO: We can't get the tibiis serial number
        /*
        catchAll("Get mDevice name") {
            if (tc.mDevice != null)
            {
                AppGlobals.instance.activeProcess.pt_pe_logger_details = tc.mDevice!!.name
                Log.d("Cobalt", "Device name is ${tc.mDevice!!.name}")
            }
            else
            {
                Log.d("Cobalt", "Device is null - no serial number")
            }
        }
        */

        /*
        runOnUiThread {
            AppGlobals.instance.activeProcess.save(this)
            loadData()
        }
*/
        loadData()
    }
}

fun TestingActivity.tibiisStopPressurising()
{
    try {
        runOnUiThread {
            AppGlobals.instance.tibiisController.commandStopLogger()
        }

        Timer("startTest1", false).schedule(100) {
            AppGlobals.instance.tibiisController.commandStopLogger()
        }

        Timer("startTest1", false).schedule(200) {
            AppGlobals.instance.tibiisController.commandStopLogger()
        }

        Timer("startTest1", false).schedule(300) {
            AppGlobals.instance.tibiisController.commandStopLogger()
        }

    }
    catch (e: Exception)
    {

    }
}

fun TestingActivity.tibiisStartLogging()
{
    val tc = AppGlobals.instance.tibiisController
    if (tc.connectStatus == TibiisController.ConnectionStatus.connected) {
        testingSession.isPressurisingWithTibiis = false
        testingSession.isLoggingWithTibiis = false
        testingSession.setStartLoggingTimeNull()
        testingSession.setLastLoggingTimeNull()
        testingSession.loggingMode = TestingSessionData.LoggingMode.logging
        shouldTurnScreenOffWithNextLog = true
    }
}



