package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.EditText

class LoginActivity : BaseActivity() {

    lateinit var btnEnterCompanyId: Button

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
        val comms = CommsManager()
        val urlString = comms.WEBSERVER_VALIDATE_COMPANY_ID + value
        comms.getXMLDocument(urlString, this)

    }

}
