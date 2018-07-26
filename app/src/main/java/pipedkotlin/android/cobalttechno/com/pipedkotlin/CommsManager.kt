package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class CommsManager() {

    public final val WEBSERVER_VALIDATE_COMPANY_ID = "http://www.cobalttechno.co.uk/irateservice.asmx/GetCompanyIDCount?company_id="


    public fun getXMLDocument(urlString: String, context: Context)
    {
        val stringRequest = StringRequest(urlString,
                Response.Listener<String> { rawXML ->
                   Log.d("cobalt", rawXML)
               },
                Response.ErrorListener { error: VolleyError? ->
                    Log.d("cobalt", "VOLLEY ERROR: " + error?.message)
                })

        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(stringRequest)
    }
}