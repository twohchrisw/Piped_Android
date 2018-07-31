package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import com.android.volley.VolleyError
import org.jetbrains.anko.UI
import org.jetbrains.anko.startActivity

class LoginActivity : BaseActivity(), CommsManagerDelegate {

    lateinit var btnEnterCompanyId: Button
    lateinit var progressBar: ProgressBar

    var attemptedCompanyId = ""

    enum class ParsingContext { CountOfCompanyIds, LoadListItems, LoadClients, ModulePrefs }
    var parsingContext = ParsingContext.CountOfCompanyIds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        progressBar = findViewById(R.id.progressBar) as ProgressBar
        progressBar.visibility = View.GONE
        val existingCompanyId = EXLDSettings.getExistingCompanyId(this)

        if (existingCompanyId.length > 1)
        {
            // We have an existing company id, download list items, prefs and clients
            AppGlobals.instance.companyId = existingCompanyId
            Log.d("cobalt", "List sets company id as " + AppGlobals.instance.companyId)
            progressBar.visibility = View.VISIBLE
            loadListItems()
        }
        else {
            // Wait for the user to provide a company id
            btnEnterCompanyId = findViewById(R.id.btnEnterCompanyId)
            btnEnterCompanyId.visibility = View.VISIBLE
            btnEnterCompanyId.setOnClickListener { v -> getCompanyId() }
        }
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
        progressBar.visibility = View.VISIBLE
        parsingContext = ParsingContext.CountOfCompanyIds
        val comms = CommsManager(this)
        attemptedCompanyId = value
        val urlString = comms.WEBSERVER_VALIDATE_COMPANY_ID + attemptedCompanyId
        comms.getXMLDocument(urlString, this)
    }

    // Load the load lists
    fun loadListItems()
    {
        parsingContext = ParsingContext.LoadListItems
        val comms = CommsManager(this)
        var urlString = comms.WEBSERVER_GET_LIST_ITEMS
        comms.getXMLDocument(urlString, this)
    }

    // Load the list of clients from webservice
    fun loadClients()
    {
        parsingContext = ParsingContext.LoadClients
        val comms = CommsManager(this)
        var urlString = comms.WEBSERVER_GET_CLIENTS
        comms.getXMLDocument(urlString, this)
    }

    // Load the module preferences from webservice
    fun loadModulePreferences()
    {
        parsingContext = ParsingContext.ModulePrefs
        val comms = CommsManager(this)
        var urlString = comms.WEBSERVER_GET_MODULE_PREFS + AppGlobals.instance.companyId
        comms.getXMLDocument(urlString, this)
    }

    // Load the main process list
    fun loadMainProcessList()
    {
        val processListIntent = Intent(this, ProcessListActivity::class.java)
        startActivity(processListIntent)
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
            EXLDSettings.writeCompanyIdToDatabase(this, attemptedCompanyId)
            loadListItems()
        }
        else
        {
            progressBar.visibility = View.GONE
            if (companyIsOverlimit)
            {
                val alertHelper = AlertHelper(this)
                alertHelper.dialogForOKAlertNoAction("Company Registrations Overlimit", "Your company has used its full complement of registrations for this app.  Please contact you administrator")
            }
            else
            {
                val alertHelper = AlertHelper(this)
                alertHelper.dialogForOKAlert("Invalid Company ID", "The company id you have entered was not found in our database.", ::getCompanyId)
            }
        }
    }

    // Parse the data returned when requesting list items
    fun parseListItems(rawXML: String)
    {
        val xmlList = CommsManager.convertXmlData(rawXML)
        EXLDListItems.deleteAll(this)

        // Add the list items to the database
        for (row: CobaltXmlRow in xmlList)
        {
            val listName = nz(row.getFieldForKey("ListName")?.value)
            val listValue = nz(row.getFieldForKey("ListValue")?.value)
            val companyId = nz(row.getFieldForKey("CompanyId")?.value)

            EXLDListItems.addNew(this, companyId, listValue, listName)
        }

        loadClients()
    }

    fun parseClients(rawXML: String)
    {
        val xmlList = CommsManager.convertXmlData(rawXML)
        EXLDClients.deleteAll(this)

        // Add the list items to the database
        for (row: CobaltXmlRow in xmlList)
        {
            val clientId = nz(row.getFieldForKey("client_id")?.value)
            val clientName = nz(row.getFieldForKey("client_name")?.value)
            val companyId = nz(row.getFieldForKey("company_id")?.value)

            EXLDClients.addNew(this, clientId, clientName, companyId)
        }

        loadModulePreferences()
    }

    fun parseModulePrefs(rawXML: String)
    {
        val xmlList = CommsManager.convertXmlData(rawXML)

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

            EXLDSettings.writePermissions(this, allowSurveying, allowFilling, allowPE, allowDI, allowChlor, allowDechlor, allowFlush, allowFlush2, allowSamping, allowSwabbing, allowAuditing)
        }

        loadMainProcessList()
    }

}
