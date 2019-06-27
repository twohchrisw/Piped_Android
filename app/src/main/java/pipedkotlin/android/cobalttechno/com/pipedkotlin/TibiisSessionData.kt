package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.util.Log
import org.jetbrains.anko.db.classParser
import org.jetbrains.anko.db.parseList
import org.jetbrains.anko.db.select
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.log

class HighestLogDataResult(val logReading: LogReading, val recordedDate: Date)

class TibiisSessionData {

    enum class TibiisReadingType {
        beforePressurising, pressurising, logging, afterLogging
    }

    var lastReading: LogReading? = null
    var startPressureReading: LogReading? = null
    var isDIConditioning = false
    var loggingDurationSet = false
    var lcdOnTimeSet = false
    var hasCalculated = false
    var testingContext = TestingSessionData.TestingContext.pe

    private var highestLogData: HighestLogDataResult? = null
    fun getHighestLogData(): HighestLogDataResult?
    {
        return highestLogData
    }
    fun setHighestLogData(value: HighestLogDataResult)
    {
        highestLogData = value
        if (highestLogData != null)
        {
            val pressure = highestLogData!!.logReading.pressure.toDouble() / 1000.0
            Log.d("Cobalt", "SET HIGHEST LOG PRESSURE TO ${pressure}) at ${highestLogData!!.recordedDate}")
        }
    }

    private var logReading1: LogReading? = null
    fun getLogReading1(): LogReading?
    {
        return logReading1
    }
    fun setLogReading1(value: LogReading)
    {
        logReading1 = value
        createCoreDataRecord(value, TibiisReadingType.logging, "PE")
    }

    private var logReading2: LogReading? = null
    fun getLogReading2(): LogReading?
    {
        return logReading2
    }
    fun setLogReading2(value: LogReading)
    {
        logReading2 = value
        createCoreDataRecord(value, TibiisReadingType.logging, "PE")
    }

    private var logReading3: LogReading? = null
    fun getLogReading3(): LogReading?
    {
        return logReading3
    }
    fun setLogReading3(value: LogReading)
    {
        logReading3 = value
        createCoreDataRecord(value, TibiisReadingType.logging, "PE")
    }

    private var logDIReading15: LogReading? = null
    fun getLogDIReading15(): LogReading?
    {
        return logDIReading15
    }
    fun setLogDIReading15(value: LogReading)
    {
        logDIReading15 = value
        createCoreDataRecord(value, TibiisReadingType.logging, "DI")
    }

    private var logDIReading60: LogReading? = null
    fun getLogDIReading60(): LogReading?
    {
        return logDIReading60
    }
    fun setLogDIReading60(value: LogReading)
    {
        logDIReading60 = value
        createCoreDataRecord(value, TibiisReadingType.logging, "DI")
    }

    private var logNumberForStart = -1
    fun getLogNumberForStart(): Int
    {
        return logNumberForStart
    }
    fun setLogNumberForStart(value: Int)
    {
        logNumberForStart = value
        AppGlobals.instance.activeProcess.tibsessLogNumberForStart = value
    }

    private var logNumberForStartDI = -1
    fun getLogNumberForStartDI(): Int
    {
        return logNumberForStartDI
    }
    fun setLogNumberForStartDI(value: Int)
    {
        logNumberForStartDI = value
        AppGlobals.instance.activeProcess.tibsessDILogNumberForStart = value
    }

    private var logNumberForReading1 = -1
    fun getLogNumberForReading1(): Int
    {
        return logNumberForReading1
    }
    fun setLogNumberForReading1(value: Int)
    {
        logNumberForReading1 = value
        AppGlobals.instance.activeProcess.tibsessLogNumberForReading1 = value
    }

    private var logNumberForReading2 = -1
    fun getLogNumberForReading2(): Int
    {
        return logNumberForReading2
    }
    fun setLogNumberForReading2(value: Int)
    {
        logNumberForReading2 = value
        AppGlobals.instance.activeProcess.tibsessLogNumberForReading2 = value
    }

