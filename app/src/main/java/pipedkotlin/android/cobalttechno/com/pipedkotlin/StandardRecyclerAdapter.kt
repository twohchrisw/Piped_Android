package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import java.util.*

class StandardRecyclerAdapter(val ctx: Context, val pipedTask: PipedTask, var lastLat: Double, var lastLng: Double): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class PipedTask {
        Swabbing, Filling, Chlorination, DeChlorination, Flushing, Flushing2, Surveying, Sampling
    }

    enum class SwabbingRows(val value: PipedTableRow)
    {
        SwabbingDetailsHeader(PipedTableRow(0, PipedTableRow.PipedTableRowType.SectionHeader, "SWABBING DETAILS")),
        SwabLoaded(PipedTableRow(1, PipedTableRow.PipedTableRowType.DateSetLocation, "Swab Loaded", "", EXLDProcess.c_swab_loaded)),
        StartedSwabRun(PipedTableRow(2, PipedTableRow.PipedTableRowType.DateSet, "Started Swab Run", "", EXLDProcess.c_swab_run_started)),
        FlowratesHeader(PipedTableRow(3, PipedTableRow.PipedTableRowType.SectionHeader, "FLOWRATES")),
        Count(PipedTableRow(4, PipedTableRow.PipedTableRowType.Count, ""));

        companion object {
            fun tableRowFromPosition(position: Int): PipedTableRow?
            {
                when (position)
                {
                    SwabbingDetailsHeader.value.position -> return SwabbingDetailsHeader.value
                    SwabLoaded.value.position -> return SwabLoaded.value
                    StartedSwabRun.value.position -> return StartedSwabRun.value
                    FlowratesHeader.value.position -> return FlowratesHeader.value
                    Count.value.position -> return Count.value
                }

                return null
            }
        }
    }

    override fun getItemCount(): Int {
        when (pipedTask)
        {
            PipedTask.Swabbing -> return SwabbingRows.Count.value.position
        }

        return 0
    }

    override fun getItemViewType(position: Int): Int {

        var tableRow: PipedTableRow? = null
        if (pipedTask == PipedTask.Swabbing)
        {
            tableRow = SwabbingRows.tableRowFromPosition(position)!!
        }
        // . . . for each PipedTask

        if (tableRow != null)
        {
            return tableRow!!.rowType.value
        }
        else
        {
            return  PipedTableRow.PipedTableRowType.Unknown.value
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder? {
        when (viewType)
        {
            PipedTableRow.PipedTableRowType.SectionHeader.value -> {
                val view = LayoutInflater.from(parent?.context).inflate(R.layout.view_holder_standard_header, parent, false)
                return ViewHolderStandardHeader(view)
            }

            PipedTableRow.PipedTableRowType.DateSet.value -> {
                val view = LayoutInflater.from(parent?.context).inflate(R.layout.view_holder_date_set, parent, false)
                return ViewHolderDateSet(view)
            }

            PipedTableRow.PipedTableRowType.DateSetLocation.value -> {
                val view = LayoutInflater.from(parent?.context).inflate(R.layout.view_holder_date_set, parent, false)
                return ViewHolderDateSet(view)
            }

            PipedTableRow.PipedTableRowType.AddFlowrateButton.value -> {

            }

            PipedTableRow.PipedTableRowType.Flowrate.value -> {

            }

            PipedTableRow.PipedTableRowType.Notes.value -> {

            }

        }

        return null
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {

        var tableRow: PipedTableRow? = null
        if (pipedTask == PipedTask.Swabbing)
        {
            tableRow = SwabbingRows.tableRowFromPosition(position)
        }
        if (pipedTask == PipedTask.Filling)
        {

        }
        // etc . . .

        val p = AppGlobals.instance.activeProcess
        if (tableRow != null)
        {
            when (tableRow.rowType)
            {
                /* Section Headers */

                PipedTableRow.PipedTableRowType.SectionHeader -> {
                    val viewHolder = holder as ViewHolderStandardHeader
                    viewHolder.headerText?.text = tableRow.title
                }

                /* Date Set Location */

                PipedTableRow.PipedTableRowType.DateSetLocation -> {
                    val viewHolder = holder as ViewHolderDateSet
                    viewHolder.tvTitle?.text = tableRow.title
                    viewHolder.tvLocation?.visibility = View.VISIBLE

                    when (tableRow.field)
                    {
                        EXLDProcess.c_swab_loaded -> {
                            formatSwabLoaded(viewHolder)
                        }
                    }
                }

                /* Date Set */

                PipedTableRow.PipedTableRowType.DateSet -> {
                    val viewHolder = holder as ViewHolderDateSet
                    viewHolder.tvTitle?.text = tableRow.title
                    viewHolder.tvLocation?.visibility = View.GONE

                    var theDate = ""

                    when (tableRow.field)
                    {
                        EXLDProcess.c_swab_run_started -> theDate = p.swab_run_started
                    }

                    if (theDate.length > 1)
                    {
                        viewHolder.tvValue?.text = DateHelper.dbDateStringFormattedWithSeconds(theDate)
                        viewHolder.btnSet?.text = "Reset"
                    }
                    else
                    {
                        viewHolder.tvValue?.text = "(none)"
                        viewHolder.btnSet?.text = "Set"
                    }
                }
            }
        }
    }

    fun formatSwabLoaded(viewHolder: ViewHolderDateSet)
    {
        val p = AppGlobals.instance.activeProcess
        val theDate = p.swab_loaded
        val lat = p.swab_latitude
        val lng = p.swab_longitude

        viewHolder.btnSet?.setOnClickListener {
            if (viewHolder.btnSet?.text == "Set") {
                resetSwabbing()
            }
            else
            {
                val alert = AlertHelper(ctx)
                alert.dialogForOKAlert("Reset Task", "Are you sure you want to reset this task (including resetting flowrates)?", ::undoSwabbing)

            }
            notifyDataSetChanged()
        }

        viewHolder.tvLocation?.text = NumbersHelper.latLongString(lat, lng)
        if (theDate.length > 1)
        {
            viewHolder.tvValue?.text = DateHelper.dbDateStringFormattedWithSeconds(theDate)
        }
        else
        {
            viewHolder.tvValue?.text = "(none)"
        }

        if (p.swab_loaded.length > 1 && p.swab_removed.length == 0)
        {
            viewHolder.btnSet?.text = "Reset"
        }
        else
        {
            viewHolder.btnSet?.text = "Set"
        }

        if (lat == 0.0 && lng == 0.0)
        {
            viewHolder.tvLocation?.visibility = View.GONE
        }
    }

    fun resetSwabbing()
    {
        val p = AppGlobals.instance.activeProcess
        p.swab_loaded = DateHelper.dateToDBString(Date())
        p.swab_run_started = ""
        p.swab_home = ""
        p.swab_removed = ""
        p.swab_total_water = 0.0
        p.swab_latitude = lastLat
        p.swab_longitude = lastLng
        p.swab_removed_lat = 0.0
        p.swab_removed_long = 0.0
        p.save(ctx)
    }

    fun undoSwabbing()
    {
        val p = AppGlobals.instance.activeProcess
        p.swab_loaded = ""
        p.swab_latitude = 0.0
        p.swab_longitude = 0.0
        p.save(ctx)

        notifyDataSetChanged()
    }

}

class PipedTableRow(val position: Int, val rowType: PipedTableRowType, val title: String, val table: String = "", val field: String = "", val flowrateId: Int = -1) {

    enum class PipedTableRowType(val value: Int) {
        DateSet(0), DateSetLocation(1), SectionHeader(2), AddFlowrateButton(3), Flowrate(4), Notes(5), Count(6), Unknown(7)
    }
}
