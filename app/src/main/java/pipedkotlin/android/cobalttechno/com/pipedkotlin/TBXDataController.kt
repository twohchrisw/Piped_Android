package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.graphics.Path
import android.icu.lang.UCharacter.GraphemeClusterBreak.L
import android.os.Handler
import android.os.Looper
import android.util.Log
import org.intellij.lang.annotations.Flow
import org.jetbrains.anko.runOnUiThread
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.absoluteValue
import kotlin.math.atan
import kotlin.math.log

class TBXCommand(val sendCommand: Int, val sendLength: Int, val receivedCommand: Int, val description: String)
class OptionByteData(val bsn: Int, val optionByte: TBXDataController.OptionBytes)
class CalibrationByteData(val bsn: Int, val calibrationByte: TBXDataController.CalibrationData)
class PreviousLogs(val startLogNumber: Int,val numberOfLogs: Int,val maxLogNumber: Int, val logs: ArrayList<LogReading>, val liveLog: LogReading)
class CalibrationDataResult(val calibrationByte: TBXDataController.CalibrationData, val dataString: String)

class TBXDataController(val tibiisController: TibiisController) {

    interface TBXDataControllerDelegate
    {
        fun TbxDataControllerPacketReceived(packet:IncomingPacket)
        fun TbxDataControllerNoResponseToCommand()
    }


    var incomingPacket = IncomingPacket()
    val STX = 0x02.toInt()
    var optionByteData: OptionByteData? = null
    var calibrationByteData: CalibrationByteData? = null
    val COMMAND_WAIT_SECONDS = 3
    var bsn: Int = 0
    var delegate: TBXDataControllerDelegate? = null
    var commandWaiting = false
    var context: Context? = null

    enum class IncomingDataParseStage
    {
        STX, Command, BSN, Length, Data, LowerChecksum, UpperChecksum, Complete
    }

    enum class UnitType {
        Bar, PSI, ATM
    }

    enum class OptionBytes {
        None, FlowType, InactivityTimeout, LcdOnTime, LogInterval, UnitType, SerialNumber
    }

    enum class CalibrationData {
        None, Temp, Pressure1, Pressure2, Pressure3, Pressure4, Pressure5, Pressure6, Date, Time, Name
    }

    enum class Command(val value: TBXCommand)
    {
        ProtocolVersion(TBXCommand(0x00, 1, 128, "0x00 Protocol Version")),
        FetchLiveLog(TBXCommand(0x01, 0, 127, "0x01 Fetch Live Log")),
        FetchOldLogs(TBXCommand(0x02, 4, 126, "0x02 Fetch Old Logs")),
        TimeSync(TBXCommand(0x03, 6, 125, "0x03 Time Sync")),
        OutputControl(TBXCommand(0x04, 2, 124, "0x04 Output Control")),
        StartTest(TBXCommand(0x05, 0, 123, "0x05 Start Test")),
        FlowType(TBXCommand(0x06, 2, 122, "0x06 Flow Type")),
        ScreenControl(TBXCommand(0x07, 2, 121, "0x07 Screen Control")),
        InactivityTimeout(TBXCommand(0x09, 2, 119, "0x09 Inactivity Timeout")),
        LcdOnTime(TBXCommand(0x0A, 2, 118, "0x0A LCD On Time")),
        LogInterval(TBXCommand(0x0B, 2, 117, "0x0B Log Interval")),
        StopTest(TBXCommand(0x0C, 0, 116, "0x0C Stop Test")),
        BacklightControl(TBXCommand(0x0D, 2, 115, "0x0D Backlight Control")),
        UnitTypeSelect(TBXCommand(0x0E, 2, 114, "0x0E Unit Type Select")),
        Calibration(TBXCommand(0x0F, 2, 113, "0x0F Calibration")),
        ZeroSensor(TBXCommand(0x10, 0, 112, "0x10 Zero Sensor")),
        SoftwareVersion(TBXCommand(0x11, 0, 111, "0x11 Software Version")),
        FormatSDCard(TBXCommand(0x12, 0, 110, "0x12 Format SD Card")),
        GetOptionBytes(TBXCommand(0x13, 2, 109, "0x13 Get Option Bytes")),
        GetCalibrationData(TBXCommand(0x14, 2, 108, "0x14 Get Calibration Data")),
        ScreenControlMode(TBXCommand(0x15, 2, 107, "0x15 Screen Control Mode"));

