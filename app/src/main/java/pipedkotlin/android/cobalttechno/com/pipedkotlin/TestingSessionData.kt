package pipedkotlin.android.cobalttechno.com.pipedkotlin

import java.util.*
import kotlin.concurrent.timer

class TestingSessionData {

    var testingContext = TestingContext.none
    var hasCalculatedPETest = false
    var timerStage = 0

    var pressurisationTimer: Timer? = null

    private var startLoggingTime: Date? = null
    fun getStartLoggingTime(): Date?
    {
        return  startLoggingTime
    }
    fun setStartLoggingTime(value: Date)
    {
        startLoggingTime = value
        AppGlobals.instance.activeProcess.testSessDILastLoggingTime = DateHelper.millisToDBString(value.time)
    }
    fun setStartLoggingTimeNull()
    {
        startLoggingTime = null
    }

    private var lastLoggingTime: Date? = null
    fun getLastLoggingTime(): Date?
    {
        return lastLoggingTime
    }
    fun setLastLoggingTime(value: Date)
    {
        lastLoggingTime = value
        AppGlobals.instance.activeProcess.testsessLastLoggingTime = DateHelper.millisToDBString(value.time)
    }
    fun setLastLoggingTimeNull()
    {
        lastLoggingTime = null
    }

    var isPressurisingWithTibiis = false
    var isLoggingWithTibiis = false
    var isAmbientLoggingWithTibiis = false
    var isAutoPumpAutomaticStopPressurisationEnabled = false

    // DI Conditioning
    var isPressurisingDI = false
    var isAutoPumpAutomaticStopDIEnabled = false
    var conditioningData = DIConditioningData()
    private var isDITestConditioning = false
    fun getIsDITestConditioning(): Boolean
    {
        return isDITestConditioning
    }
    fun setIsDITestConditioning(value: Boolean)
    {
        isDITestConditioning = value
        if (isDITestConditioning)
        {
            conditioningData = DIConditioningData()
        }
    }

    enum class TestingContext(val value: String)
    {
        pe("PE"), di("DI"), none("")
    }

    enum class LoggingMode()
    {
        pressurising, logging, waiting
    }

    var loggingMode = LoggingMode.waiting

    private var firstLogReading: Date? = null
    fun getFirstLogReading(): Date?
    {
        return firstLogReading
    }
    fun setFirstLogReading(value: Date)
    {
        firstLogReading = value
        AppGlobals.instance.activeProcess.testsessFirstLogReadingDate = DateHelper.millisToDBString(value.time)
    }

    companion object {
        val DI_STAGE1_TIME = 60 * 15
        val DI_STAGE2_TIME = 60 * 60
        val PE_TESTING_UPPER_LIMIT = 1.25
        val PE_TESTING_LOWER_LIMIT = 0.7
    }

    fun resetTestingSession()
    {
        hasCalculatedPETest = false
        timerStage = 0
        startLoggingTime = null
        lastLoggingTime = null
        isPressurisingWithTibiis = false
        isLoggingWithTibiis = false
        isAmbientLoggingWithTibiis = false
        loggingMode = LoggingMode.waiting
        firstLogReading = null
        isAutoPumpAutomaticStopPressurisationEnabled = false
        isDITestConditioning = false
        conditioningData = DIConditioningData()
        isPressurisingDI = false
        isAutoPumpAutomaticStopDIEnabled = false
    }


}