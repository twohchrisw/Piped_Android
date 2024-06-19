package pipedkotlin.android.cobalttechno.com.pipedkotlin

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.TextView

class ViewHolderFlowrate(itemView: View): RecyclerView.ViewHolder(itemView) {
    val titleText = itemView?.findViewById<TextView>(R.id.tvTitle)
    val valueText = itemView?.findViewById<TextView>(R.id.tvValue)
}