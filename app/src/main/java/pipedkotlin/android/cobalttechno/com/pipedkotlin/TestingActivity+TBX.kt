package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.util.Log

fun TestingActivity.downloadPreviousReadings(currentLogNumber: Int)
{
    //TODO: Needs implementing from TasksTesting+TBX
}

fun TestingActivity.saveLiveLog(logReading: LogReading, isPrevious: Boolean = false)
{
    if (logReading.logNumber < 1)
    {
        // Live Log Mode - Not Logging
        updatePressureGauge(logReading.pressure, false, logReading.battery)

        //TODO: DI Auto Pressurisation needed here

        return
    }


    //TODO: Needs completing
    if (isPrevious)
    {
        Log.d("Cobalt", "SAVE PREVIOUS LOG: ${logReading.logNumber} NOT IMPLEMENTED")
    }
    else {
        Log.d("Cobalt", "SAVE LIVE LOG: ${logReading.logNumber} NOT IMPLEMENTED")
    }
}