        companion object {
            fun commandFromReceivedByte(byte: Int): Command?
            {
                when (byte)
                {
                    ProtocolVersion.value.receivedCommand -> return  ProtocolVersion
                    FetchLiveLog.value.receivedCommand -> return FetchLiveLog
                    FetchOldLogs.value.receivedCommand -> return FetchOldLogs
                    TimeSync.value.receivedCommand -> return TimeSync
                    OutputControl.value.receivedCommand -> return  OutputControl
                    StartTest.value.receivedCommand -> return StartTest
                    FlowType.value.receivedCommand -> return FlowType
                    ScreenControl.value.receivedCommand -> return ScreenControl
                    InactivityTimeout.value.receivedCommand -> return InactivityTimeout
                    LcdOnTime.value.receivedCommand -> return LcdOnTime
                    LogInterval.value.receivedCommand -> return LogInterval
                    StopTest.value.receivedCommand -> return StopTest
                    BacklightControl.value.receivedCommand -> return BacklightControl
                    UnitTypeSelect.value.receivedCommand -> return UnitTypeSelect
                    Calibration.value.receivedCommand -> return Calibration
                    ZeroSensor.value.receivedCommand -> return ZeroSensor
                    SoftwareVersion.value.receivedCommand -> return SoftwareVersion
                    FormatSDCard.value.receivedCommand -> return FormatSDCard
                    GetOptionBytes.value.receivedCommand -> return GetOptionBytes
                    GetCalibrationData.value.receivedCommand -> return GetCalibrationData
                    ScreenControlMode.value.receivedCommand -> return ScreenControlMode
                }

                return null
            }
        }
    }

    fun processIncomingData(data: BluetoothGattCharacteristic)
    {

        var bytesData = data.value   // Byte Array

        for (b in bytesData)
        {
            // Print incoming data
            //Log.d("Cobalt", "${b.toInt()} ${b.toInt().absoluteValue} ${b.toUInt()}")
        }


        if (incomingPacket.parseStage == IncomingDataParseStage.STX && bytesData.isNotEmpty() && bytesData[0].toInt() == STX) {
            //Log.d("Cobalt", "Received STX")

            incomingPacket.parseStage = IncomingDataParseStage.Command
            bytesData = bytesData.drop(1).toByteArray()
        }

        if (incomingPacket.parseStage == IncomingDataParseStage.Command && bytesData.isNotEmpty())
        {
            incomingPacket.parseStage = IncomingDataParseStage.BSN
            incomingPacket.setCommand(bytesData[0].toInt().absoluteValue) // .absoluteValue
            //Log.d("Cobalt","Command stage value in is: ${bytesData[0].toInt().absoluteValue}")
            bytesData = bytesData.drop(1).toByteArray()
        }

        if (incomingPacket.parseStage == IncomingDataParseStage.BSN && bytesData.isNotEmpty())
        {
            incomingPacket.parseStage = IncomingDataParseStage.Length
            incomingPacket.bsn = magicallyExtractRightValue(bytesData[0]) // .absoluteValue
            bytesData = bytesData.drop(1).toByteArray()

            if (optionByteData != null)
            {
                if (optionByteData!!.bsn == incomingPacket.bsn)
                {
                    incomingPacket.optionByte = optionByteData!!.optionByte
                    this.optionByteData = null
                }
            }

            if (calibrationByteData != null)
            {
                if (calibrationByteData!!.bsn == incomingPacket.bsn)
                {
                    incomingPacket.calibrationByte = calibrationByteData!!.calibrationByte
                    this.calibrationByteData = null
                }
            }
        }

        if (incomingPacket.parseStage == IncomingDataParseStage.Length && bytesData.isNotEmpty())
        {
            incomingPacket.length = bytesData[0].toInt().absoluteValue
            if (incomingPacket.length > 0)
            {
                incomingPacket.parseStage = IncomingDataParseStage.Data
            }
            else
            {
                incomingPacket.parseStage = IncomingDataParseStage.LowerChecksum
            }
            bytesData = bytesData.drop(1).toByteArray()
        }

        if (incomingPacket.parseStage == IncomingDataParseStage.Data && bytesData.isNotEmpty())
        {
            while (incomingPacket.data.size < incomingPacket.length && bytesData.isNotEmpty())
            {
                incomingPacket.data.add(magicallyExtractRightValue(bytesData[0]))
                bytesData = bytesData.drop(1).toByteArray()
            }

            if (incomingPacket.data.size == incomingPacket.length)
            {
                incomingPacket.parseStage = IncomingDataParseStage.LowerChecksum
            }
        }

        if (incomingPacket.parseStage == IncomingDataParseStage.LowerChecksum && bytesData.isNotEmpty())
        {
            incomingPacket.lowerChecksum = bytesData[0].toInt().absoluteValue
            incomingPacket.parseStage = IncomingDataParseStage.UpperChecksum
            bytesData = bytesData.drop(1).toByteArray()
        }

        if (incomingPacket.parseStage == IncomingDataParseStage.UpperChecksum && bytesData.isNotEmpty())
        {
            incomingPacket.upperChecksum = bytesData[0].toInt().absoluteValue
            commandWaiting = false
            //Log.d("Cobalt", "Command Receive Complete: ${incomingPacket.description()}")
            delegate?.TbxDataControllerPacketReceived(incomingPacket)
            incomingPacket = IncomingPacket()
        }
    }

