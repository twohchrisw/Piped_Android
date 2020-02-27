package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import com.android.volley.VolleyError
import kotlinx.android.synthetic.main.activity_pipe_calculator.*
import org.jetbrains.anko.UI
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.startActivity
import java.util.*
import kotlin.concurrent.schedule

class LoginActivity : BaseActivity(), CommsManagerDelegate {

    lateinit var btnEnterCompanyId: Button
    lateinit var progressBar: ProgressBar

    lateinit var vwContainerView: View
    lateinit var vwButtonPEView: View
    lateinit var vwButtonMetallicView: View
    lateinit var btnPE: Button
    lateinit var btnMetallic: Button
    lateinit var lblTitleLabel: TextView
    lateinit var etDiameter: EditText
    lateinit var etFlowrate: EditText
    lateinit var etLength: EditText
    lateinit var etSDR: EditText
    lateinit var lblSDR: TextView
    lateinit var lblVolume: TextView
    lateinit var lblTimeToFill: TextView
    lateinit var lblDiameter: TextView
    lateinit var lblFlowrate: TextView
    lateinit var lblLength: TextView
    lateinit var lblVolumeTitle: TextView
    lateinit var lblTimeToFillTitle: TextView
    lateinit var vwSeperator: View
    lateinit var btnCalculate: Button
    lateinit var lblViewName: TextView

    enum class CalculatorTab {
        Metallic, PE
    }
    var selectedTab = CalculatorTab.Metallic

    var attemptedCompanyId = ""

    enum class ParsingContext { CountOfCompanyIds, LoadListItems, LoadClients, ModulePrefs }
    var parsingContext = ParsingContext.CountOfCompanyIds
    var shouldLoadProcessListAfterParsing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        assignOutlets()
        addListeners()

