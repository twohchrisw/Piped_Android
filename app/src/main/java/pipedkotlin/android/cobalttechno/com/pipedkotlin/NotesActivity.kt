package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText

class NotesActivity : BaseActivity() {

    lateinit var etNotes: EditText

    companion object {
        val NOTES_EXTRA = "NOTES_EXTRA"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)
        supportActionBar?.title = "Notes"
        assignOutlets()
        etNotes.setText(intent.getStringExtra(NOTES_EXTRA))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.done_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item?.itemId == R.id.mnuDone)
        {
            val intent = Intent()
            intent.putExtra(NOTES_EXTRA, etNotes.text.toString())
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

        return super.onOptionsItemSelected(item)
    }

    fun assignOutlets()
    {
        etNotes = findViewById(R.id.etNotes) as EditText
    }
}
