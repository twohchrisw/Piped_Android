package pipedkotlin.android.cobalttechno.com.pipedkotlin

import java.lang.Exception


class AirPressureCalc(val process: EXLDProcess, val context: TestingSessionData.TestingContext) {

    var diameter = -1.0
    var length = -1
    var startPressure = -1.0
    var stp = -1.0
    var sdrRating = SDRRating.None
    var pumpSize = 0.0  // Liters / Min

    enum class SDRRating {
        None, SDR11, SDR17, SDR21
    }

    class AirPressureSeconds(val onePercent: Int, val twoPrecent: Int, val threePercent: Int, val fourPercent: Int)

    /* Calc Tables for 10 bar */

    val pipe_diameters = arrayOf(63.00, 75.00, 90.00, 110.00, 125.00, 140.00, 160.00, 180.00, 200.00, 225.00, 250.00, 280.00, 315.00, 355.00, 400.00, 450.00, 500.00, 560.00, 630.00, 710.00, 800.00, 900.00, 1000.00, 1200.00)

    // Volume to add for pipe pressurisation
    val pressurisation11 = doubleArrayOf(2.00, 3.00, 5.00, 7.00, 9.00, 11.00, 15.00, 19.00, 23.00, 29.00, 36.00, 45.00, 57.00, 73.00, 92.00, 116.00, 144.00, 181.00, 229.00, 290.00, 369.00, 467.00, 576.00)
    val pressurisation17 = doubleArrayOf(4.00, 6.00, 9.00, 13.00, 17.00, 22.00, 28.00, 36.00, 44.00, 55.00, 69.00, 86.00, 109.00, 138.00, 176.00, 223.00, 275.00, 345.00, 436.00, 555.00, 706.00, 894.00, 1101.00, 1588.00)
    val pressurisation21 = doubleArrayOf(6.00, 8.00, 12.00, 18.00, 23.00, 29.00, 37.00, 48.00, 59.00, 74.00, 93.00, 116.00, 147.00, 187.00, 236.00, 299.00, 369.00, 464.00, 589.00, 745.00, 949.00, 1200.00, 1480.00, 2133.00)

    // Unpressurised pipe volume
    val toFill11 = doubleArrayOf(207.00, 296.00, 425.00, 636.00, 820.00, 1031.00, 1344.00, 1702.00, 2102.00, 2659.00, 3288.00, 4126.00, 5220.00, 6633.00, 8419.00, 10648.00, 13151.00, 16504.00, 20879.00, 26512.00, 33675.00, 42614.00, 52604.00)
    val toFill17 = doubleArrayOf(241.00, 342.00, 493.00, 736.00, 954.00, 1196.00, 1561.00, 1976.00, 2438.00, 3085.00, 3815.00, 4784.00, 6052.00, 7685.00, 9765.00, 12354.00, 15247.00, 19136.00, 24210.00, 30758.00, 39058.00, 49440.00, 61015.00, 87881.00)
    val toFill21 = doubleArrayOf(255.00, 361.00, 520.00, 776.00, 1003.00, 1259.00, 1642.00, 2082.00, 2567.00, 3249.00, 4019.00, 5035.00, 6379.00, 8103.00, 10281.00, 13010.00, 16060.00, 20157.00, 25518.00, 32391.00, 41146.00, 52066.00, 64269.00, 92561.00)

    // 1% Air Content
    val aircont111 = doubleArrayOf(2.00, 3.00, 4.00, 6.00, 7.00, 9.00, 12.00, 15.00, 19.00, 24.00, 30.00, 37.00, 47.00, 60.00, 76.00, 97.00, 119.00, 150.00, 290.00, 241.00, 306.00, 387.00, 478.00)
    val aircont117 = doubleArrayOf(2.00, 3.00, 4.00, 7.00, 9.00, 11.00, 14.00, 18.00, 22.00, 28.00, 35.00, 43.00, 55.00, 70.00, 89.00, 112.00, 138.00, 174.00, 220.00, 279.00, 355.00, 449.00, 554.00, 798.00)
    val aircont121 = doubleArrayOf(2.00, 3.00, 5.00, 7.00, 9.00, 11.00, 15.00, 19.00, 23.00, 30.00, 36.00, 46.00, 58.00, 74.00, 93.00, 118.00, 146.00, 183.00, 232.00, 294.00, 374.00, 473.00, 584.00, 840.00)

