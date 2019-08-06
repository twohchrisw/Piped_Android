package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.jetbrains.anko.db.classParser
import org.jetbrains.anko.db.delete
import org.jetbrains.anko.db.parseList
import org.jetbrains.anko.db.select
import java.util.*

class StandardRecyclerAdapter(val ctx: Context, val pipedTask: PipedTask, var lastLat: Double, var lastLng: Double,
                              var delegate: StandardRecyclerAdapterInterface?): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var pauseSessions: List<EXLDPauseSessions>? = null
    var taskIsPaused = false
    var currentPause: EXLDPauseSessions? = null
    var shouldCalculateTotalWater = false

    interface StandardRecyclerAdapterInterface {
        fun didRequestMainImage(fieldName: String)
        fun didRequestNotes(fieldName: String)
    }

    enum class PipedTask {
        Swabbing, Filling, Chlorination, DeChlorination, Flushing, Flushing2, Surveying, Sampling
    }

    enum class FillingRows(val value: PipedTableRow)
    {
        FillingDetailsHeader(PipedTableRow(0, PipedTableRow.PipedTableRowType.SectionHeader, "FILLNG DETAILS")),
        StartFilling(PipedTableRow(1, PipedTableRow.PipedTableRowType.DateSetLocation, "Start Filling", "", EXLDProcess.c_filling_started)),
        FlowratesHeader(PipedTableRow(2, PipedTableRow.PipedTableRowType.PauseSectionHeader, "FLOWRATES")),
        FillingDetailsContdHeader(PipedTableRow(3, PipedTableRow.PipedTableRowType.SectionHeader, "FILLING DETAILS CONTD")),
        MainFull(PipedTableRow(4, PipedTableRow.PipedTableRowType.DateSet, "Main Full", "", EXLDProcess.c_filling_stopped)),
        TotalFillingTime(PipedTableRow(5, PipedTableRow.PipedTableRowType.TitleValue, "Total Filling Time", "", "filling_time")),
        TotalWater(PipedTableRow(6, PipedTableRow.PipedTableRowType.TitleValue, "Total Water Volume (Ltrs)", "", EXLDProcess.c_filling_total_water_volume)),
        NotesSectionHeader(PipedTableRow(7, PipedTableRow.PipedTableRowType.SectionHeader, "NOTES")),
        Notes(PipedTableRow(8, PipedTableRow.PipedTableRowType.Notes, "", "", EXLDProcess.c_filling_notes)),
        FooterSection(PipedTableRow(9, PipedTableRow.PipedTableRowType.SectionHeader, "")),
        Count(PipedTableRow(10, PipedTableRow.PipedTableRowType.Count, ""));

        companion object {
            fun tableRowFromPosition(position: Int): PipedTableRow?
            {
                when (position)
                {
                    FillingDetailsHeader.value.position -> return FillingDetailsHeader.value
                    StartFilling.value.position -> return StartFilling.value
                    FlowratesHeader.value.position -> return FlowratesHeader.value
                    FillingDetailsContdHeader.value.position -> return FillingDetailsContdHeader.value
                    MainFull.value.position -> return MainFull.value
                    TotalFillingTime.value.position -> return TotalFillingTime.value
                    TotalWater.value.position -> return TotalWater.value
                    NotesSectionHeader.value.position -> return NotesSectionHeader.value
                    Notes.value.position -> return Notes.value
                    FooterSection.value.position -> return FooterSection.value
                    Count.value.position -> return Count.value
                }

                return null
            }
        }
    }

    enum class SwabbingRows(val value: PipedTableRow)
    {
        SwabbingDetailsHeader(PipedTableRow(0, PipedTableRow.PipedTableRowType.SectionHeader, "SWABBING DETAILS")),
        SwabLoaded(PipedTableRow(1, PipedTableRow.PipedTableRowType.DateSetLocation, "Swab Loaded", "", EXLDProcess.c_swab_loaded)),
        StartedSwabRun(PipedTableRow(2, PipedTableRow.PipedTableRowType.DateSet, "Started Swab Run", "", EXLDProcess.c_swab_run_started)),
        FlowratesHeader(PipedTableRow(3, PipedTableRow.PipedTableRowType.PauseSectionHeader, "FLOWRATES")),
        SwabbingDetailsContdHeader(PipedTableRow(4, PipedTableRow.PipedTableRowType.SectionHeader, "SWABBING DETAILS CONTD")),
        SwabHome(PipedTableRow(5, PipedTableRow.PipedTableRowType.DateSet, "Swab Home", "", EXLDProcess.c_swab_home)),
        SwabRemoved(PipedTableRow(6, PipedTableRow.PipedTableRowType.DateSetLocation, "Swab Removed", "", EXLDProcess.c_swab_removed)),
        TotalWater(PipedTableRow(7, PipedTableRow.PipedTableRowType.TitleValue, "Total Water Volume (Ltrs)", "", EXLDProcess.c_swab_total_water)),
        PhotoSectionHeader(PipedTableRow(8, PipedTableRow.PipedTableRowType.SectionHeader, "PHOTOS")),
        ConditionPhoto(PipedTableRow(9, PipedTableRow.PipedTableRowType.MainPicture, "Condition Photo", "", EXLDProcess.c_swab_photo)),
        NotesSectionHeader(PipedTableRow(10, PipedTableRow.PipedTableRowType.SectionHeader, "NOTES")),
        Notes(PipedTableRow(11, PipedTableRow.PipedTableRowType.Notes, "", "", EXLDProcess.c_swab_notes)),
        FooterSection(PipedTableRow(12, PipedTableRow.PipedTableRowType.SectionHeader, "")),
        Count(PipedTableRow(13, PipedTableRow.PipedTableRowType.Count, ""));

        companion object {
            fun tableRowFromPosition(position: Int): PipedTableRow?
            {
                when (position)
                {
                    SwabbingDetailsHeader.value.position -> return SwabbingDetailsHeader.value
                    SwabLoaded.value.position -> return SwabLoaded.value
                    StartedSwabRun.value.position -> return StartedSwabRun.value
                    FlowratesHeader.value.position -> return FlowratesHeader.value
                    SwabHome.value.position -> return SwabHome.value
                    SwabbingDetailsContdHeader.value.position -> return SwabbingDetailsContdHeader.value
                    SwabRemoved.value.position -> return SwabRemoved.value
                    TotalWater.value.position -> return TotalWater.value
                    ConditionPhoto.value.position -> return ConditionPhoto.value
                    Count.value.position -> return Count.value
                    PhotoSectionHeader.value.position -> return PhotoSectionHeader.value
                    NotesSectionHeader.value.position -> return NotesSectionHeader.value
                    Notes.value.position -> return Notes.value
                    FooterSection.value.position -> return FooterSection.value
                }

                return null
            }
        }
    }

    override fun getItemCount(): Int {

        val p = AppGlobals.instance.activeProcess
        when (pipedTask)
        {
            PipedTask.Swabbing -> return SwabbingRows.Count.value.position + EXLDSwabFlowrates.getSwabbingFlowrates(ctx, p.columnId).size
            PipedTask.Filling -> return FillingRows.Count.value.position + EXLDFillingFlowrates.getFillingFlowrates(ctx, p.columnId).size
        }

        return 0
    }

    override fun getItemViewType(position: Int): Int {

        if (isFlowratePosition(position).first)
        {
            return PipedTableRow.PipedTableRowType.Flowrate.value
        }

        // Remove the flowrates count from the position if after flowrate position

        val p = AppGlobals.instance.activeProcess
        var tableRow: PipedTableRow? = null
        if (pipedTask == PipedTask.Swabbing)
        {
            val flowrateStartRow = 4
            val frCount = EXLDSwabFlowrates.getSwabbingFlowrates(ctx, p.columnId).size
            var workingPosition = position
            if (position >= (frCount + flowrateStartRow))
            {
                workingPosition = workingPosition - frCount
            }

            tableRow = SwabbingRows.tableRowFromPosition(workingPosition)!!
        }

        if (pipedTask == PipedTask.Filling)
        {
            val flowrateStartRow = 3
            val frCount = EXLDFillingFlowrates.getFillingFlowrates(ctx, p.columnId).size
            var workingPosition = position
            if (position >= (frCount + flowrateStartRow))
            {
                workingPosition = workingPosition - frCount
            }

            tableRow = FillingRows.tableRowFromPosition(workingPosition)!!
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
                val view = LayoutInflater.from(parent?.context).inflate(R.layout.view_holder_flowrate, parent, false)
                return ViewHolderFlowrate(view)
            }

            PipedTableRow.PipedTableRowType.TitleValue.value -> {
                val view = LayoutInflater.from(parent?.context).inflate(R.layout.view_holder_title_value, parent, false)
                return ViewHolderTitleValue(view)
            }

            PipedTableRow.PipedTableRowType.Notes.value -> {
                val view = LayoutInflater.from(parent?.context).inflate(R.layout.view_holder_one_line_text, parent, false)
                return ViewHolderOneLineText(view)
            }

            PipedTableRow.PipedTableRowType.MainPicture.value -> {
                val view = LayoutInflater.from(parent?.context).inflate(R.layout.view_holder_picture, parent, false)
                return ViewHolderPicture(view)
            }

            PipedTableRow.PipedTableRowType.PauseSectionHeader.value -> {
                val view = LayoutInflater.from(parent?.context).inflate(R.layout.view_holder_pause_section_header, parent, false)
                return ViewHolderPauseSectionHeader(view)
            }

        }

        return null
    }

    fun isFlowratePosition(position: Int): Pair<Boolean, Int>
    {
        val p = AppGlobals.instance.activeProcess
        var flowrateCount = 0
        var flowrateStartRow = 0

        if (pipedTask == PipedTask.Swabbing)
        {
            flowrateCount = EXLDSwabFlowrates.getSwabbingFlowrates(ctx, p.columnId).size
            flowrateStartRow = 4
        }

        if (pipedTask == PipedTask.Filling)
        {
            flowrateCount = EXLDFillingFlowrates.getFillingFlowrates(ctx, p.columnId).size
            flowrateStartRow = 3
        }

        if (position >= flowrateStartRow && position < flowrateStartRow + flowrateCount)
        {
            // It's a flowrate
            val flowratePosition = position - flowrateStartRow
            return Pair(true, flowratePosition)
        }
        else
        {
            return Pair(false, 0)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {

        val p = AppGlobals.instance.activeProcess
        var tableRow: PipedTableRow? = null

        /* Flowrate positions, for all tasks */

        if (pipedTask == PipedTask.Swabbing)
        {
            val flowrateStartRow = 4
            val frCount = EXLDSwabFlowrates.getSwabbingFlowrates(ctx, p.columnId).size
            var workingPosition = position
            if (position >= (frCount + flowrateStartRow))
            {
                workingPosition = workingPosition - frCount
            }
            tableRow = SwabbingRows.tableRowFromPosition(workingPosition)
        }

        if (pipedTask == PipedTask.Filling)
        {
            val flowrateStartRow = 3
            val frCount = EXLDFillingFlowrates.getFillingFlowrates(ctx, p.columnId).size
            var workingPosition = position
            if (position >= (frCount + flowrateStartRow))
            {
                workingPosition = workingPosition - frCount
            }
            tableRow = FillingRows.tableRowFromPosition(workingPosition)
        }

        // etc . . .

        /* Flowrates */

        if (isFlowratePosition(position).first)
        {
            var dateString = ""
            var value = 0.0

            // It's a flowrate
            when (pipedTask)
            {
                PipedTask.Swabbing -> {
                    val flowrates = EXLDSwabFlowrates.getSwabbingFlowrates(ctx, p.columnId)
                    val flowrate = flowrates[isFlowratePosition(position).second]
                    dateString = DateHelper.dbDateStringFormattedWithSeconds(flowrate.swab_created)
                    value = flowrate.swab_flowrate
                }

                PipedTask.Filling -> {
                    val flowrates = EXLDFillingFlowrates.getFillingFlowrates(ctx, p.columnId)
                    val flowrate = flowrates[isFlowratePosition(position).second]
                    dateString = DateHelper.dbDateStringFormattedWithSeconds(flowrate.filling_created)
                    value = flowrate.filling_flowrate
                }
            }

            val viewHolder = holder as ViewHolderFlowrate
            viewHolder.titleText?.text = dateString
            viewHolder.valueText?.text = value.toString()
            return
        }

        /* Row Types */

        if (tableRow != null)
        {
            when (tableRow.rowType)
            {
                /* Section Headers */

                PipedTableRow.PipedTableRowType.SectionHeader -> {
                    val viewHolder = holder as ViewHolderStandardHeader
                    viewHolder.headerText?.text = tableRow.title
                }

                /* Notes */

                PipedTableRow.PipedTableRowType.Notes -> {
                    val viewHolder = holder as ViewHolderOneLineText
                    when (tableRow.field)
                    {
                        EXLDProcess.c_swab_notes -> viewHolder.mainText?.text = p.swab_notes
                        EXLDProcess.c_filling_notes -> viewHolder.mainText?.text = p.filling_notes
                    }

                    if (viewHolder.mainText!!.text!!.length < 1)
                    {
                        viewHolder.mainText?.text = "(none)"
                    }

                    viewHolder.itemView.setOnClickListener {
                        delegate?.didRequestNotes(tableRow.field)
                    }
                }

                /* Picture */

                PipedTableRow.PipedTableRowType.MainPicture -> {
                    val viewHolder = holder as ViewHolderPicture
                    viewHolder.titleText?.text = tableRow.title

                    when (tableRow.field)
                    {
                        EXLDProcess.c_swab_photo -> {
                            if (p.swab_photo.length < 2)
                            {
                                // No picture
                                viewHolder.picture?.visibility = View.GONE
                                viewHolder.valueText?.visibility = View.VISIBLE
                            }
                            else
                            {
                                // Have picture
                                viewHolder.valueText?.visibility = View.GONE
                                viewHolder.picture?.visibility = View.VISIBLE

                                val imageUri = AppGlobals.uriForSavedImage(p.swab_photo)
                                viewHolder.picture?.setImageURI(imageUri)
                            }
                        }
                    }

                    viewHolder.itemView.setOnClickListener {
                        delegate?.didRequestMainImage(tableRow.field)
                    }

                }

                /* Title Value */
                PipedTableRow.PipedTableRowType.TitleValue -> {
                    val viewHolder = holder as ViewHolderTitleValue
                    viewHolder.titleText?.text = tableRow.title

                    when (tableRow.field)
                    {
                        EXLDProcess.c_swab_total_water -> {

                            if (shouldCalculateTotalWater)
                            {
                                val totalWater = EXLDSwabFlowrates.totalWaterVolume(ctx, p.columnId, getPauseTypeString())
                                p.swab_total_water = totalWater
                                p.save(ctx)
                                shouldCalculateTotalWater = false
                            }
                            viewHolder.valueText?.text = p.swab_total_water.formatForDecPlaces(0)
                        }

                        EXLDProcess.c_filling_total_water_volume -> {
                            if (shouldCalculateTotalWater)
                            {
                                val totalWater = EXLDFillingFlowrates.totalWaterVolume(ctx, p.columnId, getPauseTypeString())
                                p.filling_total_water_volume = totalWater
                                p.save(ctx)
                                shouldCalculateTotalWater = false
                            }
                            viewHolder.valueText?.text = p.filling_total_water_volume.formatForDecPlaces(0)
                        }

                        "filling_time" -> {
                            val fillingStarted = DateHelper.dbStringToDateOrNull(p.filling_started)
                            val fillingStopped = DateHelper.dbStringToDateOrNull(p.filling_stopped)

                            if (fillingStarted != null && fillingStopped != null)
                            {
                                val totalMillis = fillingStopped!!.time - fillingStarted!!.time
                                viewHolder.valueText?.text = DateHelper.timeDifferenceFormattedForCountdown(totalMillis)
                            }
                            else
                            {
                                viewHolder.valueText?.text = DateHelper.timeDifferenceFormattedForCountdown(0)
                            }
                        }
                    }
                }

                /* Pause Headers */

                PipedTableRow.PipedTableRowType.PauseSectionHeader -> {
                    val viewHolder = holder as ViewHolderPauseSectionHeader

                    var isRunning = false
                    if (pipedTask == PipedTask.Swabbing)
                    {
                        isRunning = operationIsInProgress(p.swab_run_started, p.swab_home)
                    }
                    if (pipedTask == PipedTask.Filling)
                    {
                        isRunning = operationIsInProgress(p.filling_started, p.filling_stopped)
                    }

                    // Formatting
                    if (taskIsPaused)
                    {
                        viewHolder.btnPause?.text = "Restart"
                        viewHolder.btnPause?.setOnClickListener {
                            restartButtonPressed()
                        }

                        viewHolder.btnAddFlowrate?.visibility = View.GONE
                    }
                    else
                    {

                        viewHolder.btnAddFlowrate?.visibility = View.VISIBLE
                        viewHolder.btnAddFlowrate?.setOnClickListener {
                            addFlowratePressed()
                        }

                        if (isRunning)
                        {
                            viewHolder.btnPause?.visibility = View.VISIBLE
                            viewHolder.btnPause?.text = "Pause"
                            viewHolder.btnPause?.setOnClickListener {
                                pauseButtonPressed()
                            }
                        }
                        else
                        {
                            viewHolder.btnPause?.visibility = View.GONE
                        }
                    }
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

                        EXLDProcess.c_swab_removed -> {
                            formatSwabRemoved(viewHolder)
                        }

                        EXLDProcess.c_filling_started -> {
                            formatFillingStarted(viewHolder)
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
                        EXLDProcess.c_swab_home -> {
                            theDate = p.swab_home
                            closePauseSessions()
                        }
                        EXLDProcess.c_filling_stopped -> {
                            theDate = p.filling_stopped
                            closePauseSessions()
                        }
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

                    viewHolder.btnSet?.setOnClickListener {
                        val now = DateHelper.dateToDBString(Date())
                        when (tableRow.field)
                        {
                            EXLDProcess.c_swab_run_started -> p.swab_run_started = now
                            EXLDProcess.c_swab_home -> p.swab_home = now
                            EXLDProcess.c_filling_stopped -> p.filling_stopped = now
                        }

                        p.save(ctx)
                        notifyDataSetChanged()
                    }
                }
            }
        }
    }

    fun formatFillingStarted(viewHolder: ViewHolderDateSet)
    {
        val p = AppGlobals.instance.activeProcess
        val theDate = p.filling_started
        val lat = p.filling_lat
        val lng = p.filling_long

        viewHolder.btnSet?.setOnClickListener {
            if (viewHolder.btnSet?.text == "Set") {
                resetFilling()
            }
            else
            {
                val alert = AlertHelper(ctx)
                alert.dialogForOKAlert("Reset Task", "Are you sure you want to reset this task (including resetting flowrates)?", ::undoFilling)

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

        if (p.filling_started.length > 1 && p.filling_stopped.length == 0)
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

    fun formatSwabRemoved(viewHolder: ViewHolderDateSet)
    {
        val p = AppGlobals.instance.activeProcess
        val theDate = p.swab_removed
        val lat = p.swab_removed_lat
        val lng = p.swab_removed_long

        viewHolder.btnSet?.setOnClickListener {
            if (viewHolder.btnSet?.text == "Set")
            {
                p.swab_removed = DateHelper.dateToDBString(Date())
                p.swab_removed_lat = lastLat
                p.swab_removed_long = lastLng
            }
            else
            {
                p.swab_removed = ""
                p.swab_removed_lat = 0.0
                p.swab_removed_long = 0.0
            }
            p.save(ctx)
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

        if (p.swab_removed.length > 1)
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

        resetPauses()
        EXLDSwabFlowrates.deleteFlowrates(ctx, AppGlobals.instance.activeProcess.columnId)
        notifyDataSetChanged()
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

    fun resetFilling()
    {
        val p = AppGlobals.instance.activeProcess
        p.filling_started = DateHelper.dateToDBString(Date())
        p.filling_stopped = ""
        p.filling_lat = lastLat
        p.filling_long = lastLng
        p.filling_total_water_volume = 0.0
        p.save(ctx)

        resetPauses()
        EXLDFillingFlowrates.deleteFlowrates(ctx, p.columnId)
        notifyDataSetChanged()
    }

    fun undoFilling()
    {
        val p = AppGlobals.instance.activeProcess
        p.filling_started = ""
        p.filling_lat = 0.0
        p.filling_long = 0.0
        p.save(ctx)

        notifyDataSetChanged()
    }

    // Find these methods in TableViewBase.PauseSessions
    fun loadPauseSessions()
    {
        var pauseType = getPauseTypeString()
        if (pauseType.length > 1)
        {
            pauseSessions = ctx.database.use {
                select(EXLDPauseSessions.TABLE_NAME)
                        .whereArgs("${EXLDPauseSessions.COLUMN_PAUSE_PROCESS_ID} = ${AppGlobals.instance.activeProcess.columnId} AND ${EXLDPauseSessions.COLUMN_PAUSE_TYPE} = '$pauseType'")
                        .orderBy(EXLDPauseSessions.COLUMN_ID)
                        .exec {
                            parseList<EXLDPauseSessions>(classParser())
                        }
            }
        }
    }

    // Ensures tht orphan pauses are closed
    fun closePauseSessions()
    {
        var pauseType = getPauseTypeString()
        val pauses = ctx.database.use {
            select(EXLDPauseSessions.TABLE_NAME)
                    .whereArgs("${EXLDPauseSessions.COLUMN_PAUSE_PROCESS_ID} = ${AppGlobals.instance.activeProcess.columnId} AND ${EXLDPauseSessions.COLUMN_PAUSE_TYPE} = '$pauseType'")
                    .orderBy(EXLDPauseSessions.COLUMN_ID)
                    .exec {
                        parseList<EXLDPauseSessions>(classParser())
                    }
        }

        val p = AppGlobals.instance.activeProcess
        for (pause in pauses)
        {
            val pauseEnd = DateHelper.dbStringToDateOrNull(pause.pause_end)
            if (pauseEnd == null)
            {
                when (pause.pause_type)
                {
                    EXLDPauseSessions.PAUSE_TYPE_FILLING -> pause.pause_end = p.filling_stopped
                    EXLDPauseSessions.PAUSE_TYPE_SWABBING -> pause.pause_end = p.swab_home
                    EXLDPauseSessions.PAUSE_TYPE_CHLOR -> pause.pause_end = p.pt_chlor_main_chlorinated
                    EXLDPauseSessions.PAUSE_TYPE_DECHLOR -> pause.pause_end = p.pt_dec_dechlorinated
                    EXLDPauseSessions.PAUSE_TYPE_FLUSH -> pause.pause_end = p.pt_flush_completed
                    EXLDPauseSessions.PAUSE_TYPE_FLUSH2 -> pause.pause_end = p.pt_flush_completed2
                }
            }

            pause.save(ctx)
        }

        taskIsPaused = false
    }

    fun resetPauses()
    {
        currentPause = null
        taskIsPaused = false
        pauseSessions = null

        // Delete any existing pauses of the correct type
        var pauseType = getPauseTypeString()
        if (pauseType.length > 1)
        {
            ctx.database.use {
                delete(EXLDPauseSessions.TABLE_NAME, "${EXLDPauseSessions.COLUMN_PAUSE_PROCESS_ID} = ${AppGlobals.instance.activeProcess.columnId} AND ${EXLDPauseSessions.COLUMN_PAUSE_TYPE} = '$pauseType'")
            }
        }

        updateTotalWater()
    }

    
    fun pauseButtonPressed()
    {
        currentPause = EXLDPauseSessions()
        currentPause!!.pause_process_id = AppGlobals.instance.activeProcess.columnId
        currentPause!!.pause_type = getPauseTypeString()
        currentPause!!.pause_start = DateHelper.dateToDBString(Date())
        currentPause!!.pause_flowrate = getCurrentFlowrate()
        taskIsPaused = true
        currentPause!!.save(ctx)
        notifyDataSetChanged()
    }

    fun restartButtonPressed()
    {
        currentPause!!.pause_end = DateHelper.dateToDBString(Date())
        taskIsPaused = false
        currentPause!!.save(ctx)
        updateTotalWater()
        notifyDataSetChanged()
    }

    fun addFlowratePressed()
    {
        var guardLowerDate = ""
        var guardUpperDate = ""

        val p = AppGlobals.instance.activeProcess

        if (pipedTask == PipedTask.Swabbing)
        {
            guardLowerDate = p.swab_run_started
            guardUpperDate = p.swab_home
        }

        if (pipedTask == PipedTask.Filling)
        {
            guardLowerDate = p.filling_started
            guardUpperDate = p.filling_stopped
        }

        val lowerDate = DateHelper.dbStringToDateOrNull(guardLowerDate)
        val upperDate = DateHelper.dbStringToDateOrNull(guardUpperDate)

        if (lowerDate == null || upperDate != null)
        {
            val alert = AlertHelper(ctx)
            alert.dialogForOKAlertNoAction("Task Not Running", "The task must be running to enter a flowrate")
            return
        }

        val alert = AlertHelper(ctx)
        alert.dialogForTextInput("Enter Flowrate", "0", {
            val doubleValue = it.toDoubleOrNull()
            if (doubleValue != null)
            {
                createNewFlowrate(doubleValue)
            }
        })
    }

    fun createNewFlowrate(value: Double)
    {
        val p = AppGlobals.instance.activeProcess
        when (pipedTask)
        {
            PipedTask.Swabbing -> {

                val initialFrCount = EXLDSwabFlowrates.getSwabbingFlowrates(ctx, p.columnId).size
                Log.d("cobswab", "Flowate count is $initialFrCount")
                val fr = EXLDSwabFlowrates.createFlowrate(ctx, value, p.columnId)
                if (initialFrCount == 0) {
                    if (DateHelper.dbStringToDateOrNull(p.swab_run_started) != null)
                    {
                        fr.swab_created = p.swab_run_started
                    }
                }
                fr.save(ctx)
            }

            PipedTask.Filling -> {
                val initialFrCount = EXLDFillingFlowrates.getFillingFlowrates(ctx, p.columnId).size
                Log.d("cobswab", "Flowate count is $initialFrCount")
                val fr = EXLDFillingFlowrates.createFlowrate(ctx, value, p.columnId)
                if (initialFrCount == 0) {
                    if (DateHelper.dbStringToDateOrNull(p.filling_started) != null)
                    {
                        fr.filling_created = p.filling_started
                    }
                }
                fr.save(ctx)
            }
        }

        updateTotalWater()
        notifyDataSetChanged()
    }



    fun operationIsInProgress(startDateString: String, finishDateString: String): Boolean
    {
        val startDate = DateHelper.dbStringToDateOrNull(startDateString)
        val finishDate = DateHelper.dbStringToDateOrNull(finishDateString)

        if (startDate != null)
        {
            if (finishDate != null)
            {
                if (startDate.time < finishDate.time)
                {
                    return false
                }
                else
                {
                    return true
                }
            }
            else
            {
                return  true
            }
        }
        else
        {
            return false
        }
    }

    fun operationIsCompleted(startDateString: String, finishDateString: String): Boolean
    {
        val startDate = DateHelper.dbStringToDateOrNull(startDateString)
        val finishDate = DateHelper.dbStringToDateOrNull(finishDateString)

        if (startDate != null)
        {
            if (finishDate != null)
            {
                if (finishDate.time > startDate.time)
                {
                    return true
                }
            }
        }

        return false
    }

    fun getCurrentFlowrate(): Double
    {
        val p = AppGlobals.instance.activeProcess
        when (pipedTask)
        {
            PipedTask.Swabbing -> {
                val flowrates = EXLDSwabFlowrates.getSwabbingFlowrates(ctx, p.columnId)
                if (flowrates.size > 0)
                {
                    return flowrates.last().swab_flowrate
                }
                else
                {
                    return 0.0
                }
            }

            PipedTask.Filling -> {
                val flowrates = EXLDFillingFlowrates.getFillingFlowrates(ctx, p.columnId)
                if (flowrates.size > 0)
                {
                    return flowrates.last().filling_flowrate
                }
                else
                {
                    return 0.0
                }
            }
        }

        return 0.0
    }

    fun getPauseTypeString(): String
    {
        var pauseType = ""
        when (pipedTask)
        {
            PipedTask.Swabbing -> pauseType = EXLDPauseSessions.PAUSE_TYPE_SWABBING
            PipedTask.Filling -> pauseType = EXLDPauseSessions.PAUSE_TYPE_FILLING
            PipedTask.Chlorination -> pauseType = EXLDPauseSessions.PAUSE_TYPE_CHLOR
            PipedTask.DeChlorination -> pauseType = EXLDPauseSessions.PAUSE_TYPE_DECHLOR
            PipedTask.Flushing -> pauseType = EXLDPauseSessions.PAUSE_TYPE_FLUSH
            PipedTask.Flushing2 -> pauseType = EXLDPauseSessions.PAUSE_TYPE_FLUSH2
        }

        return pauseType
    }

    fun updateTotalWater()
    {
        shouldCalculateTotalWater = true
        notifyDataSetChanged()
    }

}

class PipedTableRow(val position: Int, val rowType: PipedTableRowType, val title: String, val table: String = "", val field: String = "", val flowrateId: Int = -1) {

    enum class PipedTableRowType(val value: Int) {
        DateSet(0), DateSetLocation(1), SectionHeader(2), AddFlowrateButton(3), Flowrate(4), Notes(5), PauseSectionHeader(6), Count(7), Unknown(8),
        TitleValue(9), MainPicture(10)
    }
}
