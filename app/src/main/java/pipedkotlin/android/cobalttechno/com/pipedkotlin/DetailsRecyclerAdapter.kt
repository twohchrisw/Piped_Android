package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.Menu
import android.view.ViewGroup
import java.util.*

class DetailsRecyclerAdapter(val clickListener: DetailsRecyclerClickListener): RecyclerView.Adapter<RecyclerView.ViewHolder>()
{
    interface DetailsRecyclerClickListener {
        fun listItemClicked(menuItem: Int)
    }

    enum class MenuItems(val value: Int) {
        companyId(0),
        technician(1),
        vehicle(2),
        installationTech(3),
        client(4),
        scheme(5),
        address(6),
        pipeType(7),
        pipeLength(8),
        pipeDiameter(9),
        notes(10),
        startDate(11),
        finishDate(12),
        count(13)
    }

    override fun getItemCount(): Int {
        return MenuItems.count.value
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.view_holder_title_value, parent, false)
        return ViewHolderTitleValue(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val viewHolder = holder as ViewHolderTitleValue
        val title = viewHolder.titleText
        val value = viewHolder.valueText
        val p = appGlobals.activeProcess

        viewHolder.itemView.setOnClickListener({
            clickListener.listItemClicked(position)
        })

        when (position)
        {
            MenuItems.companyId.value -> {
                title?.text = "Company ID"
                value?.text = p.company_id
            }

            MenuItems.technician.value -> {
                title?.text = "Technician"
                value?.text = if (p.technician_name.isNotEmpty()) p.technician_name else "(none)"
            }

            MenuItems.vehicle.value -> {
                title?.text = "Vehicle"
                value?.text = if (p.vehicle_name.isNotEmpty()) p.vehicle_name else "(none)"
            }

            MenuItems.installationTech.value -> {
                title?.text = "Installation Technique"
                if (p.pt_installation_tech.toLowerCase().equals("other"))
                {
                    value?.text = p.pt_installation_tech_other
                }
                else
                {
                    value?.text = if (p.pt_installation_tech.isNotEmpty()) p.pt_installation_tech else "(none)"
                }
            }

            MenuItems.client.value -> {
                title?.text = "Client"
                value?.text = if (p.client.isNotEmpty()) p.client else "(none)"
            }

            MenuItems.scheme.value -> {
                title?.text = "Scheme Name"
                value?.text = if (p.scheme_name.isNotEmpty()) p.scheme_name else "(none)"
            }

            MenuItems.address.value -> {
                title?.text = "Address"
                value?.text = if (p.address.isNotEmpty()) p.address else "(none)"
            }

            MenuItems.pipeType.value -> {
                title?.text = "Pipe Type"
                value?.text = if (p.pipe_description.isNotEmpty()) p.pipe_description else "(none)"
            }

            MenuItems.pipeLength.value -> {
                title?.text = "Pipe Length (m)"
                value?.text = p.pipe_length.toString()
            }

            MenuItems.pipeDiameter.value -> {
                title?.text = "Pipe Diameter (mm)"
                value?.text = p.pipe_diameter.toString()
            }

            MenuItems.notes.value -> {
                title?.text = "Notes"
                value?.text = if (p.general_other.isNotEmpty()) p.general_other else "(none)"
            }

            MenuItems.startDate.value -> {
                title?.text = "Start Date/Time"
                value?.text = if (p.start_time.isNotEmpty()) DateHelper.dbDateStringFormatted(p.start_time) else "(none)"
            }

            MenuItems.finishDate.value -> {
                title?.text = "Finish Date/Time"
                value?.text = if (p.finish_time.isNotEmpty()) DateHelper.dbDateStringFormatted(p.finish_time) else "(none)"
            }
        }
    }
}