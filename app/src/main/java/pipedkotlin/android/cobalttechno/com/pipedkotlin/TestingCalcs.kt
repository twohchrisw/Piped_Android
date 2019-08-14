package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.content.Context
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.log10

class TestingCalcs(val testingContext: TestingSessionData.TestingContext, val process: EXLDProcess) {

    var peTestPassed = false

    fun calculatePETestResults()
    {
        peTestPassed = false

        val r1 = DateHelper.dbStringToDate(process.pt_reading1_time, Date())
        val r2 = DateHelper.dbStringToDate(process.pt_reading2_time, Date())
        val r3 = DateHelper.dbStringToDate(process.pt_reading3_time, Date())
        val finish = DateHelper.dbStringToDate(process.pt_pressurising_finish, Date())

        val t1_diff = r1.time - finish.time
        val t2_diff = r2.time - finish.time
        val t3_diff = r3.time - finish.time

        val t1_seconds = TimeUnit.MILLISECONDS.toSeconds(t1_diff).toDouble()
        val t2_seconds = TimeUnit.MILLISECONDS.toSeconds(t2_diff).toDouble()
        val t3_seconds = TimeUnit.MILLISECONDS.toSeconds(t3_diff).toDouble()

        val correctionValue = 0.4 + t1_seconds
        val t1_corrected = t1_seconds + correctionValue
        val t2_corrected = t2_seconds + correctionValue
        val t3_corrected = t3_seconds + correctionValue

        val reading1 = process.pt_reading_1
        val reading2 = process.pt_reading_2
        val reading3 = process.pt_reading_3

        var pressureAt_t1 = reading1 - process.pt_start_pressure
        var pressureAt_t2 = reading2 - process.pt_start_pressure
        var pressureAt_t3 = reading3 - process.pt_start_pressure

        // Ensure we have no negatives
        if (pressureAt_t1 < 0) { pressureAt_t1 = pressureAt_t1 * -1 }
        if (pressureAt_t2 < 0) { pressureAt_t2 = pressureAt_t2 * -1 }
        if (pressureAt_t3 < 0) { pressureAt_t3 = pressureAt_t3 * -1 }

        val log_pa_t1 = log10(pressureAt_t1)
        val log_pa_t2 = log10(pressureAt_t2)
        val log_pa_t3 = log10(pressureAt_t3)

        val log_t1 = log10(t1_corrected)
        val log_t2 = log10(t2_corrected)
        val log_t3 = log10(t3_corrected)

        var calcResult = 0.0
        val n1 = (log_pa_t1 - log_pa_t2) / (log_t2 - log_t1)
        val n2 = (log_pa_t2 - log_pa_t3) / (log_t3 - log_t2)

        if (n1 == 0.0)
        {
            calcResult = 0.0
        }
        else
        {
            calcResult = n2 / n1
        }

        // Check the values are not infinity or null
        if (log_pa_t1 == null || log_pa_t2 == null || log_pa_t3 == null || log_t1 == null || log_t2 == null || log_t3 == null
                || log_pa_t1 == Double.NEGATIVE_INFINITY || log_pa_t2 == Double.NEGATIVE_INFINITY || log_pa_t3 == Double.NEGATIVE_INFINITY
                || log_t1 == Double.NEGATIVE_INFINITY || log_t2 == Double.NEGATIVE_INFINITY || log_t3 == Double.NEGATIVE_INFINITY
        )
        {
            // These are bad values likely from a 0 pressure test
            // skip saving calculations

            return
        }

        // Save the values to the process
        process.pe_pdf_log_pa_t1 = log_pa_t1
        process.pe_pdf_log_pa_t2 = log_pa_t2
        process.pe_pdf_log_pa_t3 = log_pa_t3
        process.pe_pdf_log_t1 = log_t1
        process.pe_pdf_log_t2 = log_t2
        process.pe_pdf_log_t3 = log_t3
        process.pe_pdf_calc_result = calcResult
        process.pt_calc_result = calcResult
        process.pe_test_has_calculated = 1

        val LOWER_N = 0.04
        val UPPER_N = 0.13

        // Now test the individual n1 values
        var hasNValueFailed = false
        AppGlobals.instance.peFailMessage = ""

        if (n1 < LOWER_N || n2 < LOWER_N)
        {
            hasNValueFailed = true
            AppGlobals.instance.peFailMessage = "Pipe is likely to have air in it."
        }

        if (n1 > UPPER_N || n2 > UPPER_N)
        {
            hasNValueFailed = true
            AppGlobals.instance.peFailMessage = "Pipe is likely to be leaking."
        }

        if (calcResult >= TestingSessionData.PE_TESTING_LOWER_LIMIT && calcResult <= TestingSessionData.PE_TESTING_UPPER_LIMIT)
        {
            process.pe_pdf_pass = 1
            peTestPassed = true
        }
        else
        {
            peTestPassed = false
            process.pe_pdf_pass = 0
            AppGlobals.instance.peFailMessageAfterSync = true
        }

        process.pe_pdf_n1 = n1
        process.pe_pdf_n2 = n2

        // Save the reading values to a class for use when drawing the graph

        AppGlobals.instance.excelPEReadings = PEReadings()
        AppGlobals.instance.excelPEReadings.log_pa_t1 = log_pa_t1
        AppGlobals.instance.excelPEReadings.log_pa_t2 = log_pa_t2
        AppGlobals.instance.excelPEReadings.log_pa_t3 = log_pa_t3
        AppGlobals.instance.excelPEReadings.log_t1 = log_t1
        AppGlobals.instance.excelPEReadings.log_t2 = log_t2
        AppGlobals.instance.excelPEReadings.log_t3 = log_t3
        AppGlobals.instance.excelPEReadings.n1 = n1
        AppGlobals.instance.excelPEReadings.n2 = n2
        AppGlobals.instance.excelPEReadings.calcResult = calcResult
        AppGlobals.instance.excelPEReadings.calcPass = peTestPassed

    }
}