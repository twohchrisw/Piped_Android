package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView

class ViewHolderPicture(itemView: View?): RecyclerView.ViewHolder(itemView)
{
    val titleText = itemView?.findViewById<TextView>(R.id.tvTitle)
    val valueText = itemView?.findViewById<TextView>(R.id.tvValue)
    val picture = itemView?.findViewById<ImageView>(R.id.ivPicture)
}