    fun magicallyExtractRightValue(o: Byte): Int = when {
        (o.toInt() < 0) -> 255 + o.toInt() + 1
        else -> o.toInt()
    }

    /* Commands */

    fun sendCommandProtocolVersion()
    {
        sendPacket(Command.ProtocolVersion.value.sendCommand, Command.ProtocolVersion.value.sendLength, arrayOf(0x01))
    }

    fun sendCommandLiveLog()
    {
        sendPacket(Command.FetchLiveLog.value.sendCommand, Command.FetchLiveLog.value.sendLength, arrayOf())
    }

    fun sendCommandFetchOldLogs(startLogNumber: Int, numberOfLogs: Int)
    {
        if (startLogNumber < 65535)
        {
            val startLogByte1 = (startLogNumber shr 16) and 0xFF
            val startLogByte2 = (startLogNumber shr 8) and 0xFF
            val startLogByte3 = startLogNumber and 0xFF
            val data = arrayOf(startLogByte1, startLogByte2, startLogByte3, numberOfLogs)
            sendPacket(Command.FetchOldLogs.value.sendCommand, Command.FetchOldLogs.value.sendLength, data)
        }
        else
        {
            val startLogByte1 = (startLogNumber shr 16) and 0xFF
            val b2Prior = (startLogNumber shr 8) and 0xFF
            val startLogByte2 = b2Prior
            val startLogByte3 = startLogNumber and 0xFF
            val data = arrayOf(startLogByte1, startLogByte2, startLogByte3, numberOfLogs)
            sendPacket(Command.FetchOldLogs.value.sendCommand, Command.FetchOldLogs.value.sendLength, data)
        }
    }

    fun sendCommandInactivityTimeout(seconds: Int)
    {
        val byte1 = (seconds shr 8) and 0xFF
        val byte2 = (seconds and 0xFF)
        val data = arrayOf(byte1, byte2)
        sendPacket(Command.InactivityTimeout.value.sendCommand, Command.InactivityTimeout.value.sendLength, data)
    }

    fun sendCommandStartTest()
    {
        sendPacket(Command.StartTest.value.sendCommand, Command.StartTest.value.sendLength, arrayOf())
    }

    fun sendCommandStopTest()
    {
        sendPacket(Command.StopTest.value.sendCommand, Command.StopTest.value.sendLength, arrayOf())
    }

    fun sendCommandGetOptionBytes(optionByte: OptionBytes)
    {
       var data = arrayOf(0x00, 0x00)
        when (optionByte)
        {
            OptionBytes.FlowType -> data = arrayOf(0x00, 0x00)
            OptionBytes.InactivityTimeout -> data = arrayOf(0x00, 0x01)
            OptionBytes.LcdOnTime -> data = arrayOf(0x00, 0x02)
            OptionBytes.LogInterval -> data = arrayOf(0x00, 0x03)
            OptionBytes.UnitType -> data = arrayOf(0x00, 0x04)
            OptionBytes.SerialNumber -> data = arrayOf(0x00, 0x05)
        }

        sendPacket(Command.GetOptionBytes.value.sendCommand, Command.GetOptionBytes.value.sendLength, data)
    }

