package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.os.Handler
import android.util.Log
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule



fun TestingActivity.saveLiveLog(logReading: LogReading, isPrevious: Boolean = false)
{

    /* Test - Ignore Reading 1 and 2 */
    /*
    if (logReading.logNumber == 1 || logReading.logNumber == 2)
    {
        Log.d("LogReading", "TEST IGNORE LOGNUMBER ${logReading.logNumber}")
        return
    }
    */
    /* End of Test */

    if (logReading.logNumber < 1)
    {
        // Live Log Mode - Not Logging
        updatePressureGauge(logReading.pressure, false, logReading.battery)
        Log.d("ditest", "Log recevied but not logging")

        if (testingSession.testingContext == TestingSessionData.TestingContext.di)
        {
            //Log.d("cobsep1", "Setting Log Reading before logging for DI Test.  Pressure ${logReading.pressure.toDouble()}")
            tibiisSession.lastReading = logReading
        }

        //TODO: DI Auto Pressurisation needed here
        if (testingSession.isPressurisingDI)
        {
            val actualReading = logReading.pressure.toDouble() / 1000.0
            val stp = appGlobals.activeProcess.pt_di_stp
            if (actualReading >= stp)
            {
                endDIPressurisation()
            }
        }

        appGlobals.activeProcess.save(this)
        return
    }

    // Is this a previous test running when it shouldn't
    if (testingSession.loggingMode == TestingSessionData.LoggingMode.waiting)
    {
        runOnUiThread {
            appGlobals.tibiisController.tbxDataController.sendCommandStopTest()
        }
        Log.d("Cobalt", "Previous test running when it shouldn't - test stopped")
    }


    val logReceivedDate = Date()
    val process = appGlobals.activeProcess

    // Set the pressurising start date
    if (process.pt_pressurising_start == "" && testingSession.loggingMode == TestingSessionData.LoggingMode.pressurising)
    {
        process.pt_pressurising_start = DateHelper.dateToDBString(logReceivedDate)
        tibiisSession.startPressureReading = logReading
        val pressure = logReading.pressure.toDouble() / 1000.0
        process.pt_start_pressure = pressure

        runOnUiThread {
            process.save(this)
        }

        loadData()
    }

    // Set the highest log reading
    if (testingSession.loggingMode == TestingSessionData.LoggingMode.pressurising)
    {
        if (tibiisSession.getHighestLogData() != null)
        {
            if (logReading.pressure >= tibiisSession.getHighestLogData()!!.logReading.pressure)
            {
                tibiisSession.setHighestLogData(HighestLogDataResult(logReading, logReceivedDate))
            }
        }
        else
        {
            tibiisSession.setHighestLogData(HighestLogDataResult(logReading, logReceivedDate))
        }
    }

    if (!isPrevious)
    {
        Log.d("LogReading", "Save Live Log: ${logReading.description()} Context: ${testingSession.testingContext.value}")
        tibiisSession.lastReading = logReading
    }

    // Store the first reading, setting this value will cause the reading log numbers to be calculated
    if (testingSession.getFirstLogReading() == null && !isPrevious)
    {
        testingSession.setFirstLogReading(logReceivedDate)
    }

    // PE Only
    if (testingSession.loggingMode == TestingSessionData.LoggingMode.pressurising && !isPrevious)
    {
        updatePressureGauge(logReading.pressure, true, logReading.battery)
        tibiisSession.addReading(logReading, TibiisSessionData.TibiisReadingType.pressurising)
        if (testingSession.getStartLoggingTime() == null)
        {
            testingSession.setStartLoggingTime(logReceivedDate)
        }
        testingSession.setLastLoggingTime(logReceivedDate)

        // Have we reached the stp pressure
        if (testingSession.isAutoPumpAutomaticStopPressurisationEnabled)
        {
            val actualReading = logReading.pressure.toDouble() / 1000.0
            val stp = process.pt_system_test_pressure
            if (actualReading > stp)
            {
                stopPressurisingButtonPressed()
            }
        }
    }

    // Logging Only
    if (testingSession.loggingMode == TestingSessionData.LoggingMode.logging)
    {
        if (tibiisSession.getFirstLoggingReading() == null && !isPrevious)
        {
            if (tibiisSession.getHighestLogData() != null)
            {
                val h = tibiisSession.getHighestLogData()!!
                Log.d("Cobalt", "Highest recorded log: ${h.recordedDate} L${h.logReading.logNumber}:P${h.logReading.pressure}")
            }
            else
            {
                Log.d("Cobalt", "No highest log")
            }

            tibiisSession.setFirstLoggingReading(logReading)
        }

        if (!isPrevious)
        {
            updatePressureGauge(logReading.pressure, false, logReading.battery)

            if (!isCheckingIntegrity)
            {
                //turnOffPreviousReadings()
            }
        }
        else
        {
            if (!tibiisSession.hasCalculated)
            {
                //Log.d("Cobalt", "[Previous Log: ${logReading.description()}")
                //Log.d("cobpr", "[Previous Log: ${logReading.description()}")

                // Calculate the rough percentage
                var downloadMessage = "Downloading . . ."
                if (tibiisSession.lastReading != null)
                {
                    val lastActualLog = tibiisSession.lastReading!!.logNumber
                    val thisLog = logReading.logNumber

                    if (lastActualLog > 0 && thisLog > 0)
                    {
                        //val percentage = (thisLog.toDouble() / lastActualLog.toDouble()) * 100

                        val percentage = ((thisLog.toDouble() - previousDownloadStartLog.toDouble()) / (lastPreviousLogRequired.toDouble() - previousDownloadStartLog.toDouble())) * 100
                        val calcString = "((${thisLog.toDouble()} - ${previousDownloadStartLog.toDouble()}) / (${lastPreviousLogRequired.toDouble()} - ${previousDownloadStartLog.toDouble()})) * 100 = ${percentage}"
                        Log.d("zzz", "% Calc $calcString")
                        val percentageString = percentage.formatForDecPlaces(0)
                        if (percentage > 0 && percentage < 101.0) {
                            downloadMessage = "Downloading ${percentageString}% . . ."
                        }
                    }
                }

                if (!isCheckingIntegrity)
                {
                    displayPreviousReadingData(downloadMessage)
                }
            }
        }

        // Catch the actual reading values from previous
        if (testingSession.testingContext == TestingSessionData.TestingContext.pe)
        {
            if (logReading.logNumber == tibiisSession.getLogNumberForReading1())
            {
                saveReading1(logReading)
                loadData()
                Log.d("petest", "Reading 1 saved by log number ${logReading.logNumber}")
            }

            if (logReading.logNumber == tibiisSession.getLogNumberForReading2())
            {
                saveReading2(logReading)
                loadData()
                Log.d("petest", "Reading 2 saved by log number ${logReading.logNumber}")
            }

            if (logReading.logNumber == tibiisSession.getLogNumberForReading3())
            {
                saveReading3(logReading)
                loadData()
                Log.d("petest", "Reading 3 saved by log number ${logReading.logNumber}")
            }

            // Save the reading to the tibiis session
            if (logReading.logNumber > tibiisSession.getLogNumberForReading3())
            {
                tibiisSession.addReading(logReading, TibiisSessionData.TibiisReadingType.afterLogging)
            }
            else if (logReading.logNumber < tibiisSession.getLogNumberForStart())
            {
                tibiisSession.addReading(logReading, TibiisSessionData.TibiisReadingType.pressurising)
            }
            else
            {
                tibiisSession.addReading(logReading, TibiisSessionData.TibiisReadingType.logging)
            }
        }

        if (testingSession.testingContext == TestingSessionData.TestingContext.di)
        {
            if (logReading.logNumber == tibiisSession.getLogNumberForR15())
            {
                saveReading15(logReading)
                loadData()
            }

            if (logReading.logNumber == tibiisSession.getLogNumberForR60())
            {
                saveReading60(logReading)
                loadData()
            }

            if (logReading.logNumber > tibiisSession.getLogNumberForR60())
            {
                tibiisSession.addReading(logReading, TibiisSessionData.TibiisReadingType.afterLogging)
            }
            else
            {
                tibiisSession.addReading(logReading, TibiisSessionData.TibiisReadingType.logging)
            }

            if (testingSession.getStartLoggingTime() == null)
            {
                testingSession.setStartLoggingTime(logReceivedDate)
            }

            testingSession.setLastLoggingTime(logReceivedDate)
        }

        if (testingSession.loggingMode == TestingSessionData.LoggingMode.waiting)
        {
            updatePressureGauge(logReading.pressure, false, logReading.battery)
            tibiisSession.addReading(logReading, TibiisSessionData.TibiisReadingType.beforePressurising)
            testingSession.setLastLoggingTime(logReceivedDate)
        }
    }

    appGlobals.activeProcess.save(this)
}

