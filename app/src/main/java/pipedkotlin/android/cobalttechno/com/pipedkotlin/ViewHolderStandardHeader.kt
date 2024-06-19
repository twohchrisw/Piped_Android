package pipedkotlin.android.cobalttechno.com.pipedkotlin

//import android.support.v7.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.TextView

class ViewHolderStandardHeader(itemView: View): RecyclerView.ViewHolder(itemView)
{
    val headerText = itemView?.findViewById<TextView>(R.id.tvHeaderText)
}