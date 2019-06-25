package pipedkotlin.android.cobalttechno.com.pipedkotlin



fun TibiisController.commandStartLogger(pumpEnabled: Boolean = true)
{
    if (connectStatus == TibiisController.ConnectionStatus.connected)
    {
        tbxDataController.sendCommandTimeSync()
        tbxDataController.sendCommandStartTest()
        tbxDataController.sendCommandOutputControl(pumpEnabled)
    }
}

fun TibiisController.commandStopLogger()
{
    if (connectStatus == TibiisController.ConnectionStatus.connected)
    {
        tbxDataController.sendCommandOutputControl(false)
        tbxDataController.sendCommandStopTest()
    }
}

