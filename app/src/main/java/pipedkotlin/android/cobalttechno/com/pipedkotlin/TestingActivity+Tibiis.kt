package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.util.Log
import junit.framework.Test
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
    runOnUiThread {
        tvConnectStatus.text = "Tibiis Connected"
        btnConnect.setText("Disconnect")
    }

    // Get the calibration details
    saveCalibrationDetails()

    // Set the log interval
    AppGlobals.instance.tibiisController.tbxDataController.sendCommandSetLogInterval(1)

    // Start the live log request loop after a 100ms delay
    Timer("LiveLogRequestLoop", false).schedule(100) {
    startTibiisLiveLogRequestLoop()
}
}

fun TestingActivity.reconnectTibiis()
{
    formatTibiisForConnected()
    AppGlobals.instance.tibiisController.commandSendAck()
}

fun TestingActivity.formatTibiisForNotConnected()
{
    tvConnectStatus.text = "Tibiis Not Connected"
    btnConnect.setText("Connect")
    //TODO: battery sync and so on as per TaskTesting+Tibiis#67
    //TODO: Much more to go here, including formatting the action panel
    AppGlobals.instance.tibiisController.hasSendDateSync = false
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

fun TestingActivity.tibiisStopPressurising()
{
    AppGlobals.instance.tibiisController.commandStopLogger()
}