    private var logNumberForReading3 = -1
    fun getLogNumberForReading3(): Int
    {
        return logNumberForReading3
    }
    fun setLogNumberForReading3(value: Int)
    {
        logNumberForReading3 = value
        AppGlobals.instance.activeProcess.tibsessLogNumberForReading3 = value
    }

    private var logNumberForR15 = -1
    fun getLogNumberForR15(): Int
    {
        return logNumberForR15
    }
    fun setLogNumberForR15(value: Int)
    {
        logNumberForR15 = value
        AppGlobals.instance.activeProcess.tibsessLogR15 = value
    }

    private var logNumberForR60 = -1
    fun getLogNumberForR60(): Int
    {
        return logNumberForR60
    }
    fun setLogNumberForR60(value: Int)
    {
        logNumberForR60 = value
        AppGlobals.instance.activeProcess.tibsessLogR60 = value
    }


    private var firstLoggingReading: LogReading? = null
    fun getFirstLoggingReading(): LogReading?
    {
        return firstLoggingReading
    }
    fun setFirstLoggingReading(value: LogReading)
    {
        firstLoggingReading = value
        if (testingContext == TestingSessionData.TestingContext.pe)
        {
            if (firstLoggingReading != null && logNumberForReading1 < 1 && logNumberForReading2 < 1 && logNumberForReading3 < 1)
            {
                setLogNumberForStart(highestLogData!!.logReading.logNumber)

                // Calc the log numbers for each reading
                Log.d("Cobalt", "Calculating Log Numbers - First Log Number: $logNumberForStart")
                val loggingStarted = highestLogData!!.recordedDate
                val r1time = DateHelper.dbStringToDate(AppGlobals.instance.activeProcess.pt_reading1_time, DateHelper.date1970())
                val r2time = DateHelper.dbStringToDate(AppGlobals.instance.activeProcess.pt_reading2_time, DateHelper.date1970())
                val r3time = DateHelper.dbStringToDate(AppGlobals.instance.activeProcess.pt_reading3_time, DateHelper.date1970())

                val timeDiff1 = (r1time.time - loggingStarted.time) / 1000.0
                val timeDiff2 = (r2time.time - loggingStarted.time) / 1000.0
                val timeDiff3 = (r3time.time - loggingStarted.time) / 1000.0

                setLogNumberForReading1(logNumberForStart + timeDiff1.toInt())
                setLogNumberForReading2(logNumberForStart + timeDiff2.toInt())
                setLogNumberForReading3(logNumberForStart + timeDiff3.toInt())

                printReadingLogNumbers()
            }
        }

        if (testingContext == TestingSessionData.TestingContext.di)
        {
            if (firstLoggingReading != null && logNumberForR15 < 1 && logNumberForR60 < 1)
            {
                setLogNumberForStartDI(firstLoggingReading!!.logNumber)

                Log.d("Cobalt", "Calculating Log Numbers for DI")

                // Double check the pressurising started date
                if (AppGlobals.instance.activeProcess.pt_di_pressurising_started.length < 4)
                {
                    AppGlobals.instance.activeProcess.pt_di_pressurising_started = DateHelper.dateToDBString(Date())
                }

                val loggingStarted = DateHelper.dbStringToDate(AppGlobals.instance.activeProcess.pt_di_pressurising_started, DateHelper.date1970())
                val r15time = DateHelper.dbStringToDate(AppGlobals.instance.activeProcess.pt_di_r15_time, DateHelper.date1970())
                val r60time = DateHelper.dbStringToDate(AppGlobals.instance.activeProcess.pt_di_r60_time, DateHelper.date1970())
                val timeDiff15 = (r15time.time - loggingStarted.time) / 1000.0
                val timeDiff60 = (r60time.time - loggingStarted.time) / 1000.0
                setLogNumberForR15(logNumberForStartDI + timeDiff15.toInt())
                setLogNumberForR60(logNumberForStartDI + timeDiff60.toInt())

                printReadingLogNumbersDI()

                // Save the first value as the stp
                val press = firstLoggingReading!!.pressure
                if (!isDIConditioning)
                {
                    val convertedPress = press.toDouble() / 1000.0
                    AppGlobals.instance.activeProcess.pt_di_stp = convertedPress
                    AppGlobals.instance.activeProcess.pt_di_start_pressure = convertedPress
                }
            }
        }
    }

