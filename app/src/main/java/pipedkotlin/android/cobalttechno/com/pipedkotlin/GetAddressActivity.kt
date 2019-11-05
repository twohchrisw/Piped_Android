package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import org.jetbrains.anko.alert
import pipedkotlin.android.cobalttechno.com.pipedkotlin.R.id.*
import java.util.*

class GetAddressActivity : BaseActivity() {

    lateinit var tvProcessHeader: TextView
    lateinit var etAddress: EditText
    lateinit var tvLocation: TextView

    var currentLat: Double = NULL_COORDINATE
    var currentLng: Double = NULL_COORDINATE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_address)
        supportActionBar?.title = "Address"
        assignOutlets()
        loadData()

        // Location services are in BaseActivity
        setupLocationClient()
        getCurrentLocation(::locationReceived)  // Fire the location received method when we have a location
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.get_address_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item?.itemId)
        {
            R.id.mnuGetLocation -> getAddressFromCurrentLocation()
        }

        return super.onOptionsItemSelected(item)
    }

    fun assignOutlets()
    {
        tvProcessHeader = findViewById(R.id.tvAddressProcess) as TextView
        etAddress = findViewById(R.id.etAddress) as EditText
        tvLocation = findViewById(R.id.tvLocation) as TextView

        val myContext: Context = this

        etAddress.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                appGlobals.activeProcess.address = etAddress.text.toString()
                appGlobals.activeProcess.location_lat = currentLat
                appGlobals.activeProcess.location_long = currentLng

                runOnUiThread {
                    appGlobals.activeProcess.save(myContext)
                }

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })
    }

    fun loadData()
    {
        tvProcessHeader.text = appGlobals.activeProcess.processNoDescription()
        if (appGlobals.activeProcess.address.isNotEmpty())
        {
            etAddress.setText(appGlobals.activeProcess.address)
        }

        // Get the current location
        tvLocation.text = "Updating location . . ."
    }

    // Menu request to get address
    fun getAddressFromCurrentLocation()
    {
        val address = getAddressFromCoordinates(currentLat, currentLng)
        if (address != null)
        {
            val addressText = address.getAddressLine(0).toString()
            etAddress.setText(addressText)
        }
    }

    // BaseActivity has received a location
    fun locationReceived(lat: Double, lng: Double)
    {
        if (lat != NULL_COORDINATE && lng != NULL_COORDINATE)
        {
            appGlobals.lastLat = lat
            appGlobals.lastLng = lng
            currentLat = lat
            currentLng = lng
            tvLocation.text = "Lat: " + currentLat + " Lng: " + currentLng
        }
        else
        {
            Log.d("cobalt", "Location is null")
        }
    }

    override fun locationPermissionsGranted()
    {
        getCurrentLocation(::locationReceived)
    }


}
