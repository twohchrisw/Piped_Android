package pipedkotlin.android.cobalttechno.com.pipedkotlin

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.TextView

class ViewHolderEquipExtra(itemView: View): RecyclerView.ViewHolder(itemView) {
    val mainText = itemView?.findViewById<TextView>(R.id.tvMainText)
}