    var numberOfReading = -1
    var peReading1MissedByLogger = false
    var peReading2MissedByLogger = false
    var peReading3MissedByLogger = false

    fun resetTibiisSesssionData()
    {
        lastReading = null
        startPressureReading = null
        loggingDurationSet = false
        lcdOnTimeSet = false
        hasCalculated = false
        firstLoggingReading = null
        logReading1 = null
        logReading2 = null
        logReading3 = null
        logDIReading15 = null
        logDIReading60 = null
        setLogNumberForStart(-1)
        setLogNumberForStartDI(-1)
        setLogNumberForReading1(-1)
        setLogNumberForReading2(-1)
        setLogNumberForReading3(-1)
        setLogNumberForR15(-1)
        setLogNumberForR60(-1)
        isDIConditioning = false
    }

    fun printReadingLogNumbers()
    {
        Log.d("Cobalt", "TibiisSession: Log Number for Reading 1 = $logNumberForReading1")
        Log.d("Cobalt", "TibiisSession: Log Number for Reading 2 = $logNumberForReading2")
        Log.d("Cobalt", "TibiisSession: Log Number for Reading 3 = $logNumberForReading3")
    }

    fun printReadingLogNumbersDI()
    {
        Log.d("Cobalt", "TibiisSession: Log Number for Reading 15 = $logNumberForR15")
        Log.d("Cobalt", "TibiisSession: Log Number for Reading 60 = $logNumberForR60")
    }

    fun addReading(pressureReading: LogReading, readingType: TibiisReadingType, timerStage: Int = 0, isReadingForStage: Int= -1)
    {
        if (hasCalculated)
        {
            Log.d("Cobalt", "Ignore reading save - is after calculation")
            return
        }

        if (testingContext == TestingSessionData.TestingContext.pe)
        {
            if (pressureReading.logNumber >= logNumberForReading1 && pressureReading.logNumber <= logNumberForReading3)
            {
                createCoreDataRecord(pressureReading, TibiisReadingType.logging, testingContext.value)
            }
            else
            {
                createCoreDataRecord(pressureReading, readingType, testingContext.value)
            }
        }

        if (testingContext == TestingSessionData.TestingContext.di)
        {
            if (AppGlobals.instance.activeProcess.di_test_has_calculated == 0)
            {
                if (pressureReading.logNumber >= logNumberForR15 && pressureReading.logNumber <= logNumberForR60)
                {
                    createCoreDataRecord(pressureReading, TibiisReadingType.logging, testingContext.value)
                }
                else
                {
                    createCoreDataRecord(pressureReading, readingType, testingContext.value)
                }
            }
        }
    }

    fun createCoreDataRecord(logReading: LogReading, readingType: TibiisReadingType, testType: String): EXLDTibiisReading
    {
        var typeDescription = ""
        when (readingType)
        {
            TibiisReadingType.afterLogging -> typeDescription = "After Logging"
            TibiisReadingType.beforePressurising -> typeDescription = "BeforePressurising"
            TibiisReadingType.logging -> typeDescription = "Logging"
            TibiisReadingType.pressurising -> typeDescription = "Pressurising"
        }

        var tibiisReading = EXLDTibiisReading()
        tibiisReading.battery = logReading.battery
        tibiisReading.logNumber = logReading.logNumber
        tibiisReading.pressure = logReading.pressure
        tibiisReading.flowRate = logReading.flowRate
        tibiisReading.readingType = typeDescription
        tibiisReading.processId = AppGlobals.instance.activeProcess.columnId
        tibiisReading.testType = testType
        tibiisReading.save(AppGlobals.instance.tibiisController.appContext!!)

        if (testType == "PE")
        {
            AppGlobals.instance.activeProcess.peNeedsUploading = 1
        }
        else
        {
            AppGlobals.instance.activeProcess.diNeedsUploading = 1
        }

        return tibiisReading
    }

