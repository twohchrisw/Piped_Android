package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import org.jetbrains.anko.runOnUiThread
import java.util.*
import java.util.logging.Handler

class TibiisController() {

    interface TibiisControllerDelegate
    {
        fun tibiisConnected()
        fun tibiisDisconnected()
        fun tibiisFailedToConnect()
    }

    enum class ConnectionStatus
    {
        notConnected, connecting, connected
    }

    var mBluetoothAdapter: BluetoothAdapter? = null
    var mBluetoothGatt: BluetoothGatt? = null
    var mDataMDLP: BluetoothGattCharacteristic? = null
    var mDeviceFound = false

    lateinit var delegate: TibiisControllerDelegate
    var connectStatus = ConnectionStatus.notConnected
    var connectToTibiisAfterStatusUpdate = false
    var appContext: Context? = null

    // Constants
    val EXCEL_DEVICE_UUID = "5EED5548-AF85-4F2F-9EB2-2FF16119761F"
    val MICROCHIP_SERVICE_ID = "00035b03-58e6-07dd-021a-08123a000300"
    val MLDP_CHAR_ID = "00035B03-58E6-07DD-021A-08123A000301"
    val DEVICE_INFO_SERVICE = "180A"

    enum class CurrentCommand {
        none,
        loggerWaitingForAck,
        logger,
        loggerReceivingData,
        stopLogger,
        setLCDOnTime,
        syncDateTime,
        batteryReadingWaitingForAck,
        batteryReading,
        pressurisingWaitingForAck,
        pressurising,
        pressurisingStop,
        ambientLoggerWaitingForAck,
        ambientLogging,
        reconnection
    }
    var currentCommand = CurrentCommand.none
    var previousCommand = CurrentCommand.none
    var testingContext = TestingSessionData.TestingContext.pe
    var hasSendDateSync = false


    init {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    }

    private val bleScannerCallback = object: ScanCallback()
    {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)

            Log.d("cobalt", "Scanner callback type ${callbackType}")
            val tibiisUUID = ParcelUuid.fromString(MICROCHIP_SERVICE_ID)
            if (result?.scanRecord?.serviceUuids != null)
            {
                if (result!!.scanRecord!!.serviceUuids!!.contains(tibiisUUID))
                {
                    // Stop the scan
                    Log.d("cobalt", "Stopping Scanner")
                    mBluetoothAdapter!!.bluetoothLeScanner.stopScan(this)
                    mBluetoothAdapter!!.bluetoothLeScanner.flushPendingScanResults(this)

                    mDeviceFound = true
                    val device = result.device

                    if (mBluetoothGatt != null)
                    {
                        mBluetoothGatt!!.close()
                        mBluetoothGatt = null
                    }

                    Log.d("cobalt", "Connecting via onScanResult")
                    mBluetoothGatt = device.connectGatt(appContext!!, false, mGattCallback)
                }
            }
        }
    }

    private val mGattCallback = object: BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED)
            {
                Log.d("cobalt", "GATT Connected")
                mBluetoothGatt!!.discoverServices()
                delegate.tibiisConnected()



            }
            else if (newState == BluetoothProfile.STATE_DISCONNECTED)
            {
                Log.d("cobalt", "GATT DISConnected")
                mBluetoothGatt!!.close()
                mBluetoothGatt = null
                delegate.tibiisDisconnected()
                mDeviceFound = false
                connectStatus = ConnectionStatus.notConnected
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            Log.d("cobalt", "Gatt onServicesDiscovered")
            if (status == BluetoothGatt.GATT_SUCCESS && mBluetoothGatt != null)
            {
                findMDLPGattService(mBluetoothGatt!!.services)
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            Log.d("cobalt", "Gatt onCharacteristicRead")
            //TODO: This is where the data comes in

            if (characteristic != null)
            {
                val dataValue = characteristic!!.getStringValue(0)
                processIncomingData(dataValue)
            }
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            Log.d("cobalt", "Gatt onCharacteristicWrite")
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            Log.d("cobalt", "Gatt onCharacteristicChanged")
        }
    }

    fun findMDLPGattService(gattServices: List<BluetoothGattService>)
    {
        var myUUID = ""
        mDataMDLP = null
        for (gattService in gattServices)
        {
            myUUID = gattService.uuid.toString()
            if (myUUID.equals(MICROCHIP_SERVICE_ID))
            {
                Log.d("cobalt", "got microochip service")
                val gattCharacteristics = gattService.characteristics
                for (gc in gattCharacteristics)
                {
                    val gcUUID = gc.uuid.toString().toUpperCase()
                    if (gcUUID.equals(MLDP_CHAR_ID))
                    {
                        Log.d("cobalt", "Found MLDP characteristic")
                        mDataMDLP = gc
                        delegate.tibiisConnected()
                        connectStatus = TibiisController.ConnectionStatus.connected
                    }
                    else
                    {
                        Log.d("cobalt", "Other char: " + gcUUID)
                    }

                    //TODO: Do we need to set all of the characteristic properties??
                }
            }
        }

        if (mDataMDLP == null)
        {
            mBluetoothGatt = null
            mDeviceFound = false
            delegate.tibiisFailedToConnect()
            connectStatus = TibiisController.ConnectionStatus.notConnected
        }
    }

    fun processIncomingData(data: String)
    {
        print("Incoming data: " + data)
    }



    fun supportsBluetooth(): Boolean
    {
        return mBluetoothAdapter != null
    }

    fun bluetoothIsEnabled(): Boolean
    {
        if (mBluetoothAdapter == null)
        {
            return false
        }

        return mBluetoothAdapter!!.isEnabled
    }


    fun resetController()
    {
        //TODO: Needs implementing
    }

    fun connectToTibiis()
    {
        if (mBluetoothAdapter == null)
        {
            Log.d("cobalt", "TibiisController.connectToTibiis() Bluetooth Adapter is null")
            return
        }

        Log.d("cobalt", "TibiisController.connectToTibiis() Beginning bluetooth scan")
        mBluetoothAdapter!!.bluetoothLeScanner.startScan(bleScannerCallback)

        // Cancel the scanning after a timeout - using bleScanner

        android.os.Handler().postDelayed({

            if (!mDeviceFound) {
                mBluetoothAdapter!!.bluetoothLeScanner.stopScan(bleScannerCallback)
                Log.d("cobalt", "TibiisController.connectToTibiis() Tibiis failed to connect")
                delegate.tibiisFailedToConnect()
            }
        }, 10000)
    }

    /*
    private val mLeScanCallback = BluetoothAdapter.LeScanCallback { device, rssi, scanRecord ->
        Log.d("cobalt", device.address + " " + scanRecord)

    }
    */

    fun disconnectTibiis()
    {
        // Stop the scan
        Log.d("cobalt", "Stopping Scanner")
        mBluetoothAdapter!!.bluetoothLeScanner.stopScan(bleScannerCallback)
        mBluetoothAdapter!!.bluetoothLeScanner.flushPendingScanResults(bleScannerCallback)
        mBluetoothGatt!!.disconnect()
        //mBluetoothGatt!!.close()
        //mBluetoothGatt = null
    }

    fun commandSendAck()
    {
        //TODO: Not implemented
    }


}