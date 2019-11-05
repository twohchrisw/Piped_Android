package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import java.text.SimpleDateFormat
import java.util.*

class TestingRecyclerAdapter(val testingContext: TestingSessionData.TestingContext, val testingSession: TestingSessionData, val clickListener: TestingRecyclerClickListener): RecyclerView.Adapter<RecyclerView.ViewHolder>()
{

    interface TestingRecyclerClickListener {
        fun didSelectPERow(row: Int)
        fun didSelectDIRow(row: Int)
    }

    enum class PERows(val value: Int)
    {
        mainHeader(0), sectionName(1), sectionLength(2), pipeDiameter(3), installTech(4),
        pumpSize(5), volume(6), loggerDetails(7), startPressure(8), stp(9),
        readingsHeader(10), reading1(11), reading2(12), reading3(13), readingFooter(14), notes(15), count(16)
    }

    enum class DIRows(val value: Int)
    {
        mainHeader(0), sectionName(1), sectionLength(2), pipeDiameter(3), pumpSize(4), allowedLoss(5),
        loggerDetails(6), startPressure(7), stp(8), readingsHeader(9), reading15(10), reading60(11), readingsFooter(12), notes(13), count(14)
    }

    enum class TestingViewType(val value: Int)
    {
        twoLineStandard(0), twoLineLatLng(1), standardHeader(2), peFooter(3), diFooter(4), oneLineStandard(5)
    }

    override fun getItemCount(): Int {

        if (testingContext == TestingSessionData.TestingContext.pe)
        {
            //Log.d("cob2", "Recycler got item count ${PERows.count.value}")
            return PERows.count.value
        }
        else
        {
            return DIRows.count.value
        }
    }

    // Which cell are we using for the row
    override fun getItemViewType(position: Int): Int {
        if (testingContext == TestingSessionData.TestingContext.pe)
        {
            when (position)
            {
                PERows.mainHeader.value, PERows.readingsHeader.value -> return TestingViewType.standardHeader.value
                PERows.sectionName.value -> return TestingViewType.twoLineLatLng.value
                PERows.readingFooter.value -> return TestingViewType.peFooter.value
                PERows.notes.value -> return TestingViewType.oneLineStandard.value
            }
        }

        if (testingContext == TestingSessionData.TestingContext.di)
        {
            when (position)
            {
                DIRows.mainHeader.value, DIRows.readingsHeader.value -> return TestingViewType.standardHeader.value
                DIRows.sectionName.value -> return TestingViewType.twoLineLatLng.value
                DIRows.readingsFooter.value -> return TestingViewType.diFooter.value
                DIRows.notes.value -> return TestingViewType.oneLineStandard.value
            }
        }

        return TestingViewType.twoLineStandard.value
    }

    // Return the correct cell for the row
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder? {
        when (viewType)
        {
            TestingViewType.twoLineStandard.value -> {
                val view = LayoutInflater.from(parent?.context).inflate(R.layout.view_holder_title_value, parent, false)
                return ViewHolderTitleValue(view)
            }
            TestingViewType.twoLineLatLng.value -> {
                val view = LayoutInflater.from(parent?.context).inflate(R.layout.view_holder_title_value_lat_lng, parent, false)
                return ViewHolderTitleValueLatLng(view)
            }
            TestingViewType.standardHeader.value -> {
                val view = LayoutInflater.from(parent?.context).inflate(R.layout.view_holder_standard_header, parent, false)
                return ViewHolderStandardHeader(view)
            }
            TestingViewType.peFooter.value -> {
                val view = LayoutInflater.from(parent?.context).inflate(R.layout.view_holder_readings_footer, parent, false)
                return ViewHolderReadingsFooter(view)
            }
            TestingViewType.diFooter.value -> {
                val view = LayoutInflater.from(parent?.context).inflate(R.layout.view_holder_readings_footer, parent, false)
                return ViewHolderReadingsFooter(view)
            }
            TestingViewType.oneLineStandard.value -> {
                val view = LayoutInflater.from(parent?.context).inflate(R.layout.view_holder_one_line_text, parent, false)
                return ViewHolderOneLineText(view)
            }
        }

        return null
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        when (holder?.itemViewType)
        {
            TestingViewType.twoLineStandard.value -> cellForTwoLineStandard(holder, position)
            TestingViewType.twoLineLatLng.value -> cellForTwoLineLatLng(holder, position)
            TestingViewType.standardHeader.value -> cellForStandardHeader(holder, position)
            TestingViewType.peFooter.value -> cellForPEFooter(holder, position)
            TestingViewType.diFooter.value -> cellForDIFooter(holder, position)
            TestingViewType.oneLineStandard.value -> cellForOneLineText(holder, position)
        }

        holder?.itemView?.setOnClickListener({
            if (testingContext == TestingSessionData.TestingContext.pe)
            {
                clickListener.didSelectPERow(position)
            }
            else {
                clickListener.didSelectDIRow(position)
            }
        })
    }

