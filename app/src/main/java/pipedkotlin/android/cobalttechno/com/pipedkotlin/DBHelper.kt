package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import org.jetbrains.anko.db.*

class DBHelper(ctx: Context) : ManagedSQLiteOpenHelper(ctx, "PipedDatabase", null, 2)
{
    companion object {
        private var instance: DBHelper? = null

        @Synchronized
        fun getInstance(ctx: Context): DBHelper {
            if (instance == null)
            {
                instance = DBHelper(ctx.applicationContext)
            }
            return instance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase?) {

        // Settings
        db!!.createTable(EXLDSettings.TABLE_NAME, true,
                EXLDSettings.COLUMN_ID to INTEGER + PRIMARY_KEY + UNIQUE,
                EXLDSettings.COLUMN_ALERT_EMAIL_ADDRESS to TEXT,
                EXLDSettings.COLUMN_ALERT_SMS_NUMBER to TEXT,
                EXLDSettings.COLUMN_ALLOW_AUDITING to TEXT,
                EXLDSettings.COLUMN_ALLOW_CHLOR to TEXT,
                EXLDSettings.COLUMN_ALLOW_DECHLOR to TEXT,
                EXLDSettings.COLUMN_ALLOW_DI to TEXT,
                EXLDSettings.COLUMN_ALLOW_FILLING to TEXT,
                EXLDSettings.COLUMN_ALLOW_FLUSH to TEXT,
                EXLDSettings.COLUMN_ALLOW_FLUSH2 to TEXT,
                EXLDSettings.COLUMN_ALLOW_PE to TEXT,
                EXLDSettings.COLUMN_ALLOW_SAMPLING to TEXT,
                EXLDSettings.COLUMN_ALLOW_SWABBING to TEXT,
                EXLDSettings.COLUMN_ALLOW_SURVEYING to TEXT,
                EXLDSettings.COLUMN_COMPANY_ID to TEXT,
                EXLDSettings.COLUMN_USER_ID to TEXT,
                EXLDSettings.COLUMN_USER_ID_STRING to TEXT)

        // List Items
        db!!.createTable(EXLDListItems.TABLE_NAME, true,
                EXLDListItems.COLUMN_ID to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
                EXLDListItems.COLUMN_COMPANY_ID to TEXT,
                EXLDListItems.COLUMN_LIST_NAME to TEXT,
                EXLDListItems.COLUMN_LIST_ITEM to TEXT)

        // Clients
        db!!.createTable(EXLDClients.TABLE_NAME, true,
                EXLDClients.COLUMN_ID to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
                EXLDClients.COLUMN_CLIENT_ID to TEXT,
                EXLDClients.COLUMN_CLIENT_NAME to TEXT,
                EXLDClients.COLUMN_COMPANY_ID to TEXT)

        // Process
        db!!.createTable(EXLDProcess.TABLE_NAME, true, EXLDProcess.COLUMN_ID to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
                EXLDProcess.c_address to TEXT + DEFAULT("''"),
                EXLDProcess.c_client to TEXT + DEFAULT("''"),
                EXLDProcess.c_company_id to TEXT + DEFAULT("''"),
                EXLDProcess.c_consum_additional_fire_hose_qty to INTEGER + DEFAULT("0"),
                EXLDProcess.c_consum_flanges_qty to INTEGER + DEFAULT("0"),
                EXLDProcess.c_consum_flanges_size to REAL + DEFAULT("0"),
                EXLDProcess.c_consum_notes to TEXT + DEFAULT("''"),
                EXLDProcess.c_consum_sodium_bisulphate to INTEGER + DEFAULT("0"),
                EXLDProcess.c_consum_sodium_hypoclorite to INTEGER + DEFAULT("0"),
                EXLDProcess.c_consum_swabs_qty to INTEGER + DEFAULT("0"),
                EXLDProcess.c_consum_swabs_size to REAL + DEFAULT("0"),
                EXLDProcess.c_create_device to TEXT + DEFAULT("''"),
                EXLDProcess.c_create_timestamp to TEXT + DEFAULT("''"),
                EXLDProcess.c_di_is_zero_loss to INTEGER + DEFAULT("0"),
                EXLDProcess.c_di_lat to REAL + DEFAULT("0"),
                EXLDProcess.c_di_long to REAL + DEFAULT("0"),
                EXLDProcess.c_di_test_has_calculated to INTEGER + DEFAULT("0"),
                EXLDProcess.c_edit_timestamp to TEXT + DEFAULT("''"),
                EXLDProcess.c_equip_chlorometer to INTEGER + DEFAULT("0"),
                EXLDProcess.c_equip_chlorosense to INTEGER + DEFAULT("0"),
                EXLDProcess.c_equip_corelator to INTEGER + DEFAULT("0"),
                EXLDProcess.c_equip_data_logger to INTEGER + DEFAULT("0"),
                EXLDProcess.c_equip_direct_injection to INTEGER + DEFAULT("0"),
                EXLDProcess.c_equip_dp_1000 to INTEGER + DEFAULT("0"),
                EXLDProcess.c_equip_dt_1500 to INTEGER + DEFAULT("0"),
                EXLDProcess.c_equip_dt_3000 to INTEGER + DEFAULT("0"),
                EXLDProcess.c_equip_dt_5000 to INTEGER + DEFAULT("0"),
                EXLDProcess.c_equip_elec_listening_stick to INTEGER + DEFAULT("0"),
                EXLDProcess.c_equip_ferometer to INTEGER + DEFAULT("0"),
                EXLDProcess.c_equip_hydrogen_detector to INTEGER + DEFAULT("0"),
                EXLDProcess.c_equip_ptp_12 to INTEGER + DEFAULT("0"),
                EXLDProcess.c_equip_ptp_30 to INTEGER + DEFAULT("0"),
                EXLDProcess.c_equip_ptp_50 to INTEGER + DEFAULT("0"),
                EXLDProcess.c_equip_ptp_120 to INTEGER + DEFAULT("0"),
                EXLDProcess.c_equip_ptp_250 to INTEGER + DEFAULT("0"),
                EXLDProcess.c_equip_swab_tracking to INTEGER + DEFAULT("0"),
                EXLDProcess.c_equip_turbidity to INTEGER + DEFAULT("0"),
                EXLDProcess.c_filling_flow_rate to REAL + DEFAULT("0"),
                EXLDProcess.c_filling_has_started to INTEGER + DEFAULT("0"),
                EXLDProcess.c_filling_lat to REAL + DEFAULT("0"),
                EXLDProcess.c_filling_long to REAL + DEFAULT("0"),
                EXLDProcess.c_filling_notes to TEXT + DEFAULT("''"),
                EXLDProcess.c_filling_started to TEXT + DEFAULT("''"),
                EXLDProcess.c_filling_stopped to TEXT + DEFAULT("''"),
                EXLDProcess.c_filling_total_water_volume to REAL + DEFAULT("0"),
                EXLDProcess.c_finish_time to TEXT + DEFAULT("''"),
                EXLDProcess.c_general_other to TEXT + DEFAULT("''"),
                EXLDProcess.c_internalId to TEXT + DEFAULT("''"),
                EXLDProcess.c_iphone_sync_id to TEXT + DEFAULT("''"),
                EXLDProcess.c_location_lat to REAL + DEFAULT("0"),
                EXLDProcess.c_location_long to REAL + DEFAULT("0"),
                EXLDProcess.c_needs_server_sync to INTEGER + DEFAULT("0"),
                EXLDProcess.c_pe_pdf_calc_result to REAL + DEFAULT("0"),
                EXLDProcess.c_pe_pdf_log_pa_t1 to REAL + DEFAULT("0"),
                EXLDProcess.c_pe_pdf_log_pa_t2 to REAL + DEFAULT("0"),
                EXLDProcess.c_pe_pdf_log_pa_t3 to REAL + DEFAULT("0"),
                EXLDProcess.c_pe_pdf_log_t1 to REAL + DEFAULT("0"),
                EXLDProcess.c_pe_pdf_log_t2 to REAL + DEFAULT("0"),
                EXLDProcess.c_pe_pdf_log_t3 to REAL + DEFAULT("0"),
                EXLDProcess.c_pe_pdf_n1 to REAL + DEFAULT("0"),
                EXLDProcess.c_pe_pdf_n2 to REAL + DEFAULT("0"),
                EXLDProcess.c_pe_pdf_n3 to REAL + DEFAULT("0"),
                EXLDProcess.c_pe_pdf_n4 to REAL + DEFAULT("0"),
                EXLDProcess.c_pe_pdf_pass to INTEGER + DEFAULT("0"),
                EXLDProcess.c_pe_test_aborted to INTEGER + DEFAULT("0"),
                EXLDProcess.c_pe_test_has_calculated to INTEGER + DEFAULT("0"),
                EXLDProcess.c_pipe_description to TEXT + DEFAULT("''"),
                EXLDProcess.c_pipe_diameter to INTEGER + DEFAULT("0"),
                EXLDProcess.c_pipe_length to INTEGER + DEFAULT("0"),
                EXLDProcess.c_pt_calc_result to REAL + DEFAULT("0"),
                EXLDProcess.c_pt_chlor_contact_ends to TEXT + DEFAULT("''"),
                EXLDProcess.c_pt_chlor_end_photo to TEXT + DEFAULT("''"),
                EXLDProcess.c_pt_chlor_end_strength to REAL + DEFAULT("0"),
                EXLDProcess.c_pt_chlor_main_chlorinated to TEXT + DEFAULT("''"),
                EXLDProcess.c_pt_chlor_notes to TEXT + DEFAULT("''"),
                EXLDProcess.c_pt_chlor_start_flowrate to INTEGER  + DEFAULT("0"),
                EXLDProcess.c_pt_chlor_start_lat to REAL + DEFAULT("0"),
                EXLDProcess.c_pt_chlor_start_long to REAL + DEFAULT("0"),
                EXLDProcess.c_pt_chlor_start_photo to TEXT + DEFAULT("''"),
                EXLDProcess.c_pt_chlor_start_strength to REAL + DEFAULT("0"),
                EXLDProcess.c_pt_chlor_start_time to TEXT + DEFAULT("''"),
                EXLDProcess.c_pt_chlor_volume to REAL + DEFAULT("0"),
                EXLDProcess.c_pt_dec_dechlorinated to TEXT + DEFAULT("''"),
                EXLDProcess.c_pt_dec_notes to TEXT + DEFAULT("''"),
                EXLDProcess.c_pt_dec_photo to TEXT + DEFAULT("''"),
                EXLDProcess.c_pt_dec_start to TEXT + DEFAULT("''"),
                EXLDProcess.c_pt_dec_start_lat to REAL + DEFAULT("0"),
                EXLDProcess.c_pt_dec_start_long to REAL + DEFAULT("0"),
                EXLDProcess.c_pt_dec_volume to REAL + DEFAULT("0"),
                EXLDProcess.c_pt_di_logger_details to TEXT + DEFAULT("''"),
                EXLDProcess.c_pt_di_notes to TEXT + DEFAULT("''"),
                EXLDProcess.c_pt_di_pipe_diameter to INTEGER + DEFAULT("0"),
                EXLDProcess.c_pt_di_pressurising_started to TEXT + DEFAULT("''"),
                EXLDProcess.c_pt_di_pump_size to TEXT + DEFAULT("''"),
                EXLDProcess.c_pt_di_r15_time to TEXT + DEFAULT("''"),
                EXLDProcess.c_pt_di_r15_value to REAL + DEFAULT("0"),
                EXLDProcess.c_pt_di_r60_time to TEXT + DEFAULT("''"),
                EXLDProcess.c_pt_di_r60_value to REAL + DEFAULT("0"),
                EXLDProcess.c_pt_di_section_length to TEXT + DEFAULT("''"),
                EXLDProcess.c_pt_di_section_name to TEXT + DEFAULT("''"),
                EXLDProcess.c_pt_di_start_pressure to REAL + DEFAULT("0"),
                EXLDProcess.c_pt_di_stp to REAL + DEFAULT("0"),
                EXLDProcess.c_pt_di_timer_status to INTEGER + DEFAULT("0"),
                EXLDProcess.c_pt_flush_completed to TEXT + DEFAULT("''"),
                EXLDProcess.c_pt_flush_completed2 to TEXT + DEFAULT("''"),
                EXLDProcess.c_pt_flush_notes to TEXT + DEFAULT("''"),
                EXLDProcess.c_pt_flush_notes2 to TEXT + DEFAULT("''"),
                EXLDProcess.c_pt_flush_start_lat to REAL + DEFAULT("0"),
                EXLDProcess.c_pt_flush_start_lat2 to REAL + DEFAULT("0"),
                EXLDProcess.c_pt_flush_start_long to REAL + DEFAULT("0"),
                EXLDProcess.c_pt_flush_start_long2 to REAL + DEFAULT("0"),
                EXLDProcess.c_pt_flush_started to TEXT + DEFAULT("''"),
                EXLDProcess.c_pt_flush_started2 to TEXT + DEFAULT("''"),
                EXLDProcess.c_pt_flush_total_water to REAL + DEFAULT("0"),
                EXLDProcess.c_pt_flush_total_water2 to REAL + DEFAULT("0"),
                EXLDProcess.c_pt_id to INTEGER + DEFAULT("0"),
                EXLDProcess.c_pt_installation_tech to TEXT + DEFAULT("''"),
                EXLDProcess.c_pt_installation_tech_id to INTEGER + DEFAULT("0"),
                EXLDProcess.c_pt_installation_tech_other to TEXT + DEFAULT("''"),
                EXLDProcess.c_pt_lat to REAL + DEFAULT("0"),
                EXLDProcess.c_pt_long to REAL + DEFAULT("0"),
                EXLDProcess.c_pt_pass to REAL + DEFAULT("0"),
                EXLDProcess.c_pt_pe_it to TEXT + DEFAULT("''"),
                EXLDProcess.c_pt_pe_logger_details to TEXT + DEFAULT("''"),
                EXLDProcess.c_pt_pe_notes to TEXT + DEFAULT("''"),
                EXLDProcess.c_pt_pe_pipe_diameter to INTEGER + DEFAULT("0"),
                EXLDProcess.c_pt_pe_pipe_length to TEXT + DEFAULT("''"),
                EXLDProcess.c_pt_pe_pump_size to TEXT + DEFAULT("''"),
                EXLDProcess.c_pt_pe_readings_count to INTEGER + DEFAULT("0"),
                EXLDProcess.c_pt_pipe_description_other to TEXT + DEFAULT("''"),
                EXLDProcess.c_pt_pressurising_finish to TEXT + DEFAULT("''"),
                EXLDProcess.c_pt_pressurising_start to TEXT + DEFAULT("''"),
                EXLDProcess.c_pt_process_no to TEXT + DEFAULT("''"),
                EXLDProcess.c_pt_reading1_time to TEXT + DEFAULT("''"),
                EXLDProcess.c_pt_reading2_time to TEXT + DEFAULT("''"),
                EXLDProcess.c_pt_reading3_time to TEXT + DEFAULT("''"),
                EXLDProcess.c_pt_reading_1 to REAL + DEFAULT("0"),
                EXLDProcess.c_pt_reading_2 to REAL + DEFAULT("0"),
                EXLDProcess.c_pt_reading_3 to REAL + DEFAULT("0"),
                EXLDProcess.c_pt_sampl_given_time to TEXT + DEFAULT("''"),
                EXLDProcess.c_pt_sampl_given_to to TEXT + DEFAULT("''"),
                EXLDProcess.c_pt_sampl_notes to TEXT + DEFAULT("''"),
                EXLDProcess.c_pt_sampl_taken_to_address to TEXT + DEFAULT("''"),
                EXLDProcess.c_pt_sampl_taken_to_lat to REAL + DEFAULT("0"),
                EXLDProcess.c_pt_sampl_taken_to_long to REAL + DEFAULT("0"),
                EXLDProcess.c_pt_section_length to TEXT + DEFAULT("''"),
                EXLDProcess.c_pt_section_name to TEXT + DEFAULT("''"),
                EXLDProcess.c_pt_start_pressure to REAL + DEFAULT("0"),
                EXLDProcess.c_pt_system_test_pressure to REAL + DEFAULT("0"),
                EXLDProcess.c_pt_type to TEXT + DEFAULT("''"),
                EXLDProcess.c_scheme_name to TEXT + DEFAULT("''"),
                EXLDProcess.c_server_process_id to INTEGER + DEFAULT("0"),
                EXLDProcess.c_start_time to TEXT + DEFAULT("''"),
                EXLDProcess.c_survey_notes to TEXT + DEFAULT("''"),
                EXLDProcess.c_swab_home to TEXT + DEFAULT("''"),
                EXLDProcess.c_swab_latitude to REAL + DEFAULT("0"),
                EXLDProcess.c_swab_loaded to TEXT + DEFAULT("''"),
                EXLDProcess.c_swab_longitude to REAL + DEFAULT("0"),
                EXLDProcess.c_swab_notes to TEXT + DEFAULT("''"),
                EXLDProcess.c_swab_photo to TEXT + DEFAULT("''"),
                EXLDProcess.c_swab_removed to TEXT + DEFAULT("''"),
                EXLDProcess.c_swab_removed_lat to REAL + DEFAULT("0"),
                EXLDProcess.c_swab_removed_long to REAL + DEFAULT("0"),
                EXLDProcess.c_swab_run_started to TEXT + DEFAULT("''"),
                EXLDProcess.c_swab_total_water to REAL + DEFAULT("0"),
                EXLDProcess.c_technician_id to INTEGER + DEFAULT("0"),
                EXLDProcess.c_technician_name to TEXT + DEFAULT("''"),
                EXLDProcess.c_testSessDIFirstLogReadingDate to TEXT + DEFAULT("''"),
                EXLDProcess.c_testSessDILastLoggingTime to TEXT + DEFAULT("''"),
                EXLDProcess.c_testSessDIStartLoggingTime to TEXT + DEFAULT("''"),
                EXLDProcess.c_testsessFirstLogReadingDate to TEXT + DEFAULT("''"),
                EXLDProcess.c_testsessLastLoggingTime to TEXT + DEFAULT("''"),
                EXLDProcess.c_testsessStartLoggingTime to TEXT + DEFAULT("''"),
                EXLDProcess.c_tibsessDILogNumberForStart to INTEGER + DEFAULT("0"),
                EXLDProcess.c_tibsessLogNumberForReading1 to INTEGER + DEFAULT("0"),
                EXLDProcess.c_tibsessLogNumberForReading2 to INTEGER + DEFAULT("0"),
                EXLDProcess.c_tibsessLogNumberForReading3 to INTEGER + DEFAULT("0"),
                EXLDProcess.c_tibsessLogNumberForStart to INTEGER + DEFAULT("0"),
                EXLDProcess.c_tibsessLogR15 to INTEGER + DEFAULT("0"),
                EXLDProcess.c_tibsessLogR60 to INTEGER + DEFAULT("0"),
                EXLDProcess.c_vehicle_id to INTEGER + DEFAULT("0"),
                EXLDProcess.c_vehicle_name to TEXT + DEFAULT("''"),
                EXLDProcess.c_pe_needs_uploading to INTEGER + DEFAULT("0"),
                EXLDProcess.c_di_needs_uploading to INTEGER + DEFAULT("0"),
                EXLDProcess.c_pt_reading_5 to REAL + DEFAULT("0"))

        // Tibiis Readings
        db!!.createTable(EXLDTibiisReading.TABLE_NAME, true,
                EXLDTibiisReading.COLUMN_ID to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
                EXLDTibiisReading.COLUMN_BATTERY to INTEGER,
                EXLDTibiisReading.COLUMN_CREATED_ON to TEXT,
                EXLDTibiisReading.COLUMN_FLOW_RATE to INTEGER,
                EXLDTibiisReading.COLUMN_LOG_NUMBER to INTEGER,
                EXLDTibiisReading.COLUMN_PRESSURE to INTEGER,
                EXLDTibiisReading.COLUMN_PROCESS_ID to INTEGER,
                EXLDTibiisReading.COLUMN_READING_TYPE to TEXT,
                EXLDTibiisReading.COLUMN_TEST_TYPE to TEXT,
                EXLDTibiisReading.COLUMN_UPLOADED to INTEGER
        )

        // Pause Sessions
        db!!.createTable(EXLDPauseSessions.TABLE_NAME, true,
                EXLDPauseSessions.COLUMN_ID to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
                EXLDPauseSessions.COLUMN_PAUSE_DURATION_SECONDS to INTEGER,
                EXLDPauseSessions.COLUMN_PAUSE_END to TEXT,
                EXLDPauseSessions.COLUMN_PAUSE_FLOWRATE to REAL,
                EXLDPauseSessions.COLUMN_PAUSE_PROCESS_ID to INTEGER,
                EXLDPauseSessions.COLUMN_PAUSE_START to TEXT,
                EXLDPauseSessions.COLUMN_PAUSE_TYPE to TEXT
                )

        /** WE MUST CREATE COLUMNS IN THE SAME ORDER AS THEY APPEAR IN THE CLASS CONSTRUCTOR - for the classParser() to work **/

        // Swabbing Flowrates
        db!!.createTable(EXLDSwabFlowrates.TABLE_NAME, true,
                EXLDSwabFlowrates.COLUMN_ID to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
                EXLDSwabFlowrates.COLUMN_SWAB_CREATED to TEXT,
                EXLDSwabFlowrates.COLUMN_SWAB_FLOWRATE to REAL,
                EXLDSwabFlowrates.COLUMN_PROCESS_ID to INTEGER
                )

        // Filling Flowrates
        db!!.createTable(EXLDFillingFlowrates.TABLE_NAME, true,
                EXLDFillingFlowrates.COLUMN_ID to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
                EXLDFillingFlowrates.COLUMN_FILLING_PROCESS_ID to INTEGER,
                EXLDFillingFlowrates.COLUMN_FILLING_CREATED to TEXT,
                EXLDFillingFlowrates.COLUMN_FILLING_FLOWRATE to REAL)

        // Flushing Flowrates
        db!!.createTable(EXLDFlushFlowrates.TABLE_NAME, true,
                EXLDFlushFlowrates.COLUMN_ID to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
                EXLDFlushFlowrates.COLUMN_FLUSH_PROCESS_ID to INTEGER,
                EXLDFlushFlowrates.COLUMN_FLUSH_CREATED to TEXT,
                EXLDFlushFlowrates.COLUMN_FLUSH_FLOWRATE to REAL,
                EXLDFlushFlowrates.COLUMN_FLUSH_TYPE to INTEGER)

        // Chlor Flowrates
        db!!.createTable(EXLDChlorFlowrates.TABLE_NAME, true,
                EXLDChlorFlowrates.COLUMN_ID to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
                EXLDChlorFlowrates.COLUMN_CHLOR_PROCESS_ID to INTEGER,
                EXLDChlorFlowrates.COLUMN_CHLOR_FLOWRATE to REAL,
                EXLDChlorFlowrates.COLUMN_CHLOR_PHOTO to TEXT,
                EXLDChlorFlowrates.COLUMN_CHLOR_STRENGTH to REAL,
                EXLDChlorFlowrates.COLUMN_CHLOR_TIMESTAMP to TEXT)

        // Dec Flowrates
        db!!.createTable(EXLDDecFlowrates.TABLE_NAME, true,
                EXLDDecFlowrates.COLUMN_ID to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
                EXLDDecFlowrates.COLUMN_DEC_PROCESS_ID to INTEGER,
                EXLDDecFlowrates.COLUMN_DEC_DISCHARGE to REAL,
                EXLDDecFlowrates.COLUMN_DEC_FLOWRATE to REAL,
                EXLDDecFlowrates.COLUMN_DEC_PHOTO to TEXT,
                EXLDDecFlowrates.COLUMN_DEC_STRENGTH to REAL,
                EXLDDecFlowrates.COLUMN_DEC_TIMESTAMP to TEXT)

        // Survey Notes
        db!!.createTable(EXLDSurveyNotes.TABLE_NAME, true,
                EXLDSurveyNotes.COLUMN_ID to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
                EXLDSurveyNotes.COLUMN_SN_PROCESS_ID to INTEGER,
                EXLDSurveyNotes.COLUMN_SN_LAT to REAL,
                EXLDSurveyNotes.COLUMN_SN_LONG to REAL,
                EXLDSurveyNotes.COLUMN_SN_NOTE to TEXT,
                EXLDSurveyNotes.COLUMN_SN_PHOTO to TEXT,
                EXLDSurveyNotes.COLUMN_SN_TIMESTAMP to TEXT)

        // Equipment Extra
        db!!.createTable(EXLDEquipmentExtra.TABLE_NAME, true,
                EXLDEquipmentExtra.COLUMN_EE_ID to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
                EXLDEquipmentExtra.COLUMN_EE_PROCESS_ID to INTEGER,
                EXLDEquipmentExtra.COLUMN_EE_DESC to TEXT,
                EXLDEquipmentExtra.COLUMN_EE_TYPE to INTEGER)

        // Sampling
        db!!.createTable(EXLDSamplingData.TABLE_NAME, true,
                EXLDSamplingData.COLUMN_SAMP_ID to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
                EXLDSamplingData.COLUMN_PROCESS_ID to INTEGER,
                EXLDSamplingData.COLUMN_SAMP_CHLOR_FREE to REAL,
                EXLDSamplingData.COLUMN_SAMP_CHLOR_TOTAL to REAL,
                EXLDSamplingData.COLUMN_SAMP_DESC to TEXT,
                EXLDSamplingData.COLUMN_SAMP_FAILNOTES to TEXT,
                EXLDSamplingData.COLUMN_SAMP_LAT to REAL,
                EXLDSamplingData.COLUMN_SAMP_LNG to REAL,
                EXLDSamplingData.COLUMN_SAMP_SAMPLE_ID to TEXT,
                EXLDSamplingData.COLUMN_SAMP_LOCATION to TEXT,
                EXLDSamplingData.COLUMN_SAMP_OTHER_INFO to TEXT,
                EXLDSamplingData.COLUMN_SAMP_PHOTO to TEXT,
                EXLDSamplingData.COLUMN_SAMP_TEST_STATUS to INTEGER,
                EXLDSamplingData.COLUMN_SAMP_TIMESTAMP to TEXT,
                EXLDSamplingData.COLUMN_SAMP_TURBIDITY to REAL,
                EXLDSamplingData.COLUMN_SAMP_WATER_TEMP to INTEGER)

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }
}

val Context.database: DBHelper get() = DBHelper.getInstance(applicationContext)