fun TestingActivity.downloadPreviousReadings(currentLogNumber: Int)
{

    val lastLogButThis = tibiisSession.maxLogLessThanSuppliedLogNumner(appGlobals.activeProcess.columnId, currentLogNumber - 1)
    var startLogNumber = lastLogButThis + 1

    Log.d("LogReading", "downloadPreviousReadings(currentLog: $currentLogNumber startLog: $startLogNumber) lastLogButThis: $lastLogButThis")
    if (startLogNumber < 1)
    {
        startLogNumber = 1
    }

    // We don't need to download any logs after the the R3 log
    var lastLogToDownload = currentLogNumber + 1
    if (testingSession.testingContext == TestingSessionData.TestingContext.pe) {
        if (lastLogToDownload > tibiisSession.getLogNumberForReading3()) {
            lastLogToDownload = tibiisSession.getLogNumberForReading3() + 1
            Log.d("zzz", "Resetting max log to download from ${currentLogNumber} to R3 Log ${lastLogToDownload}")
        }
        Log.d("zzz", "Log R3 is ${tibiisSession.getLogNumberForReading3()}")
    }

    if (testingSession.testingContext == TestingSessionData.TestingContext.di)
    {
        val r60Log = tibiisSession.getLogNumberForR60()
        Log.d("LogReading", "r60Log: ${r60Log}")
        if (lastLogToDownload > tibiisSession.getLogNumberForR60())
        {
            lastLogToDownload = tibiisSession.getLogNumberForR60() + 1
        }
    }

    if (lastLogToDownload > startLogNumber)
    {
        val numberOfLogs = lastLogToDownload - startLogNumber
        previousDownloadStartLog = startLogNumber

        Log.d("LogReading", "[Previous Download Start Log: $startLogNumber Max Log To Download: $lastLogToDownload Count: $numberOfLogs")
        if (numberOfLogs > MAX_PREVIOUS_LOGS)
        {
            lastPreviousLogRequired = lastLogToDownload
            runOnUiThread {
                Log.d("LogReading", "About to send first command for logs [multiple commands necessary]")
                prev_download_cycle_start = Date().time
                PREVIOUS_LOG_REQUEST_START_LOG = startLogNumber // We use this with a timer to test if it was actually downloaded
                PREVIOUS_LOG_REQUEST_NUMBER_LOGS = MAX_PREVIOUS_LOGS
                appGlobals.tibiisController.tbxDataController.sendCommandFetchOldLogs(startLogNumber, MAX_PREVIOUS_LOGS)
            }

            // Run a timer here to ensure the log has been downloaded, otherwise fire the command again
            Timer("prevLogCheck1", false).schedule(930) {
                runOnUiThread {
                    if (tibiisSession.getReadingForLogNumber(startLogNumber + 1) == null) {
                        Log.d("LogReading", "XXX We do not have first log number - refiring previous logs command 1")
                        appGlobals.tibiisController.tbxDataController.sendCommandFetchOldLogs(startLogNumber, MAX_PREVIOUS_LOGS)
                    }
                }
            }

            Timer("prevLogCheck2", false).schedule(1920) {
                runOnUiThread {
                    if (tibiisSession.getReadingForLogNumber(startLogNumber + 1) == null) {
                        Log.d("LogReading", "XXX We do not have first log number - refiring previous logs command 2")
                        appGlobals.tibiisController.tbxDataController.sendCommandFetchOldLogs(startLogNumber, MAX_PREVIOUS_LOGS)
                    }
                }
            }
        }
        else
        {
            if (numberOfLogs > 0)
            {
                runOnUiThread {
                    Log.d("LogReading", "About to send first command for logs single request only")
                    prev_download_cycle_start = Date().time
                    PREVIOUS_LOG_REQUEST_START_LOG = startLogNumber // We use this with a timer to test if it was actually downloaded
                    PREVIOUS_LOG_REQUEST_NUMBER_LOGS = numberOfLogs
                    appGlobals.tibiisController.tbxDataController.sendCommandFetchOldLogs(startLogNumber, numberOfLogs)
                }

                // Run timer again here
                Timer("prevLogCheck3", false).schedule(930) {
                    runOnUiThread {
                        if (tibiisSession.getReadingForLogNumber(startLogNumber + 1) == null) {
                            Log.d("LogReading", "XXX We do not have first log number - refiring previous logs command 1 (single)")
                            appGlobals.tibiisController.tbxDataController.sendCommandFetchOldLogs(startLogNumber, MAX_PREVIOUS_LOGS)
                        }
                    }
                }

                Timer("prevLogCheck4", false).schedule(1920) {
                    runOnUiThread {
                        if (tibiisSession.getReadingForLogNumber(startLogNumber + 1) == null) {
                            Log.d("LogReading", "XXX We do not have first log number - refiring previous logs command 2 (single)")
                            appGlobals.tibiisController.tbxDataController.sendCommandFetchOldLogs(startLogNumber, MAX_PREVIOUS_LOGS)
                        }
                    }
                }

            }
            else
            {
                isDownloadingPreviousData = false
            }
        }
    }
    else
    {
        isDownloadingPreviousData = false
    }
}

