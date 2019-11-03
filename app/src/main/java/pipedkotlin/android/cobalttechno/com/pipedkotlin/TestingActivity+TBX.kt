package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.os.Handler
import android.util.Log
import java.text.NumberFormat
import java.util.*
import kotlin.concurrent.schedule



fun TestingActivity.saveLiveLog(logReading: LogReading, isPrevious: Boolean = false)
{

    /* TEST STUFF */
/*

    if (logReading.logNumber > 10)
    {
        val log2 = tibiisSession.getReadingForLogNumber(2)
        if (log2 != null)
        {
            Log.d("Cobalt", "DB TEST: Reading 2: ${log2!!.description()}")
        }
    }

    val pressurisingCount = tibiisSession.countReadingOfType(TibiisSessionData.TibiisReadingType.pressurising, AppGlobals.instance.activeProcess.columnId)
    Log.d("Cobalt", "DB TeST Count of Pressurising: $pressurisingCount")

    val maxLog = tibiisSession.maxLogNumberReceived(AppGlobals.instance.activeProcess.columnId)
    Log.d("Cobalt", "DB TEST: Max Log = $maxLog")

    val maxLogButThis = tibiisSession.maxLogLessThanSuppliedLogNumner(AppGlobals.instance.activeProcess.columnId, logReading.logNumber)
    Log.d("Cobalt", "DB TEST: Max Log but this = $maxLogButThis")
*/

    //val maxLog = tibiisSession.maxLogNumberReceived(AppGlobals.instance.activeProcess.columnId)
    //Log.d("Cobalt", "DB TEST: Max Log = $maxLog")

    //val maxPressure = tibiisSession.getMaxPressurisingValue(AppGlobals.instance.activeProcess.columnId)
    //Log.d("Cobalt", "Max Pressurising valye = $maxPressure  THIS SHOULD BE > 0")

    /* END OF TEST STUFF */

    if (logReading.logNumber < 1)
    {
        // Live Log Mode - Not Logging
        updatePressureGauge(logReading.pressure, false, logReading.battery)
        Log.d("ditest", "Log recevied but not logging")

        //TODO: DI Auto Pressurisation needed here
        if (testingSession.isPressurisingDI)
        {
            val actualReading = logReading.pressure.toDouble() / 1000.0
            val stp = AppGlobals.instance.activeProcess.pt_di_stp
            if (actualReading >= stp)
            {
                endDIPressurisation()
            }
        }

        AppGlobals.instance.activeProcess.save(this)
        return
    }

    // Is this a previous test running when it sholdn't
    if (testingSession.loggingMode == TestingSessionData.LoggingMode.waiting)
    {
        runOnUiThread {
            AppGlobals.instance.tibiisController.tbxDataController.sendCommandStopTest()
        }
        Log.d("Cobalt", "Previous test running when it shouldn't - test stopped")
    }


    val logReceivedDate = Date()
    val process = AppGlobals.instance.activeProcess

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
        Log.d("Cobalt", "Save Live Log: ${logReading.description()} Context: ${testingSession.testingContext.value}")
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

    AppGlobals.instance.activeProcess.save(this)
}

