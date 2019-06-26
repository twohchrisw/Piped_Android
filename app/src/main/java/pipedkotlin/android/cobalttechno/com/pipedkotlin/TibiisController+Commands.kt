package pipedkotlin.android.cobalttechno.com.pipedkotlin

import org.jetbrains.anko.runOnUiThread
import java.util.*
import kotlin.concurrent.schedule


fun TibiisController.commandStartLogger(pumpEnabled: Boolean = true)
{
    if (connectStatus == TibiisController.ConnectionStatus.connected)
    {

        appContext!!.runOnUiThread {
            tbxDataController.sendCommandTimeSync()
        }

        Timer("startTest", false).schedule(100) {
            appContext!!.runOnUiThread {
                tbxDataController.sendCommandStartTest()
            }
        }

        Timer("outputControlOn", false).schedule(200) {
            appContext!!.runOnUiThread {
                tbxDataController.sendCommandOutputControl(pumpEnabled)
            }
        }
    }
}

fun TibiisController.commandStopLogger()
{
    if (connectStatus == TibiisController.ConnectionStatus.connected)
    {
        appContext!!.runOnUiThread {
            tbxDataController.sendCommandOutputControl(false)
        }

        Timer("stopTest", false).schedule(100) {
            appContext!!.runOnUiThread {
                tbxDataController.sendCommandStopTest()
            }
        }

        Timer("stopTest", false).schedule(200) {
            appContext!!.runOnUiThread {
                tbxDataController.sendCommandStopTest()
            }
        }
    }
}

