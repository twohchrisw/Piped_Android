package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView

class ViewHolderReadingsFooter(itemView: View?): RecyclerView.ViewHolder(itemView)
{
    val headerText = itemView?.findViewById<TextView>(R.id.tvHeaderText)
    val pressurisingStarted = itemView?.findViewById<TextView>(R.id.tvPressurisingStarted)
    val pressureReaced = itemView?.findViewById<TextView>(R.id.tvPressureReached)
    val calcResult = itemView?.findViewById<TextView>(R.id.tvCalcResult)
    val testStatus = itemView?.findViewById<TextView>(R.id.tvTestStatus)
}