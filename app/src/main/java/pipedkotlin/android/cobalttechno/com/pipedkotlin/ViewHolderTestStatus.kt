package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView

class ViewHolderTestStatus(itemView: View?): RecyclerView.ViewHolder(itemView) {
    val radioGroup = itemView?.findViewById<RadioGroup>(R.id.radioGroup)
    val rdNotSet = itemView?.findViewById<RadioButton>(R.id.rdNotSet)
    val rdFail = itemView?.findViewById<RadioButton>(R.id.rdFail)
    val rdPass = itemView?.findViewById<RadioButton>(R.id.rdPass)
    val tvFailMessage = itemView?.findViewById<TextView>(R.id.tvFailMessage)
}