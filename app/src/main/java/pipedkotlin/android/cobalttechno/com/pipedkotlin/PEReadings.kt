package pipedkotlin.android.cobalttechno.com.pipedkotlin

import kotlin.math.abs
import kotlin.math.atan

class PEReadings {
    val SECTION_ANGLE_TOLERANCE_DEGREES = 10.0
    val HORIZ_DIVISIONS = 6.0
    val VERT_DIVISIONS = 6.0

    var number_of_readings = 3

    var log_t1 = 0.0
    var log_t2 = 0.0
    var log_t3 = 0.0
    var log_t4 = 0.0
    var log_t5 = 0.0

    var log_pa_t1 = 0.0
    var log_pa_t2 = 0.0
    var log_pa_t3 = 0.0
    var log_pa_t4 = 0.0
    var log_pa_t5 = 0.0

    var n1 = 0.0
    var n2 = 0.0
    var n3 = 0.0
    var n4 = 0.0
    var calcResult = 0.0

    var section1Angle = 0.0
    var section2Angle = 0.0
    var angleDifference = 0.0

    var calcPass = false
    var anglePass = false

    fun valueSpread(): Pair<Double, Double>
    {
        val minLx = minL()
        val maxLx = maxL()
        val minPax = minPa()
        val maxPax = maxPa()

        return Pair(maxLx - minLx, maxPax - minPax)
    }

    fun minL(): Double
    {
        return minOf(log_t1, log_t2, log_t3)
    }

    fun maxL(): Double
    {
        return maxOf(log_t1, log_t2, log_t3)
    }

    fun minPa(): Double
    {
        return minOf(log_pa_t1, log_pa_t2, log_pa_t3)
    }

    fun maxPa(): Double
    {
        return maxOf(log_pa_t1, log_pa_t2, log_pa_t3)
    }

    fun tangentialDifference(): Double
    {
        val sec1Adj = (log_pa_t1 - log_pa_t2)
        val sec1Opp = (log_t2 - log_t1)
        val tanSec1 = sec1Opp / sec1Adj
        val sec2Adj = (log_pa_t2 - log_pa_t3)
        val sec2Opp = (log_t3 - log_t2)
        val tanSec2 = sec2Opp / sec2Adj
        val sec1Deg = atan(tanSec1)
        val sec2Deg = atan(tanSec2)

        return abs(sec1Deg - sec2Deg)
    }

}