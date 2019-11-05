package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.utils.ColorTemplate

class PEGraphActivity : AppCompatActivity() {

    lateinit var tvPassFailLabel: TextView
    lateinit var tvResultLabel: TextView
    lateinit var tvAngleResultLabel: TextView
    lateinit var chartView: LineChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pegraph)
        supportActionBar?.title = "PE Test Results"
        assignOutlets()
        writeResults()
        configureChart()
    }

    fun assignOutlets()
    {
        tvPassFailLabel = findViewById<TextView>(R.id.tvPassFailLabel)
        tvResultLabel = findViewById(R.id.tvResultLabel)
        tvAngleResultLabel = findViewById(R.id.tvAngleResultLabel)
        chartView = findViewById(R.id.chartView)
    }

    fun writeResults()
    {
        val data = appGlobals.excelPEReadings
        if (data.calcPass)
        {
            tvPassFailLabel.text = "Test Passed"
            tvPassFailLabel.setTextColor(Color.GREEN)
        }
        else
        {
            tvPassFailLabel.text = "Test Failed"
            if (appGlobals.peFailMessage.length > 0)
            {
                tvPassFailLabel.text = "Test Failed: ${appGlobals.peFailMessage}"
                tvPassFailLabel.setTextColor(Color.RED)
            }
        }

        val nText = "n2 / n1 = ${data.n2.formatForDecPlaces(4)} / ${data.n1.formatForDecPlaces(4)} = ${data.calcResult.formatForDecPlaces(4)}"
        tvResultLabel.text = nText

        val tanText = "Tangential difference between sections = ${data.tangentialDifference().formatForDecPlaces(2)}"
        tvAngleResultLabel.text = tanText
    }

    fun configureChart()
    {
        var peReadings = appGlobals.excelPEReadings
        var dataArray = ArrayList<Entry>()
        dataArray.add(BarEntry(peReadings.log_t1.toFloat(), peReadings.log_pa_t1.toFloat()))
        dataArray.add(BarEntry(peReadings.log_t2.toFloat(), peReadings.log_pa_t2.toFloat()))
        dataArray.add(BarEntry(peReadings.log_t3.toFloat(), peReadings.log_pa_t3.toFloat()))


        val bardataSet = LineDataSet(dataArray, "")
        //chartView.animateY(2000)
        chartView.animate()
        chartView.description.isEnabled = false
        chartView.legend.isEnabled = false
        val data = LineData(bardataSet)
        chartView.data = data

    }
}
