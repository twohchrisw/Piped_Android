package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.widget.EditText

open class BaseActivity: AppCompatActivity() {

    public fun nz(value: String?): String
    {
        if (value == null)
        {
            return ""
        }
        else
        {
            return value
        }
    }

}