    fun sendCommandGetCalibrationData(calibrationByte: CalibrationData)
    {
        var data = arrayOf(0x00, 0x00)
        when (calibrationByte)
        {
            CalibrationData.Temp -> data = arrayOf(0x00, 0x00)
            CalibrationData.Pressure1 -> data = arrayOf(0x00, 0x01)
            CalibrationData.Pressure2 -> data = arrayOf(0x00, 0x02)
            CalibrationData.Pressure3 -> data = arrayOf(0x00, 0x03)
            CalibrationData.Pressure4 -> data = arrayOf(0x00, 0x04)
            CalibrationData.Pressure5 -> data = arrayOf(0x00, 0x05)
            CalibrationData.Pressure6 -> data = arrayOf(0x00, 0x06)
            CalibrationData.Date -> data = arrayOf(0x00, 0x07)
            CalibrationData.Time -> data = arrayOf(0x00, 0x08)
            CalibrationData.Name -> data = arrayOf(0x00, 0x09)
        }

        sendPacket(Command.GetCalibrationData.value.sendCommand, Command.GetCalibrationData.value.sendLength, data, OptionBytes.None, calibrationByte)
    }

    fun sendCommandScreenControl(on: Boolean)
    {
        var data = arrayOf(0x00, 0x02)
        if (!on)
        {
            data = arrayOf(0x00, 0x00)
        }

        sendPacket(Command.ScreenControl.value.sendCommand, Command.ScreenControl.value.sendLength, data)
    }

    fun sendCommandBacklightControl(on: Boolean)
    {
        var data = arrayOf(0x00, 0x02)
        if (!on)
        {
            data = arrayOf(0x00, 0x00)
        }
        sendPacket(Command.BacklightControl.value.sendCommand, Command.BacklightControl.value.sendLength, data)
    }

    fun sendCommandOutputControl(on: Boolean)
    {
        var data = arrayOf(0x00, 0x01)
        if (!on)
        {
            data = arrayOf(0x00, 0x00)
        }
        sendPacket(Command.OutputControl.value.sendCommand, Command.OutputControl.value.sendLength, data)
    }

    fun sendCommandScreenControlMode(app: Boolean)
    {
        var data = arrayOf(0x00, 0x01)
        if (!app)
        {
            data = arrayOf(0x00, 0x00)
        }
        sendPacket(Command.ScreenControlMode.value.sendCommand, Command.ScreenControlMode.value.sendLength, data)
    }

    fun sendCommandSetLCDOnTime(seconds: Int)
    {
        val byte1 = (seconds shr 8) and 0xFF
        val byte2 = (seconds and 0xFF)
        val data = arrayOf(byte1, byte2)
        sendPacket(Command.LcdOnTime.value.sendCommand, Command.LcdOnTime.value.sendLength, data)
    }

    fun sendCommandSetLogInterval(seconds: Int)
    {
        val byte1 = (seconds shr 8) and 0xFF
        val byte2 = (seconds and 0xFF)
        val data = arrayOf(byte1, byte2)
        sendPacket(Command.LogInterval.value.sendCommand, Command.LogInterval.value.sendLength, data)
    }

    fun sendCommandZeroSensor()
    {
        sendPacket(Command.ZeroSensor.value.sendCommand, Command.ZeroSensor.value.sendLength, arrayOf())
    }

    fun sendCommandSoftwareVersion()
    {
        sendPacket(Command.SoftwareVersion.value.sendCommand, Command.SoftwareVersion.value.sendLength, arrayOf())
    }

    fun sendCommandFlowType(flowType: LogReading.FlowrateType)
    {
        var data = arrayOf(0x00, 0x00)
        if (flowType == LogReading.FlowrateType.Pulse)
        {
            data = arrayOf(0x00, 0x01)
        }
        if (flowType == LogReading.FlowrateType.Current)
        {
            data = arrayOf(0x00, 0x02)
        }
        sendPacket(Command.FlowType.value.sendCommand, Command.FlowType.value.sendLength, data)
    }

