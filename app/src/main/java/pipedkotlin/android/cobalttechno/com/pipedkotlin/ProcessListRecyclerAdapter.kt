package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class ProcessListRecyclerAdapter(val processes: List<EXLDProcess>, val clickListener: ProcessListRecyclerViewClickListener): RecyclerView.Adapter<RecyclerView.ViewHolder>()
{
    interface ProcessListRecyclerViewClickListener {
        fun listItemClicked(process: EXLDProcess)
    }

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

        if (AppGlobals.instance.syncManager.processBeingSynced?.columnId == process.columnId)
        {
            viewHolder.syncMessage.visibility = View.VISIBLE
            viewHolder.syncMessage.text = "[Sync in Progress]"
        }
        else if (process.needsSync())
        {
            viewHolder.syncMessage.visibility = View.VISIBLE
            viewHolder.syncMessage.text = "[Needs Sync]"
        }
        else
        {
            viewHolder.syncMessage.visibility = View.GONE
            viewHolder.syncMessage.text = ""
        }

        Log.d("cobsync", "process sync: ${process.last_sync_millis}, update: ${process.last_update_millis}")
        Log.d("cobcalib", process.calibDetailsDescription())

        // Add the click listener
        viewHolder.itemView.setOnClickListener({
            clickListener.listItemClicked(process)
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.view_holder_process_list, parent, false)
        return ViewHolderProcessList(view)
    }

}