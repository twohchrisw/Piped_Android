package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.util.Log
import android.view.MenuItem
import junit.framework.Test
import org.jetbrains.anko.runOnUiThread
import org.jetbrains.anko.startActivityForResult
import java.util.*
import kotlin.concurrent.schedule

fun TestingActivity.reloadTable()
{
    recyclerView.adapter.notifyDataSetChanged()
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
        R.id.mnuAddNote -> {
            setNotes()
        }

        R.id.mnuZeroTibiisSensors -> {
            AppGlobals.instance.tibiisController.commandZeroPressureSensors()

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
            //TODO: Needs completing
        }

        R.id.mnuEnableAutoPumpConditioning -> {
            //TODO: Needs completing
        }

        R.id.mnuDisableAutoPump -> {
            //TODO: Needs completing
        }

        R.id.mnuLoadData -> {
            loadData()
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
    if (!AppGlobals.instance.tibiisController.supportsBluetooth())
    {
        val alert = AlertHelper(this)
        alert.dialogForOKAlertNoAction("Bluetooth Unavailable", "BLE is not supported on this device")
        return
    }

    // Is bluetooth enabled
    if (!AppGlobals.instance.tibiisController.bluetoothIsEnabled())
    {
        val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBluetoothIntent, TestingActivity.ActivityRequestCodes.enableBluetooth.value)
        return
    }

    val t = AppGlobals.instance.tibiisController


    // Disconnect
    if (t.connectStatus == TibiisController.ConnectionStatus.connected || t.connectStatus == TibiisController.ConnectionStatus.connecting)
    {
        t.disconnectTibiis()
        //formatTibiisForNotConnected()
        return
    }

    // Connect
    if (t.connectStatus == TibiisController.ConnectionStatus.notConnected)
    {
        formatTibiisForConnecting()

        t.connectToTibiis()
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
    AppGlobals.instance.activeProcess.pe_test_aborted = 1
    tibiisStopPressurising()
    resetPETest()

    Timer("stopTest", false).schedule(1000) {
        if (AppGlobals.instance.tibiisController.connectStatus == TibiisController.ConnectionStatus.connected) {
            AppGlobals.instance.tibiisController.disconnectTibiis()
        }
    }

    AppGlobals.instance.activeProcess.needs_server_sync = 1
    AppGlobals.instance.activeProcess.save(this)
}

fun TestingActivity.abortDITest()
{
    runOnUiThread {
        btnAction.isEnabled = true
        tibiisStopPressurising()
        resetDITest()
    }

    AppGlobals.instance.tibiisController.disconnectTibiis()
    AppGlobals.instance.activeProcess.needs_server_sync = 1
    AppGlobals.instance.activeProcess.save(this)
}