    // MARK: Cell formatting

    fun cellForTwoLineStandard(holder: RecyclerView.ViewHolder?, position: Int)
    {
        val viewHolder = holder as ViewHolderTitleValue
        val title = viewHolder.titleText
        val value = viewHolder.valueText
        value?.setTextColor(Color.parseColor("#1c3d92"));
        val p = appGlobals.activeProcess

        if (testingContext == TestingSessionData.TestingContext.pe)
        {
            when (position)
            {
                PERows.sectionLength.value -> {
                    title?.text = "Section Length (m)"
                    value?.text = p.pt_section_length.valueOrNone()

                }
                PERows.pipeDiameter.value -> {
                    title?.text = "Pipe Diameter (mm)"
                    value?.text = p.pt_pe_pipe_diameter.toString()
                }
                PERows.installTech.value -> {
                    title?.text = "Installation Technique"
                    value?.text = p.pt_pe_it.valueOrNone()
                }
                PERows.pumpSize.value -> {
                    title?.text = "Pump Size"
                    value?.text = p.pt_pe_pump_size.valueOrNone()
                }
                PERows.volume.value -> {
                    title?.text = "Volume Added (ltrs)"
                    value?.text = p.waterVolumePressurisedString(testingContext)
                }
                PERows.loggerDetails.value -> {
                    title?.text = "Logger Details"
                    value?.text = p.pt_pe_logger_details.valueOrNone()
                }
                PERows.startPressure.value -> {
                    title?.text = "Start Pressure (bar)"
                    value?.text = p.pt_start_pressure.formatForDecPlaces(3)
                }
                PERows.stp.value -> {
                    title?.text = "System Test Pressure (bar)"
                    value?.text = p.pt_system_test_pressure.formatForDecPlaces(3)
                }
                PERows.reading1.value -> {
                    val reading1Date = p.pt_reading1_time
                    val r1Time = DateHelper.dbDateStringFormattedWithSeconds(reading1Date)
                    title?.text = "Reading 1: " + r1Time.valueOrNone()
                    val reading1 = p.pt_reading_1
                    Log.d("cob2", "REFRESH TABLE: READING 1 IS $reading1")
                    value?.text = p.pt_reading_1.formatForDecPlaces(3)

                    val r1AsDate = DateHelper.dbStringToDateOrNull(reading1Date)
                    if (r1AsDate != null)
                    {
                        if (r1AsDate.time < Date().time && reading1 == 0.0)
                        {
                            value?.setTextColor(Color.parseColor("#ff0000"))
                        }
                    }
                }
                PERows.reading2.value -> {
                    val readingDate = p.pt_reading2_time
                    val rTime = DateHelper.dbDateStringFormattedWithSeconds(readingDate)
                    title?.text = "Reading 2: " + rTime.valueOrNone()
                    val reading2 = p.pt_reading_2
                    Log.d("cob2", "REFRESH TABLE: READING 2 IS $reading2")
                    value?.text = p.pt_reading_2.formatForDecPlaces(3)

                    val r1AsDate = DateHelper.dbStringToDateOrNull(readingDate)
                    if (r1AsDate != null)
                    {
                        if (r1AsDate.time < Date().time && reading2 == 0.0)
                        {
                            value?.setTextColor(Color.parseColor("#ff0000"))
                        }
                    }
                }
                PERows.reading3.value -> {
                    val readingDate = p.pt_reading3_time
                    val rTime = DateHelper.dbDateStringFormattedWithSeconds(readingDate)
                    title?.text = "Reading 3: " + rTime.valueOrNone()
                    value?.text = p.pt_reading_3.formatForDecPlaces(3)

                    val r1AsDate = DateHelper.dbStringToDateOrNull(readingDate)
                    if (r1AsDate != null)
                    {
                        if (r1AsDate.time < Date().time && p.pt_reading_3 == 0.0)
                        {
                            value?.setTextColor(Color.parseColor("#ff0000"))
                        }
                    }
                }
            }
        }
        else
        {
            when (position)
            {
                DIRows.sectionLength.value -> {
                    title?.text = "Section Length (m)"
                    value?.text = p.pt_di_section_length.valueOrNone()
                }
                DIRows.pipeDiameter.value -> {
                    title?.text = "Pipe Diameter (mm)"
                    value?.text = p.pt_di_pipe_diameter.toString()
                }
                DIRows.pumpSize.value -> {
                    title?.text = "Pump Size"
                    value?.text = p.pt_di_pump_size.valueOrNone()
                }
                DIRows.allowedLoss.value -> {
                    title?.text = "Allowed Loss"
                    if (p.di_is_zero_loss == 0)
                    {
                        value?.text = "0.2 bar"
                    }
                    else
                    {
                        value?.text = "0.0 bar"
                    }
                }
                DIRows.loggerDetails.value -> {
                    title?.text = "Logger Details"
                    value?.text = p.pt_di_logger_details.valueOrNone()
                }
                DIRows.startPressure.value -> {
                    title?.text = "Start Pressure (bar)"
                    value?.text = p.pt_di_start_pressure.formatForDecPlaces(3)
                }
                DIRows.stp.value -> {
                    title?.text = "System Test Pressure (bar)"
                    value?.text = p.pt_di_stp.formatForDecPlaces(3)
                }
                DIRows.reading15.value -> {
                    val readingDate = p.pt_di_r15_time
                    val r15Time = DateHelper.dbDateStringFormattedWithSeconds(readingDate)
                    title?.text = "Reading 15m: " + r15Time.valueOrNone()
                    value?.text = p.pt_di_r15_value.formatForDecPlaces(3)
                }
                DIRows.reading60.value -> {
                    val readingDate = p.pt_di_r60_time
                    val r60Time = DateHelper.dbDateStringFormattedWithSeconds(readingDate)
                    title?.text = "Reading 60m: " + r60Time.valueOrNone()
                    value?.text = p.pt_di_r60_value.formatForDecPlaces(3)
                }
            }
        }
    }

