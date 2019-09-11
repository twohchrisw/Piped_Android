package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import com.android.volley.VolleyError
import org.jetbrains.anko.UI
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.startActivity
import java.util.*
import kotlin.concurrent.schedule

class LoginActivity : BaseActivity(), CommsManagerDelegate {

    lateinit var btnEnterCompanyId: Button
    lateinit var progressBar: ProgressBar

    var attemptedCompanyId = ""

    enum class ParsingContext { CountOfCompanyIds, LoadListItems, LoadClients, ModulePrefs }
    var parsingContext = ParsingContext.CountOfCompanyIds
    var shouldLoadProcessListAfterParsing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        startUp()
    }

    fun startUp()
    {
        Log.d("ditest", "Start Up")
        checkPermissions()
        progressBar = findViewById(R.id.progressBar) as ProgressBar
        progressBar.visibility = View.GONE
        val existingCompanyId = EXLDSettings.getExistingCompanyId(this)

        if (existingCompanyId.length > 1)
        {
            // We have an existing company id, download list items, prefs and clients
            AppGlobals.instance.companyId = existingCompanyId
            AppGlobals.instance.userId = EXLDSettings.getExistingUserId(this)
            Log.d("cobalt", "List sets company id as " + AppGlobals.instance.companyId)
            progressBar.visibility = View.VISIBLE

            shouldLoadProcessListAfterParsing = true
            loadListItems()

        }
        else {
            formatForEnterCompanyID()
        }
    }

    fun formatForEnterCompanyID()
    {
        // Wait for the user to provide a company id
        btnEnterCompanyId = findViewById(R.id.btnEnterCompanyId)
        btnEnterCompanyId.visibility = View.VISIBLE
        btnEnterCompanyId.setOnClickListener { v -> getCompanyId() }
        supportActionBar?.hide()
        shouldLoadProcessListAfterParsing = true
        progressBar.visibility = View.GONE
    }


    fun checkPermissions()
    {
        val permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH)
        val p1 = ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
        val p2 = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val p3 = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE)
        if (permission != PackageManager.PERMISSION_GRANTED || p1 != PackageManager.PERMISSION_GRANTED || p2 != PackageManager.PERMISSION_GRANTED || p3 != PackageManager.PERMISSION_GRANTED)
        {
            val perms = arrayOf(android.Manifest.permission.BLUETOOTH, android.Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.INTERNET, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_NETWORK_STATE)
            ActivityCompat.requestPermissions(this, perms, TestingActivity.ActivityRequestCodes.permissions.value)
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
        val a = this

        val comms = CommsManager(a)
        var urlString = comms.WEBSERVER_GET_LIST_ITEMS + AppGlobals.instance.companyId

        comms.getXMLDocument(urlString, a)
    }

    // Load the list of clients from webservice
    fun loadClients()
    {
        parsingContext = ParsingContext.LoadClients
        val comms = CommsManager(this)
        var urlString = comms.WEBSERVER_GET_CLIENTS + AppGlobals.instance.companyId
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
        formatForEnterCompanyID()
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
        var noResultReturn = true
        var ownerCompanyId = ""
        var userIdString = ""

        // Check for valid, overlimit and invalid
        if (xmlList.size > 0)
        {
            // Only one row returned for this request
            val dataRow: CobaltXmlRow = xmlList.get(0)

            val resultCountField = dataRow.getFieldForKey("resultCount")?.value ?: "0"
            userIdString = dataRow.getFieldForKey("userId")?.value ?: ""
            ownerCompanyId = dataRow.getFieldForKey("companyId")?.value ?: ""
            Log.d("cobalt", "company id is $ownerCompanyId")
            val resultAsInt = resultCountField.toInt()
            noResultReturn = false

            if (resultAsInt == -1)
            {
                // Overlimit
                companyIsOverlimit = true
                companyIdIsValid = false
            }

            if (resultAsInt == 1)
            {
                companyIdIsValid = true
            }

            if (resultAsInt == 0)
            {
                companyIdIsValid = false
            }
        }

        if (companyIdIsValid)
        {
            AppGlobals.instance.companyId = ownerCompanyId
            AppGlobals.instance.userId = userIdString

            runOnUiThread {
                EXLDSettings.writeCompanyIdToDatabase(this, ownerCompanyId, userIdString, attemptedCompanyId)
            }

            loadListItems()
        }
        else
        {
            progressBar.visibility = View.GONE

            if (noResultReturn)
            {
                val alertHelper = AlertHelper(this)
                alertHelper.dialogForOKAlertNoAction("No Network", "An network error occurred whilst attempting to validate your company id.  Please ensure you have an internet connection and try again.")
            }
            else {
                if (companyIsOverlimit) {
                    val alertHelper = AlertHelper(this)
                    alertHelper.dialogForOKAlertNoAction("Account Suspended", "This account has been suspended.  Please contact your account administrator for advice.")
                } else {
                    val alertHelper = AlertHelper(this)
                    alertHelper.dialogForOKAlert("Invalid Company ID", "The company id you have entered was not found in our database.", ::getCompanyId)
                }
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

            //runOnUiThread {
                Log.d("Cobalt", "Write List Items")
                EXLDListItems.addNew(this, companyId, listValue, listName)
            //}

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
            val companyId = AppGlobals.instance.companyId

            //runOnUiThread {
                Log.d("Cobalt", "Write Clients: $clientName")
                EXLDClients.addNew(this, clientId, clientName, companyId)
            //}

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

            //runOnUiThread {
                Log.d("Cobalt", "Write Permissions")
                EXLDSettings.writePermissions(this, allowSurveying, allowFilling, allowPE, allowDI, allowChlor, allowDechlor, allowFlush, allowFlush2, allowSamping, allowSwabbing, allowAuditing)
            //}
        }

        if (shouldLoadProcessListAfterParsing) {
            loadMainProcessList()
        }
    }

}
