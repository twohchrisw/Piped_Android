package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

class ListSelectionRecyclerAdapter(val listContext: Int, val listItems: List<EXLDListItems>?, val clients: List<EXLDClients>?, val clickListener: ListSelectionRecyclerClickListener): RecyclerView.Adapter<RecyclerView.ViewHolder>()
{
    interface ListSelectionRecyclerClickListener {
        fun listItemClicked(listContext: Int, listItem: EXLDListItems)
        fun clientItemClicked(client: EXLDClients)
        fun stringItemClicked(value: String)
    }

    val PIPE_TYPES = arrayListOf("(none)", "PE", "DI", "ST", "PVC", "PE SDR 11", "PE SDR 17", "PE SDR 26")
    val PUMP_TYPES = arrayListOf("(none)", "Hand Pump", "6 ltr/min Pressure Test Pump", "12 ltr/min Pressure Test Pump", "30 ltr/min Pressure Test Pump", "50 ltr/min Pressure Test Pump", "64 ltr/min Pressure Test Pump", "120 ltr/min Pressure Test Pump",
            "150 ltr/min Pressure Test Pump", "250 ltr/min Pressure Test Pump", "400 ltr/min Pressure Test Pump", "500 ltr/min Pressure Test Pump")

    override fun getItemCount(): Int {
        if (listContext == ListSelectionActivity.ListContext.clients.value)
        {
            return clients?.size ?: 0
        }
        else if (listContext == ListSelectionActivity.ListContext.pipeType.value)
        {
            return PIPE_TYPES.size
        }
        else if (listContext == ListSelectionActivity.ListContext.pumpType.value)
        {
            return PUMP_TYPES.size
        }
        else
        {
            return listItems?.size ?: 0
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.view_holder_one_line_text, parent, false)
        return ViewHolderOneLineText(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder = holder as ViewHolderOneLineText
        val mainText = viewHolder.mainText

        if (listContext == ListSelectionActivity.ListContext.clients.value)
        {
            val client = clients!!.get(position)
            mainText?.text = client.clientName
        }
        else if (listContext == ListSelectionActivity.ListContext.pipeType.value)
        {
            mainText?.text = PIPE_TYPES.get(position)
        }
        else if (listContext == ListSelectionActivity.ListContext.pumpType.value)
        {
            mainText?.text = PUMP_TYPES.get(position)
        }
        else
        {
            val listItem = listItems!!.get(position)
            mainText?.text = listItem.listItem
        }

        viewHolder.itemView.setOnClickListener({
            if (listContext == ListSelectionActivity.ListContext.clients.value) {
                clickListener.clientItemClicked(clients!!.get(position))
            }
            else if (listContext == ListSelectionActivity.ListContext.pipeType.value)
            {
                clickListener.stringItemClicked(PIPE_TYPES.get(position))
            }
            else if (listContext == ListSelectionActivity.ListContext.pumpType.value)
            {
                clickListener.stringItemClicked(PUMP_TYPES.get(position))
            }
            else
            {
                clickListener.listItemClicked(listContext, listItems!!.get(position))
            }
        })
    }
}