package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.format.Time
import android.util.Log
import org.jetbrains.anko.db.INTEGER
import org.jetbrains.anko.startActivityForResult
import java.util.*

class DetailsActivity : AppCompatActivity(), DetailsRecyclerAdapter.DetailsRecyclerClickListener {

    lateinit var recyclerView: RecyclerView

    enum class ActivityRequestCodes(val value: Int)
    {
        listSelection(0), addressSelection(1), notes(3), startDate(4), finishDate(5)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
        supportActionBar?.title = appGlobals.activeProcess.processNoDescription()
        assignOutlets()
    }

    fun assignOutlets()
    {
        recyclerView = findViewById(R.id.detailsRecycler) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = DetailsRecyclerAdapter(this)
    }

    fun savePipeLength(value: String)
    {
        val p = appGlobals.activeProcess
        val intValue = value.toIntOrNull()
        if (intValue != null)
        {
            p.pipe_length = intValue
            p.save(this)
            assignOutlets()
        }
    }

    fun savePipeDiameter(value: String)
    {
        val p = appGlobals.activeProcess
        val intValue = value.toIntOrNull()
        if (intValue != null) {
            p.pipe_diameter = intValue
            p.save(this)
            assignOutlets()
        }
    }

    fun getDateTime(defaultDateTime: String, dateContext: ActivityRequestCodes)
    {
        var selectedYear = 0
        var selectedMonth = 0
        var selectedDay = 0

        val defaultDate = DateHelper.dbStringToDate(defaultDateTime, Date())
        val c = Calendar.getInstance()
        c.setTime(defaultDate)

        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        val datePicker = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            selectedYear = year
            selectedMonth = month
            selectedDay = dayOfMonth

            val timePicker = TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { view, selectedHour, selectedMinute ->
                val selectedDateTime = DateHelper.dateFromValues(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute)
                val dateAsString = DateHelper.dateToDBString(selectedDateTime)

                if (dateContext == ActivityRequestCodes.startDate)
                {
                    appGlobals.activeProcess.start_time = dateAsString
                }

                if (dateContext == ActivityRequestCodes.finishDate)
                {
                    appGlobals.activeProcess.finish_time = dateAsString
                }

                appGlobals.activeProcess.save(this)
                assignOutlets()

            }, hour, minute, true)
            timePicker.show()

        }, year, month, day)
        datePicker.show()
    }

    // One of the main details rows has been selected
    override fun listItemClicked(menuItem: Int) {

        val listIntent = Intent(this, ListSelectionActivity::class.java)

        when (menuItem)
        {
            DetailsRecyclerAdapter.MenuItems.technician.value -> {
                listIntent.putExtra("ListType", ListSelectionActivity.ListContext.technicians.value)
                startActivityForResult(listIntent, ActivityRequestCodes.listSelection.value)
            }
            DetailsRecyclerAdapter.MenuItems.vehicle.value -> {
                listIntent.putExtra("ListType", ListSelectionActivity.ListContext.vehicles.value)
                startActivityForResult(listIntent, ActivityRequestCodes.listSelection.value)
            }
            DetailsRecyclerAdapter.MenuItems.installationTech.value -> {
                listIntent.putExtra("ListType", ListSelectionActivity.ListContext.installTechs.value)
                startActivityForResult(listIntent, ActivityRequestCodes.listSelection.value)
            }
            DetailsRecyclerAdapter.MenuItems.client.value -> {
                listIntent.putExtra("ListType", ListSelectionActivity.ListContext.clients.value)
                startActivityForResult(listIntent, ActivityRequestCodes.listSelection.value)
            }
            DetailsRecyclerAdapter.MenuItems.scheme.value -> {
                listIntent.putExtra("ListType", ListSelectionActivity.ListContext.schemes.value)
                startActivityForResult(listIntent, ActivityRequestCodes.listSelection.value)
            }
            DetailsRecyclerAdapter.MenuItems.address.value -> {
                val addressIntent = Intent(this, GetAddressActivity::class.java)
                startActivityForResult(addressIntent, ActivityRequestCodes.addressSelection.value)
            }
            DetailsRecyclerAdapter.MenuItems.pipeType.value -> {
                listIntent.putExtra("ListType", ListSelectionActivity.ListContext.pipeType.value)
                startActivityForResult(listIntent, ActivityRequestCodes.listSelection.value)
            }
            DetailsRecyclerAdapter.MenuItems.pipeLength.value -> {
                val alertHelper = AlertHelper(this)
                alertHelper.dialogForIntegerInput("Pipe Length", appGlobals.activeProcess.pipe_length.toString(), ::savePipeLength)
            }
            DetailsRecyclerAdapter.MenuItems.pipeDiameter.value -> {
                val alertHelper = AlertHelper(this)
                alertHelper.dialogForIntegerInput("Pipe Diameter", appGlobals.activeProcess.pipe_diameter.toString(), ::savePipeDiameter)
            }
            DetailsRecyclerAdapter.MenuItems.notes.value -> {
                val notesIntent = Intent(this, NotesActivity::class.java)
                notesIntent.putExtra(NotesActivity.NOTES_EXTRA, appGlobals.activeProcess.general_other)
                startActivityForResult(notesIntent, ActivityRequestCodes.notes.value)
            }
            DetailsRecyclerAdapter.MenuItems.startDate.value -> {
                getDateTime(appGlobals.activeProcess.start_time, ActivityRequestCodes.startDate)
            }
            DetailsRecyclerAdapter.MenuItems.finishDate.value -> {
                getDateTime(appGlobals.activeProcess.finish_time, ActivityRequestCodes.finishDate)
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        val p = appGlobals.activeProcess

        // A list selection has been made
        if (requestCode == ActivityRequestCodes.listSelection.value && data != null)
        {
            val listId = data!!.getIntExtra("listId", -1)
            val listItem = data!!.getStringExtra("listValue")

            when (listId)
            {
                ListSelectionActivity.ListContext.technicians.value -> p.technician_name = listItem
                ListSelectionActivity.ListContext.vehicles.value -> p.vehicle_name = listItem
                ListSelectionActivity.ListContext.installTechs.value -> p.pt_installation_tech = listItem
                ListSelectionActivity.ListContext.clients.value -> p.client = listItem
                ListSelectionActivity.ListContext.pipeType.value -> p.pipe_description = listItem
            }
        }

        if (requestCode == ActivityRequestCodes.addressSelection.value && data != null)
        {
            appGlobals.activeProcess.address = data!!.getStringExtra("address")
        }

        if (requestCode == ActivityRequestCodes.notes.value && data != null)
        {
            appGlobals.activeProcess.general_other = data!!.getStringExtra(NotesActivity.NOTES_EXTRA)
        }

        p.save(this)
        assignOutlets()
    }



}
