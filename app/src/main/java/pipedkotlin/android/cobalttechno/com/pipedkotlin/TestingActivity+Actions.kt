package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.util.Log
import java.util.*
import kotlin.concurrent.schedule

fun TestingActivity.reloadTable()
{
    recyclerView.adapter?.notifyDataSetChanged()
}

fun TestingActivity.formatOptionsMenuForContext(connected: Boolean)
{
    // Enablig this routine causes the recycler view to disappear on refresh???
    return

    // Hide/Show menu options depending if we're connected
    if (connected)
    {
        Log.d("Cobalt", "Enabling Tibiis Menu Commands")
        menuDisableAutoPump.isEnabled = true
        menuEnableAutoPump.isEnabled = true
        menuZeroTibiis.isEnabled = true
        menuEnableConditioning.isEnabled = testingSession.testingContext == TestingSessionData.TestingContext.di
    }
    else
    {
        Log.d("Cobalt", "Disabling Tibiis Menu Commands")
        menuDisableAutoPump.isEnabled = false
        menuEnableAutoPump.isEnabled = false
        menuZeroTibiis.isEnabled = false
        menuEnableConditioning.isEnabled = false
    }
}

// Select an item from the optoins menu
fun TestingActivity.didPressActionButton(menuId: Int)
{
    when(menuId)
    {
        R.id.mnuCalc -> {
            if (testingSession.testingContext == TestingSessionData.TestingContext.pe)
            {
                appGlobals.calculatorTitle = "PE Test"
            }
            else
            {
                appGlobals.calculatorTitle = "Metallic Test"
            }

            val pipeCalculatorIntent = Intent(this, PipeCalculatorActivity::class.java)
            startActivity(pipeCalculatorIntent)
        }

        R.id.mnuAddNote -> {
            /*
            runOnUiThread {
                //appGlobals.tibiisController.tbxDataController.sendCommandFetchOldLogs(66626, 16)
                appGlobals.tibiisController.tbxDataController.sendCommandFetchOldLogs(66626, 16)
            }

             */

            //appGlobals.activeProcess.clearTibiisUploadFlag(this)

            setNotes()
        }

        R.id.mnuZeroTibiisSensors -> {

            if (appGlobals.tibiisController.connectStatus != TibiisController.ConnectionStatus.connected)
            {
                val alert = AlertHelper(this)
                alert.dialogForOKAlertNoAction("Tibiis Not Connected", "")
                return
            }

            appGlobals.tibiisController.commandZeroPressureSensors()

            val alert = AlertHelper(this)
            runOnUiThread {
                alert.dialogForOKAlertNoAction("Tibiis Sensors Zeroed", "")
            }
        }

        R.id.mnuAbortTest -> {
            val alert = AlertHelper(this)
            runOnUiThread {
                alert.dialogForOKAlert("Abort Test", "Are you sure you want to abort this test?") {
                    if (testingSession.testingContext == TestingSessionData.TestingContext.pe)
                    {
                        abortPETest()
                    }

                    if (testingSession.testingContext == TestingSessionData.TestingContext.di)
                    {
                        abortDITest()
                    }
                }
            }
        }

        R.id.mnuNewTest -> {
            val alert = AlertHelper(this)
            runOnUiThread {
                alert.dialogForOKAlert("Abort Test", "Are you sure you want to abort this test?") {
                    if (testingSession.testingContext == TestingSessionData.TestingContext.pe)
                    {
                        abortPETest()
                    }

                    if (testingSession.testingContext == TestingSessionData.TestingContext.di)
                    {
                        abortDITest()
                    }
                }
            }
        }

        R.id.mnuEnableAutoPump -> {
            if (appGlobals.tibiisController.connectStatus != TibiisController.ConnectionStatus.connected)
            {
                val alert = AlertHelper(this)
                alert.dialogForOKAlertNoAction("Tibiis Not Connected", "")
                return
            }

            //TODO: Needs completing
        }

        R.id.mnuEnableAutoPumpConditioning -> {
            if (appGlobals.tibiisController.connectStatus != TibiisController.ConnectionStatus.connected)
            {
                val alert = AlertHelper(this)
                alert.dialogForOKAlertNoAction("Tibiis Not Connected", "")
                return
            }

            //TODO: Needs completing
        }

        R.id.mnuDisableAutoPump -> {
            if (appGlobals.tibiisController.connectStatus != TibiisController.ConnectionStatus.connected)
            {
                val alert = AlertHelper(this)
                alert.dialogForOKAlertNoAction("Tibiis Not Connected", "")
                return
            }

            //TODO: Needs completing
        }


    }
}

fun TestingActivity.stopExistingTest()
{
    runOnUiThread {
        tibiisStopPressurising()
    }
}