    // 2% Air Content
    val aircont211 = doubleArrayOf(4.00, 5.00, 8.00, 12.00, 15.00, 19.00, 24.00, 31.00, 38.00, 48.00, 60.00, 75.00, 95.00, 120.00, 153.00, 193.00, 239.00, 300.00, 379.00, 481.00, 612.00, 774.00, 955.00)
    val aircont217 = doubleArrayOf(4.00, 6.00, 9.00, 13.00, 17.00, 22.00, 28.00, 36.00, 44.00, 56.00, 69.00, 87.00, 110.00, 140.00, 177.00, 224.00, 277.00, 347.00, 440.00, 559.00, 709.00, 898.00, 1108.00, 1596.00)
    val aircont221 = doubleArrayOf(5.00, 7.00, 9.00, 14.00, 18.00, 23.00, 30.00, 38.00, 47.00, 59.00, 73.00, 91.00, 116.00, 147.00, 187.00, 236.00, 292.00, 366.00, 463.00, 588.00, 747.00, 946.00, 1167.00, 1681.00)

    // 3% Air Content
    val aircont311 = doubleArrayOf(6.00, 8.00, 12.00, 17.00, 22.00, 28.00, 37.00, 46.00, 57.00, 72.00, 90.00, 112.00, 142.00, 181.00, 229.00, 290.00, 358.00, 450.00, 569.00, 722.00, 917.00, 1161.00, 1433.00)
    val aircont317 = doubleArrayOf(7.00, 9.00, 13.00, 20.00, 26.00, 33.00, 43.00, 54.00, 66.00, 84.00, 104.00, 130.00, 165.00, 209.00, 266.00, 337.00, 415.00, 521.00, 659.00, 838.00, 1064.00, 1347.00, 1662.00, 2394.0)
    val aircont321 = doubleArrayOf(7.00, 10.00, 14.00, 21.00, 27.00, 34.00, 45.00, 57.00, 70.00, 89.00, 109.00, 137.00, 174.00, 221.00, 280.00, 354.00, 437.00, 549.00, 695.00, 882.00, 1121.00, 1418.00, 1751.00, 2521.00)

    // 4% Air Content
    val aircont411 = doubleArrayOf(8.00, 11.00, 15.00, 23.00, 30.00, 37.00, 49.00, 62.00, 76.00, 97.00, 119.00, 150.00, 190.00, 241.00, 306.00, 387.00, 478.00, 599.00, 758.00, 963.00, 1223.00, 1548.00, 1911.0)
    val aircont417 = doubleArrayOf(9.00, 12.00, 18.00, 27.00, 35.00, 43.00, 57.00, 72.00, 89.00, 112.00, 139.00, 174.00, 220.00, 279.00, 355.00, 449.00, 554.00, 695.00, 879.00, 1117.00, 1419.00, 1796.00, 2216.00, 3192.00)
    val aircont421 = doubleArrayOf(9.00, 13.00, 19.00, 28.00, 36.00, 46.00, 60.00, 76.00, 93.00, 118.00, 146.00, 183.00, 232.00, 294.00, 373.00, 473.00, 583.00, 732.00, 927.00, 1176.00, 1494.00, 1891.00, 2334.00, 3362.00)

    init {
        pumpSize = process.pumpLitersPerSecond(context) * 60.0
        if (process.pipe_description.contains("SDR 11"))
        {
            sdrRating = SDRRating.SDR11
        }
        if (process.pipe_description.contains("SDR 17"))
        {
            sdrRating = SDRRating.SDR17
        }
        if (process.pipe_description.contains("SDR 21"))
        {
            sdrRating = SDRRating.SDR21
        }

        if (context == TestingSessionData.TestingContext.pe)
        {
            diameter = process.pt_pe_pipe_diameter.toDouble()
            var lengthString = process.pt_section_length.replace("m", "")

            try {
                length = lengthString.toInt()
            }
            catch (e: Exception)
            {
                length = 0
            }

            startPressure = process.pt_start_pressure
            stp = process.pt_system_test_pressure
        }

        if (context == TestingSessionData.TestingContext.di)
        {
            diameter = process.pt_di_pipe_diameter.toDouble()
            var lengthString = process.pt_di_section_length.replace("m", "")

            try {
                length = lengthString.toInt()
            }
            catch (e: Exception)
            {
                length = 0
            }

            startPressure = process.pt_di_start_pressure
            stp = process.pt_di_stp
        }
    }

    fun performCalc(): AirPressureSeconds?
    {
        if (!isValid().first)
        {
            return null
        }

        stp = stp - startPressure
        if (context == TestingSessionData.TestingContext.pe)
        {
            return performCalcPE()
        }
        else
        {
            return performCalcDI()
        }
    }

