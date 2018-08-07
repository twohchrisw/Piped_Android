package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.content.Context
import android.provider.SyncStateContract.Helpers.insert
import org.jetbrains.anko.db.*
import java.text.SimpleDateFormat
import java.util.*

data class EXLDProcess(val columnId: Long = -1,
                  var address: String = "",
                  var client: String = "",
                  var company_id: String = "",
                  var consum_additional_fire_hose_qty: Int = 0,
                  var consum_flanges_qty: Int = 0,
                  var consum_flanges_size: Double = 0.0,
                  var consum_notes: String = "",
                  var consum_sodium_bisulphate: Int = 0,
                  var consum_sodium_hypoclorite: Int = 0,
                  var consum_swabs_qty: Int = 0,
                  var consum_swabs_size: Double = 0.0,
                  var create_device: String = "",
                  var create_timestamp: String = "",
                       var di_is_zero_loss: Int = 0,
                       var di_lat: Double = 0.0,
                       var di_long: Double = 0.0,
                       var di_test_has_calculated: Int = 0,
                       var edit_timestamp: String = "",
                       var equip_chlorometer: Int = 0,
                       var equip_chlorosense: Int = 0,
                       var equip_corelator: Int = 0,
                       var equip_data_logger: Int = 0,
                       var equip_direct_injection: Int = 0,
                       var equip_dp_1000: Int = 0,
                       var equip_dt_1500: Int = 0,
                       var equip_dt_3000: Int = 0,
                       var equip_dt_5000: Int = 0,
                       var equip_elec_listening_stick: Int = 0,
                       var equip_ferometer: Int = 0,
                       var equip_hydrogen_detector: Int = 0,
                       var equip_ptp_12: Int = 0,
                       var equip_ptp_30: Int = 0,
                       var equip_ptp_50: Int = 0,
                       var equip_ptp_120: Int = 0,
                       var equip_ptp_250: Int = 0,
                       var equip_swab_tracking: Int = 0,
                       var equip_turbidity: Int = 0,
                       var filling_flow_rate: Double = 0.0,
                       var filling_has_started: Int = 0,
                       var filling_lat: Double = 0.0,
                       var filling_long: Double = 0.0,
                       var filling_notes: String = "",
                       var filling_started: String = "",
                       var filling_stopped: String = "",
                       var filling_total_water_volume: Double = 0.0,
                       var finish_time: String = "",
                       var general_other: String = "",
                       var internalId: String = "",
                       var iphone_sync_id: String = "",
                       var location_lat: Double = 0.0,
                       var location_long: Double = 0.0,
                       var needs_server_sync: Int = 0,
                       var pe_pdf_calc_result: Double = 0.0,
                       var pe_pdf_log_pa_t1: Double = 0.0,
                       var pe_pdf_log_pa_t2: Double = 0.0,
                       var pe_pdf_log_pa_t3: Double = 0.0,
                       var pe_pdf_log_t1: Double = 0.0,
                       var pe_pdf_log_t2: Double = 0.0,
                       var pe_pdf_log_t3: Double = 0.0,
                       var pe_pdf_n1: Double = 0.0,
                       var pe_pdf_n2: Double = 0.0,
                       var pe_pdf_n3: Double = 0.0,
                       var pe_pdf_n4: Double = 0.0,
                       var pe_pdf_pass: Int = 0,
                       var pe_test_aborted: Int = 0,
                       var pe_test_has_calculated: Int = 0,
                       var pipe_description: String = "",
                       var pipe_diameter: Int = 0,
                       var pipe_length: Int = 0,
                       var pt_calc_result: Double = 0.0,
                       var pt_chlor_contact_ends: String = "",
                       var pt_chlor_end_photo: String = "",
                       var pt_chlor_end_strength: Double = 0.0,
                       var pt_chlor_main_chlorinated: String = "",
                       var pt_chlor_notes: String = "",
                       var pt_chlor_start_flowrate: Int = 0,
                       var pt_chlor_start_lat: Double = 0.0,
                       var pt_chlor_start_long: Double = 0.0,
                       var pt_chlor_start_photo: String = "",
                       var pt_chlor_start_strength: Double = 0.0,
                       var pt_chlor_start_time: String = "",
                       var pt_chlor_volume: Double = 0.0,
                       var pt_dec_dechlorinated: String = "",
                       var pt_dec_notes: String = "",
                       var pt_dec_photo: String = "",
                       var pt_dec_start: String = "",
                       var pt_dec_start_lat: Double = 0.0,
                       var pt_dec_start_long: Double = 0.0,
                       var pt_dec_volume: Double = 0.0,
                       var pt_di_logger_details: String = "",
                       var pt_di_notes: String = "",
                       var pt_di_pipe_diameter: Int = 0,
                       var pt_di_pressurising_started: String = "",
                       var pt_di_pump_size: String = "",
                       var pt_di_r15_time: String = "",
                       var pt_di_r15_value: Double = 0.0,
                       var pt_di_r60_time: String = "",
                       var pt_di_r60_value: Double = 0.0,
                       var pt_di_section_length: String = "",
                       var pt_di_section_name: String = "",
                       var pt_di_start_pressure: Double = 0.0,
                       var pt_di_stp: Double = 0.0,
                       var pt_di_timer_status: Int = 0,
                       var pt_flush_completed: String = "",
                       var pt_flush_completed2: String = "",
                       var pt_flush_notes: String = "",
                       var pt_flush_notes2: String = "",
                       var pt_flush_start_lat: Double = 0.0,
                       var pt_flush_start_lat2: Double = 0.0,
                       var pt_flush_start_long: Double = 0.0,
                       var pt_flush_start_long2: Double = 0.0,
                       var pt_flush_started: String = "",
                       var pt_flush_started2: String = "",
                       var pt_flush_total_water: Double = 0.0,
                       var pt_flush_total_water2: Double = 0.0,
                       var pt_id: Int = 0,
                       var pt_installation_tech: String = "",
                       var pt_installation_tech_id: Int = 0,
                       var pt_installation_tech_other: String = "",
                       var pt_lat: Double = 0.0,
                       var pt_long: Double = 0.0,
                       var pt_pass: Double = 0.0,
                       var pt_pe_it: String = "",
                       var pt_pe_logger_details: String = "",
                       var pt_pe_notes: String = "",
                       var pt_pe_pipe_diameter: Int = 0,
                       var pt_pe_pipe_length: String = "",
                       var pt_pe_pump_size: String = "",
                       var pt_pe_readings_count: Int = 0,
                       var pt_pipe_description_other: String = "",
                       var pt_pressurising_finish: String = "",
                       var pt_pressurising_start: String = "",
                       var pt_process_no: String = "",
                       var pt_reading1_time: String = "",
                       var pt_reading2_time: String = "",
                       var pt_reading3_time: String = "",
                       var pt_reading_1: Double = 0.0,
                       var pt_reading_2: Double = 0.0,
                       var pt_reading_3: Double = 0.0,
                       var pt_sampl_given_time: String = "",
                       var pt_sampl_given_to: String = "",
                       var pt_sampl_notes: String = "",
                       var pt_sampl_taken_to_address: String = "",
                       var pt_sampl_taken_to_lat: Double = 0.0,
                       var pt_sampl_taken_to_long: Double = 0.0,
                       var pt_section_length: String = "",
                       var pt_section_name: String = "",
                       var pt_start_pressure: Double = 0.0,
                       var pt_system_test_pressure: Double = 0.0,
                       var pt_type: String = "",
                       var scheme_name: String = "",
                       var server_process_id: Int = -1,
                       var start_time: String = "",
                       var survey_notes: String = "",
                       var swab_home: String = "",
                       var swab_latitude: Double = 0.0,
                       var swab_loaded: String = "",
                       var swab_longitude: Double = 0.0,
                       var swab_notes: String = "",
                       var swab_photo: String = "",
                       var swab_removed: String = "",
                       var swab_removed_lat: Double = 0.0,
                       var swab_removed_long: Double = 0.0,
                       var swab_run_started: String = "",
                       var swab_total_water: Double = 0.0,
                       var technician_id: Int = 0,
                       var technician_name: String = "",
                       var testSessDIFirstLogReadingDate: String = "",
                       var testSessDILastLoggingTime: String = "",
                       var testSessDIStartLoggingTime: String = "",
                       var testsessFirstLogReadingDate: String = "",
                       var testsessLastLoggingTime: String = "",
                       var testsessStartLoggingTime: String = "",
                       var tibsessDILogNumberForStart: Int = 0,
                       var tibsessLogNumberForReading1: Int = 0,
                       var tibsessLogNumberForReading2: Int = 0,
                       var tibsessLogNumberForReading3: Int = 0,
                       var tibsessLogNumberForStart: Int = 0,
                       var tibsessLogR15: Int = 0,
                       var tibsessLogR60: Int = 0,
                       var vehicle_id: Int = 0,
                       var vehicle_name: String = ""
                       )
{
    var processSyncInProgress = false

    companion object {
        val TABLE_NAME = "EXLDProcess"
        val COLUMN_ID = "ID"
        val c_address = "address"
        val c_client = "client"
        val c_company_id = "company_id"
        val c_consum_additional_fire_hose_qty = "consum_additional_fire_hose_qty"
        val c_consum_flanges_qty = "consum_flanges_qty"
        val c_consum_flanges_size = "consum_flanges_size"
        val c_consum_notes = "consum_notes"
        val c_consum_sodium_bisulphate = "consum_sodium_bisulphate"
        val c_consum_sodium_hypoclorite = "consum_sodium_hypoclorite"
        val c_consum_swabs_qty = "consum_swabs_qty"
        val c_consum_swabs_size = "consum_swabs_size"
        val c_create_device = "create_device"
        val c_create_timestamp = "create_timestamp"
        val c_di_is_zero_loss = "di_is_zero_loss"
        val c_di_lat = "di_lat"
        val c_di_long = "di_long"
        val c_di_test_has_calculated = "di_test_has_calculated"
        val c_edit_timestamp = "edit_timestamp"
        val c_equip_chlorometer = "equip_chlorometer"
        val c_equip_chlorosense = "equip_chlorosense"
        val c_equip_corelator = "equip_corelator"
        val c_equip_data_logger = "equip_data_logger"
        val c_equip_direct_injection = "equip_direct_injection"
        val c_equip_dp_1000 = "equip_dp_1000"
        val c_equip_dt_1500 = "equip_dt_1500"
        val c_equip_dt_3000 = "equip_dt_3000"
        val c_equip_dt_5000 = "equip_dt_5000"
        val c_equip_elec_listening_stick = "equip_elec_listening_stick"
        val c_equip_ferometer = "equip_ferometer"
        val c_equip_hydrogen_detector = "equip_hydrogen_detector"
        val c_equip_ptp_12 = "equip_ptp_12"
        val c_equip_ptp_30 = "equip_ptp_30"
        val c_equip_ptp_50 = "equip_ptp_50"
        val c_equip_ptp_120 = "equip_ptp_120"
        val c_equip_ptp_250 = "equip_ptp_250"
        val c_equip_swab_tracking = "equip_swab_tracking"
        val c_equip_turbidity = "equip_turbidity"
        val c_filling_flow_rate = "filling_flow_rate"
        val c_filling_has_started = "filling_has_started"
        val c_filling_lat = "filling_lat"
        val c_filling_long = "filling_long"
        val c_filling_notes = "filling_notes"
        val c_filling_started = "filling_started"
        val c_filling_stopped = "filling_stopped"
        val c_filling_total_water_volume = "filling_total_water_volume"
        val c_finish_time = "finish_time"
        val c_general_other = "general_other"
        val c_internalId = "internalId"
        val c_iphone_sync_id = "iphone_sync_id"
        val c_location_lat = "location_lat"
        val c_location_long = "location_long"
        val c_needs_server_sync = "needs_server_sync"
        val c_pe_pdf_calc_result = "pe_pdf_calc_result"
        val c_pe_pdf_log_pa_t1 = "pe_pdf_log_pa_t1"
        val c_pe_pdf_log_pa_t2 = "pe_pdf_log_pa_t2"
        val c_pe_pdf_log_pa_t3 = "pe_pdf_log_pa_t3"
        val c_pe_pdf_log_t1 = "pe_pdf_log_t1"
        val c_pe_pdf_log_t2 = "pe_pdf_log_t2"
        val c_pe_pdf_log_t3 = "pe_pdf_log_t3"
        val c_pe_pdf_n1 = "pe_pdf_n1"
        val c_pe_pdf_n2 = "pe_pdf_n2"
        val c_pe_pdf_n3 = "pe_pdf_n3"
        val c_pe_pdf_n4 = "pe_pdf_n4"
        val c_pe_pdf_pass = "pe_pdf_pass"
        val c_pe_test_aborted = "pe_test_aborted"
        val c_pe_test_has_calculated = "pe_test_has_calculated"
        val c_pipe_description = "pipe_description"
        val c_pipe_diameter = "pipe_diameter"
        val c_pipe_length = "pipe_length"
        val c_pt_calc_result = "pt_calc_result"
        val c_pt_chlor_contact_ends = "pt_chlor_contact_ends"
        val c_pt_chlor_end_photo = "pt_chlor_end_photo"
        val c_pt_chlor_end_strength = "pt_chlor_end_strength"
        val c_pt_chlor_main_chlorinated = "pt_chlor_main_chlorinated"
        val c_pt_chlor_notes = "pt_chlor_notes"
        val c_pt_chlor_start_flowrate = "pt_chlor_start_flowrate"
        val c_pt_chlor_start_lat = "pt_chlor_start_lat"
        val c_pt_chlor_start_long = "pt_chlor_start_long"
        val c_pt_chlor_start_photo = "pt_chlor_start_photo"
        val c_pt_chlor_start_strength = "pt_chlor_start_strength"
        val c_pt_chlor_start_time = "pt_chlor_start_time"
        val c_pt_chlor_volume = "pt_chlor_volume"
        val c_pt_dec_dechlorinated = "pt_dec_dechlorinated"
        val c_pt_dec_notes = "pt_dec_notes"
        val c_pt_dec_photo = "pt_dec_photo"
        val c_pt_dec_start = "pt_dec_start"
        val c_pt_dec_start_lat = "pt_dec_start_lat"
        val c_pt_dec_start_long = "pt_dec_start_long"
        val c_pt_dec_volume = "pt_dec_volume"
        val c_pt_di_logger_details = "pt_di_logger_details"
        val c_pt_di_notes = "pt_di_notes"
        val c_pt_di_pipe_diameter = "pt_di_pipe_diameter"
        val c_pt_di_pressurising_started = "pt_di_pressurising_started"
        val c_pt_di_pump_size = "pt_di_pump_size"
        val c_pt_di_r15_time = "pt_di_r15_time"
        val c_pt_di_r15_value = "pt_di_r15_value"
        val c_pt_di_r60_time = "pt_di_r60_time"
        val c_pt_di_r60_value = "pt_di_r60_value"
        val c_pt_di_section_length = "pt_di_section_length"
        val c_pt_di_section_name = "pt_di_section_name"
        val c_pt_di_start_pressure = "pt_di_start_pressure"
        val c_pt_di_stp = "pt_di_stp"
        val c_pt_di_timer_status = "pt_di_timer_status"
        val c_pt_flush_completed = "pt_flush_completed"
        val c_pt_flush_completed2 = "pt_flush_completed2"
        val c_pt_flush_notes = "pt_flush_notes"
        val c_pt_flush_notes2 = "pt_flush_notes2"
        val c_pt_flush_start_lat = "pt_flush_start_lat"
        val c_pt_flush_start_lat2 = "pt_flush_start_lat2"
        val c_pt_flush_start_long = "pt_flush_start_long"
        val c_pt_flush_start_long2 = "pt_flush_start_long2"
        val c_pt_flush_started = "pt_flush_started"
        val c_pt_flush_started2 = "pt_flush_started2"
        val c_pt_flush_total_water = "pt_flush_total_water"
        val c_pt_flush_total_water2 = "pt_flush_total_water2"
        val c_pt_id = "pt_id"
        val c_pt_installation_tech = "pt_installation_tech"
        val c_pt_installation_tech_id = "pt_installation_tech_id"
        val c_pt_installation_tech_other = "pt_installation_tech_other"
        val c_pt_lat = "pt_lat"
        val c_pt_long = "pt_long"
        val c_pt_pass = "pt_pass"
        val c_pt_pe_it = "pt_pe_it"
        val c_pt_pe_logger_details = "pt_pe_logger_details"
        val c_pt_pe_notes = "pt_pe_notes"
        val c_pt_pe_pipe_diameter = "pt_pe_pipe_diameter"
        val c_pt_pe_pipe_length = "pt_pe_pipe_length"
        val c_pt_pe_pump_size = "pt_pe_pump_size"
        val c_pt_pe_readings_count = "pt_pe_readings_count"
        val c_pt_pipe_description_other = "pt_pipe_description_other"
        val c_pt_pressurising_finish = "pt_pressurising_finish"
        val c_pt_pressurising_start = "pt_pressurising_start"
        val c_pt_process_no = "pt_process_no"
        val c_pt_reading1_time = "pt_reading1_time"
        val c_pt_reading2_time = "pt_reading2_time"
        val c_pt_reading3_time = "pt_reading3_time"
        val c_pt_reading_1 = "pt_reading_1"
        val c_pt_reading_2 = "pt_reading_2"
        val c_pt_reading_3 = "pt_reading_3"
        val c_pt_sampl_given_time = "pt_sampl_given_time"
        val c_pt_sampl_given_to = "pt_sampl_given_to"
        val c_pt_sampl_notes = "pt_sampl_notes"
        val c_pt_sampl_taken_to_address = "pt_sampl_taken_to_address"
        val c_pt_sampl_taken_to_lat = "pt_sampl_taken_to_lat"
        val c_pt_sampl_taken_to_long = "pt_sampl_taken_to_long"
        val c_pt_section_length = "pt_section_length"
        val c_pt_section_name = "pt_section_name"
        val c_pt_start_pressure = "pt_start_pressure"
        val c_pt_system_test_pressure = "pt_system_test_pressure"
        val c_pt_type = "pt_type"
        val c_scheme_name = "scheme_name"
        val c_server_process_id  = "server_process_id"
        val c_start_time = "start_time"
        val c_survey_notes = "survey_notes"
        val c_swab_home = "swab_home"
        val c_swab_latitude = "swab_latitude"
        val c_swab_loaded = "swab_loaded"
        val c_swab_longitude = "swab_longitude"
        val c_swab_notes = "swab_notes"
        val c_swab_photo = "swab_photo"
        val c_swab_removed = "swab_removed"
        val c_swab_removed_lat = "swab_removed_lat"
        val c_swab_removed_long = "swab_removed_long"
        val c_swab_run_started = "swab_run_started"
        val c_swab_total_water = "swab_total_water"
        val c_technician_id = "technician_id"
        val c_technician_name = "technician_name"
        val c_testSessDIFirstLogReadingDate = "testSessDIFirstLogReadingDate"
        val c_testSessDILastLoggingTime = "testSessDILastLoggingTime"
        val c_testSessDIStartLoggingTime = "testSessDIStartLoggingTime"
        val c_testsessFirstLogReadingDate = "testsessFirstLogReadingDate"
        val c_testsessLastLoggingTime = "testsessLastLoggingTime"
        val c_testsessStartLoggingTime = "testsessStartLoggingTime"
        val c_tibsessDILogNumberForStart = "tibsessDILogNumberForStart"
        val c_tibsessLogNumberForReading1 = "tibsessLogNumberForReading1"
        val c_tibsessLogNumberForReading2 = "tibsessLogNumberForReading2"
        val c_tibsessLogNumberForReading3 = "tibsessLogNumberForReading3"
        val c_tibsessLogNumberForStart = "tibsessLogNumberForStart"
        val c_tibsessLogR15 = "tibsessLogR15"
        val c_tibsessLogR60 = "tibsessLogR60"
        val c_vehicle_id = "vehicle_id"
        val c_vehicle_name  = "vehicle_name"

        fun allProcesses(context: Context):List<EXLDProcess>
        {
            val processes = context.database.use {
                select(EXLDProcess.TABLE_NAME).orderBy(COLUMN_ID, SqlOrderDirection.DESC).exec {
                    parseList<EXLDProcess>(classParser())
                }
            }

            return processes
        }

        fun processForId(context: Context, columnId: Long): EXLDProcess?
        {
            val processes = context.database.use {
                select(EXLDProcess.TABLE_NAME).whereArgs(COLUMN_ID + " = " + columnId.toString()).exec {
                    parseList<EXLDProcess>(classParser())
                }
            }

            if (processes.isNotEmpty()) {
                return processes.get(0)
            }
            else
            {
                return null
            }
        }

    }

    // Save the process
    public fun save(context: Context): Long
    {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:MM:SS")
        val today = sdf.format(Date())
        var saveId: Long = -1
        var defaultCoordinate = -10000.0

        if (columnId < 1)
        {
            // Insert
            context.database.use {
                saveId = insert(EXLDProcess.TABLE_NAME, EXLDProcess.c_create_timestamp to today, EXLDProcess.c_create_device to "Android", EXLDProcess.c_company_id to company_id,
                        EXLDProcess.c_pt_lat to defaultCoordinate,
                        EXLDProcess.c_pt_long to defaultCoordinate,
                        EXLDProcess.c_swab_removed_lat to defaultCoordinate,
                        EXLDProcess.c_swab_removed_long to defaultCoordinate,
                        EXLDProcess.c_pt_sampl_taken_to_lat to defaultCoordinate,
                        EXLDProcess.c_pt_sampl_taken_to_long to defaultCoordinate,
                        EXLDProcess.c_location_lat to defaultCoordinate,
                        EXLDProcess.c_location_long to defaultCoordinate,
                        EXLDProcess.c_pt_flush_start_lat to defaultCoordinate,
                        EXLDProcess.c_pt_flush_start_long to defaultCoordinate,
                        EXLDProcess.c_pt_flush_start_lat2 to defaultCoordinate,
                        EXLDProcess.c_pt_flush_start_long2 to defaultCoordinate,
                        EXLDProcess.c_pt_dec_start_lat to defaultCoordinate,
                        EXLDProcess.c_pt_dec_start_long to defaultCoordinate,
                        EXLDProcess.c_pt_chlor_start_lat to defaultCoordinate,
                        EXLDProcess.c_pt_chlor_start_long to defaultCoordinate)
            }
        }
        else
        {
            // Update
            context.database.use {
                update(EXLDProcess.TABLE_NAME, EXLDProcess.c_address to address,
                        EXLDProcess.c_client to client,
                        EXLDProcess.c_company_id to company_id,
                        EXLDProcess.c_consum_additional_fire_hose_qty to consum_additional_fire_hose_qty,
                        EXLDProcess.c_consum_flanges_qty to consum_flanges_qty,
                        EXLDProcess.c_consum_flanges_size to consum_flanges_size,
                        EXLDProcess.c_consum_notes to consum_notes,
                        EXLDProcess.c_consum_sodium_bisulphate to consum_sodium_bisulphate,
                        EXLDProcess.c_consum_sodium_hypoclorite to consum_sodium_hypoclorite,
                        EXLDProcess.c_consum_swabs_qty to consum_swabs_qty,
                        EXLDProcess.c_consum_swabs_size to consum_swabs_size,
                        EXLDProcess.c_create_device to create_device,
                        EXLDProcess.c_create_timestamp to create_timestamp,
                        EXLDProcess.c_di_is_zero_loss to di_is_zero_loss,
                        EXLDProcess.c_di_lat to di_lat,
                        EXLDProcess.c_di_long to di_long,
                        EXLDProcess.c_di_test_has_calculated to di_test_has_calculated,
                        EXLDProcess.c_edit_timestamp to today,
                        EXLDProcess.c_equip_chlorometer to equip_chlorometer,
                        EXLDProcess.c_equip_chlorosense to equip_chlorosense,
                        EXLDProcess.c_equip_corelator to equip_corelator,
                        EXLDProcess.c_equip_data_logger to equip_data_logger,
                        EXLDProcess.c_equip_direct_injection to equip_direct_injection,
                        EXLDProcess.c_equip_dp_1000 to equip_dp_1000,
                        EXLDProcess.c_equip_dt_1500 to equip_dt_1500,
                        EXLDProcess.c_equip_dt_3000 to equip_dt_3000,
                        EXLDProcess.c_equip_dt_5000 to equip_dt_5000,
                        EXLDProcess.c_equip_elec_listening_stick to equip_elec_listening_stick,
                        EXLDProcess.c_equip_ferometer to equip_ferometer,
                        EXLDProcess.c_equip_hydrogen_detector to equip_hydrogen_detector,
                        EXLDProcess.c_equip_ptp_12 to equip_ptp_12,
                        EXLDProcess.c_equip_ptp_30 to equip_ptp_30,
                        EXLDProcess.c_equip_ptp_50 to equip_ptp_50,
                        EXLDProcess.c_equip_ptp_120 to equip_ptp_120,
                        EXLDProcess.c_equip_ptp_250 to equip_ptp_250,
                        EXLDProcess.c_equip_swab_tracking to equip_swab_tracking,
                        EXLDProcess.c_equip_turbidity to equip_turbidity,
                        EXLDProcess.c_filling_flow_rate to filling_flow_rate,
                        EXLDProcess.c_filling_has_started to filling_has_started,
                        EXLDProcess.c_filling_lat to filling_lat,
                        EXLDProcess.c_filling_long to filling_long,
                        EXLDProcess.c_filling_notes to filling_notes,
                        EXLDProcess.c_filling_started to filling_started,
                        EXLDProcess.c_filling_stopped to filling_stopped,
                        EXLDProcess.c_filling_total_water_volume to filling_total_water_volume,
                        EXLDProcess.c_finish_time to finish_time,
                        EXLDProcess.c_general_other to general_other,
                        EXLDProcess.c_internalId to internalId,
                        EXLDProcess.c_iphone_sync_id to iphone_sync_id,
                        EXLDProcess.c_location_lat to location_lat,
                        EXLDProcess.c_location_long to location_long,
                        EXLDProcess.c_needs_server_sync to needs_server_sync,
                        EXLDProcess.c_pe_pdf_calc_result to pe_pdf_calc_result,
                        EXLDProcess.c_pe_pdf_log_pa_t1 to pe_pdf_log_pa_t1,
                        EXLDProcess.c_pe_pdf_log_pa_t2 to pe_pdf_log_pa_t2,
                        EXLDProcess.c_pe_pdf_log_pa_t3 to pe_pdf_log_pa_t3,
                        EXLDProcess.c_pe_pdf_log_t1 to pe_pdf_log_t1,
                        EXLDProcess.c_pe_pdf_log_t2 to pe_pdf_log_t2,
                        EXLDProcess.c_pe_pdf_log_t3 to pe_pdf_log_t3,
                        EXLDProcess.c_pe_pdf_n1 to pe_pdf_n1,
                        EXLDProcess.c_pe_pdf_n2 to pe_pdf_n2,
                        EXLDProcess.c_pe_pdf_n3 to pe_pdf_n3,
                        EXLDProcess.c_pe_pdf_n4 to pe_pdf_n4,
                        EXLDProcess.c_pe_pdf_pass to pe_pdf_pass,
                        EXLDProcess.c_pe_test_aborted to pe_test_aborted,
                        EXLDProcess.c_pe_test_has_calculated to pe_test_has_calculated,
                        EXLDProcess.c_pipe_description to pipe_description,
                        EXLDProcess.c_pipe_diameter to pipe_diameter,
                        EXLDProcess.c_pipe_length to pipe_length,
                        EXLDProcess.c_pt_calc_result to pt_calc_result,
                        EXLDProcess.c_pt_chlor_contact_ends to pt_chlor_contact_ends,
                        EXLDProcess.c_pt_chlor_end_photo to pt_chlor_end_photo,
                        EXLDProcess.c_pt_chlor_end_strength to pt_chlor_end_strength,
                        EXLDProcess.c_pt_chlor_main_chlorinated to pt_chlor_main_chlorinated,
                        EXLDProcess.c_pt_chlor_notes to pt_chlor_notes,
                        EXLDProcess.c_pt_chlor_start_flowrate to pt_chlor_start_flowrate,
                        EXLDProcess.c_pt_chlor_start_lat to pt_chlor_start_lat,
                        EXLDProcess.c_pt_chlor_start_long to pt_chlor_start_long,
                        EXLDProcess.c_pt_chlor_start_photo to pt_chlor_start_photo,
                        EXLDProcess.c_pt_chlor_start_strength to pt_chlor_start_strength,
                        EXLDProcess.c_pt_chlor_start_time to pt_chlor_start_time,
                        EXLDProcess.c_pt_chlor_volume to pt_chlor_volume,
                        EXLDProcess.c_pt_dec_dechlorinated to pt_dec_dechlorinated,
                        EXLDProcess.c_pt_dec_notes to pt_dec_notes,
                        EXLDProcess.c_pt_dec_photo to pt_dec_photo,
                        EXLDProcess.c_pt_dec_start to pt_dec_start,
                        EXLDProcess.c_pt_dec_start_lat to pt_dec_start_lat,
                        EXLDProcess.c_pt_dec_start_long to pt_dec_start_long,
                        EXLDProcess.c_pt_dec_volume to pt_dec_volume,
                        EXLDProcess.c_pt_di_logger_details to pt_di_logger_details,
                        EXLDProcess.c_pt_di_notes to pt_di_notes,
                        EXLDProcess.c_pt_di_pipe_diameter to pt_di_pipe_diameter,
                        EXLDProcess.c_pt_di_pressurising_started to pt_di_pressurising_started,
                        EXLDProcess.c_pt_di_pump_size to pt_di_pump_size,
                        EXLDProcess.c_pt_di_r15_time to pt_di_r15_time,
                        EXLDProcess.c_pt_di_r15_value to pt_di_r15_value,
                        EXLDProcess.c_pt_di_r60_time to pt_di_r60_time,
                        EXLDProcess.c_pt_di_r60_value to pt_di_r60_value,
                        EXLDProcess.c_pt_di_section_length to pt_di_section_length,
                        EXLDProcess.c_pt_di_section_name to pt_di_section_name,
                        EXLDProcess.c_pt_di_start_pressure to pt_di_start_pressure,
                        EXLDProcess.c_pt_di_stp to pt_di_stp,
                        EXLDProcess.c_pt_di_timer_status to pt_di_timer_status,
                        EXLDProcess.c_pt_flush_completed to pt_flush_completed,
                        EXLDProcess.c_pt_flush_completed2 to pt_flush_completed2,
                        EXLDProcess.c_pt_flush_notes to pt_flush_notes,
                        EXLDProcess.c_pt_flush_notes2 to pt_flush_notes2,
                        EXLDProcess.c_pt_flush_start_lat to pt_flush_start_lat,
                        EXLDProcess.c_pt_flush_start_lat2 to pt_flush_start_lat2,
                        EXLDProcess.c_pt_flush_start_long to pt_flush_start_long,
                        EXLDProcess.c_pt_flush_start_long2 to pt_flush_start_long2,
                        EXLDProcess.c_pt_flush_started to pt_flush_started,
                        EXLDProcess.c_pt_flush_started2 to pt_flush_started2,
                        EXLDProcess.c_pt_flush_total_water to pt_flush_total_water,
                        EXLDProcess.c_pt_flush_total_water2 to pt_flush_total_water2,
                        EXLDProcess.c_pt_id to pt_id,
                        EXLDProcess.c_pt_installation_tech to pt_installation_tech,
                        EXLDProcess.c_pt_installation_tech_id to pt_installation_tech_id,
                        EXLDProcess.c_pt_installation_tech_other to pt_installation_tech_other,
                        EXLDProcess.c_pt_lat to pt_lat,
                        EXLDProcess.c_pt_long to pt_long,
                        EXLDProcess.c_pt_pass to pt_pass,
                        EXLDProcess.c_pt_pe_it to pt_pe_it,
                        EXLDProcess.c_pt_pe_logger_details to pt_pe_logger_details,
                        EXLDProcess.c_pt_pe_notes to pt_pe_notes,
                        EXLDProcess.c_pt_pe_pipe_diameter to pt_pe_pipe_diameter,
                        EXLDProcess.c_pt_pe_pipe_length to pt_pe_pipe_length,
                        EXLDProcess.c_pt_pe_pump_size to pt_pe_pump_size,
                        EXLDProcess.c_pt_pe_readings_count to pt_pe_readings_count,
                        EXLDProcess.c_pt_pipe_description_other to pt_pipe_description_other,
                        EXLDProcess.c_pt_pressurising_finish to pt_pressurising_finish,
                        EXLDProcess.c_pt_pressurising_start to pt_pressurising_start,
                        EXLDProcess.c_pt_process_no to pt_process_no,
                        EXLDProcess.c_pt_reading1_time to pt_reading1_time,
                        EXLDProcess.c_pt_reading2_time to pt_reading2_time,
                        EXLDProcess.c_pt_reading3_time to pt_reading3_time,
                        EXLDProcess.c_pt_reading_1 to pt_reading_1,
                        EXLDProcess.c_pt_reading_2 to pt_reading_2,
                        EXLDProcess.c_pt_reading_3 to pt_reading_3,
                        EXLDProcess.c_pt_sampl_given_time to pt_sampl_given_time,
                        EXLDProcess.c_pt_sampl_given_to to pt_sampl_given_to,
                        EXLDProcess.c_pt_sampl_notes to pt_sampl_notes,
                        EXLDProcess.c_pt_sampl_taken_to_address to pt_sampl_taken_to_address,
                        EXLDProcess.c_pt_sampl_taken_to_lat to pt_sampl_taken_to_lat,
                        EXLDProcess.c_pt_sampl_taken_to_long to pt_sampl_taken_to_long,
                        EXLDProcess.c_pt_section_length to pt_section_length,
                        EXLDProcess.c_pt_section_name to pt_section_name,
                        EXLDProcess.c_pt_start_pressure to pt_start_pressure,
                        EXLDProcess.c_pt_system_test_pressure to pt_system_test_pressure,
                        EXLDProcess.c_pt_type to pt_type,
                        EXLDProcess.c_scheme_name to scheme_name,
                        EXLDProcess.c_server_process_id to server_process_id,
                        EXLDProcess.c_start_time to start_time,
                        EXLDProcess.c_survey_notes to survey_notes,
                        EXLDProcess.c_swab_home to swab_home,
                        EXLDProcess.c_swab_latitude to swab_latitude,
                        EXLDProcess.c_swab_loaded to swab_loaded,
                        EXLDProcess.c_swab_longitude to swab_longitude,
                        EXLDProcess.c_swab_notes to swab_notes,
                        EXLDProcess.c_swab_photo to swab_photo,
                        EXLDProcess.c_swab_removed to swab_removed,
                        EXLDProcess.c_swab_removed_lat to swab_removed_lat,
                        EXLDProcess.c_swab_removed_long to swab_removed_long,
                        EXLDProcess.c_swab_run_started to swab_run_started,
                        EXLDProcess.c_swab_total_water to swab_total_water,
                        EXLDProcess.c_technician_id to technician_id,
                        EXLDProcess.c_technician_name to technician_name,
                        EXLDProcess.c_testSessDIFirstLogReadingDate to testSessDIFirstLogReadingDate,
                        EXLDProcess.c_testSessDILastLoggingTime to testSessDILastLoggingTime,
                        EXLDProcess.c_testSessDIStartLoggingTime to testSessDIStartLoggingTime,
                        EXLDProcess.c_testsessFirstLogReadingDate to testsessFirstLogReadingDate,
                        EXLDProcess.c_testsessLastLoggingTime to testsessLastLoggingTime,
                        EXLDProcess.c_testsessStartLoggingTime to testsessStartLoggingTime,
                        EXLDProcess.c_tibsessDILogNumberForStart to tibsessDILogNumberForStart,
                        EXLDProcess.c_tibsessLogNumberForReading1 to tibsessLogNumberForReading1,
                        EXLDProcess.c_tibsessLogNumberForReading2 to tibsessLogNumberForReading2,
                        EXLDProcess.c_tibsessLogNumberForReading3 to tibsessLogNumberForReading3,
                        EXLDProcess.c_tibsessLogNumberForStart to tibsessLogNumberForStart,
                        EXLDProcess.c_tibsessLogR15 to tibsessLogR15,
                        EXLDProcess.c_tibsessLogR60 to tibsessLogR60,
                        EXLDProcess.c_vehicle_id to vehicle_id,
                        EXLDProcess.c_vehicle_name to vehicle_name
                        ).whereArgs(EXLDProcess.COLUMN_ID + " = " + columnId.toString()).exec()
            }
        }

        return saveId
    }

    // A formatted process description either LOCAL (if not synced) or using the company ud
    public fun processNoDescription(): String
    {
        if (server_process_id > 0)
        {
            return company_id.toUpperCase() + "-PROCESS-" + String.format("%05d", server_process_id)
        }
        else
        {
            return "LOCAL-PROCESS-" + String.format("%05d", columnId)
        }
    }
}