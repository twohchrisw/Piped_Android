package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

class ProcessListRecyclerAdapter(val processes: List<EXLDProcess>): RecyclerView.Adapter<RecyclerView.ViewHolder>()
{
    override fun getItemCount(): Int {
        return processes.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {

        val process = processes.get(position)
        val viewHolder = holder as ViewHolderProcessList
        viewHolder.processNo.text = process.processNoDescription()
        viewHolder.address.text = process.address.toString()
        viewHolder.schemeName.text = process.scheme_name
        viewHolder.clientName.text = process.client
        viewHolder.createdOn.text = process.create_timestamp
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.view_holder_process_list, parent, false)
        return ViewHolderProcessList(view)
    }
}