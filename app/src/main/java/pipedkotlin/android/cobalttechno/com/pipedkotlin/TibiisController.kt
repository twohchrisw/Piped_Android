package pipedkotlin.android.cobalttechno.com.pipedkotlin

class TibiisController {

    enum class ConnectionStatus
    {
        notConnected, connecting, connected
    }

    var connectStatus = ConnectionStatus.notConnected
    var connectToTibiisAfterStatusUpdate = false

}