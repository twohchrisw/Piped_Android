package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

class ViewHolderEquip(itemView: View?): RecyclerView.ViewHolder(itemView) {
    val mainText = itemView?.findViewById<TextView>(R.id.tvMainText)
    val linMain = itemView?.findViewById<LinearLayout>(R.id.linMain)
    val vwDot = itemView?.findViewById<View>(R.id.vwDot)
}