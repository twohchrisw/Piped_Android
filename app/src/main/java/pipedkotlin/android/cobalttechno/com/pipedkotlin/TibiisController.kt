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
    val CHARACTERISTIC_NOTIFICATION_CONFIG = "00002902-0000-1000-8000-00805f9b34fb"

    //TBX
    var tbxDataController = TBXDataController(this)
    var shouldCheckForMissingLogs = false
    var testingContext = TestingSessionData.TestingContext.pe
    var hasSendDateSync = false
    var autoPumpEnableHasBeenSet = false
    var tibiisHasBeenConnected = false


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
            //Log.d("cobalt", "Gatt onCharacteristicRead")
            //TODO: This is where the data comes in

            if (characteristic != null)
            {
                tbxDataController.processIncomingData(characteristic!!)
            }
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            //Log.d("cobalt", "Gatt onCharacteristicWrite")
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            //TODO: Or it may come in here
            //Log.d("cobalt", "Gatt onCharacteristicChanged")

            if (characteristic != null)
            {
                tbxDataController.processIncomingData(characteristic!!)
            }
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
                Log.d("cobalt", "got microchip service")
                val gattCharacteristics = gattService.characteristics
                for (gc in gattCharacteristics)
                {

                    val gcUUID = gc.uuid.toString().toUpperCase()
                    if (gcUUID.equals(MLDP_CHAR_ID))
                    {
                        Log.d("cobalt", "Found MLDP characteristic")
                        mDataMDLP = gc

                        val charProperties = gc.properties
                        if ((charProperties and (BluetoothGattCharacteristic.PROPERTY_NOTIFY)) > 0)
                        {
                            Log.d("cobalt", "Set NOTIFY value")
                            mBluetoothGatt!!.setCharacteristicNotification(gc, true)
                            val descriptor = gc.getDescriptor(UUID.fromString(CHARACTERISTIC_NOTIFICATION_CONFIG))
                            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            mBluetoothGatt!!.writeDescriptor(descriptor)
                        }

                        if ((charProperties and (BluetoothGattCharacteristic.PROPERTY_INDICATE)) > 0)
                        {
                            Log.d("cobalt", "Set INDICATE value")
                            mBluetoothGatt!!.setCharacteristicNotification(gc, true)
                            val descriptor = gc.getDescriptor(UUID.fromString(CHARACTERISTIC_NOTIFICATION_CONFIG))
                            descriptor.value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                            mBluetoothGatt!!.writeDescriptor(descriptor)
                        }
                        if ((charProperties and (BluetoothGattCharacteristic.PROPERTY_WRITE)) > 0)
                        {
                            Log.d("cobalt", "Set WRITE TYPE value")
                            gc.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                        }
                        if ((charProperties and (BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) > 0)
                        {
                            Log.d("cobalt", "Set WRITE NO RESPONSE value")
                            gc.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                        }

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
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter!!.bluetoothLeScanner.stopScan(bleScannerCallback)
            mBluetoothAdapter!!.bluetoothLeScanner.flushPendingScanResults(bleScannerCallback)
            mBluetoothGatt!!.disconnect()
        }
        //mBluetoothGatt!!.close()
        //mBluetoothGatt = null
    }

    fun commandSendAck()
    {
        //TODO: Not implemented
    }


    //TODO: This will need refactoring into TBXDataController - I just want to see if we can actually send a command and get gata
    fun sendTestCommand()
    {
        tbxDataController.resetIncomingData()

        val command = 0x00  // Protocol Version

        val length = 1
        val dataPayload = 1

        // Trying this with UInts
        val bsn = 6   // Unsigned Int
        var checksum = command    // Command
        checksum += bsn.toInt()
        checksum += length.toInt()
        checksum += dataPayload.toInt()

        // No data at this point

        val lowerChecksum = checksum and 0xff
        val upperChecksim = checksum shr 8

        var data = arrayOf( 0x02, command.toByte(), bsn.toByte(), length.toByte(), dataPayload.toByte(), lowerChecksum.toByte(), upperChecksim.toByte() ).toByteArray()
        Log.d("cobalt", "Byte array is ${data}")
        mDataMDLP!!.setValue(data)


        writeCharacteristic(mDataMDLP!!)
        // iOS has perip.writeValue( for characteristic) -- is this how we do this here?  We need an example
    }

    // There is no payload on this command
    fun sendTestCommandFetchLiveLog()
    {
        tbxDataController.resetIncomingData()

        val command = 0x01  // Fetch Live Log
        val length = 0

        // Trying this with UInts
        val bsn = 34   // Unsigned Int
        var checksum = command.toInt()    // Command
        checksum += bsn
        checksum += length

        // No data at this point

        val lowerChecksum = checksum and 0xff
        val upperChecksim = checksum shr 8

        var data = arrayOf( 0x02, command.toByte(), bsn.toByte(), length.toByte(), lowerChecksum.toByte(), upperChecksim.toByte() ).toByteArray()
        mDataMDLP!!.setValue(data)
        writeCharacteristic(mDataMDLP!!)
    }

    fun sendTestBacklightOn()
    {
        tbxDataController.resetIncomingData()

        val command = 0x0D  // Protocol Version

        val length = 2
        val dataPayload1 = 0
        val dataPayload2 = 1

        // Trying this with UInts
        val bsn = 3   // Unsigned Int
        var checksum = command    // Command
        checksum += bsn.toInt()
        checksum += length.toInt()
        checksum += dataPayload1.toInt()
        checksum += dataPayload2.toInt()

        // No data at this point

        val lowerChecksum = checksum and 0xff
        val upperChecksim = checksum shr 8

        var data = arrayOf( 0x02, command.toByte(), bsn.toByte(), length.toByte(), dataPayload1.toByte(), dataPayload2.toByte(), lowerChecksum.toByte(), upperChecksim.toByte() ).toByteArray()
        mDataMDLP!!.setValue(data)
        writeCharacteristic(mDataMDLP!!)
    }

    fun sendCommandBacklightOnTest()
    {

    }

    fun writeCharacteristic(characteristic: BluetoothGattCharacteristic)
    {
        if (mBluetoothAdapter == null || mBluetoothGatt == null)
        {
            Log.d("cobalt", "Can't write characteristic, adapter or GATT is null")
            return
        }

        mBluetoothGatt!!.writeCharacteristic(characteristic)
    }

}