package pipedkotlin.android.cobalttechno.com.pipedkotlin

fun TestingActivity.setupTibiis()
{
    //TODO: Not fully implemented
    AppGlobals.instance.tibiisController.delegate = this
    AppGlobals.instance.tibiisController.testingContext = testingSession.testingContext
    AppGlobals.instance.tibiisController.appContext = this
    tibiisSession.testingContext = testingSession.testingContext

    if (AppGlobals.instance.tibiisController.connectStatus == TibiisController.ConnectionStatus.connected)
    {
        formatTibiisForConnected()
    }
    else
    {
        formatTibiisForNotConnected()
    }
}

fun TestingActivity.formatTibiisForConnected()
{
    tvConnectStatus.text = "Tibiis Connected"
    btnConnect.setText("Disconnect")
}

fun TestingActivity.reconnectTibiis()
{
    formatTibiisForConnected()
    AppGlobals.instance.tibiisController.commandSendAck()
}

fun TestingActivity.formatTibiisForNotConnected()
{
    tvConnectStatus.text = "Tibiis Not Connected"
    btnConnect.setText("Connect")
    //TODO: battery sync and so on as per TaskTesting+Tibiis#67
    AppGlobals.instance.tibiisController.hasSendDateSync = false
}

fun TestingActivity.formatTibiisForConnecting()
{
    tvConnectStatus.text = "Finding Tibiis . . ."
    btnConnect.setText("Disconnect")
}



