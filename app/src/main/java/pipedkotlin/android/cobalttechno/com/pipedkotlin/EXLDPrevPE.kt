package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.content.Context
import org.jetbrains.anko.db.*

class EXLDPrevPE(var _id: Long = -1,
                 var processId: Long = -1,
                 var pe_test_aborted: Int = 0,
                 var peWaterVolume: Double = 0.0,
                 var prev_pe_id: Int = 0,
                 var prev_pe_process_id: Long = 0,
                 var start_pressure: Double = 0.0,
                 var system_test_pressure: Double = 0.0,
                 var pressurising_start: String = "",
                 var pressurising_finish: String = "",
                 var reading_1: Double = 0.0,
                 var reading_2: Double = 0.0,
                 var reading_3: Double = 0.0,
                 var reading_4: Double = 0.0,
                 var reading_5: Double = 0.0,
                 var reading_time_1: String = "",
                 var reading_time_2: String = "",
                 var reading_time_3: String = "",
                 var reading_time_4: String = "",
                 var reading_time_5: String = "",
                 var calc_result: Double = 0.0,
                 var lat: Double = 0.0,
                 var longitude: Double = 0.0,
                 var pass: Double = 0.0,
                 var notes: String = "",
                 var pdf_log_pa_t1: Double = 0.0,
                 var pdf_log_pa_t2: Double = 0.0,
                 var pdf_log_pa_t3: Double = 0.0,
                 var pdf_log_pa_t4: Double = 0.0,
                 var pdf_log_pa_t5: Double = 0.0,
                 var pdf_log_t1: Double = 0.0,
                 var pdf_log_t2: Double = 0.0,
                 var pdf_log_t3: Double = 0.0,
                 var pdf_log_t4: Double = 0.0,
                 var pdf_log_t5: Double = 0.0,
                 var pdf_pass: Int = 0,
                 var pdf_calc_result:Double = 0.0,
                 var pdf_n1: Double = 0.0,
                 var pdf_n2: Double = 0.0,
                 var pdf_n3: Double = 0.0,
                 var pdf_n4: Double = 0.0,
                 var pipe_length: String = "",
                 var pipe_diameter: Int = 0,
                 var installation_tech: String = "",
                 var pump_size: String = "",
                 var logger_details: String = "",
                 var pt_pe_readings_count: Int = 0)
{

    companion object {

        fun getPrevPETests(ctx: Context, processId: Long): List<EXLDPrevPE>
        {
            return ctx.database.use {
                select(EXLDPrevPE.TABLE_NAME)
                        .whereArgs("${EXLDPrevPE.COLUMN_PROCESS_ID} = $processId")
                        .orderBy(EXLDPrevPE.COLUMN_ID, SqlOrderDirection.DESC)
                        .exec {
                            parseList<EXLDPrevPE>(classParser())
                        }
            }
        }

        val TABLE_NAME = "EXLDPrevPE"
        val COLUMN_ID = "_id"
        val COLUMN_PROCESS_ID = "processId"
        val COLUMN_PE_TEST_ABORTED = "pe_test_aborted"
        val COLUMN_PE_WATER_VOLUME = "pe_water_volume"
        val COLUMN_PREV_PE_ID = "prev_pe_id"
        val COLUMN_PREV_PE_PROCESS_ID = "prev_pe_process_id"
        val COLUMN_START_PRESSURE = "start_pressure"
        val COLUMN_SYSTEM_TEST_PRESSURE = "system_test_pressure"
        val COLUMN_PRESSURISING_START = "pressurising_start"
        val COLUMN_PRESSURISING_FINISH = "pressurising_finish"
        val COLUMN_READING_1 = "reading_1"
        val COLUMN_READING_2 = "reading_2"
        val COLUMN_READING_3 = "reading_3"
        val COLUMN_READING_4 = "reading_4"
        val COLUMN_READING_5 = "reading_5"
        val COLUMN_READING_TIME_1 = "reading_time_1"
        val COLUMN_READING_TIME_2 = "reading_time_2"
        val COLUMN_READING_TIME_3 = "reading_time_3"
        val COLUMN_READING_TIME_4 = "reading_time_4"
        val COLUMN_READING_TIME_5 = "reading_time_5"
        val COLUMN_CALC_RESULT = "calc_result"
        val COLUMN_LAT = "lat"
        val COLUMN_LONG = "longitude"
        val COLUMN_PASS = "pass"
        val COLUMN_NOTES = "notes"
        val COLUMN_PDF_LOG_PA_T1 = "pdf_log_pa_t1"
        val COLUMN_PDF_LOG_PA_T2 = "pdf_log_pa_t2"
        val COLUMN_PDF_LOG_PA_T3 = "pdf_log_pa_t3"
        val COLUMN_PDF_LOG_PA_T4 = "pdf_log_pa_t4"
        val COLUMN_PDF_LOG_PA_T5 = "pdf_log_pa_t5"
        val COLUMN_LOG_T1 = "pdf_log_t1"
        val COLUMN_LOG_T2 = "pdf_log_t2"
        val COLUMN_LOG_T3 = "pdf_log_t3"
        val COLUMN_LOG_T4 = "pdf_log_t4"
        val COLUMN_LOG_T5 = "pdf_log_t5"
        val COLUMN_PDF_PASS = "pdf_pass"
        val COLUMN_PDF_CALC_RESULT = "pdf_calc_result"
        val COLUMN_PDF_N1 = "pdf_n1"
        val COLUMN_PDF_N2 = "pdf_n2"
        val COLUMN_PDF_N3 = "pdf_n3"
        val COLUMN_PDF_N4 = "pdf_n4"
        val COLUMN_PIPE_LENGTH = "pipe_length"
        val COLUMN_PIPE_DIAMETER = "pipe_diameter"
        val COLUMN_INSTALLATION_TECH = "installation_tech"
        val COLUMN_PUMP_SIZE = "pump_size"
        val COLUMN_LOGGER_DETAILS = "logger_details"
        val COLUMN_PT_PE_READINGS_COUNT = "pt_pe_readings_count"
    }

    fun save(context: Context): Long
    {
        var saveId: Long = _id

        if (_id < 1)
        {
            context.database.use {
                /*
                saveId = insert(TABLE_NAME,
                        COLUMN_PROCESS_ID to processId, COLUMN_PE_TEST_ABORTED to pe_test_aborted, COLUMN_PE_WATER_VOLUME to peWaterVolume,
                        COLUMN_PREV_PE_ID to prev_pe_id,
                        COLUMN_PREV_PE_PROCESS_ID to prev_pe_process_id,
                        COLUMN_START_PRESSURE to start_pressure,
                        COLUMN_SYSTEM_TEST_PRESSURE to system_test_pressure,
                        COLUMN_PRESSURISING_START to pressurising_start,
                        COLUMN_PRESSURISING_FINISH to pressurising_finish,
                        COLUMN_READING_1 to reading_1, COLUMN_READING_2 to reading_2, COLUMN_READING_3 to reading_3, COLUMN_READING_4 to reading_4, COLUMN_READING_5 to reading_5,
                        COLUMN_READING_TIME_1 to reading_time_1, COLUMN_READING_TIME_2 to reading_time_2, COLUMN_READING_TIME_3 to reading_time_3, COLUMN_READING_TIME_4 to reading_time_4, COLUMN_READING_TIME_5 to reading_time_5,
                        COLUMN_CALC_RESULT to calc_result, COLUMN_LAT to lat, COLUMN_LONG to longitude, COLUMN_PASS to pass, COLUMN_NOTES to notes,
                        COLUMN_PDF_LOG_PA_T1 to pdf_log_pa_t1, COLUMN_PDF_LOG_PA_T2 to pdf_log_pa_t2, COLUMN_PDF_LOG_PA_T3 to pdf_log_pa_t3, COLUMN_PDF_LOG_PA_T4 to pdf_log_pa_t4, COLUMN_PDF_LOG_PA_T5 to pdf_log_pa_t5,
                        COLUMN_LOG_T1 to pdf_log_t1, COLUMN_LOG_T2 to pdf_log_t2, COLUMN_LOG_T3 to pdf_log_t3, COLUMN_LOG_T4 to pdf_log_t4, COLUMN_LOG_T5 to pdf_log_t5,
                        COLUMN_PDF_PASS to pdf_pass, COLUMN_PDF_N1 to pdf_n1, COLUMN_PDF_N2 to pdf_n2, COLUMN_PDF_N3 to pdf_n3, COLUMN_PDF_N4 to pdf_n4,
                        COLUMN_PIPE_LENGTH to pipe_length, COLUMN_PIPE_DIAMETER to pipe_diameter, COLUMN_INSTALLATION_TECH to installation_tech, COLUMN_PUMP_SIZE to pump_size, COLUMN_LOGGER_DETAILS to logger_details,
                        COLUMN_PT_PE_READINGS_COUNT to pt_pe_readings_count, COLUMN_PDF_CALC_RESULT to pdf_calc_result
                        )
                        */
                saveId = insert(TABLE_NAME,
                        COLUMN_PROCESS_ID to processId, COLUMN_PE_TEST_ABORTED to pe_test_aborted, COLUMN_PE_WATER_VOLUME to peWaterVolume,
                        COLUMN_PREV_PE_ID to prev_pe_id,
                        COLUMN_PREV_PE_PROCESS_ID to prev_pe_process_id,
                        COLUMN_START_PRESSURE to start_pressure,
                        COLUMN_SYSTEM_TEST_PRESSURE to system_test_pressure,
                        COLUMN_PRESSURISING_START to pressurising_start,
                        COLUMN_PRESSURISING_FINISH to pressurising_finish,
                        COLUMN_READING_1 to reading_1, COLUMN_READING_2 to reading_2, COLUMN_READING_3 to reading_3, COLUMN_READING_4 to reading_4, COLUMN_READING_5 to reading_5,
                        COLUMN_READING_TIME_1 to reading_time_1, COLUMN_READING_TIME_2 to reading_time_2, COLUMN_READING_TIME_3 to reading_time_3, COLUMN_READING_TIME_4 to reading_time_4, COLUMN_READING_TIME_5 to reading_time_5,
                        COLUMN_CALC_RESULT to calc_result, COLUMN_LAT to lat, COLUMN_LONG to longitude, COLUMN_PASS to pass, COLUMN_NOTES to notes,
                        COLUMN_PDF_LOG_PA_T1 to pdf_log_pa_t1, COLUMN_PDF_LOG_PA_T2 to pdf_log_pa_t2, COLUMN_PDF_LOG_PA_T3 to pdf_log_pa_t3, COLUMN_PDF_LOG_PA_T4 to pdf_log_pa_t4, COLUMN_PDF_LOG_PA_T5 to pdf_log_pa_t5,
                        COLUMN_LOG_T1 to pdf_log_t1, COLUMN_LOG_T2 to pdf_log_t2, COLUMN_LOG_T3 to pdf_log_t3, COLUMN_LOG_T4 to pdf_log_t4, COLUMN_LOG_T5 to pdf_log_t5,
                        COLUMN_PDF_PASS to pdf_pass, COLUMN_PDF_N1 to pdf_n1, COLUMN_PDF_N2 to pdf_n2, COLUMN_PDF_N3 to pdf_n3, COLUMN_PDF_N4 to pdf_n4,
                        COLUMN_PIPE_LENGTH to pipe_length, COLUMN_PIPE_DIAMETER to pipe_diameter, COLUMN_INSTALLATION_TECH to installation_tech, COLUMN_PUMP_SIZE to pump_size, COLUMN_LOGGER_DETAILS to logger_details,
                        COLUMN_PT_PE_READINGS_COUNT to pt_pe_readings_count, COLUMN_PDF_CALC_RESULT to pdf_calc_result)
                _id = saveId
            }
        }

        return saveId
    }
}