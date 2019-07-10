package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_testing_acitivty.*
import java.text.DecimalFormat
import java.util.*
import kotlin.concurrent.schedule

fun TestingActivity.setPressurisingButtonText(timerText: String)
{
    runOnUiThread {
        if (!isPressurisingDI)
        {
            // Doesn't like us changing the button text, makes the button unusable
            //var buttonString = "$BUTTON_TEXT_STOP_PRESS $timerText"
            //btnAction.text = buttonString
            tvPressurisingLabel.text = "Pressurising [$timerText]"
        }
        else
        {

            var buttonString = "Pressurising: $timerText"
            btnAction.text = buttonString
        }
    }
}

fun TestingActivity.hideTopContent()
{
    Log.d("Cobalt", "PANEL: hideTopContent()")
    runOnUiThread {
        btnAction.alpha = 0.0f
        //tvCountdown.alpha = 0.0f
        pvActivity.alpha = 0.0f
        //progCountdown.alpha = 0.0f
        linCountdown.visibility = View.GONE
        pvActivity.alpha = 0.0f
        tvPressurisingLabel.alpha = 0.0f
    }
}

fun TestingActivity.formatForBatteryReading(reading: Int)
{
    Log.d("Cobalt", "PANEL: formatForBatteryReading()")
    runOnUiThread {
        ivBattery.alpha = 1.0f
        tvBattery.alpha = 1.0f
        tvBattery.text = "$reading%"
    }
}

fun TestingActivity.formatForReadyToPressurise()
{
    Log.d("Cobalt", "PANEL: formatForReadyToPressurise()")
    hideTopContent()

    runOnUiThread {
        btnAction.alpha = 1.0f
        btnAction.text = "$BUTTON_TEXT_START_PRESS"
        btnConnect.isEnabled = true
        tvPressurisingLabel.alpha = 1.0f
        tvPressurisingLabel.text = ""
    }
}

fun TestingActivity.formatActionPanelForPressurising()
{
    Log.d("Cobalt", "PANEL: formatActionPanelForPressurising()")
    hideTopContent()

    runOnUiThread {
        btnAction.alpha = 1.0f
        btnConnect.isEnabled = true
        btnAction.setText(BUTTON_TEXT_STOP_PRESS)
        linPressurising.alpha = 1.0f
        tvPressurisingLabel.alpha = 1.0f
        tvPressurisingLabel.text = "Pressurising . . ."
        linWaitingForReading.alpha = 0.0f
        linCountdown.visibility = View.GONE
        //tvPressureValueLabel.alpha = 0.0f
        pvActivity.alpha = 1.0f
    }
}

fun TestingActivity.formatActionPanelForCountdown(readingTime: Date, earlierTime: Date, countdownText: String, progressText: String)
{
    Log.d("Cobalt","PANEL: formatActionPanelForCountdown")

    runOnUiThread {
        btnAction.alpha = 0.0f
        linPressurising.alpha = 1.0f
        tvPressurisingLabel.alpha = 0.0f
        tvPressurisingLabel.text = "countdown test"

        linWaitingForReading.alpha = 1.0f
        tvWaiting.text = progressText

        linCountdown.visibility = View.VISIBLE
        tvCountdown.text = countdownText
        progCountdown.alpha = 1.0f
        tvCountdown.alpha = 1.0f

        if (!isDownloadingPreviousData) {
            pvActivity.alpha = 0.0f
        } else {
            pvActivity.alpha = 1.0f
        }

        val totalTimeDiff = readingTime.time - earlierTime.time
        val currentTimeDiff = readingTime.time - Date().time
        val timeLeft = totalTimeDiff - currentTimeDiff
        val progValue = (timeLeft.toDouble() / totalTimeDiff.toDouble()) * 100.0
        progCountdown.progress = progValue.toInt() + 1

        // Testing
        val countdownAlpha = linCountdown.alpha
        val countdownVis = linCountdown.visibility == View.VISIBLE
        val tvcountdownAlpha = tvCountdown.alpha
        val tvcountdownVis = tvCountdown.visibility == View.VISIBLE
        val progcountdown = progCountdown.alpha
        val progcountdownVis = progCountdown.visibility == View.VISIBLE

        val actionAlpha = btnAction.alpha
        val actionVis = btnAction.visibility == View.VISIBLE
        val linpressAlpha = linPressurising.alpha
        val linpressVis = linPressurising.visibility == View.VISIBLE
        val linwaitAlpha = linWaitingForReading.alpha
        val linwaitVis = linWaitingForReading.visibility == View.VISIBLE
        val presslabelAlpha = tvPressurisingLabel.alpha
        val presslabelVis = tvPressureValueLabel.visibility == View.VISIBLE
        val pressValueLabelAlpha = tvPressureValueLabel.alpha
        val pressValueLabelvis = tvPressureValueLabel.visibility == View.VISIBLE

        val stop = tvPressureValueLabel.visibility

    }
}