fun TestingActivity.connectButtonTapped()
{

    // Can we use bluetooth
    if (!appGlobals.tibiisController.supportsBluetooth())
    {
        val alert = AlertHelper(this)
        alert.dialogForOKAlertNoAction("Bluetooth Unavailable", "BLE is not supported on this device")
        return
    }

    // Is bluetooth enabled
    if (!appGlobals.tibiisController.bluetoothIsEnabled())
    {
        val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBluetoothIntent, TestingActivity.ActivityRequestCodes.enableBluetooth.value)
        return
    }

    val t = appGlobals.tibiisController


    // Disconnect
    if (t.connectStatus == TibiisController.ConnectionStatus.connected || t.connectStatus == TibiisController.ConnectionStatus.connecting)
    {
        //tibiisStopPressurising()
        //timer.cancel()
        //timer = Timer()
        //liveLogTimer.cancel()
        //liveLogTimer = Timer()


        Timer("stopTest", false).schedule(200) {
            if (appGlobals.tibiisController.connectStatus == TibiisController.ConnectionStatus.connected) {
                appGlobals.tibiisController.disconnectTibiis()
            }
        }

        //AppGlobals.instance.activeProcess.needs_server_sync = 1
        appGlobals.activeProcess.save(this)

        //timer.cancel()
        //timer = Timer()
        //liveLogTimer.cancel()
        //liveLogTimer = Timer()

        return
    }

    // Connect
    if (t.connectStatus == TibiisController.ConnectionStatus.notConnected)
    {
        formatTibiisForConnecting()

        runOnUiThread {
            appGlobals.tibiisController.connectToTibiis()
        }

        return
    }
}

fun TestingActivity.actionButtonTapped()
{
    val buttonTitle = btnAction.text.toString()
    if (testingSession.testingContext == TestingSessionData.TestingContext.pe)
    {
        when (buttonTitle)
        {
            BUTTON_TEXT_START_PRESS -> startPressurisingButtonPressed()
            BUTTON_TEXT_STOP_PRESS -> stopPressurisingButtonPressed()
            BUTTON_TEXT_CALCULATE -> calculatePEButtonPressed()
        }
    }

    if (testingSession.testingContext == TestingSessionData.TestingContext.di)
    {
        when (buttonTitle)
        {
            BUTTON_TEXT_START_TEST -> startDITest()
            BUTTON_TEXT_CALCULATE -> calculateDIButtonPressed()
        }
    }
}

fun TestingActivity.abortPETest()
{
    appGlobals.activeProcess.pe_test_aborted = 1
    tibiisStopPressurising()
    resetPETest()

    /*
    Timer("stopTest", false).schedule(100) {
        //if (AppGlobals.instance.tibiisController.connectStatus == TibiisController.ConnectionStatus.connected) {
        appGlobals.tibiisController.disconnectTibiis()
        //}
    }
    */


    Timer("stopTest1", false).schedule(500) {
        //if (AppGlobals.instance.tibiisController.connectStatus == TibiisController.ConnectionStatus.connected) {
        runOnUiThread {
            appGlobals.tibiisController.disconnectTibiis()
        }
    }

    Timer("stopTest2", false).schedule(700) {
        //if (AppGlobals.instance.tibiisController.connectStatus == TibiisController.ConnectionStatus.connected) {
        runOnUiThread {
            appGlobals.tibiisController.disconnectTibiis()
        }
        //}
    }

    Timer("stopTes3", false).schedule(1000) {
        //if (AppGlobals.instance.tibiisController.connectStatus == TibiisController.ConnectionStatus.connected) {
        runOnUiThread {
            appGlobals.tibiisController.disconnectTibiis()
        }
        //}
    }

    appGlobals.activeProcess.needs_server_sync = 1
    appGlobals.activeProcess.save(this)
}

fun TestingActivity.abortDITest()
{
    runOnUiThread {
        btnAction.isEnabled = true
    }

    tibiisStopPressurising()
    resetDITest()

    /*
    Timer("stopTest", false).schedule(100) {
        //if (AppGlobals.instance.tibiisController.connectStatus == TibiisController.ConnectionStatus.connected) {
        appGlobals.tibiisController.disconnectTibiis()
        //}
    }
    */


    Timer("stopTest1", false).schedule(500) {
        //if (AppGlobals.instance.tibiisController.connectStatus == TibiisController.ConnectionStatus.connected) {
        runOnUiThread {
            appGlobals.tibiisController.disconnectTibiis()
        }
        //}
    }

    Timer("stopTest2", false).schedule(700) {
        //if (AppGlobals.instance.tibiisController.connectStatus == TibiisController.ConnectionStatus.connected) {
        runOnUiThread {
            appGlobals.tibiisController.disconnectTibiis()
        }
        //}
    }

    Timer("stopTes3", false).schedule(1000) {
        //if (AppGlobals.instance.tibiisController.connectStatus == TibiisController.ConnectionStatus.connected) {
        runOnUiThread {
            appGlobals.tibiisController.disconnectTibiis()
        }
        //}
    }

    appGlobals.activeProcess.needs_server_sync = 1
    appGlobals.activeProcess.save(this)
}





