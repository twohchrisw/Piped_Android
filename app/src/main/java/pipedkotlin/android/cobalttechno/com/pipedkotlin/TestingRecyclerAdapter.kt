package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.icu.lang.UCharacter.GraphemeClusterBreak.L
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import junit.framework.Test

class TestingRecyclerAdapter(val testingContext: TestingSessionData.TestingContext, val clickListener: TestingRecyclerClickListener): RecyclerView.Adapter<RecyclerView.ViewHolder>()
{
    interface TestingRecyclerClickListener {
        fun didSelectPERow(row: PERows)
        fun didSelectDIRow(row: DIRows)
    }

    enum class PERows(val value: Int)
    {
        mainHeader(0), sectionName(1), sectionLength(2), pipeDiameter(3), installTech(4),
        pumpSize(5), volume(6), loggerDetails(7), startPressure(8), stp(9),
        readingsHeader(10), reading1(11), reading2(12), reading3(13), readingFooter(14), notes(17), count(18)
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
                PERows.mainHeader.value, PERows.readingsHeader.value -> TestingViewType.standardHeader.value
                PERows.sectionName.value -> TestingViewType.twoLineLatLng.value
                PERows.readingFooter.value -> TestingViewType.peFooter.value
                PERows.notes.value -> TestingViewType.oneLineStandard.value
            }
        }

        if (testingContext == TestingSessionData.TestingContext.di)
        {
            when (position)
            {
                DIRows.mainHeader.value, DIRows.readingsHeader.value -> TestingViewType.standardHeader.value
                DIRows.sectionName.value -> TestingViewType.twoLineLatLng.value
                DIRows.readingsFooter.value -> TestingViewType.diFooter.value
                DIRows.notes.value -> TestingViewType.oneLineStandard.value
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
                val view = LayoutInflater.from(parent?.context).inflate(R.layout.view_holder_title_value, parent, false)
                return ViewHolderTitleValue(view)
            }
            TestingViewType.standardHeader.value -> {
                val view = LayoutInflater.from(parent?.context).inflate(R.layout.view_holder_title_value, parent, false)
                return ViewHolderTitleValue(view)
            }
            TestingViewType.peFooter.value -> {
                val view = LayoutInflater.from(parent?.context).inflate(R.layout.view_holder_title_value, parent, false)
                return ViewHolderTitleValue(view)
            }
            TestingViewType.diFooter.value -> {
                val view = LayoutInflater.from(parent?.context).inflate(R.layout.view_holder_title_value, parent, false)
                return ViewHolderTitleValue(view)
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
    }

    // MARK: Cell formatting

    fun cellForTwoLineStandard(holder: RecyclerView.ViewHolder?, position: Int)
    {
        val viewHolder = holder as ViewHolderTitleValue
        val title = viewHolder.titleText
        val value = viewHolder.valueText
        val p = AppGlobals.instance.activeProcess

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
                    //TODO: Water volume calculation
                }
                PERows.loggerDetails.value -> {
                    title?.text = "Logger Details"
                    value?.text = p.pt_pe_logger_details.valueOrNone()
                }
                PERows.startPressure.value -> {
                    title?.text = "Start Pressure (bar)"
                    value?.text = p.pt_start_pressure.toString()
                }
                PERows.stp.value -> {
                    title?.text = "System Test Pressure (bar)"
                    value?.text = p.pt_system_test_pressure.toString()
                }
                PERows.reading1.value -> {
                    val reading1Date = p.pt_reading1_time
                    val r1Time = DateHelper.dbDateStringFormatted(reading1Date)
                    title?.text = "Reading 1: " + r1Time.valueOrNone()
                    value?.text = p.pt_reading_1.toString()
                }
                PERows.reading2.value -> {
                    val readingDate = p.pt_reading2_time
                    val rTime = DateHelper.dbDateStringFormatted(readingDate)
                    title?.text = "Reading 2: " + rTime.valueOrNone()
                    value?.text = p.pt_reading_2.toString()
                }
                PERows.reading3.value -> {
                    val readingDate = p.pt_reading3_time
                    val rTime = DateHelper.dbDateStringFormatted(readingDate)
                    title?.text = "Reading 3: " + rTime.valueOrNone()
                    value?.text = p.pt_reading_3.toString()
                }
            }
        }
        else
        {
            when (position)
            {
                DIRows.sectionLength.value -> {

                }
                DIRows.pipeDiameter.value -> {

                }
                DIRows.pumpSize.value -> {

                }
                DIRows.allowedLoss.value -> {

                }
                DIRows.loggerDetails.value -> {

                }
                DIRows.startPressure.value -> {

                }
                DIRows.stp.value -> {

                }
                DIRows.reading15.value -> {

                }
                DIRows.reading60.value -> {

                }
                DIRows.notes.value -> {

                }
            }
        }
    }

    fun cellForTwoLineLatLng(holder: RecyclerView.ViewHolder?, position: Int)
    {
        if (testingContext == TestingSessionData.TestingContext.pe)
        {

        }
        else
        {

        }
    }

    fun cellForStandardHeader(holder: RecyclerView.ViewHolder?, position: Int)
    {
        if (testingContext == TestingSessionData.TestingContext.pe)
        {

        }
        else
        {

        }
    }

    fun cellForPEFooter(holder: RecyclerView.ViewHolder?, position: Int)
    {

    }

    fun cellForDIFooter(holder: RecyclerView.ViewHolder?, position: Int)
    {

    }

    fun cellForOneLineText(holder: RecyclerView.ViewHolder?, position: Int)
    {

    }

}