fun TestingActivity.continueProcessingPreviousLogs()
{

    if (lastPreviousLogRequired < 0)
    {
        if (isDownloadingPreviousData)
        {
            //turnOffPreviousReadings()
        }
        isDownloadingPreviousData = false
        return
    }

    Log.d("zzz", "continueProcessingPreviousLogs() Logs Required")

    if (lastMaxLogNumber < lastPreviousLogRequired)
    {
        Log.d("zzz", "Last max: $lastMaxLogNumber Last Previous Log: $lastPreviousLogRequired")
        val numberOfLogsRemaining = lastPreviousLogRequired - lastMaxLogNumber + 1

        if (numberOfLogsRemaining > MAX_PREVIOUS_LOGS)
        {
            Log.d("zzz", "Requesting previous logs from $lastMaxLogNumber Number: $MAX_PREVIOUS_LOGS")
            prev_download_cycle_start = Date().time
            PREVIOUS_LOG_REQUEST_START_LOG = lastMaxLogNumber
            PREVIOUS_LOG_REQUEST_NUMBER_LOGS = MAX_PREVIOUS_LOGS

            //Timer("prevLog1", false).schedule(157) {
           // }
            runOnUiThread {
                prev_download_cycle_start = Date().time
                appGlobals.tibiisController.tbxDataController.sendCommandFetchOldLogs(lastMaxLogNumber, MAX_PREVIOUS_LOGS)
            }
        }
        else
        {
            Log.d("zzz", "Requesting previous logs from $lastMaxLogNumber Number: $numberOfLogsRemaining is last")
            PREVIOUS_LOG_REQUEST_START_LOG = lastMaxLogNumber
            PREVIOUS_LOG_REQUEST_NUMBER_LOGS = numberOfLogsRemaining
            //Timer("prevLog2", false).schedule(157) {
            //}
            runOnUiThread {
                prev_download_cycle_start = Date().time
                appGlobals.tibiisController.tbxDataController.sendCommandFetchOldLogs(lastMaxLogNumber, numberOfLogsRemaining)
            }
            lastPreviousLogRequired = -1
        }
    }
    else
    {
        lastPreviousLogRequired = -1
        isDownloadingPreviousData = false
        //turnOffPreviousReadings()
    }
}

