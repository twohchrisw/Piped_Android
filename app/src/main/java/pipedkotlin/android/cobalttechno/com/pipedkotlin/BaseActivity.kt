package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.os.Environment
import android.os.PersistableBundle
import android.provider.MediaStore
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
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*
import java.util.logging.LogRecord

open class BaseActivity: AppCompatActivity() {

    lateinit var fusedLocationClient: FusedLocationProviderClient
    val NULL_COORDINATE: Double = -10000.0
    val LOCATION_PERMISSION_REQUEST_CODE = 1
    val CAMERA_PERMISSION_REQUEST_CODE  = 2
    //var lastLat: Double = NULL_COORDINATE
    //var lastLng: Double = NULL_COORDINATE

    val CAMERA_REQUEST_CAMERA = 1
    val CAMERA_REQUEST_GALLERY = 2
    val NOTES_REQUEST = 3

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

    fun requestCameraPermissions()
    {
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE)
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
                {   AppGlobals.instance.lastLat = it.result.latitude
                    AppGlobals.instance.lastLng = it.result.longitude
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

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE)
        {
            if (grantResults.size == 3 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                cameraPermissionsGranted()
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

    open fun cameraPermissionsGranted()
    {
        // STUB:
    }

    fun choosePicFromCamera()
    {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CAMERA)
    }

    fun saveImageToExternalStorage(bitmap: Bitmap, filename: String)
    {
        val path = Environment.getExternalStorageDirectory().toString()
        val file = File(path, filename)

        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
            Log.d("cobswab", "image saved successfully!!")
        }
        catch (e: IOException)
        {
            Log.d("cobswab", "Error saving image: ${e.localizedMessage}")
        }
    }

    fun setNotes(defaultValue: String)
    {
        val notesIntent = Intent(this, NotesActivity::class.java)
        notesIntent.putExtra(NotesActivity.NOTES_EXTRA, defaultValue)
        startActivityForResult(notesIntent, NOTES_REQUEST)
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