    fun sendCommandUnitTypeSelection(unitType: UnitType)
    {
        var data = arrayOf(0x00, 0x00)
        if (unitType == UnitType.PSI)
        {
            data = arrayOf(0x00, 0x01)
        }
        if (unitType == UnitType.ATM)
        {
            data = arrayOf(0x00, 0x02)
        }
        sendPacket(Command.UnitTypeSelect.value.sendCommand, Command.UnitTypeSelect.value.sendLength, data)
    }

    fun sendCommandTimeSync()
    {
        val date = Date()
        val cal = Calendar.getInstance()
        cal.time = date
        val year = cal.get(Calendar.YEAR) - 2000
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val mins = cal.get(Calendar.MINUTE)
        val seconds = cal.get(Calendar.SECOND)
        val formatString = "%02d"

        val dataString = "${String.format(formatString, day)}${String.format(formatString, month)}${String.format(formatString, year)}${String.format(formatString, hour)}${String.format(formatString, mins)}${String.format(formatString, seconds)}"
        val data = dataString.toByteArray()

        // convert data to an array list of ints
        val intData = ArrayList<Int>()
        for (byte in data)
        {
            intData.add(byte.toInt())
        }
        val intDataArray = intData.toTypedArray()

        // convert array list to an array

        //TODO: Come back to this later - it only affects the reading times on the card
        sendPacket(Command.TimeSync.value.sendCommand, intDataArray.size, intDataArray)
    }


    /* Send Packet */

    fun sendPacket(command: Int, length: Int, data: Array<Int>, optionByteFlag: OptionBytes = OptionBytes.None, calibrationFlag: CalibrationData = CalibrationData.None)
    {
        val this_bsn = incrementBsn()
        var checksum = command
        checksum += this_bsn
        checksum += length

        if (optionByteFlag != OptionBytes.None)
        {
            this.optionByteData = OptionByteData(this_bsn, optionByteFlag)
        }

        if (calibrationFlag != CalibrationData.None)
        {
            this.calibrationByteData = CalibrationByteData(this_bsn, calibrationFlag)
        }

        for (dataByte in data)
        {
            checksum += dataByte
        }

        val lowerChecksum = checksum and 0xff
        val upperChecksim = checksum shr 8

        var commandData = ArrayList<Byte>()
        commandData.add(0x02)
        commandData.add(command.toByte())
        commandData.add(this_bsn.toByte())
        commandData.add(length.toByte())
        for (dataByte in data)
        {
            commandData.add(dataByte.toByte())
        }
        commandData.add(lowerChecksum.toByte())
        commandData.add(upperChecksim.toByte())

        this.commandWaiting = true

        tibiisController.mDataMDLP!!.setValue(commandData.toByteArray())
        tibiisController.writeCharacteristic(tibiisController.mDataMDLP!!)
    }

    /*
    fun resetIncomingData()
    {
        this.incomingPacket = IncomingPacket()
    }
    */

    fun incrementBsn(): Int
    {
        if (bsn < 240) {
            bsn = bsn + 1
        }
        else
        {
            bsn = 1
        }

        return bsn
    }

    class IncomingPacket {
        var parseStage = IncomingDataParseStage.STX
        var command: Command? = null
        var bsn: Int = 0
        var length: Int = 0
        var data = ArrayList<Int>()
        var lowerChecksum = 0
        var upperChecksum = 0
        var optionByte = OptionBytes.None
        var calibrationByte = CalibrationData.None

        fun setCommand(commandByte: Int)
        {
            command = Command.commandFromReceivedByte(commandByte)
            if (command == null)
            {
                Log.d("Cobalt","Error parsing command byte $commandByte)")
            }
            else
            {
                //Log.d("Cobalt", "Command set as ${command!!.value.description}")
            }
        }

        fun description(): String {
            if (command != null)
            {
                return "Command: ${command!!.value.description} BSN: $bsn Length: $length, Data: $data"
            }
            else
            {
                return "Command: (no command) BSN: $bsn Length: $length, Data: $data"
            }
        }

        fun parseDataAsProtocolVersion(): Int?
        {
            if (data.isNotEmpty())
            {
                return data[0]
            }

            return null
        }

        fun parseAsLogReading(): LogReading?
        {
            return LogReading(data)
        }

