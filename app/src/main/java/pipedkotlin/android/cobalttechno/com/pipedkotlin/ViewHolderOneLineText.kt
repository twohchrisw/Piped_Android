package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView

class ViewHolderOneLineText(itemView: View?): RecyclerView.ViewHolder(itemView)
{
    val mainText = itemView?.findViewById<TextView>(R.id.tvMainText)
}