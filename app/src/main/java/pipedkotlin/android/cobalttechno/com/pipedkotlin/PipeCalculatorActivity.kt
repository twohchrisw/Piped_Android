package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Binder
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_pipe_calculator.*
import org.w3c.dom.Text

class PipeCalculatorActivity : AppCompatActivity() {

    lateinit var vwContainerView: View
    lateinit var vwButtonPEView: View
    lateinit var vwButtonMetallicView: View
    lateinit var btnPE: Button
    lateinit var btnMetallic: Button
    lateinit var lblTitleLabel: TextView
    lateinit var etDiameter: EditText
    lateinit var etFlowrate: EditText
    lateinit var etLength: EditText
    lateinit var etSDR: EditText
    lateinit var lblSDR: TextView
    lateinit var lblVolume: TextView
    lateinit var lblTimeToFill: TextView
    lateinit var lblDiameter: TextView
    lateinit var lblFlowrate: TextView
    lateinit var lblLength: TextView
    lateinit var lblVolumeTitle: TextView
    lateinit var lblTimeToFillTitle: TextView
    lateinit var vwSeperator: View
    lateinit var btnCalculate: Button
    lateinit var lblViewName: TextView

    enum class CalculatorTab {
        Metallic, PE
    }
    var selectedTab = CalculatorTab.Metallic

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pipe_calculator)
        assignOutlets()
        addListeners()

        formatForSelection()
        initialiseInputBoxes()
        supportActionBar?.title = "Pipe Calculator"

        if (appGlobals.calculatorTitle.isNotBlank())
        {
            tvViewName.text = "(" + appGlobals.calculatorTitle + ")";
            appGlobals.calculatorTitle = ""
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.done_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        if (item?.itemId == R.id.mnuDone)
        {
            finish()
        }

        return super.onOptionsItemSelected(item)
    }

    fun assignOutlets()
    {
        vwContainerView = findViewById(R.id.vwContainerView)
        vwButtonMetallicView = findViewById(R.id.buttonMetallicView)
        vwButtonPEView = findViewById(R.id.buttonPEView)
        btnMetallic = findViewById(R.id.btnMetallic)
        btnPE = findViewById(R.id.btnPE)
        lblTitleLabel = findViewById(R.id.lblTitleLabel)
        etDiameter = findViewById(R.id.etDiameter)
        etFlowrate = findViewById(R.id.etFlowrate)
        etLength = findViewById(R.id.etLength)
        etSDR = findViewById(R.id.etSDR)
        lblSDR = findViewById(R.id.lblSDR)
        lblVolume = findViewById(R.id.lblVolume)
        lblTimeToFill = findViewById(R.id.lblTimeToFill)
        lblDiameter = findViewById(R.id.lblDiameter)
        lblFlowrate = findViewById(R.id.lblFlowrate)
        lblLength = findViewById(R.id.lblLength)
        lblVolumeTitle = findViewById(R.id.lblVolumeTitle)
        lblTimeToFillTitle = findViewById(R.id.lblTimeToFillTitle)
        vwSeperator = findViewById(R.id.vwSeperator)
        btnCalculate = findViewById(R.id.btnCalculate)
        lblViewName = findViewById(R.id.tvViewName)
    }

    fun addListeners()
    {
        btnPE.setOnClickListener {
            selectedTab = CalculatorTab.PE
            formatForSelection()
        }

        btnMetallic.setOnClickListener {
            selectedTab = CalculatorTab.Metallic
            formatForSelection()
        }

        btnCalculate.setOnClickListener {
            calculate()
        }
    }

    fun formatForSelection()
    {
        initialiseInputBoxes()

        etDiameter.setTextColor(Color.BLACK)
        etFlowrate.setTextColor(Color.BLACK)
        etSDR.setTextColor(Color.BLACK)
        etLength.setTextColor(Color.BLACK)

        if (selectedTab == CalculatorTab.Metallic)
        {
            vwButtonMetallicView.bringToFront()
            vwContainerView.bringToFront()
            vwButtonMetallicView.background = resources.getDrawable(R.drawable.calc_active_tab)
            vwButtonPEView.background = resources.getDrawable(R.drawable.calc_inactive_tab)
            btnPE.setTextColor(Color.BLACK)
            btnMetallic.setTextColor(Color.WHITE)
            lblTitleLabel.setText("Metallic Pipe")
            lblSDR.visibility = View.GONE
            etSDR.visibility = View.GONE
            etLength.imeOptions = EditorInfo.IME_ACTION_DONE
        }

        if (selectedTab == CalculatorTab.PE)
        {
            vwButtonPEView.bringToFront()
            vwContainerView.bringToFront()
            vwButtonMetallicView.background = resources.getDrawable(R.drawable.calc_inactive_tab)
            vwButtonPEView.background = resources.getDrawable(R.drawable.calc_active_tab)
            btnPE.setTextColor(Color.WHITE)
            btnMetallic.setTextColor(Color.BLACK)
            lblTitleLabel.setText("Polyethelene Pipe")
            lblSDR.visibility = View.VISIBLE
            etSDR.visibility = View.VISIBLE

            etLength.imeOptions = EditorInfo.IME_ACTION_NEXT
            etSDR.imeOptions = EditorInfo.IME_ACTION_DONE
        }

        btnMetallic.bringToFront()
        btnPE.bringToFront()
        lblTitleLabel.bringToFront()
        lblDiameter.bringToFront()
        etDiameter.bringToFront()
        lblFlowrate.bringToFront()
        etFlowrate.bringToFront()
        lblLength.bringToFront()
        etLength.bringToFront()
        lblSDR.bringToFront()
        etSDR.bringToFront()
        vwSeperator.bringToFront()
        lblVolumeTitle.bringToFront()
        lblVolume.bringToFront()
        lblTimeToFillTitle.bringToFront()
        lblTimeToFill.bringToFront()
        btnCalculate.bringToFront()
        tvViewName.bringToFront()
    }

    fun initialiseInputBoxes()
    {
        etDiameter.setText("0")
        etLength.setText("0")
        etSDR.setText("0")
        etFlowrate.setText("0")
        lblVolume.setText("0 Ltrs")
        lblTimeToFill.setText("0hr 0m")
    }

    fun validateInputs(): CalcInputs
    {
        var isValid = true
        etDiameter.setTextColor(Color.BLACK)
        etLength.setTextColor(Color.BLACK)
        etSDR.setTextColor(Color.BLACK)
        etFlowrate.setTextColor(Color.BLACK)

        val failColor = Color.RED

        val diameterInput = etDiameter.text.toString()
        val sdrInput = etSDR.text.toString()
        val lengthInput = etLength.text.toString()
        val flowrateInput = etFlowrate.text.toString()

        val diam = diameterInput.toDoubleOrNull()
        val sdr = sdrInput.toDoubleOrNull()
        val len = lengthInput.toDoubleOrNull()
        val flowrate = flowrateInput.toDoubleOrNull()

        if (diam == null) {
            isValid = false
            etDiameter.setTextColor(failColor)
        }

        if (sdr == null)
        {
            isValid = false
            etSDR.setTextColor(failColor)
        }

        if (len == null)
        {
            isValid = false
            etLength.setTextColor(failColor)
        }

        if (flowrate == null)
        {
            isValid = false
            etFlowrate.setTextColor(failColor)
        }

        if (isValid)
        {
            val calcInputs = CalcInputs()
            calcInputs.isValid = true
            calcInputs.diameter = diam!!
            calcInputs.flowrate = flowrate!!
            calcInputs.length = len!!
            calcInputs.sdr = sdr!!
            return calcInputs
        }
        else
        {
            val calcInputs = CalcInputs()
            calcInputs.isValid = false
            return calcInputs
        }
    }

    fun calculate()
    {
        val calcs = validateInputs()

        if (!calcs.isValid)
        {
            lblVolume.setText("#Error")
            lblTimeToFill.setText("#Error")
            return
        }

        if (selectedTab == CalculatorTab.PE)
        {
            val volString = calcs.calcPEVolume().formatForDecPlaces(2)
            lblVolume.text = volString

            val fillingTime = calcs.peFillingTime()
            lblTimeToFill.text = fillingTime.first.toString() + "hr " + fillingTime.second.toString() + "m"
        }

        if (selectedTab == CalculatorTab.Metallic)
        {
            val volSgtring = calcs.calcDIVolumne().formatForDecPlaces(2)
            lblVolume.text = volSgtring

            val fillingTime = calcs.diFillingTime()
            lblTimeToFill.text = fillingTime.first.toString() + "hr " + fillingTime.second.toString() + "m"
        }
    }

    class CalcInputs
    {
        var isValid = false
        var diameter = 0.0
        var sdr = 0.0
        var length = 0.0
        var flowrate = 0.0

        fun internalDiameter(): Double
        {
            return diameter - (diameter / sdr) * 2
        }

        fun calcPEVolume(): Double
        {
            return (internalDiameter() / 2.0) * (internalDiameter() / 2.0) * Math.PI * length / 1000.0
        }

        fun calcDIVolumne(): Double
        {
            return (diameter / 2.0) * (diameter / 2.0) * Math.PI / 1000.0 * length
        }

        fun peFillingTime(): Pair<Int, Int>
        {
            val totalHours = (calcPEVolume() / flowrate) / 60.0
            return hoursDecimalToHoursMins(totalHours)
        }

        fun diFillingTime(): Pair<Int, Int>
        {
            val totalHours = (calcDIVolumne() / flowrate) / 60.0
            return hoursDecimalToHoursMins(totalHours)
        }

        fun hoursDecimalToHoursMins(hours: Double): Pair<Int, Int>
        {
            val intHours = hours.toInt()
            val remainder = hours - intHours.toDouble()
            val minutes = (60.0 * remainder).toInt()
            return Pair(intHours, minutes)
        }
    }
}