        fun parseDataAsPreviousLogReadings(): PreviousLogs?
        {
            if (data.size < 4)
            {
                Log.d("Cobalt", "Invalid data from previous logs")
                return null
            }

            val startLogNumber = data[0] shl 16 or data[1] shl 8 or data[2]
            val numberOfLogs = data[3]

            // Remove the header data
            var dataWithoutHeader = data
            dataWithoutHeader = dataWithoutHeader.drop(4) as ArrayList<Int>

            // Remove the live log data
            val liveLogData = dataWithoutHeader.take(10) as ArrayList<Int>
            val liveLog = LogReading(liveLogData)
            Log.d("Cobalt", "Previous Logs Live Log: ${liveLog.description()}")

            var previousLogData = dataWithoutHeader.drop(10) as ArrayList<Int>

            if (previousLogData.size %7 != 0)
            {
                Log.d("Cobalt", "Invalid sized data back from previous logs request ${previousLogData.size}")
                return null
            }

            var currentLogNumber = startLogNumber
            var finishFlag = 0

            var logs = ArrayList<LogReading>()
            while (finishFlag == 0)
            {
                val logData = previousLogData.take(7) as ArrayList<Int>
                logs.add(LogReading(logData, currentLogNumber))

                if (previousLogData.size > 8)
                {
                    previousLogData = previousLogData.drop(7) as ArrayList<Int>
                }
                else
                {
                    finishFlag = 1
                }
                currentLogNumber = currentLogNumber + 1
            }

            return PreviousLogs(startLogNumber, numberOfLogs, currentLogNumber, logs, liveLog)
        }

        fun parseAsCalibrationData(): CalibrationDataResult
        {
            var byteArray = ArrayList<Byte>()
            for (i in data)
            {
                byteArray.add(i.toByte())
            }

            val bytesArrayTyped = byteArray.toByteArray()
            val string = String(bytesArrayTyped)
            return CalibrationDataResult(calibrationByte, string)
        }

    }
}

class LogReading(val data: ArrayList<Int>) {
    var logNumber = -1
    var pressure = -1
    var flowRate = -1
    var temperature = -1
    var battery = -1
    var control = -1
    var flowrateType = FlowrateType.None

    enum class FlowrateType {
        None, Pulse, Current
    }

    constructor(data: ArrayList<Int>, previousLogNumber: Int): this(data)
    {
        logNumber = previousLogNumber
    }

    constructor(data: ArrayList<Int>, tibiisReading: EXLDTibiisReading): this(data)
    {
        logNumber = tibiisReading.logNumber
        pressure = tibiisReading.pressure
        flowRate = tibiisReading.flowRate
        battery = tibiisReading.battery
    }

    init {
        // Parsing single fetch live logs
        if (data.size == 10)
        {
            val controlByte = data[9].toInt().absoluteValue
            logNumber = data[0].toInt() shl 16 or data[1].toInt() shl 8 or data[2].toInt()
            pressure = data[3].toInt() shl 8 or data[4].toInt()
            if (controlByte and 0x80 == 128) {
                pressure = pressure * -1
            }

            flowRate = data[5].toInt() shl 8 or data[6].toInt()
            if (controlByte and 0x40 == 64)
            {
                flowrateType = FlowrateType.Pulse
            }
            if (controlByte and 0x10 == 16)
            {
                flowrateType = FlowrateType.Current
            }

            temperature = data[7].toInt()
            if (controlByte and 0x20 == 32)
            {
                temperature = temperature * -1
            }

            battery = data[8].toInt()
            control = controlByte
        }

        if (data.size == 7)
        {
            val controlByte = data[6]
            pressure = data[0] shl 8 or data[1]
            if (controlByte and 0x80 == 128) {
                pressure = pressure * -1
            }

            flowRate = data[2].toInt() shl 8 or data[3].toInt()
            if (controlByte and 0x40 == 64)
            {
                flowrateType = FlowrateType.Pulse
            }
            if (controlByte and 0x10 == 16)
            {
                flowrateType = FlowrateType.Current
            }

            temperature = data[4].toInt()
            if (controlByte and 0x20 == 32)
            {
                temperature = temperature * -1
            }

            battery = data[5].toInt()
            control = controlByte
        }
    }

    fun description(): String
    {
        return "LogNo: $logNumber Pressure: $pressure FlowRate: ${flowRate} Temo: $temperature Battery: $battery Control: $control"
    }

}


