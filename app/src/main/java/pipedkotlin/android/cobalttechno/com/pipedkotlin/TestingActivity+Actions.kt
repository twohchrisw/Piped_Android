package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.util.Log
import java.util.*

fun TestingActivity.connectButtonTapped()
{

}

fun TestingActivity.actionButtonTapped()
{
    val buttonTitle = btnAction.text.toString()
    if (testingSession.testingContext == TestingSessionData.TestingContext.pe)
    {
        when (buttonTitle)
        {
            BUTTON_TEXT_START_PRESS -> startPressurisingButtonPressed()
            BUTTON_TEXT_STOP_PRESS -> stopPressurisingButtonPressed()
            BUTTON_TEXT_CALCULATE -> calculatePEButtonPressed()
        }
    }

    if (testingSession.testingContext == TestingSessionData.TestingContext.di)
    {
        when (buttonTitle)
        {
            BUTTON_TEXT_START_TEST -> startDITest()
            BUTTON_TEXT_CALCULATE -> calculateDIButtonPressed()
        }
    }
}





