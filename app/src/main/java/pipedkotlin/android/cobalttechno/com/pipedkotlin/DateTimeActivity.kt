package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import java.util.*

class DateTimeActivity : BaseActivity() {

    companion object {
        val DATE_EXTRA = "DATE_EXTRA"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_date_time)
        supportActionBar?.title = "Date/Time"
        assignOutlets()
        setCurrentDate()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.done_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item?.itemId == R.id.mnuDone)
        {
            val intent = Intent()
            intent.putExtra(DateTimeActivity.DATE_EXTRA, "")
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

        return super.onOptionsItemSelected(item)
    }

    fun assignOutlets()
    {

    }

    fun setCurrentDate()
    {
        // Set current date
        val dateAsString = intent.getStringExtra(DateTimeActivity.DATE_EXTRA)
        var currentDate = Date()
        if (dateAsString != null) {
            if (dateAsString.isNotEmpty())
            {
                currentDate = DateHelper.dbStringToDate(dateAsString, Date())
            }
        }
    }
}
