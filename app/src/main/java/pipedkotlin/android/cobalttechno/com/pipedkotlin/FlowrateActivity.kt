package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import java.util.*

class FlowrateActivity : BaseActivity(), StandardRecyclerAdapter.StandardRecyclerDeChlorInterface, StandardRecyclerAdapter.StandardRecyclerChlorInterface, StandardRecyclerAdapter.StandardRecyclerSamplingInterface
{

    private lateinit var recyclerView: RecyclerView
    private var chlorFlowrate: EXLDChlorFlowrates? = null
    private var decFlowrate: EXLDDecFlowrates? = null
    private var samplFlowrate: EXLDSamplingData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_swabbing)

        setupLocationClient()
        getCurrentLocation(::locationReceived)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        bindRecycler()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.done_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item?.itemId == R.id.mnuDone)
        {
            finish()
        }

        return super.onOptionsItemSelected(item)
    }

    fun bindRecycler()
    {
        when (appGlobals.currentFlowrateActivityType)
        {
            AppGlobals.Companion.FlowrateViewType.Chlor -> {
                title = "Flowrate"
                recyclerView.adapter = StandardRecyclerAdapter(this, StandardRecyclerAdapter.PipedTask.ChlorFlowrate, appGlobals.lastLat, appGlobals.lastLng, null, null, this)
            }
            AppGlobals.Companion.FlowrateViewType.DeChlor -> {
                title = "Flowrate"
                recyclerView.adapter = StandardRecyclerAdapter(this, StandardRecyclerAdapter.PipedTask.DecFlowrate, appGlobals.lastLat, appGlobals.lastLng, null, null, null, this)
            }
            AppGlobals.Companion.FlowrateViewType.Sampling -> {
                title = "Sampling Data"
                recyclerView.adapter = StandardRecyclerAdapter(this, StandardRecyclerAdapter.PipedTask.SamplingFlowrate, appGlobals.lastLat, appGlobals.lastLng, null, null, null, null, this)
            }

            else -> {}
        }
    }

    fun locationReceived(lat: Double, lng: Double) {
        appGlobals.lastLat = lat
        appGlobals.lastLng = lng
        bindRecycler()
    }

    /* Chlor Interface */

    override fun didRequestChlorImage(flowrate: EXLDChlorFlowrates) {
        chlorFlowrate = flowrate
        requestCameraPermissions()
    }

    /* Dechlor Interface */

    override fun didRequestDeChlorImage(flowrate: EXLDDecFlowrates) {
        decFlowrate = flowrate
        requestCameraPermissions()
    }

    override fun didRequestSampleImage(flowrate: EXLDSamplingData) {
        samplFlowrate = flowrate
        requestCameraPermissions()
    }

    override fun cameraPermissionsGranted() {
        super.cameraPermissionsGranted()

        when (appGlobals.currentFlowrateActivityType)
        {
            AppGlobals.Companion.FlowrateViewType.Chlor -> {
                if (chlorFlowrate!!.chlor_photo.length < 2)
                {
                    // Straight load
                    choosePicFromCamera()
                }
                else
                {
                    // Ask for user direction
                    val alert = AlertHelper(this)
                    alert.dialogForOKAlert("Delete Image", "Do you want to delete this image?", {
                        chlorFlowrate!!.chlor_photo = ""
                        chlorFlowrate!!.save(this)
                        runOnUiThread {
                            recyclerView.adapter?.notifyDataSetChanged()
                        }
                    })
                }
            }

            AppGlobals.Companion.FlowrateViewType.DeChlor -> {
                if (decFlowrate!!.dec_photo.length < 2)
                {
                    // Straight load
                    choosePicFromCamera()
                }
                else
                {
                    // Ask for user direction
                    val alert = AlertHelper(this)
                    alert.dialogForOKAlert("Delete Image", "Do you want to delete this image?", {
                        decFlowrate!!.dec_photo = ""
                        decFlowrate!!.save(this)
                        runOnUiThread {
                            recyclerView.adapter?.notifyDataSetChanged()
                        }
                    })
                }
            }

            AppGlobals.Companion.FlowrateViewType.Sampling -> {
                if (samplFlowrate!!.sampl_photo.length < 2)
                {
                    // Straight load
                    choosePicFromCamera()
                }
                else {
                    // Ask for user direction
                    val alert = AlertHelper(this)
                    alert.dialogForOKAlert("Delete Image", "Do you want to delete this image?", {
                        samplFlowrate!!.sampl_photo = ""
                        samplFlowrate!!.save(this)
                        runOnUiThread {
                            recyclerView.adapter?.notifyDataSetChanged()
                        }
                    })
                }
            }

            else -> {}
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == NOTES_REQUEST && data != null)
        {

        }

        if (requestCode == CAMERA_REQUEST_CAMERA)
        {
            val uuid = UUID.randomUUID().toString()

            when (appGlobals.currentFlowrateActivityType)
            {
                AppGlobals.Companion.FlowrateViewType.Chlor -> {
                    val fileName = "chlor_${uuid}.jpg"
                    saveImageToExternalStorage(fileName)
                    chlorFlowrate!!.chlor_photo = fileName
                    chlorFlowrate!!.save(this)
                }

                AppGlobals.Companion.FlowrateViewType.DeChlor -> {
                    val fileName = "dechlor_${uuid}.jpg"
                    saveImageToExternalStorage(fileName)
                    decFlowrate!!.dec_photo = fileName
                    decFlowrate!!.save(this)
                }

                AppGlobals.Companion.FlowrateViewType.Sampling -> {
                    val fileName = "sample${uuid}.jpg"
                    saveImageToExternalStorage(fileName)
                    samplFlowrate!!.sampl_photo = fileName
                    samplFlowrate!!.save(this)
                }

                else -> {}
            }
        }

        runOnUiThread {
            recyclerView.adapter?.notifyDataSetChanged()
        }
    }
}