    private fun performCalcPE(): AirPressureSeconds
    {
        val pipeIndex = pipe_diameters.indexOf(diameter)
        if (pipeIndex < 0)
        {
            return AirPressureSeconds(-1, -1, -1, -1)
        }

        var isError = true
        var volumeToPressurise = 0.0
        var airContent4 = 0.0
        var airContent3 = 0.0
        var airContent2 = 0.0
        var airContent1 = 0.0

        var airVolume4 = 0.0
        var airVolume3 = 0.0
        var airVolume2 = 0.0
        var airVolume1 = 0.0

        var toFill = 0.0

        var onePercentSeconds = 0
        var twoPercentSeconds = 0
        var threePercentSeconds = 0
        var fourPercentSeconds = 0

        when (sdrRating)
        {
            SDRRating.SDR11 -> {
                volumeToPressurise = pressurisation11[pipeIndex]
                airContent4 = aircont411[pipeIndex]
                airContent3 = aircont311[pipeIndex]
                airContent2 = aircont211[pipeIndex]
                airContent1 = aircont111[pipeIndex]
                toFill = toFill11[pipeIndex]
                isError = false
            }

            SDRRating.SDR17 -> {
                volumeToPressurise = pressurisation17[pipeIndex]
                airContent4 = aircont417[pipeIndex]
                airContent3 = aircont317[pipeIndex]
                airContent2 = aircont217[pipeIndex]
                airContent1 = aircont117[pipeIndex]
                toFill = toFill17[pipeIndex]
                isError = false
            }

            SDRRating.SDR21 -> {
                volumeToPressurise = pressurisation21[pipeIndex]
                airContent4 = aircont421[pipeIndex]
                airContent3 = aircont321[pipeIndex]
                airContent2 = aircont221[pipeIndex]
                airContent1 = aircont121[pipeIndex]
                toFill = toFill21[pipeIndex]
                isError = false
            }

            else -> {}
        }

        if (!isError)
        {
            toFill = (toFill / 100.00) * length.toDouble()
            volumeToPressurise = (volumeToPressurise / 100.0) * length.toDouble()
            volumeToPressurise = (volumeToPressurise / 10.0) * stp

            // Volume to compress 1% air
            airVolume1 = airContent1 * (length.toDouble() / 100.0)
            val pump2Pres1 = volumeToPressurise + airVolume1
            val time1 = pump2Pres1 / pumpSize
            val totalHours1 = time1 / 100
            onePercentSeconds = (totalHours1 * 3600).toInt()

            // Volume to compress 2% air
            airVolume2 = airContent2 * (length.toDouble() / 100.0)
            val pump2Pres2 = volumeToPressurise + airVolume2
            val time2 = pump2Pres2 / pumpSize
            val totalHours2 = time2 / 100
            twoPercentSeconds = (totalHours2 * 3600).toInt()

            // Volume to compress 3% air
            airVolume3 = airContent3 * (length.toDouble() / 100.0)
            val pump2Pres3 = volumeToPressurise + airVolume3
            val time3 = pump2Pres3 / pumpSize
            val totalHours3 = time3 / 100
            threePercentSeconds = (totalHours3 * 3600).toInt()

            // Volume to compress 4% air
            airVolume4 = airContent4 * (length.toDouble() / 100.0)
            val pump2Pres4 = volumeToPressurise + airVolume4
            val time4 = pump2Pres4 / pumpSize
            val totalHours4 = time4 / 100
            fourPercentSeconds = (totalHours4 * 3600).toInt()

            return AirPressureSeconds(onePercentSeconds, twoPercentSeconds, threePercentSeconds, fourPercentSeconds)
        }
        else
        {
            return AirPressureSeconds(0, 0,0,0)
        }

    }

    private fun performCalcDI(): AirPressureSeconds
    {
        return AirPressureSeconds(0,0,0,0)
    }


    fun isValid(): Pair<Boolean, String>
    {
        val pipeIndex = pipe_diameters.indexOf(diameter)
        if (pipeIndex < 0)
        {
            return Pair(false, "Invalid Pipe Diameter")
        }

        if (diameter < 1)
        {
            return Pair(false, "Invalid Pipe Diameter $diameter")
        }

        if (length < 1)
        {
            return Pair(false, "Invalid Section Length $length")
        }

        if (sdrRating == SDRRating.None)
        {
            return Pair(false, "SDR Rating not supplied")
        }

        if (pumpSize < 0.05)
        {
            return Pair(false, "Invalid Pump Size")
        }



        return Pair(true, "")
    }
}