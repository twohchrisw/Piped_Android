package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.jetbrains.anko.db.classParser
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

interface CommsManagerDelegate {
    fun didReceiveXMLDocument(rawXML: String)
    fun didReceiveVolleyError(error: VolleyError?)
}

class CommsManager(delegate: CommsManagerDelegate) {

    val delegate = delegate
    val SERVICE_DOMAIN = "http://pipedapp-001-site1.dtempurl.com/"
    public final val WEBSERVER_VALIDATE_COMPANY_ID = "${SERVICE_DOMAIN}irateservice.asmx/GetCompanyIDCount2?company_id="
    public final val WEBSERVER_GET_LIST_ITEMS = "${SERVICE_DOMAIN}irateservice.asmx/GetAllListItems"
    public final val WEBSERVER_GET_CLIENTS = "${SERVICE_DOMAIN}irateservice.asmx/GetAllClients"
    public final val WEBSERVER_GET_MODULE_PREFS = "${SERVICE_DOMAIN}irateservice.asmx/GetExcelModulePreferences?company_id="

    // Retrieves an XML file from the server
    public fun getXMLDocument(urlString: String, context: Context)
    {
        val stringRequest = StringRequest(urlString,
                Response.Listener<String> { rawXML ->
                   delegate.didReceiveXMLDocument(rawXML)
               },
                Response.ErrorListener { error: VolleyError? ->
                    Log.d("cobalt", "VOLLEY ERROR: " + error?.message)
                    delegate.didReceiveVolleyError(error)
                })

        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(stringRequest)
    }

    // Static functions
    companion object {

        // List of CobaltXMLRows each row has an array list of CobaltXmlFields with dict pairs
        fun convertXmlData(rawXML: String): List<CobaltXmlRow> {

            // Read the XML File
            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()
            val inputSource = InputSource(StringReader(rawXML))
            val xmlDoc = builder.parse(inputSource)

            xmlDoc.normalize()

            // Parse the XML
            var rowsList: NodeList = xmlDoc.getElementsByTagName("row")
            var myList = ArrayList<CobaltXmlRow>()

            for (i in 0..rowsList.length - 1)
            {

                var rowNode: Node = rowsList.item(i)

                if (rowNode.nodeType == Node.ELEMENT_NODE)
                {
                    var xmlRow = CobaltXmlRow()

                    val elem = rowNode as Element
                    for (j in 0..elem.attributes.length - 1)
                    {
                        val fieldName = elem.attributes.item(j).nodeName.toString()
                        var fieldValue = elem.attributes.item(j).nodeValue.toString()
                        var xmlField = CobaltXmlField()
                        xmlField.key = fieldName
                        xmlField.value = fieldValue
                        xmlRow.fields.add(xmlField)
                    }

                    myList.add(xmlRow)
                }
            }

            return myList
        }
    }

}