fun TestingActivity.printControlStatus()
{

}

fun TestingActivity.formatForWaitingToCalculate()
{
    Log.d("Cobalt", "PANEL: formatForWaitingToCalculate()")
    runOnUiThread {
        btnConnect.isEnabled = true
        tvCountdown.alpha = 0.0f
        tvPressurisingLabel.alpha = 1.0f
        tvPressurisingLabel.text = "Ready to Calculate"
        pvActivity.alpha = 0.0f
        linPressurising.alpha = 1.0f
        progCountdown.alpha = 0.0f
    }
}

fun TestingActivity.formatActionPanelForCalculate()
{
    Log.d("Cobalt", "PANEL: formatForCalculate()")
    runOnUiThread {
        btnAction.alpha = 1.0f
        btnAction.setText(BUTTON_TEXT_CALCULATE)
        linPressurising.alpha = 0.0f
        linWaitingForReading.alpha = 0.0f
        linCountdown.visibility = View.GONE
        tvPressureValueLabel.alpha = 0.0f
    }
}

fun TestingActivity.formatForCheckingIntegrity()
{
    Log.d("Cobalt", "PANEL: formatForCheckingIntegrity()")
    runOnUiThread {
        linPressurising.alpha = 1.0f
        tvPressurisingLabel.alpha = 1.0f
        pvActivity.alpha = 1.0f
        tvPressurisingLabel.text = "Checking Log Integrity"
    }
}

fun TestingActivity.displayPreviousReadingData(message: String)
{
    Log.d("Cobalt", "displayPreviousReadingData")
    Timer("previousReadings", false).schedule(2000) {
        turnOffPreviousReadings()
    }

    runOnUiThread {
        lastPreviousReading = Date()
        tvPressurisingLabel.text = message
        tvPressurisingLabel.alpha = 1.0f
        linPressurising.alpha = 1.0f
        pvActivity.alpha = 1.0f
        linWaitingForReading.alpha = 0.0f
    }
}

fun TestingActivity.turnOffPreviousReadings()
{
    Log.d("Cobalt", "turnOffPreviousReadings()")
    runOnUiThread {
        tvPressurisingLabel.text = ""
        tvPressurisingLabel.alpha = 0.0f
        //linPressurising.alpha = 0.0f
        pvActivity.alpha = 0.0f
        linWaitingForReading.alpha = 1.0f
    }
}

fun TestingActivity.formatForStartTest()
{
    Log.d("Cobalt", "PANEL: formatFoStartTest()")
    hideTopContent()

    runOnUiThread {
        btnAction.alpha = 1.0f
        btnConnect.isEnabled = true
        btnAction.text = BUTTON_TEXT_START_TEST
    }
}

fun TestingActivity.formatActionPanelForDefault()
{
    Log.d("Cobalt", "PANEL: formatActionPanelForDefault()")
    runOnUiThread {
        linPressurising.alpha = 1.0f
        linWaitingForReading.alpha = 0.0f
        linCountdown.visibility = View.GONE
        btnAction.alpha = 1.0f
        tvPressureValueLabel.text = ""
        tvPressureValueLabel.alpha = 0.0f
        tvPressurisingLabel.text = ""
        pvActivity.alpha = 0.0f
        tvBattery.text = "" // Don't mark this view as gone, it messes the layout up
        ivBattery.alpha = 0.0f

        if (testingSession.testingContext == TestingSessionData.TestingContext.pe)
        {
            btnAction.setText(BUTTON_TEXT_START_PRESS)
        }

        if (testingSession.testingContext == TestingSessionData.TestingContext.di)
        {
            btnAction.setText(BUTTON_TEXT_START_TEST)
        }
    }
}

fun TestingActivity.updatePressureGauge(value: Int, pressurising: Boolean, batteryReading: Int)
{
    //Log.d("Cobalt", "Update Pressure Gauge")
    val pressureInBar = value.toDouble() / 1000
    val formatter = DecimalFormat("0.000")
    val pressureFormatted = formatter.format(pressureInBar)

    runOnUiThread {
        linPressurising.alpha = 1.0f
        linPressurising.visibility = View.VISIBLE
        tvPressureValueLabel.text = "${pressureFormatted} bar"
        tvPressureValueLabel.alpha = 1.0f
        tvPressureValueLabel.alpha = 1.0f

        if (pressurising)
        {
            tvPressurisingLabel.alpha = 1.0f
            pvActivity.alpha = 1.0f
        }
        else
        {
            tvPressurisingLabel.alpha = 0.0f
            pvActivity.alpha = 0.0f
        }
    }

    formatForBatteryReading(batteryReading)
}

fun TestingActivity.updatePressureGuageForZero()
{
    Log.d("Cobalt", "updatePressureGaugeForZero")
    runOnUiThread {
        tvPressureValueLabel.text = ""
        tvPressureValueLabel.alpha = 0.0f
        tvBattery.text = ""
        ivBattery.alpha = 0.0f
    }
}





