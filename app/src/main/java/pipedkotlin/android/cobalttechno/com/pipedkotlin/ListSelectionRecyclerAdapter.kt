package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

class ListSelectionRecyclerAdapter(val listContext: Int, val listItems: List<EXLDListItems>?, val clients: List<EXLDClients>?, val clickListener: ListSelectionRecyclerClickListener): RecyclerView.Adapter<RecyclerView.ViewHolder>()
{
    interface ListSelectionRecyclerClickListener {
        fun listItemClicked(listContext: Int, listItem: EXLDListItems)
        fun clientItemClicked(client: EXLDClients)
    }

    override fun getItemCount(): Int {
        if (listContext == ListSelectionActivity.ListContext.clients.value)
        {
            return clients?.size ?: 0
        }
        else
        {
            return listItems?.size ?: 0
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.view_holder_one_line_text, parent, false)
        return ViewHolderOneLineText(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val viewHolder = holder as ViewHolderOneLineText
        val mainText = viewHolder.mainText

        if (listContext == ListSelectionActivity.ListContext.clients.value)
        {
            val client = clients!!.get(position)
            mainText?.text = client.clientName
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
            else
            {
                clickListener.listItemClicked(listContext, listItems!!.get(position))
            }
        })
    }
}