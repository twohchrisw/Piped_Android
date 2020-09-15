package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.PersistableBundle
import android.os.StrictMode
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.util.Log
import android.view.WindowManager
import android.widget.EditText
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import org.jetbrains.anko.act
import org.jetbrains.anko.db.NULL
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.LogRecord

open class BaseActivity: AppCompatActivity() {

    lateinit var fusedLocationClient: FusedLocationProviderClient


    val NULL_COORDINATE: Double = -10000.0
    val LOCATION_PERMISSION_REQUEST_CODE = 1
    val CAMERA_PERMISSION_REQUEST_CODE  = 2


    val CAMERA_REQUEST_CAMERA = 1
    val CAMERA_REQUEST_GALLERY = 2
    val NOTES_REQUEST = 3
    var TEMP_IMAGE_LOCATION = "/sdcard/temppic.jpg"

    var isUpdatingLocation = false
    var mLocationManager: LocationManager? = null
    var mLocationRequest: LocationRequest? = null
    private val LOCATION_INTERVAL = (10 * 1000).toLong()
    private val LOCATION_INTERVAL_FASTEST = (2 * 1000).toLong()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mLocationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        requestLocationPermissions()
    }

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
                {   appGlobals.lastLat = it.result.latitude
                    appGlobals.lastLng = it.result.longitude
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

    fun startUpdatingLocation()
    {
        Log.d("cobsep1", "Start Updating Location")

        if (!isUpdatingLocation)
        {
            mLocationRequest = LocationRequest.create().apply {
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                interval = LOCATION_INTERVAL
                fastestInterval = LOCATION_INTERVAL_FASTEST
            }
        }
        else
        {
            Log.d("cobsep1", "Already updating location")
        }

    }

    open fun locationPermissionsGranted()
    {
        startUpdatingLocation()
    }

    open fun cameraPermissionsGranted()
    {
        // STUB:
    }

    fun choosePicFromCamera()
    {
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        val outfile = File(TEMP_IMAGE_LOCATION)
        val outuri = Uri.fromFile(outfile)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outuri)
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CAMERA)
    }

    /*
    fun saveImageToExternalStorage(bitmap: Bitmap, filename: String)
    {
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        val internalBitmap = BitmapFactory.decodeFile(TEMP_IMAGE_LOCATION, options) // The bitmap in the parameter is the thumbnail

        val path = Environment.getExternalStorageDirectory().toString()
        val file = File(path, filename)

        try {
            val stream: OutputStream = FileOutputStream(file)
            internalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
            Log.d("cobswab", "image saved successfully!!")
        }
        catch (e: IOException)
        {
            Log.d("cobswab", "Error saving image: ${e.localizedMessage}")
        }
    }
    */


    fun saveImageToExternalStorage(filename: String)
    {
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        //options.inJustDecodeBounds = true
        val internalBitmap = BitmapFactory.decodeFile(TEMP_IMAGE_LOCATION, options) // The bitmap in the parameter is the thumbnail

        //val inWidth = options.outWidth
        //val inHeight = options.outHeight

        val path = Environment.getExternalStorageDirectory().toString()
        val file = File(path, filename)

        try {
            val stream: OutputStream = FileOutputStream(file)

            val reducedBitmp = Bitmap.createScaledBitmap(internalBitmap, 1000, 1000, false)
            reducedBitmp.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
            Log.d("cobswab", "image saved successfully!!")
            appGlobals.activeProcess.save(this)    // To force an update check
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