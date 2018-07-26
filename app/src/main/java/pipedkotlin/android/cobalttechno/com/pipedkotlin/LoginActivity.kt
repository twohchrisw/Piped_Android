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

    enum class ParsingContext { CountOfCompanyIds }
    var parsingContext = ParsingContext.CountOfCompanyIds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

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
        val comms = CommsManager(this)
        val urlString = comms.WEBSERVER_VALIDATE_COMPANY_ID + value
        comms.getXMLDocument(urlString, this)
    }

    fun refreshListItemsFromServer()
    {

    }

    // MARK: Comms Manager Delegate

    override fun didReceiveVolleyError(error: VolleyError?) {
    }

    override fun didReceiveXMLDocument(rawXML: String) {
        if (parsingContext == ParsingContext.CountOfCompanyIds)
        {
            val xmlList = CommsManager.convertXmlData(rawXML)
            var companyIdIsValid = false
            var companyIsOverlimit = false

            // Check for valid, overlimit and invalid
            if (xmlList.size > 0)
            {
                // Only one row returned for this request
                val dataRow: CobaltXmlRow = xmlList.get(0)

                if (dataRow.key == "resultCount")
                {
                    val isValidCompanyId = dataRow.value

                    if (isValidCompanyId == "0")
                    {
                        Log.d("cobalt", "Invalid CompanyId")
                    }
                    else if (isValidCompanyId == "-1")
                    {
                        Log.d("cobalt", "Company overlimit")
                        companyIsOverlimit = true
                    }
                    else
                    {
                        Log.d("cobalt", "Valid CompanyId")
                        companyIdIsValid = true
                    }
                }
            }

            if (companyIdIsValid)
            {
                // Assign the global company id and write it to the settings database
                // Download the lists from the server
                refreshListItemsFromServer()
            }
            else
            {
                if (companyIsOverlimit)
                {
                    // Message user
                }
                else
                {
                    // Inform user of invalid company id
                    // Then relaunch the companyid dialog
                }
            }

        }
    }

}