    fun cellForTwoLineLatLng(holder: RecyclerView.ViewHolder?, position: Int)
    {
        val p = appGlobals.activeProcess
        val viewHolder = holder as ViewHolderTitleValueLatLng
        if (testingContext == TestingSessionData.TestingContext.pe)
        {
            viewHolder.titleText?.text = "Test Section Name"
            viewHolder.valueText?.text = p.pt_section_name.valueOrNone()
            viewHolder.latLngText?.text = NumbersHelper.latLongString(p.pt_lat, p.pt_long)
        }
        else
        {
            viewHolder.titleText?.text = "Test Section Name"
            viewHolder.valueText?.text= p.pt_di_section_name.valueOrNone()
            viewHolder.latLngText?.text = NumbersHelper.latLongString(p.di_lat, p.di_long)
        }
    }

    fun cellForStandardHeader(holder: RecyclerView.ViewHolder?, position: Int)
    {
        val viewHolder = holder as ViewHolderStandardHeader
        if (testingContext == TestingSessionData.TestingContext.pe)
        {
            when (position)
            {
                PERows.mainHeader.value -> viewHolder.headerText?.text = "TESTING SETTINGS"
                PERows.readingsHeader.value -> viewHolder.headerText?.text = "READINGS"
            }
        }
        else
        {
            when (position)
            {
                DIRows.mainHeader.value -> viewHolder.headerText?.text = "TESTING SETTINGS"
                DIRows.readingsHeader.value -> viewHolder.headerText?.text = "READINGS"
            }
        }
    }

