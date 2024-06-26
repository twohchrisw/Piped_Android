package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.icu.lang.UCharacter.GraphemeClusterBreak.L
import android.os.ParcelUuid
import android.util.Log
import org.jetbrains.anko.runOnUiThread
import java.util.*
import java.util.logging.Handler
import androidx.core.app.ActivityCompat

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
    var mDevice: BluetoothDevice? = null

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
    var deviceSerialNumber = ""
    var ignoreLoopCommands = false


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
        @SuppressLint(
            "MissingPermission"
        )
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)

            /*
            if (result?.scanRecord != null) {
                val deviceName = result!!.scanRecord!!.deviceName
                Log.d("zzz", "Device name = $deviceName")
            }
             */


            //Log.d("cobalt", "Scanner callback type ${callbackType}")
            val tibiisUUID = ParcelUuid.fromString(MICROCHIP_SERVICE_ID)
            if (result?.scanRecord?.serviceUuids != null)
            {
                if (result!!.scanRecord!!.serviceUuids!!.contains(tibiisUUID))
                {
                    // Stop the scan
                    //Log.d("cobalt", "Stopping Scanner")
                    mBluetoothAdapter!!.bluetoothLeScanner.stopScan(this)
                    mBluetoothAdapter!!.bluetoothLeScanner.flushPendingScanResults(this)

                    mDeviceFound = true
                    val device = result.device
                    mDevice = device

                    if (mBluetoothGatt != null)
                    {
                        mBluetoothGatt!!.close()
                        mBluetoothGatt = null
                    }

                    //Log.d("cobalt", "Connecting via onScanResult")
                    mBluetoothGatt = device.connectGatt(appContext!!, false, mGattCallback)
                }
            }
        }
    }

    private val mGattCallback = object: BluetoothGattCallback() {
        @SuppressLint(
            "MissingPermission"
        )
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
            //Log.d("cobalt", "Gatt onServicesDiscovered")
            if (status == BluetoothGatt.GATT_SUCCESS && mBluetoothGatt != null)
            {
                findMDLPGattService(mBluetoothGatt!!.services)
            }
        }

        @Deprecated(
            "Deprecated in Java"
        )
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

        @Deprecated(
            "Deprecated in Java"
        )
        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            //TODO: Or it may come in here
            //Log.d("cobalt", "Gatt onCharacteristicChanged")

            if (characteristic != null)
            {
                tbxDataController.processIncomingData(characteristic!!)
            }
        }
    }

    @SuppressLint(
        "MissingPermission"
    )
    fun findMDLPGattService(gattServices: List<BluetoothGattService>)
    {
        var myUUID = ""
        mDataMDLP = null
        for (gattService in gattServices)
        {
            myUUID = gattService.uuid.toString()
            if (myUUID.equals(MICROCHIP_SERVICE_ID))
            {
                //Log.d("cobalt", "got microchip service")
                val gattCharacteristics = gattService.characteristics
                for (gc in gattCharacteristics)
                {

                    val gcUUID = gc.uuid.toString().toUpperCase()
                    if (gcUUID.equals(MLDP_CHAR_ID))
                    {
                        //Log.d("cobalt", "Found MLDP characteristic")
                        mDataMDLP = gc

                        val charProperties = gc.properties
                        if ((charProperties and (BluetoothGattCharacteristic.PROPERTY_NOTIFY)) > 0)
                        {
                            //Log.d("cobalt", "Set NOTIFY value")
                            mBluetoothGatt!!.setCharacteristicNotification(gc, true)
                            val descriptor = gc.getDescriptor(UUID.fromString(CHARACTERISTIC_NOTIFICATION_CONFIG))
                            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            mBluetoothGatt!!.writeDescriptor(descriptor)
                        }

                        if ((charProperties and (BluetoothGattCharacteristic.PROPERTY_INDICATE)) > 0)
                        {
                            //Log.d("cobalt", "Set INDICATE value")
                            mBluetoothGatt!!.setCharacteristicNotification(gc, true)
                            val descriptor = gc.getDescriptor(UUID.fromString(CHARACTERISTIC_NOTIFICATION_CONFIG))
                            descriptor.value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                            mBluetoothGatt!!.writeDescriptor(descriptor)
                        }
                        if ((charProperties and (BluetoothGattCharacteristic.PROPERTY_WRITE)) > 0)
                        {
                            //Log.d("cobalt", "Set WRITE TYPE value")
                            gc.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                        }
                        if ((charProperties and (BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) > 0)
                        {
                            L//og.d("cobalt", "Set WRITE NO RESPONSE value")
                            gc.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                        }

                        delegate.tibiisConnected()
                        connectStatus = TibiisController.ConnectionStatus.connected

                    }
                    else
                    {
                        //Log.d("cobalt", "Other char: " + gcUUID)
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
        currentCommand = CurrentCommand.none

        if (connectStatus == ConnectionStatus.connected) {
            if (appContext != null) {
                appContext!!.runOnUiThread {
                    tbxDataController.sendCommandOutputControl(false)
                }
            }
        }
    }

    fun connectToTibiis()
    {
        if (mBluetoothAdapter == null)
        {
            Log.d("cobalt", "TibiisController.connectToTibiis() Bluetooth Adapter is null")
            return
        }

        Log.d("cobalt", "TibiisController.connectToTibiis() Beginning bluetooth scan")

        if (ActivityCompat.checkSelfPermission(
                appContext!!,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }

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

            //mBluetoothAdapter!!.bluetoothLeScanner.stopScan(bleScannerCallback)
            //mBluetoothAdapter!!.bluetoothLeScanner.flushPendingScanResults(bleScannerCallback)

            if (mBluetoothGatt != null)
            {
                mBluetoothGatt!!.disconnect()
            }
        }

        connectStatus = ConnectionStatus.notConnected
        //mBluetoothGatt!!.close()
        //mBluetoothGatt = null
    }

    fun commandSendAck()
    {
        //TODO: Not implemented
    }


    fun writeCharacteristic(characteristic: BluetoothGattCharacteristic)
    {
        if (mBluetoothAdapter == null || mBluetoothGatt == null)
        {
            Log.d("cobalt", "Can't write characteristic, adapter or GATT is null")
            return
        }

        if (ActivityCompat.checkSelfPermission(
                appContext!!,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        mBluetoothGatt!!.writeCharacteristic(characteristic)
    }

}