package pipedkotlin.android.cobalttechno.com.pipedkotlin

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.View
import android.widget.TextView

class ViewHolderProcessList(itemView: View): RecyclerView.ViewHolder(itemView)
{
    val processNo = itemView?.findViewById<TextView>(R.id.tvProcessNo) as TextView
    val address = itemView?.findViewById<TextView>(R.id.tvAddress) as TextView
    val schemeName = itemView?.findViewById<TextView>(R.id.tvSchemeName) as TextView
    val clientName = itemView?.findViewById<TextView>(R.id.tvClientName) as TextView
    val createdOn = itemView?.findViewById<TextView>(R.id.tvCreatedOn) as TextView
    var syncMessage = itemView?.findViewById<TextView>(R.id.tvSyncMessage) as TextView

}