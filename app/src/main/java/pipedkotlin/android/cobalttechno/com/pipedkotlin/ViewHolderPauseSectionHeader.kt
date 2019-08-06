package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Button

class ViewHolderPauseSectionHeader(itemView: View?): RecyclerView.ViewHolder(itemView)
{
    val btnPause = itemView?.findViewById<Button>(R.id.btnPause)
    val btnAddFlowrate = itemView?.findViewById<Button>(R.id.btnAddFlowrate)
}