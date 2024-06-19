package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class EquipmentRecycler(val ctx: Context): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var equipRows = ArrayList<EquipRow>()

    val VIEW_TYPE_TEXT_LINE = 0
    val VIEW_TYPE_HEADER = 1
    val VIEW_TYPE_ADDITIONAL = 2

    init {
        refresh()
    }

    fun createEquipmentRows()
    {
        val p = appGlobals.activeProcess
        equipRows = ArrayList<EquipRow>()

        // DOSING

        equipRows.add(EquipRow("DOSING EQUIPMENT", false, false, true, ""))
        equipRows.add(EquipRow("1000 ltrs/min Dosing Pump", p.equip_dp_1000 == 1, false, false, EXLDProcess.c_equip_dp_1000))
        equipRows.add(EquipRow("1500 ltrs/min Dosing Trailer", p.equip_dt_1500 == 1, false, false, EXLDProcess.c_equip_dt_1500))
        equipRows.add(EquipRow("3000 ltrs/min Dosing Trailer", p.equip_dt_3000 == 1, false, false, EXLDProcess.c_equip_dt_3000))
        equipRows.add(EquipRow("5000 ltrs/min Dosing Trailer", p.equip_dt_5000 == 1, false, false, EXLDProcess.c_equip_dt_5000))
        equipRows.add(EquipRow("Direct Injection Dosing Pump", p.equip_direct_injection == 1, false, false, EXLDProcess.c_equip_direct_injection))
        equipRows.add(EquipRow("Add Additional . . .", false, false, false, "", true, EXLDEquipmentExtra.EquipSection.Dosing.value))

        val additionalDosing = EXLDEquipmentExtra.getExtrasForSection(ctx, p.columnId, EXLDEquipmentExtra.EquipSection.Dosing.value)
        for (row in additionalDosing)
        {
            equipRows.add(EquipRow(row.ee_desc, false, true, false, ""))
        }

        // PRESSURE TEST PUMPS

        equipRows.add(EquipRow("PRESSURE TEST PUMPS", false, false, true, ""))
        equipRows.add(EquipRow("12 ltr/min Pressure Test Pump", p.equip_ptp_12 == 1, false, false, EXLDProcess.c_equip_ptp_12))
        equipRows.add(EquipRow("30 ltr/min Pressure Test Pump", p.equip_ptp_30 == 1, false, false, EXLDProcess.c_equip_ptp_30))
        equipRows.add(EquipRow("50 ltr/min Pressure Test Pump", p.equip_ptp_50 == 1, false, false, EXLDProcess.c_equip_ptp_50))
        equipRows.add(EquipRow("120 ltr/min Pressure Test Pump", p.equip_ptp_120 == 1, false, false, EXLDProcess.c_equip_ptp_120))
        equipRows.add(EquipRow("250 ltr/min Pressure Test Pump", p.equip_ptp_250 == 1, false, false, EXLDProcess.c_equip_ptp_250))
        equipRows.add(EquipRow("Add Additional . . .", false, false, false, "", true, EXLDEquipmentExtra.EquipSection.PTP.value))

        val additionalPTP = EXLDEquipmentExtra.getExtrasForSection(ctx, p.columnId, EXLDEquipmentExtra.EquipSection.PTP.value)
        for (row in additionalPTP)
        {
            equipRows.add(EquipRow(row.ee_desc, false, true, false, ""))
        }

        // LEAK DETECTION

        equipRows.add(EquipRow("LEAK DETECTION", false, false, true, ""))
        equipRows.add(EquipRow("Corelator", p.equip_corelator == 1, false, false, EXLDProcess.c_equip_corelator))
        equipRows.add(EquipRow("Electronic Listening Stick", p.equip_elec_listening_stick == 1, false, false, EXLDProcess.c_equip_elec_listening_stick))
        equipRows.add(EquipRow("Hydrogen Gas Detector", p.equip_hydrogen_detector == 1, false, false, EXLDProcess.c_equip_hydrogen_detector))
        equipRows.add(EquipRow("Add Additional . . .", false, false, false, "", true, EXLDEquipmentExtra.EquipSection.Leak.value))

        val additionalLeak = EXLDEquipmentExtra.getExtrasForSection(ctx, p.columnId, EXLDEquipmentExtra.EquipSection.Leak.value)
        for (row in additionalLeak)
        {
            equipRows.add(EquipRow(row.ee_desc, false, true, false, ""))
        }

        // MEASUREMENT

        equipRows.add(EquipRow("MEASUREMENT", false, false, true, ""))
        equipRows.add(EquipRow("Chlorometer", p.equip_chlorometer == 1, false, false, EXLDProcess.c_equip_chlorometer))
        equipRows.add(EquipRow("Chlorosense", p.equip_chlorosense == 1, false, false, EXLDProcess.c_equip_chlorosense))
        equipRows.add(EquipRow("Turbidity Meter", p.equip_turbidity == 1, false, false, EXLDProcess.c_equip_turbidity))
        equipRows.add(EquipRow("Ferometer", p.equip_ferometer == 1, false, false, EXLDProcess.c_equip_ferometer))
        equipRows.add(EquipRow("Data Logger", p.equip_data_logger == 1, false, false, EXLDProcess.c_equip_data_logger))
        equipRows.add(EquipRow("Add Additional . . .", false, false, false, "", true, EXLDEquipmentExtra.EquipSection.Measure.value))

        val additionalMeasure = EXLDEquipmentExtra.getExtrasForSection(ctx, p.columnId, EXLDEquipmentExtra.EquipSection.Measure.value)
        for (row in additionalMeasure)
        {
            equipRows.add(EquipRow(row.ee_desc, false, true, false, ""))
        }

        // OTHER

        equipRows.add(EquipRow("OTHER", false, false, true, ""))
        equipRows.add(EquipRow("Swab Tracking Equipment", p.equip_swab_tracking == 1, false, false, EXLDProcess.c_equip_swab_tracking))
        equipRows.add(EquipRow("Add Additional . . .", false, false, false, "", true, EXLDEquipmentExtra.EquipSection.Other.value))

        val additionalOther = EXLDEquipmentExtra.getExtrasForSection(ctx, p.columnId, EXLDEquipmentExtra.EquipSection.Other.value)
        for (row in additionalOther)
        {
            equipRows.add(EquipRow(row.ee_desc, false, true, false, ""))
        }
    }

    fun refresh()
    {
        createEquipmentRows()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return equipRows.size
    }

    override fun getItemViewType(position: Int): Int {

        val row = equipRows[position]

        if (row.isAdditional)
        {
            return VIEW_TYPE_ADDITIONAL
        }

        if (row.isHeader)
        {
            return VIEW_TYPE_HEADER
        }

        return VIEW_TYPE_TEXT_LINE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == VIEW_TYPE_HEADER)
        {
            val view = LayoutInflater.from(parent?.context).inflate(R.layout.view_holder_standard_header, parent, false)
            return ViewHolderStandardHeader(view)
        }

        if (viewType == VIEW_TYPE_ADDITIONAL)
        {
            val view = LayoutInflater.from(parent?.context).inflate(R.layout.view_holder_equip_extra, parent, false)
            return ViewHolderEquipExtra(view)
        }

        val view = LayoutInflater.from(parent?.context).inflate(R.layout.view_holder_equip, parent, false)
        return ViewHolderEquip(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val row = equipRows[position]

        if (row.isHeader)
        {
            val viewHolder = holder as ViewHolderStandardHeader
            viewHolder.headerText?.text = row.title
            return
        }

        if (row.isAdditional)
        {
            val viewHolder = holder as ViewHolderEquipExtra
            viewHolder.mainText?.text = row.title
            return
        }

        val viewHolder = holder as ViewHolderEquip
        viewHolder.mainText?.text = row.title
        if (row.isChecked)
        {
            viewHolder.vwDot?.visibility = View.VISIBLE
        }
        else
        {
            viewHolder.vwDot?.visibility = View.GONE
        }

        if (row.isButton)
        {
            viewHolder.itemView.setOnClickListener {
                addAdditional(row)
            }
        }
        else
        {
            viewHolder.itemView.setOnClickListener {
                toggleChecked(row)
            }
        }
    }

    fun addAdditional(row: EquipRow)
    {
        val alert = AlertHelper(ctx)
        var title = ""
        when (row.section)
        {
            EXLDEquipmentExtra.EquipSection.Dosing.value -> title = "Add additional Dosing Equipment"
            EXLDEquipmentExtra.EquipSection.PTP.value -> title = "Add additional PTP Equipment"
            EXLDEquipmentExtra.EquipSection.Leak.value -> title = "Add additional Leak Detection Equipment"
            EXLDEquipmentExtra.EquipSection.Measure.value -> title = "Add additional Measurement Equipment"
            EXLDEquipmentExtra.EquipSection.Other.value -> title = "Add additional Other Equipment"
        }

        alert.dialogForTextInput(title, "", {
            if (it.length > 0)
            {
                EXLDEquipmentExtra.addExtra(ctx, appGlobals.activeProcess.columnId, it, row.section)
                refresh()
            }
        })
    }

    fun toggleChecked(row: EquipRow)
    {
        val p = appGlobals.activeProcess

        var setValue = 1

        if (row.isChecked)
        {
            setValue = 0
        }

        when (row.field)
        {
            EXLDProcess.c_equip_dp_1000 -> p.equip_dp_1000 = setValue
            EXLDProcess.c_equip_dt_1500 -> p.equip_dt_1500 = setValue
            EXLDProcess.c_equip_dt_3000 -> p.equip_dt_3000 = setValue
            EXLDProcess.c_equip_dt_5000 -> p.equip_dt_5000 = setValue
            EXLDProcess.c_equip_direct_injection -> p.equip_direct_injection = setValue

            EXLDProcess.c_equip_ptp_12 -> p.equip_ptp_12 = setValue
            EXLDProcess.c_equip_ptp_30 -> p.equip_ptp_30 = setValue
            EXLDProcess.c_equip_ptp_50 -> p.equip_ptp_50 = setValue
            EXLDProcess.c_equip_ptp_120 -> p.equip_ptp_120 = setValue
            EXLDProcess.c_equip_ptp_250 -> p.equip_ptp_250 = setValue

            EXLDProcess.c_equip_corelator -> p.equip_corelator = setValue
            EXLDProcess.c_equip_elec_listening_stick -> p.equip_elec_listening_stick = setValue
            EXLDProcess.c_equip_hydrogen_detector -> p.equip_hydrogen_detector = setValue

            EXLDProcess.c_equip_chlorometer -> p.equip_chlorometer = setValue
            EXLDProcess.c_equip_chlorosense -> p.equip_chlorosense = setValue
            EXLDProcess.c_equip_turbidity -> p.equip_turbidity = setValue
            EXLDProcess.c_equip_ferometer -> p.equip_ferometer = setValue
            EXLDProcess.c_equip_data_logger -> p.equip_data_logger = setValue

            EXLDProcess.c_equip_swab_tracking -> p.equip_swab_tracking = setValue

        }

        p.save(ctx)
        refresh()
    }

    class EquipRow(var title: String, var isChecked: Boolean, var isAdditional: Boolean, var isHeader: Boolean, var field: String, var isButton: Boolean = false, var section: Int = -1)
    {
    }
}