fun TestingActivity.checkLogIntegrity(): Boolean
{
    Log.d("feb", "Check Log Integrity")
    isCheckingIntegrity = true

    var lastLogNumber = tibiisSession.getLogNumberForReading3()
    if (tibiisSession.testingContext == TestingSessionData.TestingContext.di)
    {
        lastLogNumber = tibiisSession.getLogNumberForR60()
    }

    // Get the log numbers we should have
    val logNumbersWeShouldHave = (1..lastLogNumber).toList().toIntArray().toSet()

    // Get the log numbers we have
    val logNumbersWeHave = EXLDTibiisReading.getAllLogNumbers(this).toSet()
    val missingLogs: Array<Int> = logNumbersWeShouldHave.minus(logNumbersWeHave).toIntArray().toTypedArray().sortedArray()
    Log.d("feb", "Last Log: " + lastLogNumber)

    // Bunch the missing logs into groups
    if (lastLogNumber > 1 && missingLogs.count() > 0) {
        var missingGroups = ArrayList<Pair<Int, Int>>()
        var currentStart = missingLogs[0]
        var currentNumber = 1

        for (n in 1..(missingLogs.count() - 1))
        {
            val thisVal = missingLogs[n]
            if (thisVal == currentStart + currentNumber && currentNumber < MAX_PREVIOUS_LOGS)
            {
                currentNumber = currentNumber + 1
            }
            else
            {
                missingGroups.add(Pair(currentStart, currentNumber))
                currentNumber = 1
                currentStart = thisVal
            }
        }

        missingGroups.add(Pair(currentStart, currentNumber))

        runOnUiThread {
            formatForCheckingIntegrity()
        }

        isDownloadingPreviousData = true
        for (group in missingGroups)
        {
            Timer("prevLog2", false).schedule(300) {
                if (group.second < MAX_PREVIOUS_LOGS + 2)
                {
                    lastPreviousLogRequired = -1
                    Log.d("feb", "Integrity requesting logs from " + group.first + " count " + group.second)
                    runOnUiThread {
                        prev_download_cycle_start = Date().time
                        appGlobals.tibiisController.tbxDataController.sendCommandFetchOldLogs(group.first, group.second)
                    }
                }

                if (group == missingGroups.last())
                {
                    runOnUiThread {
                        Log.d("feb", "Integrity Check complete")
                        isCheckingIntegrity = false
                        hasCheckedIntegrity = true
                        appGlobals.activeProcess.iphone_sync_id = "yes"
                        isDownloadingPreviousData = false
                        formatActionPanelForCalculate()
                    }
                }
            }
        }
    }
    else
    {
        // We had all logs no need for integrity check
        runOnUiThread {
            isCheckingIntegrity = false
            hasCheckedIntegrity = true
            appGlobals.activeProcess.iphone_sync_id = "yes"
            isDownloadingPreviousData = false
            formatActionPanelForCalculate()
        }
    }



    return true
}

fun TestingActivity.logNumbersWeHave()
{

}

