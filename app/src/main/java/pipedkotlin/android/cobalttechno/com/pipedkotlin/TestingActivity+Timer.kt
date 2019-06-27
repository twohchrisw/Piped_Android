package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.util.Log

fun TestingActivity.saveReading1(pr: LogReading)
{
    val p = AppGlobals.instance.activeProcess
    if (p.pt_reading_1 > 0.0)
    {
        return
    }

    val press = pr.pressure.toDouble() / 1000.0
    p.pt_reading_1 = press
    tibiisSession.peReading1MissedByLogger = false
    tibiisSession.setLogReading1(pr)
    Log.d("Cobalt", "Add reading 1")
}

fun TestingActivity.saveReading2(pr: LogReading)
{
    val p = AppGlobals.instance.activeProcess
    if (p.pt_reading_2 > 0.0)
    {
        return
    }

    val press = pr.pressure.toDouble() / 1000.0
    p.pt_reading_2 = press
    tibiisSession.peReading2MissedByLogger = false
    tibiisSession.setLogReading2(pr)
    Log.d("Cobalt", "Add reading 2")
}

fun TestingActivity.saveReading3(pr: LogReading)
{
    val p = AppGlobals.instance.activeProcess
    if (p.pt_reading_3 > 0.0)
    {
        return
    }

    val press = pr.pressure.toDouble() / 1000.0
    p.pt_reading_3 = press
    tibiisSession.peReading3MissedByLogger = false
    tibiisSession.setLogReading3(pr)
    Log.d("Cobalt", "Add reading 3")
}