    fun resetMissedReadingFlags()
    {
        peReading1MissedByLogger = false
        peReading2MissedByLogger = false
        peReading3MissedByLogger = false
    }

    fun getReadingForLogNumber(logNumber: Int): LogReading?
    {
        var logs = AppGlobals.instance.tibiisController.appContext!!.database.use {
            select(EXLDTibiisReading.TABLE_NAME).whereArgs(EXLDTibiisReading.COLUMN_LOG_NUMBER +
                    " = $logNumber AND processId = ${AppGlobals.instance.activeProcess.columnId} AND testType = '${testingContext.value}'").exec {
                parseList<EXLDTibiisReading>(classParser())
            }
        }

        if (logs.isNotEmpty())
        {
            val first = logs[0]
            val emptyDataArray = ArrayList<Int>()
            emptyDataArray.add(1)   // Just because I've had a crash with an empty array before, log reading only processes data if it has 7 or 10 values in it
            return LogReading(emptyDataArray, first)
        }

        return null
    }

    fun countReadingOfType(readingType: TibiisReadingType, processId: Long): Int
    {
        val readings = readingsOfType(readingType, processId)
        return  readings.size
    }

    fun readingsOfType(readingType: TibiisReadingType, processId: Long): List<EXLDTibiisReading>
    {
        var typeDescription = ""
        when (readingType)
        {
            TibiisReadingType.afterLogging -> typeDescription = "After Logging"
            TibiisReadingType.beforePressurising -> typeDescription = "BeforePressurising"
            TibiisReadingType.logging -> typeDescription = "Logging"
            TibiisReadingType.pressurising -> typeDescription = "Pressurising"
        }

        val db = AppGlobals.instance.tibiisController.appContext!!.database
        val whereString = "${EXLDTibiisReading.COLUMN_PROCESS_ID} = $processId AND testType='${testingContext.value}' AND ${EXLDTibiisReading.COLUMN_READING_TYPE} = '$typeDescription'"
        var readings = db.use {
            select(EXLDTibiisReading.TABLE_NAME).whereArgs(whereString).exec {
                parseList<EXLDTibiisReading>(classParser())
            }
        }

        return readings
    }

    fun maxLogNumberReceived(processId: Long): Int
    {
        val db = AppGlobals.instance.tibiisController.appContext!!.database
        val where = "${EXLDTibiisReading.COLUMN_PROCESS_ID} = $processId AND testType='${testingContext.value}'"
        var maxlogNumber = 0
        db.use {
            select(EXLDTibiisReading.TABLE_NAME, "MAX(${EXLDTibiisReading.COLUMN_LOG_NUMBER}) as max").whereArgs(where).exec {
                moveToNext()
                maxlogNumber = getInt(getColumnIndex("max"))
            }
        }

        return maxlogNumber
    }

    fun maxLogLessThanSuppliedLogNumner(processId: Long, currentLogNumber: Int): Int
    {
        val db = AppGlobals.instance.tibiisController.appContext!!.database
        val where = "${EXLDTibiisReading.COLUMN_PROCESS_ID} = $processId AND testType='${testingContext.value}' AND ${EXLDTibiisReading.COLUMN_LOG_NUMBER} < $currentLogNumber"
        var maxlogNumber = 0
        db.use {
            select(EXLDTibiisReading.TABLE_NAME, "MAX(${EXLDTibiisReading.COLUMN_LOG_NUMBER}) as max").whereArgs(where).exec {
                moveToNext()
                maxlogNumber = getInt(getColumnIndex("max"))
            }
        }

        return maxlogNumber
    }

}