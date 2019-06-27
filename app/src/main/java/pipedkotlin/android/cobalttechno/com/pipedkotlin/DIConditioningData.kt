package pipedkotlin.android.cobalttechno.com.pipedkotlin

class DIConditioningData
{
    var currentRun = 0
    var runLosses = ArrayList<Double>()

    fun failMessage(): String
    {
        var failMessage = "Metallic Test conditioning failed with the following losses:\r\n\r\n"

        for (i in 0 until runLosses.size - 1)
        {
            val lossString = "Run ${i + 1}: ${runLosses[i]}"
            failMessage = "$failMessage\r\n$lossString"
        }

        return failMessage
    }
}