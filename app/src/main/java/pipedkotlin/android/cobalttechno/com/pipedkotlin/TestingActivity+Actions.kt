package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.util.Log
import org.jetbrains.anko.startActivityForResult
import java.util.*

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
        formatTibiisForNotConnected()
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





