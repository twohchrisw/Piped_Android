package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.content.Context
import java.util.*

class FlowrateBag(val datetime: Date, val flowrate: Double) {

    companion object {
        fun totalWaterForFlowrates(ctx: Context, processId: Long, flowrateBag: List<FlowrateBag>, startTime: Date, endTime: Date?, pauseType: String): Double
        {
            var waterVolume = 0.0
            var currentFlowrate = 0.0
            var lastReadingTime = startTime
            var finishTime = Date()

            if (endTime != null)
            {
                finishTime = endTime!!
            }

            for (fr in flowrateBag)
            {
                val flowrateTime = fr.datetime
                val flowrateValue = fr.flowrate

                val timeDiff = (flowrateTime.time - lastReadingTime.time) / 1000.0
                lastReadingTime = flowrateTime

                if (flowrateTime.time < finishTime.time)
                {
                    if (timeDiff > 0)
                    {
                        val totalMinutes = timeDiff / 60.0
                        waterVolume = waterVolume + (currentFlowrate * totalMinutes)
                    }

                    currentFlowrate = flowrateValue
                }
            }

            // Add the final reading based on the stop time
            val timeDiff = (finishTime.time - lastReadingTime.time) / 1000.0
            val durationMinutes = timeDiff / 60.0
            waterVolume = waterVolume + (currentFlowrate * durationMinutes)

            // Subtract the value of the pause sessions
            waterVolume = waterVolume - getPauseVolumeToSubtract(ctx, processId, pauseType)
            return waterVolume
        }

        fun getPauseVolumeToSubtract(ctx: Context, processId: Long, pauseType: String): Double
        {
            var totalVol = 0.0

            val pauseSessions = EXLDPauseSessions.pauseSessions(ctx, processId, pauseType)
            for (pause in pauseSessions)
            {
                val minutes = pause.totalPauseSeconds() / 60.0
                val lineVol = pause.pause_flowrate * minutes
                totalVol = totalVol + lineVol
            }

            return totalVol
        }
    }
}