    // Readings footer for PE
    fun cellForPEFooter(holder: RecyclerView.ViewHolder?, position: Int)
    {
        val viewHolder = holder as ViewHolderReadingsFooter
        val p = appGlobals.activeProcess

        viewHolder.pressurisingStarted?.text = "Pressurising Started:"
        viewHolder.pressureReaced?.text = "Pressure Reached: "
        viewHolder.calcResult?.text = "Calc Result: N/A"
        viewHolder.testStatus?.text = "In Progress"
        viewHolder.headerText?.text = "NOTES"
        viewHolder.testLabel?.text = ""

        if (DateHelper.dateIsValid(p.pt_pressurising_start))
        {
            viewHolder.pressurisingStarted?.text = "Pressurising Started: " + DateHelper.dbDateStringFormattedWithSeconds(p.pt_pressurising_start)
        }

        if (DateHelper.dateIsValid(p.pt_pressurising_finish))
        {
            viewHolder.pressureReaced?.text = "Pressure Reached: " + DateHelper.dbDateStringFormattedWithSeconds(p.pt_pressurising_finish)
        }

        if (arePEReadingsComplete())
        {
            if (p.isWaitingForPEData()) {
                viewHolder.testStatus?.text = "Waiting for data"
            }
            else
            {
                viewHolder.testStatus?.text = "Ready to Calculate"
            }
        }

        // Air Pressure Calc
        viewHolder.testLabel?.text = ""
        if (p.isPEPressurising())
        {
            var airPercentage = 0
            var pstart = DateHelper.dbStringToDateOrNull(appGlobals.activeProcess.pt_pressurising_start)

            //if (pstart != null && pend != null)
            //{
                val pressurisingSeconds = (Date().time - pstart!!.time) / 1000
                Log.d("petest", "Pressurising seconds: $pressurisingSeconds")
                val airCalc = AirPressureCalc(appGlobals.activeProcess, TestingSessionData.TestingContext.pe)
                if (airCalc.isValid().first)
                {
                    val airPressureSeconds = airCalc.performCalc()
                    if (airPressureSeconds != null)
                    {
                        val calendar = Calendar.getInstance()
                        calendar.time = pstart
                        calendar.add(Calendar.SECOND, airPressureSeconds.fourPercent)
                        val cutOff4PercentSeconds = calendar.time
                        val format = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
                        val formatted4Percent = format.format(cutOff4PercentSeconds)

                        if (pressurisingSeconds >= airPressureSeconds!!.onePercent && pressurisingSeconds < airPressureSeconds!!.twoPrecent)
                        {
                            airPercentage = 1
                        }
                        if (pressurisingSeconds >= airPressureSeconds!!.twoPrecent && pressurisingSeconds < airPressureSeconds!!.threePercent)
                        {
                            airPercentage = 2
                        }
                        if (pressurisingSeconds >= airPressureSeconds!!.threePercent && pressurisingSeconds < airPressureSeconds!!.fourPercent)
                        {
                            airPercentage = 3
                        }
                        if (pressurisingSeconds >= airPressureSeconds.fourPercent)
                        {
                            airPercentage = 4
                        }

                        if (airPercentage > 0)
                        {
                            viewHolder.testLabel?.text = "4% Cut Off: $formatted4Percent Pipe has $airPercentage% air"
                        }
                        else
                        {
                            viewHolder.testLabel?.text = "4% Cut Off: $formatted4Percent"
                        }
                    }
                    else
                    {
                        viewHolder.testLabel?.text = "AP Calc Error: No Time Returned"
                    }
            }
            else {
                    viewHolder.testLabel?.text = "AP CALC Error: ${airCalc.isValid().second}"
                }

        }
        else
        {
            val airPercentage = p.pt_reading_5.toInt()
            if (airPercentage > 0)
            {
                viewHolder.testLabel?.text = "Pipe has $airPercentage% air"
            }
        }

        // Calculation results
        if (DateHelper.dateIsValid(p.pt_pressurising_finish))
        {
            if (arePEReadingsComplete() && !p.isWaitingForPEData() && p.havePECalculationResult())
            {
                if (p.pe_pdf_pass == 0)
                {
                    viewHolder.testStatus?.text = "Test Failed!"
                    //TODO: Format label for failed
                }
                else
                {
                    viewHolder.testStatus?.text = "Test Passed"
                    //TODO: Format label for passed
                }

                viewHolder.calcResult?.text = p.peCalcResultDescription()
            }
        }
    }

    fun arePEReadingsComplete(): Boolean
    {
        val p = appGlobals.activeProcess

        if (testingSession.timerStage > 6)
        {
            return true
        }

        if (p.pt_pe_readings_count == 3 && testingSession.timerStage > 3)
        {
            return true
        }

        if (p.pt_pe_readings_count == 4 && testingSession.timerStage > 4)
        {
            return true
        }

        val r3Time = DateHelper.dbStringToDate(p.pt_reading3_time, Date())
        if (p.pt_reading_1 > 0 && p.pt_reading_2 > 0 && p.pt_reading_3 > 0 && r3Time.time < Date().time)
        {
            return true
        }

        return false
    }

