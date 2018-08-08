package pipedkotlin.android.cobalttechno.com.pipedkotlin

class TestingSessionData {

    companion object {
        val DI_STAGE1_TIME = 60 * 15
        val DI_STAGE2_TIME = 60 * 60
        val PE_TESTING_UPPER_LIMIT = 1.25
        val PE_TESTING_LOWER_LIMIT = 0.7
    }

    var testingContext = TestingContext.none
    var hasCalculatedPETest = false
    var timerStage = 0
    var isPressurisingWithTibiis = false
    var isLoggingWithTibiis = false
    var isAmbientLoggingWithTibiis = false

    enum class TestingContext(val value: String)
    {
        pe("PE"), di("DI"), none("")
    }

    enum class LoggingMode()
    {
        pressurising, logging, waiting
    }

    var loggingMode = LoggingMode.waiting

    fun resetTestingSession()
    {
        //TODO: Not implemented
    }
}