        formatForSelection()
        initialiseInputBoxes()
        startUp()
    }

    fun startUp()
    {
        Log.d("oct23", "Start Up")
        checkPermissions()
        progressBar = findViewById(R.id.progressBar) as ProgressBar
        progressBar.visibility = View.GONE
        val existingCompanyId = EXLDSettings.getExistingCompanyId(this)

        if (existingCompanyId.length > 2)
        {
            // We have an existing company id, download list items, prefs and clients
            appGlobals.companyId = existingCompanyId
            appGlobals.userId = EXLDSettings.getExistingUserId(this)
            Log.d("oct23", "List sets company id as " + appGlobals.companyId)
            progressBar.visibility = View.VISIBLE

            shouldLoadProcessListAfterParsing = true

            if (AppGlobals.isOnline(this))
            {
                loadListItems()
            }
            else
            {
                Log.d("oct23", "Network is offline")
                loadMainProcessList()
            }

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

        //btnPipeCalculator = findViewById(R.id.btnPipeCalculator)
        //btnPipeCalculator.visibility = View.VISIBLE
        //btnPipeCalculator.setOnClickListener { v -> loadPipeCalculator() }

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

    fun loadPipeCalculator()
    {
        val pipeCalculatorIntent = Intent(this, PipeCalculatorActivity::class.java)
        startActivity(pipeCalculatorIntent)
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
        var urlString = comms.WEBSERVER_GET_LIST_ITEMS + appGlobals.companyId

        comms.getXMLDocument(urlString, a)
    }

    // Load the list of clients from webservice
    fun loadClients()
    {
        parsingContext = ParsingContext.LoadClients
        val comms = CommsManager(this)
        var urlString = comms.WEBSERVER_GET_CLIENTS + appGlobals.companyId
        comms.getXMLDocument(urlString, this)
    }

    // Load the module preferences from webservice
    fun loadModulePreferences()
    {
        parsingContext = ParsingContext.ModulePrefs
        val comms = CommsManager(this)
        var urlString = comms.WEBSERVER_GET_MODULE_PREFS + appGlobals.companyId
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
            appGlobals.companyId = ownerCompanyId
            appGlobals.userId = userIdString

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
            val companyId = appGlobals.companyId

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

    /* MARK: Calculator */

    fun assignOutlets()
    {
        vwContainerView = findViewById(R.id.vwContainerView)
        vwButtonMetallicView = findViewById(R.id.buttonMetallicView)
        vwButtonPEView = findViewById(R.id.buttonPEView)
        btnMetallic = findViewById(R.id.btnMetallic)
        btnPE = findViewById(R.id.btnPE)
        lblTitleLabel = findViewById(R.id.lblTitleLabel)
        etDiameter = findViewById(R.id.etDiameter)
        etFlowrate = findViewById(R.id.etFlowrate)
        etLength = findViewById(R.id.etLength)
        etSDR = findViewById(R.id.etSDR)
        lblSDR = findViewById(R.id.lblSDR)
        lblVolume = findViewById(R.id.lblVolume)
        lblTimeToFill = findViewById(R.id.lblTimeToFill)
        lblDiameter = findViewById(R.id.lblDiameter)
        lblFlowrate = findViewById(R.id.lblFlowrate)
        lblLength = findViewById(R.id.lblLength)
        lblVolumeTitle = findViewById(R.id.lblVolumeTitle)
        lblTimeToFillTitle = findViewById(R.id.lblTimeToFillTitle)
        vwSeperator = findViewById(R.id.vwSeperator)
        btnCalculate = findViewById(R.id.btnCalculate)
        lblViewName = findViewById(R.id.tvViewName)
    }

    fun addListeners()
    {
        btnPE.setOnClickListener {
            selectedTab = CalculatorTab.PE
            formatForSelection()
        }

        btnMetallic.setOnClickListener {
            selectedTab = CalculatorTab.Metallic
            formatForSelection()
        }

        btnCalculate.setOnClickListener {
            calculate()
        }
    }

    fun formatForSelection()
    {
        initialiseInputBoxes()

        tvViewName.visibility = View.GONE
        etDiameter.setTextColor(Color.BLACK)
        etFlowrate.setTextColor(Color.BLACK)
        etSDR.setTextColor(Color.BLACK)
        etLength.setTextColor(Color.BLACK)

        if (selectedTab == CalculatorTab.Metallic)
        {
            vwButtonMetallicView.bringToFront()
            vwContainerView.bringToFront()
            vwButtonMetallicView.background = resources.getDrawable(R.drawable.calc_active_tab)
            vwButtonPEView.background = resources.getDrawable(R.drawable.calc_inactive_tab)
            btnPE.setTextColor(Color.BLACK)
            btnMetallic.setTextColor(Color.WHITE)
            lblTitleLabel.setText("Metallic Pipe")
            lblSDR.visibility = View.GONE
            etSDR.visibility = View.GONE
            etLength.imeOptions = EditorInfo.IME_ACTION_DONE
        }

        if (selectedTab == CalculatorTab.PE)
        {
            vwButtonPEView.bringToFront()
            vwContainerView.bringToFront()
            vwButtonMetallicView.background = resources.getDrawable(R.drawable.calc_inactive_tab)
            vwButtonPEView.background = resources.getDrawable(R.drawable.calc_active_tab)
            btnPE.setTextColor(Color.WHITE)
            btnMetallic.setTextColor(Color.BLACK)
            lblTitleLabel.setText("Polyethelene Pipe")
            lblSDR.visibility = View.VISIBLE
            etSDR.visibility = View.VISIBLE

            etLength.imeOptions = EditorInfo.IME_ACTION_NEXT
            etSDR.imeOptions = EditorInfo.IME_ACTION_DONE
        }

        btnMetallic.bringToFront()
        btnPE.bringToFront()
        lblTitleLabel.bringToFront()
        lblDiameter.bringToFront()
        etDiameter.bringToFront()
        lblFlowrate.bringToFront()
        etFlowrate.bringToFront()
        lblLength.bringToFront()
        etLength.bringToFront()
        lblSDR.bringToFront()
        etSDR.bringToFront()
        vwSeperator.bringToFront()
        lblVolumeTitle.bringToFront()
        lblVolume.bringToFront()
        lblTimeToFillTitle.bringToFront()
        lblTimeToFill.bringToFront()
        btnCalculate.bringToFront()
    }

    fun initialiseInputBoxes()
    {
        etDiameter.setText("0")
        etLength.setText("0")
        etSDR.setText("0")
        etFlowrate.setText("0")
        lblVolume.setText("0 Ltrs")
        lblTimeToFill.setText("0hr 0m")
    }

    fun validateInputs(): PipeCalculatorActivity.CalcInputs
    {
        var isValid = true
        etDiameter.setTextColor(Color.BLACK)
        etLength.setTextColor(Color.BLACK)
        etSDR.setTextColor(Color.BLACK)
        etFlowrate.setTextColor(Color.BLACK)

        val failColor = Color.RED

        val diameterInput = etDiameter.text.toString()
        val sdrInput = etSDR.text.toString()
        val lengthInput = etLength.text.toString()
        val flowrateInput = etFlowrate.text.toString()

        val diam = diameterInput.toDoubleOrNull()
        val sdr = sdrInput.toDoubleOrNull()
        val len = lengthInput.toDoubleOrNull()
        val flowrate = flowrateInput.toDoubleOrNull()

        if (diam == null) {
            isValid = false
            etDiameter.setTextColor(failColor)
        }

        if (sdr == null)
        {
            isValid = false
            etSDR.setTextColor(failColor)
        }

        if (len == null)
        {
            isValid = false
            etLength.setTextColor(failColor)
        }

        if (flowrate == null)
        {
            isValid = false
            etFlowrate.setTextColor(failColor)
        }

        if (isValid)
        {
            val calcInputs = PipeCalculatorActivity.CalcInputs()
            calcInputs.isValid = true
            calcInputs.diameter = diam!!
            calcInputs.flowrate = flowrate!!
            calcInputs.length = len!!
            calcInputs.sdr = sdr!!
            return calcInputs
        }
        else
        {
            val calcInputs = PipeCalculatorActivity.CalcInputs()
            calcInputs.isValid = false
            return calcInputs
        }
    }

    fun calculate()
    {
        val calcs = validateInputs()

        if (!calcs.isValid)
        {
            lblVolume.setText("#Error")
            lblTimeToFill.setText("#Error")
            return
        }

        if (selectedTab == CalculatorTab.PE)
        {
            val volString = calcs.calcPEVolume().formatForDecPlaces(2)
            lblVolume.text = volString

            val fillingTime = calcs.peFillingTime()
            lblTimeToFill.text = fillingTime.first.toString() + "hr " + fillingTime.second.toString() + "m"
        }

        if (selectedTab == CalculatorTab.Metallic)
        {
            val volSgtring = calcs.calcDIVolumne().formatForDecPlaces(2)
            lblVolume.text = volSgtring

            val fillingTime = calcs.diFillingTime()
            lblTimeToFill.text = fillingTime.first.toString() + "hr " + fillingTime.second.toString() + "m"
        }
    }

}