    // Readings footer for DI
    fun cellForDIFooter(holder: RecyclerView.ViewHolder?, position: Int)
    {
        val viewHolder = holder as ViewHolderReadingsFooter
        val p = appGlobals.activeProcess

        viewHolder.pressurisingStarted?.text = "Test Started: N/A"
        viewHolder.pressureReaced?.text = ""
        viewHolder.calcResult?.text = "Calc Result: N/A"
        viewHolder.testStatus?.text = "Test Status: N/A"
        viewHolder.headerText?.text = "NOTES"
        viewHolder.pressureReaced?.visibility = View.GONE
        viewHolder.testLabel?.visibility = View.GONE

        if (!DateHelper.dateIsValid(p.pt_di_pressurising_started))
        {
            return
        }

        viewHolder.pressurisingStarted?.text = "Test Started: ${DateHelper.dbDateStringFormattedWithSeconds(p.pt_di_pressurising_started)}"

        if (p.isDITestRunning())
        {
            val now = Date()
            val r60Time = DateHelper.dbStringToDate(p.pt_di_r60_time, Date())
            val r15Time = DateHelper.dbStringToDate(p.pt_di_r15_time, Date())

            if (now.time < r60Time.time)
            {
                viewHolder.testStatus?.text = "In Progress"

                if (testingSession.timerStage > 0)
                {
                    if (p.pt_di_r15_value > 0.0)
                    {
                        if (p.di_is_zero_loss == 0)
                        {
                            if (p.getDIR15CalcResult() < appGlobals.DI_15_MIN_MAXIMUM)
                            {
                                viewHolder.testStatus?.text = "Passing . . ."
                            }
                            else
                            {
                                viewHolder.testStatus?.text = "Failing . . ."
                            }
                        }

                        if (p.di_is_zero_loss == 1)
                        {
                            if (p.getDIR15CalcResult() < appGlobals.DI_TESTING_ZERO_LOSS_VALUE)
                            {
                                viewHolder.testStatus?.text = "Passing . . ."
                            }
                            else
                            {
                                viewHolder.testStatus?.text = "Failing . . ."
                            }
                        }

                        viewHolder.calcResult?.text = "Calc Result (15m): ${p.getDIR15CalcResult().formatForDecPlaces(4)}"
                    }
                    else
                    {
                        viewHolder.testStatus?.text = "Waiting for Data"
                    }
                }
            }
            else
            {
                if (p.pt_di_r15_value == 0.0 || p.pt_di_r60_value == 0.0)
                {
                    viewHolder.testStatus?.text = "Waiting for Data"
                }
                else
                {
                    if (p.di_is_zero_loss == 0)
                    {
                        if (p.getDIR60CalcResult() < appGlobals.DI_TESTING_VALUE)
                        {
                            viewHolder.testStatus?.text = "TEST PASSED!"
                        }
                        else
                        {
                            viewHolder.testStatus?.text = "TEST FAILED!"
                        }
                    }

                    if (p.di_is_zero_loss == 1)
                    {
                        if (p.getDIR60CalcResult() < appGlobals.DI_TESTING_ZERO_LOSS_VALUE)
                        {
                            viewHolder.testStatus?.text = "TEST PASSED!"
                        }
                        else
                        {
                            viewHolder.testStatus?.text = "TEST FAILED!"
                        }
                    }

                    viewHolder.calcResult?.text = "Calc Result (60m): ${p.getDIR60CalcResult().formatForDecPlaces(4)}"
                }
            }
        }

    }

    fun cellForOneLineText(holder: RecyclerView.ViewHolder?, position: Int)
    {
        val viewHolder = holder as ViewHolderOneLineText
        val p = appGlobals.activeProcess
        if (testingContext == TestingSessionData.TestingContext.pe)
        {
            if (position == TestingRecyclerAdapter.PERows.notes.value)
            {
                viewHolder.mainText?.text = p.pt_pe_notes.valueOrNone()
            }
        }
        else
        {
            if (position == TestingRecyclerAdapter.DIRows.notes.value)
            {
                viewHolder.mainText?.text = p.pt_di_notes.valueOrNone()
            }
        }

    }

}