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
            var buttonString = "$BUTTON_TEXT_STOP_PRESS $timerText"
            btnAction.text = buttonString
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
    runOnUiThread {
        btnAction.visibility = View.GONE
        tvCountdown.visibility = View.GONE
        pvActivity.visibility = View.GONE
        progCountdown.visibility = View.GONE
        pvActivity.visibility = View.GONE
        tvPressurisingLabel.visibility = View.GONE
    }
}

fun TestingActivity.formatForBatteryReading(reading: Int)
{
    runOnUiThread {
        ivBattery.visibility = View.VISIBLE
        tvBattery.visibility = View.VISIBLE
        tvBattery.text = "$reading%"
    }
}

fun TestingActivity.formatForReadyToPressurise()
{
    hideTopContent()

    runOnUiThread {
        btnAction.visibility = View.VISIBLE
        btnAction.text = "$BUTTON_TEXT_START_PRESS"
        btnConnect.isEnabled = true
        tvPressurisingLabel.visibility = View.VISIBLE
    }
}

fun TestingActivity.formatActionPanelForPressurising()
{
    hideTopContent()

    runOnUiThread {
        btnAction.visibility = View.VISIBLE
        btnConnect.isEnabled = true
        btnAction.setText(BUTTON_TEXT_STOP_PRESS)
        linPressurising.visibility = View.VISIBLE
        tvPressurisingLabel.visibility = View.VISIBLE
        linWaitingForReading.visibility = View.GONE
        linCountdown.visibility = View.GONE
        tvPressureValueLabel.visibility = View.GONE
        pvActivity.visibility = View.VISIBLE
    }
}

fun TestingActivity.formatActionPanelForCountdown(readingTime: Date, earlierTime: Date, countdownText: String, progressText: String)
{
    runOnUiThread {
        btnAction.visibility = View.GONE
        linPressurising.visibility = View.GONE
        linWaitingForReading.visibility = View.VISIBLE
        tvWaiting.setText(progressText)
        linCountdown.visibility = View.VISIBLE
        tvCountdown.text = countdownText

        val totalTimeDiff = readingTime.time - earlierTime.time
        val currentTimeDiff = readingTime.time - Date().time
        val timeLeft = totalTimeDiff - currentTimeDiff
        val progValue = (timeLeft.toDouble() / totalTimeDiff.toDouble()) * 100.0
        progCountdown.progress = progValue.toInt() + 1
    }
}

fun TestingActivity.formatForWaitingToCalculate()
{
    runOnUiThread {
        btnConnect.isEnabled = true
        tvCountdown.visibility = View.GONE
        tvPressurisingLabel.visibility = View.VISIBLE
        tvPressurisingLabel.text = "Ready to Calculate"
        pvActivity.visibility = View.GONE
        linPressurising.visibility = View.VISIBLE
        progCountdown.visibility = View.GONE
    }
}

fun TestingActivity.formatActionPanelForCalculate()
{
    runOnUiThread {
        btnAction.visibility = View.VISIBLE
        btnAction.setText(BUTTON_TEXT_CALCULATE)
        linPressurising.visibility = View.GONE
        linWaitingForReading.visibility = View.GONE
        linCountdown.visibility = View.GONE
        tvPressureValueLabel.visibility = View.GONE
    }
}

fun TestingActivity.formatForCheckingIntegrity()
{
    runOnUiThread {
        linPressurising.visibility = View.VISIBLE
        tvPressurisingLabel.visibility = View.VISIBLE
        pvActivity.visibility = View.VISIBLE
        tvPressurisingLabel.text = "Checking Log Integrity"
    }
}

fun TestingActivity.displayPreviousReadingData(message: String)
{
    Timer("previousReadings", false).schedule(2000) {
        turnOffPreviousReadings()
    }

    runOnUiThread {
        lastPreviousReading = Date()
        tvPressurisingLabel.text = message
        tvPressurisingLabel.visibility = View.VISIBLE
        linPressurising.visibility = View.VISIBLE
        pvActivity.visibility = View.VISIBLE
    }
}

fun TestingActivity.turnOffPreviousReadings()
{
    runOnUiThread {
        tvPressurisingLabel.text = "Pressurising . . ."
        tvPressurisingLabel.visibility = View.VISIBLE
        linPressurising.visibility = View.VISIBLE
        pvActivity.visibility = View.VISIBLE
    }
}

fun TestingActivity.formatForStartTest()
{
    hideTopContent()

    runOnUiThread {
        btnAction.visibility = View.VISIBLE
        btnConnect.isEnabled = true
        btnAction.text = BUTTON_TEXT_START_TEST
    }
}

fun TestingActivity.formatActionPanelForDefault()
{
    runOnUiThread {
        linPressurising.visibility = View.GONE
        linWaitingForReading.visibility = View.GONE
        linCountdown.visibility = View.GONE
        btnAction.visibility = View.VISIBLE
        tvPressureValueLabel.visibility = View.GONE
        tvBattery.text = "" // Don't mark this view as gone, it messes the layout up
        ivBattery.visibility = View.GONE

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
        linPressurising.visibility = View.VISIBLE
        tvPressureValueLabel.text = "${pressureFormatted} bar"
        tvPressureValueLabel.visibility = View.VISIBLE

        if (pressurising)
        {
            tvPressurisingLabel.visibility = View.VISIBLE
            pvActivity.visibility = View.VISIBLE
        }
        else
        {
            tvPressurisingLabel.visibility = View.GONE
            pvActivity.visibility = View.GONE
        }
    }

    formatForBatteryReading(batteryReading)
}

fun TestingActivity.updatePressureGuageForZero()
{
    runOnUiThread {
        tvPressureValueLabel.visibility = View.GONE
        tvBattery.visibility = View.GONE
        ivBattery.visibility = View.GONE
    }
}




