package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.util.Log
import android.widget.EditText
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.jetbrains.anko.act
import org.jetbrains.anko.db.NULL
import java.util.*

open class BaseActivity: AppCompatActivity() {

    lateinit var fusedLocationClient: FusedLocationProviderClient
    val NULL_COORDINATE: Double = -10000.0
    val LOCATION_PERMISSION_REQUEST_CODE = 1
    var lastLat: Double = NULL_COORDINATE
    var lastLng: Double = NULL_COORDINATE

    // MARK: Location

    fun setupLocationClient()
    {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
    }

    fun getCurrentLocation(action: (lat: Double, lng: Double) -> Unit)
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            requestLocationPermissions()
        }
        else
        {
            fusedLocationClient.lastLocation.addOnCompleteListener {
                if (it.result != null)
                {   lastLat = it.result.latitude
                    lastLng = it.result.longitude
                    Log.d("cobalt", "Lat: " + it.result.latitude + " Lng: " + it.result.longitude)
                    action.invoke(it.result.latitude, it.result.longitude)
                }
                else
                {
                    action.invoke(NULL_COORDINATE, NULL_COORDINATE)
                }
            }
        }
    }

    // To get the location after the permission has been accepted
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE)
        {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                locationPermissionsGranted()
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    }

    fun getAddressFromCoordinates(lat: Double, lng: Double): Address?
    {
        if (lat != NULL_COORDINATE)
        {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            if (addresses.isNotEmpty())
            {
                val foundAddress = addresses.get(0)
                return foundAddress
            }

        }

        return null
    }

    open fun locationPermissionsGranted()
    {
        // STUB:
    }


    // MARK: Convenience

    public fun nz(value: String?): String
    {
        if (value == null)
        {
            return ""
        }
        else
        {
            return value
        }
    }



}