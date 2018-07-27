package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.android.volley.VolleyError

class LoginActivity : BaseActivity(), CommsManagerDelegate {

    lateinit var btnEnterCompanyId: Button

    var attemptedCompanyId = ""

    enum class ParsingContext { CountOfCompanyIds, LoadListItems, LoadClients, ModulePrefs }
    var parsingContext = ParsingContext.CountOfCompanyIds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //TODO: If we already have a company id, initiate the downloads - I think??

        btnEnterCompanyId = findViewById(R.id.btnEnterCompanyId)
        btnEnterCompanyId.setOnClickListener { v -> getCompanyId() }
    }

    // Get the company id from the user
    fun getCompanyId()
    {
        val alertHelper = AlertHelper(this)
        alertHelper.dialogForTextInput("Company ID", "", ::validateCompanyId)
    }

    // Validate the company id with the server
    fun validateCompanyId(value: String)
    {
        parsingContext = ParsingContext.CountOfCompanyIds
        val comms = CommsManager(this)
        attemptedCompanyId = value
        val urlString = comms.WEBSERVER_VALIDATE_COMPANY_ID + attemptedCompanyId
        comms.getXMLDocument(urlString, this)
    }

    // Load the main process list
    fun loadProcessList()
    {
        parsingContext = ParsingContext.LoadListItems
        val comms = CommsManager(this)
        var urlString = comms.WEBSERVER_GET_LIST_ITEMS
        comms.getXMLDocument(urlString, this)
    }

    // Load the . . .
    fun loadClients()
    {
        parsingContext = ParsingContext.LoadClients
        val comms = CommsManager(this)
        var urlString = comms.WEBSERVER_GET_CLIENTS
        comms.getXMLDocument(urlString, this)
    }

    // Load the . . .
    fun loadModulePreferences()
    {
        parsingContext = ParsingContext.ModulePrefs
        val comms = CommsManager(this)
        var urlString = comms.WEBSERVER_GET_MODULE_PREFS + AppGlobals.instance.companyId
        comms.getXMLDocument(urlString, this)
    }

    // MARK: Comms Manager Delegate

    override fun didReceiveVolleyError(error: VolleyError?) {
    }

    override fun didReceiveXMLDocument(rawXML: String) {

        if (parsingContext == ParsingContext.CountOfCompanyIds)
        {
            parseCompanyId(rawXML)
            return
        }

        if (parsingContext == ParsingContext.LoadListItems)
        {
            parseListItems(rawXML)
            return
        }

        if (parsingContext == ParsingContext.LoadClients)
        {
            parseClients(rawXML)
            return
        }

        if (parsingContext == ParsingContext.ModulePrefs)
        {
            parseModulePrefs(rawXML)
            return
        }
    }

    // Parse the data returned when requesting a companyId status
    fun parseCompanyId(rawXML: String)
    {
        val xmlList = CommsManager.convertXmlData(rawXML)
        var companyIdIsValid = false
        var companyIsOverlimit = false

        // Check for valid, overlimit and invalid
        if (xmlList.size > 0)
        {
            // Only one row returned for this request
            val dataRow: CobaltXmlRow = xmlList.get(0)

            val resultCountField = dataRow.getFieldForKey("resultCount")
            val resultCount: String = resultCountField?.value ?: "0"


            if (resultCount == "0")
            {
                Log.d("cobalt", "Invalid CompanyId")
            }
            else if (resultCount == "-1")
            {
                Log.d("cobalt", "Company overlimit")
                companyIsOverlimit = true
            }
            else if (resultCount == "1")
            {
                Log.d("cobalt", "Valid CompanyId")
                companyIdIsValid = true
            }
        }

        if (companyIdIsValid)
        {
            AppGlobals.instance.companyId = attemptedCompanyId
            //TODO: Assign the global company id and write it to the settings database
            loadProcessList()
        }
        else
        {
            if (companyIsOverlimit)
            {
                //TODO: Message user
            }
            else
            {
                //TODO: Inform user of invalid company id
                //TODO: Then relaunch the companyid dialog
            }
        }
    }

    // Parse the data returned when requesting list items
    fun parseListItems(rawXML: String)
    {
        val xmlList = CommsManager.convertXmlData(rawXML)
        // TODO: Delete all current list items

        // Add the list items to the database
        for (row: CobaltXmlRow in xmlList)
        {
            val listName = row.getFieldForKey("ListName")?.value
            val listValue = row.getFieldForKey("ListValue")?.value
            val companyId = row.getFieldForKey("CompanyId")?.value

            if (listName != null && listValue != null && companyId != null)
            {
                // TODO: Write to database
                Log.d("cobalt", "ListName: " + listName + " ListValue: " + listValue + " CompanyId: " + companyId)
            }
        }

        loadClients()
    }

    fun parseClients(rawXML: String)
    {
        val xmlList = CommsManager.convertXmlData(rawXML)
        // TODO: Delete all current list items

        // Add the list items to the database
        for (row: CobaltXmlRow in xmlList)
        {
            val clientId = row.getFieldForKey("client_id")?.value
            val clientName = row.getFieldForKey("client_name")?.value
            val companyId = row.getFieldForKey("company_id")?.value

            if (clientId != null && clientName != null && companyId != null)
            {
                // TODO: Write to database
                Log.d("cobalt", "ClientId: " + clientId + " ClientName: " + clientName + " CompanyId: " + companyId)
            }
        }

        loadModulePreferences()
    }

    fun parseModulePrefs(rawXML: String)
    {
        val xmlList = CommsManager.convertXmlData(rawXML)
        // TODO: Delete all current list items

        // Add the list items to the database
        for (row: CobaltXmlRow in xmlList)
        {
            val allowSurveying = nz(row.getFieldForKey("allow_surveying")?.value)
            val allowFilling = nz(row.getFieldForKey("allow_filling")?.value)
            val allowPE = nz(row.getFieldForKey("allow_pe")?.value)
            val allowDI = nz(row.getFieldForKey("allow_di")?.value)
            val allowChlor = nz(row.getFieldForKey("allow_chlor")?.value)
            val allowDechlor = nz(row.getFieldForKey("allow_dechlor")?.value)
            val allowFlush = nz(row.getFieldForKey("allow_flush")?.value)
            val allowFlush2 = nz(row.getFieldForKey("allow_flush2")?.value)
            val allowSamping = nz(row.getFieldForKey("allow_sampling")?.value)
            val allowSwabbing = nz(row.getFieldForKey("allow_swabbing")?.value)
            val allowAuditing = nz(row.getFieldForKey("allow_auditing")?.value)

            //TODO: Write to database
            Log.d("cobalt", "AllowFilling: " + allowFilling)
        }

        // TODO: We're fully loaded I think, load menu?
    }


}