fun TestingActivity.downloadPreviousReadings(currentLogNumber: Int)
{

    val lastLogButThis = tibiisSession.maxLogLessThanSuppliedLogNumner(AppGlobals.instance.activeProcess.columnId, currentLogNumber - 1)
    var startLogNumber = lastLogButThis + 1

    Log.d("zzz", "downloadPreviousReadings(currentLog: $currentLogNumber startLog: $startLogNumber)")
    if (startLogNumber < 1)
    {
        startLogNumber = 1
    }

    if (currentLogNumber > startLogNumber)
    {
        val numberOfLogs = currentLogNumber - startLogNumber
        previousDownloadStartLog = startLogNumber

        Log.d("zzz", "[Previous Download Start Log: $startLogNumber Current Log: $currentLogNumber Count: $numberOfLogs")
        if (numberOfLogs > MAX_PREVIOUS_LOGS)
        {
            lastPreviousLogRequired = currentLogNumber - 1
            runOnUiThread {
                Log.d("zzz", "About to send first command for logs [multiple commands necessary]")
                prev_download_cycle_start = Date().time
                PREVIOUS_LOG_REQUEST_START_LOG = startLogNumber // We use this with a timer to test if it was actually downloaded
                PREVIOUS_LOG_REQUEST_NUMBER_LOGS = MAX_PREVIOUS_LOGS
                AppGlobals.instance.tibiisController.tbxDataController.sendCommandFetchOldLogs(startLogNumber, MAX_PREVIOUS_LOGS)
            }
        }
        else
        {
            if (numberOfLogs > 0)
            {
                runOnUiThread {
                    Log.d("zzz", "About to send first command for logs single request only")
                    prev_download_cycle_start = Date().time
                    PREVIOUS_LOG_REQUEST_START_LOG = startLogNumber // We use this with a timer to test if it was actually downloaded
                    PREVIOUS_LOG_REQUEST_NUMBER_LOGS = numberOfLogs
                    AppGlobals.instance.tibiisController.tbxDataController.sendCommandFetchOldLogs(startLogNumber, numberOfLogs)
                }
            }
            else
            {
                isDownloadingPreviousData = false
            }
        }

        // Timers to check that the command returned values and if not to refire it
        /*
        if (PREVIOUS_LOG_REQUEST_START_LOG > -1 && PREVIOUS_LOG_REQUEST_NUMBER_LOGS > -1)
        Timer("checkPrevLogs1", false).schedule(2000) {
            runOnUiThread {
                if (PREVIOUS_LOG_REQUEST_START_LOG == startLogNumber) {
                    Log.d("zzz", "Refiring prev logs command for start log: $PREVIOUS_LOG_REQUEST_START_LOG")
                    AppGlobals.instance.tibiisController.tbxDataController.sendCommandFetchOldLogs(PREVIOUS_LOG_REQUEST_START_LOG, PREVIOUS_LOG_REQUEST_NUMBER_LOGS)
                }
            }
        }
        Timer("checkPrevLogs2", false).schedule(4000) {
            runOnUiThread {
                if (PREVIOUS_LOG_REQUEST_START_LOG == startLogNumber) {
                    Log.d("zzz", "Refiring prev logs command for start log: $PREVIOUS_LOG_REQUEST_START_LOG")
                    AppGlobals.instance.tibiisController.tbxDataController.sendCommandFetchOldLogs(PREVIOUS_LOG_REQUEST_START_LOG, PREVIOUS_LOG_REQUEST_NUMBER_LOGS)
                }
            }
        }
        Timer("checkPrevLogs1", false).schedule(6000) {
            runOnUiThread {
                if (PREVIOUS_LOG_REQUEST_START_LOG == startLogNumber) {
                    Log.d("zzz", "Refiring prev logs command for start log: $PREVIOUS_LOG_REQUEST_START_LOG")
                    AppGlobals.instance.tibiisController.tbxDataController.sendCommandFetchOldLogs(PREVIOUS_LOG_REQUEST_START_LOG, PREVIOUS_LOG_REQUEST_NUMBER_LOGS)
                }
            }
        }
        */


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

            /*
            runOnUiThread {
                AppGlobals.instance.tibiisController.tbxDataController.sendCommandFetchOldLogs(lastMaxLogNumber, MAX_PREVIOUS_LOGS)
            }
             */

            // If we run these previous log commands to close together I think it stops the live log timer from requesting a log and
            Timer("prevLog1", false).schedule(37) {
                runOnUiThread {
                    prev_download_cycle_start = Date().time
                    AppGlobals.instance.tibiisController.tbxDataController.sendCommandFetchOldLogs(lastMaxLogNumber, MAX_PREVIOUS_LOGS)
                }
            }
        }
        else
        {
            Log.d("zzz", "Requesting previous logs from $lastMaxLogNumber Number: $numberOfLogsRemaining is last")
            PREVIOUS_LOG_REQUEST_START_LOG = lastMaxLogNumber
            PREVIOUS_LOG_REQUEST_NUMBER_LOGS = numberOfLogsRemaining
            Timer("prevLog2", false).schedule(37) {
                runOnUiThread {
                    prev_download_cycle_start = Date().time
                    AppGlobals.instance.tibiisController.tbxDataController.sendCommandFetchOldLogs(lastMaxLogNumber, numberOfLogsRemaining)
                }
            }
            lastPreviousLogRequired = -1
        }

        // Timers to ensure that we refire the command in case it faults
        /*
        if (PREVIOUS_LOG_REQUEST_START_LOG > -1 && PREVIOUS_LOG_REQUEST_NUMBER_LOGS > -1)
        {
            Handler().postDelayed({
                if (PREVIOUS_LOG_REQUEST_START_LOG == lastMaxLogNumber) {
                    Log.d("zzz", "Refiring [1] prev logs command for start log: $PREVIOUS_LOG_REQUEST_START_LOG")
                    runOnUiThread {
                        AppGlobals.instance.tibiisController.tbxDataController.sendCommandFetchOldLogs(PREVIOUS_LOG_REQUEST_START_LOG, PREVIOUS_LOG_REQUEST_NUMBER_LOGS)
                    }
                }
            }, 900)

            Handler().postDelayed({
                if (PREVIOUS_LOG_REQUEST_START_LOG == lastMaxLogNumber) {
                    Log.d("zzz", "Refiring [2] prev logs command for start log: $PREVIOUS_LOG_REQUEST_START_LOG")
                    runOnUiThread {
                        AppGlobals.instance.tibiisController.tbxDataController.sendCommandFetchOldLogs(PREVIOUS_LOG_REQUEST_START_LOG, PREVIOUS_LOG_REQUEST_NUMBER_LOGS)
                    }
                }
            }, 1800)

            Handler().postDelayed({
                if (PREVIOUS_LOG_REQUEST_START_LOG == lastMaxLogNumber) {
                    Log.d("zzz", "Refiring [3] prev logs command for start log: $PREVIOUS_LOG_REQUEST_START_LOG")
                    runOnUiThread {
                        AppGlobals.instance.tibiisController.tbxDataController.sendCommandFetchOldLogs(PREVIOUS_LOG_REQUEST_START_LOG, PREVIOUS_LOG_REQUEST_NUMBER_LOGS)
                    }
                }
            }, 2700)

            Handler().postDelayed({
                if (PREVIOUS_LOG_REQUEST_START_LOG == lastMaxLogNumber) {
                    Log.d("zzz", "Refiring [4] prev logs command for start log: $PREVIOUS_LOG_REQUEST_START_LOG")
                    runOnUiThread {
                        AppGlobals.instance.tibiisController.tbxDataController.sendCommandFetchOldLogs(PREVIOUS_LOG_REQUEST_START_LOG, PREVIOUS_LOG_REQUEST_NUMBER_LOGS)
                    }
                }
            }, 3200)
        }
        */



        /*
            Timer("checkPrevLogs1", false).schedule(2000) {
                runOnUiThread {

                }
            }

            Timer("checkPrevLogs2", false).schedule(4000) {
                runOnUiThread {
                    if (PREVIOUS_LOG_REQUEST_START_LOG == lastMaxLogNumber) {
                        Log.d("zzz", "Refiring [2] prev logs command for start log: $PREVIOUS_LOG_REQUEST_START_LOG")
                        AppGlobals.instance.tibiisController.tbxDataController.sendCommandFetchOldLogs(PREVIOUS_LOG_REQUEST_START_LOG, PREVIOUS_LOG_REQUEST_NUMBER_LOGS)
                    }
                }
            }

            Timer("checkPrevLogs3", false).schedule(6000) {
                runOnUiThread {
                    if (PREVIOUS_LOG_REQUEST_START_LOG == lastMaxLogNumber) {
                        Log.d("zzz", "Refiring [3] prev logs command for start log: $PREVIOUS_LOG_REQUEST_START_LOG")
                        AppGlobals.instance.tibiisController.tbxDataController.sendCommandFetchOldLogs(PREVIOUS_LOG_REQUEST_START_LOG, PREVIOUS_LOG_REQUEST_NUMBER_LOGS)
                    }
                }
            }

            Timer("checkPrevLogs4", false).schedule(8000) {
                runOnUiThread {
                    if (PREVIOUS_LOG_REQUEST_START_LOG == lastMaxLogNumber) {
                        Log.d("zzz", "Refiring [4] prev logs command for start log: $PREVIOUS_LOG_REQUEST_START_LOG")
                        AppGlobals.instance.tibiisController.tbxDataController.sendCommandFetchOldLogs(PREVIOUS_LOG_REQUEST_START_LOG, PREVIOUS_LOG_REQUEST_NUMBER_LOGS)
                    }
                }
            }
            */
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
    //TODO: Needs completing
    return true
}

