package pipedkotlin.android.cobalttechno.com.pipedkotlin

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView

class ViewHolderDateSet(itemView: View): RecyclerView.ViewHolder(itemView) {
    val tvTitle = itemView?.findViewById<TextView>(R.id.tvTitle)
    val tvValue = itemView?.findViewById<TextView>(R.id.tvValue)
    val tvLocation = itemView?.findViewById<TextView>(R.id.tvLocation)
    val btnSet = itemView?.findViewById<Button>(R.id.btnSet)
    val ivPicture = itemView?.findViewById<ImageView>(R.id.ivPicture)

    init {
        ivPicture?.visibility = View.GONE
    }
}