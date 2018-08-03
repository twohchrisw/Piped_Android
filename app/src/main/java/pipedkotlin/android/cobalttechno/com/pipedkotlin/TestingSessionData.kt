package pipedkotlin.android.cobalttechno.com.pipedkotlin

class TestingSessionData {

    var testingContext = TestingContext.none
    var hasCalculatedPETest = false
    var timerStage = 0

    enum class TestingContext(val value: String)
    {
        pe("PE"), di("DI"), none("")
    }
}