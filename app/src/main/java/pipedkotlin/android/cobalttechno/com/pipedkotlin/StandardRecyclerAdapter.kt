package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.view_holder_picture.view.*
import org.jetbrains.anko.db.classParser
import org.jetbrains.anko.db.delete
import org.jetbrains.anko.db.parseList
import org.jetbrains.anko.db.select
import org.jetbrains.anko.runOnUiThread
import java.util.*

class StandardRecyclerAdapter(val ctx: Context, val pipedTask: PipedTask, var lastLat: Double, var lastLng: Double,
                              var delegate: StandardRecyclerAdapterInterface?,
                              var surveyNoteInterface: StandardRecyclerSurveyNoteInterface? = null,
                              var chlorInterface: StandardRecyclerChlorInterface? = null,
                              var decInterface: StandardRecyclerDeChlorInterface? = null,
                              var sampInterface: StandardRecyclerSamplingInterface? = null): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var pauseSessions: List<EXLDPauseSessions>? = null
    var taskIsPaused = false
    var currentPause: EXLDPauseSessions? = null
    var shouldCalculateTotalWater = false

    interface StandardRecyclerAdapterInterface {
        fun didRequestMainImage(fieldName: String)
        fun didRequestNotes(fieldName: String)
        fun didRequestFlowrate(position: Int)
    }

    interface StandardRecyclerSurveyNoteInterface {
        fun didRequestNote(note: EXLDSurveyNotes)
        fun didRequestImage(note: EXLDSurveyNotes)
    }

    interface StandardRecyclerChlorInterface
    {
        fun didRequestChlorImage(flowrate: EXLDChlorFlowrates)
    }

    interface StandardRecyclerDeChlorInterface
    {
        fun didRequestDeChlorImage(flowrate: EXLDDecFlowrates)
    }

    interface StandardRecyclerSamplingInterface {
        fun didRequestSampleImage(flowrate: EXLDSamplingData)
    }

    enum class PipedTask {
        Swabbing, Filling, Chlorination, DeChlorination, Flushing, Flushing2, Surveying, Sampling, DecFlowrate, ChlorFlowrate, Consumables, SamplingFlowrate
    }

    //region Rows Definition

    enum class SamplFlowrateRows(val value: PipedTableRow)
    {
        SampleID(PipedTableRow(0, PipedTableRow.PipedTableRowType.TitleValue, "Sample ID", "", EXLDSamplingData.COLUMN_SAMP_SAMPLE_ID)),
        SampleDesc(PipedTableRow(1, PipedTableRow.PipedTableRowType.TitleValue, "Sample Description", "", EXLDSamplingData.COLUMN_SAMP_DESC)),
        Location(PipedTableRow(2, PipedTableRow.PipedTableRowType.TitleValue, "Location", "", EXLDSamplingData.COLUMN_SAMP_LOCATION)),
        LocationCoords(PipedTableRow(3, PipedTableRow.PipedTableRowType.DateSet, "Location Coordinates", "", EXLDSamplingData.COLUMN_SAMP_LAT)),
        ChlorineFree(PipedTableRow(4, PipedTableRow.PipedTableRowType.TitleValue, "Chlorine Free", "", EXLDSamplingData.COLUMN_SAMP_CHLOR_FREE)),
        ChlorineTotal(PipedTableRow(5, PipedTableRow.PipedTableRowType.TitleValue, "Chlorine Total", "", EXLDSamplingData.COLUMN_SAMP_CHLOR_TOTAL)),
        Turbidity(PipedTableRow(6, PipedTableRow.PipedTableRowType.TitleValue, "Turbidity", "", EXLDSamplingData.COLUMN_SAMP_TURBIDITY)),
        WaterTemp(PipedTableRow(7, PipedTableRow.PipedTableRowType.TitleValue, "Water Temperature", "", EXLDSamplingData.COLUMN_SAMP_WATER_TEMP)),
        OtherInfo(PipedTableRow(8, PipedTableRow.PipedTableRowType.TitleValue, "Other Info", "", EXLDSamplingData.COLUMN_SAMP_OTHER_INFO)),
        TestStatus(PipedTableRow(9, PipedTableRow.PipedTableRowType.TestStatus, "", "", EXLDSamplingData.COLUMN_SAMP_TEST_STATUS)),
        Photo(PipedTableRow(10, PipedTableRow.PipedTableRowType.MainPicture, "Sample Data Photo", "", EXLDSamplingData.COLUMN_SAMP_PHOTO)),
        Count(PipedTableRow(11, PipedTableRow.PipedTableRowType.Count, ""));

        companion object {
            fun tableRowFromPosition(position: Int): PipedTableRow? {
                when (position)
                {
                    SampleID.value.position -> return SampleID.value
                    SampleDesc.value.position -> return SampleDesc.value
                    Location.value.position -> return Location.value
                    LocationCoords.value.position -> return LocationCoords.value
                    ChlorineFree.value.position -> return ChlorineFree.value
                    ChlorineTotal.value.position -> return ChlorineTotal.value
                    Turbidity.value.position -> return Turbidity.value
                    WaterTemp.value.position -> return WaterTemp.value
                    OtherInfo.value.position -> return OtherInfo.value
                    TestStatus.value.position -> return TestStatus.value
                    Photo.value.position -> return Photo.value
                    Count.value.position -> return Count.value
                }

                return null
            }
        }
    }

    enum class SamplingRows(val value: PipedTableRow)
    {
        FlowratesHeader(PipedTableRow(0, PipedTableRow.PipedTableRowType.PauseSectionHeader, "SAMLPLING DATA")),
        SamplingDetailsHeader(PipedTableRow(1, PipedTableRow.PipedTableRowType.SectionHeader, "SAMPLING DETAILS")),
        TakenTo(PipedTableRow(2, PipedTableRow.PipedTableRowType.DateSetLocation, "Taken To", "", EXLDProcess.c_pt_sampl_taken_to_address)),
        GivenTo(PipedTableRow(3, PipedTableRow.PipedTableRowType.TitleValue, "Given To", "", EXLDProcess.c_pt_sampl_given_to)),
        GivenToDate(PipedTableRow(4, PipedTableRow.PipedTableRowType.DateSet, "Sample Given At", "", EXLDProcess.c_pt_sampl_given_time)),
        NotesSectionHeader(PipedTableRow(5, PipedTableRow.PipedTableRowType.SectionHeader, "NOTES")),
        Notes(PipedTableRow(6, PipedTableRow.PipedTableRowType.Notes, "", "", EXLDProcess.c_pt_sampl_notes)),
        FooterSection(PipedTableRow(7, PipedTableRow.PipedTableRowType.SectionHeader, "")),
        Count(PipedTableRow(8, PipedTableRow.PipedTableRowType.Count, ""));

        companion object {
            fun tableRowFromPosition(position: Int): PipedTableRow? {
                when (position)
                {
                    FlowratesHeader.value.position -> return FlowratesHeader.value
                    SamplingDetailsHeader.value.position -> return SamplingDetailsHeader.value
                    TakenTo.value.position -> return TakenTo.value
                    GivenTo.value.position -> return GivenTo.value
                    GivenToDate.value.position -> return GivenToDate.value
                    NotesSectionHeader.value.position -> return NotesSectionHeader.value
                    FooterSection.value.position -> return FooterSection.value
                    Notes.value.position -> return Notes.value
                    Count.value.position -> return Count.value
                }

                return null
            }
        }
    }

    enum class ConsumableRows(val value: PipedTableRow)
    {
        Hyp(PipedTableRow(0, PipedTableRow.PipedTableRowType.TitleValue, "Sodium Hypochlorite (Ltrs)", "", EXLDProcess.c_consum_sodium_hypoclorite)),
        Bis(PipedTableRow(1, PipedTableRow.PipedTableRowType.TitleValue, "Sodium Bisulphate (Ltrs)", "", EXLDProcess.c_consum_sodium_bisulphate)),
        SwabsQty(PipedTableRow(2, PipedTableRow.PipedTableRowType.TitleValue, "Swabs Qty", "", EXLDProcess.c_consum_swabs_qty)),
        SwabsSize(PipedTableRow(3, PipedTableRow.PipedTableRowType.TitleValue, "Swabs Size", "", EXLDProcess.c_consum_swabs_size)),
        FlangesQty(PipedTableRow(4, PipedTableRow.PipedTableRowType.TitleValue, "Flanges Qty", "", EXLDProcess.c_consum_flanges_qty)),
        FlangesSize(PipedTableRow(5, PipedTableRow.PipedTableRowType.TitleValue, "Flanges Size", "", EXLDProcess.c_consum_flanges_size)),
        FireHose(PipedTableRow(6, PipedTableRow.PipedTableRowType.TitleValue, "Additional Fire Hose", "", EXLDProcess.c_consum_additional_fire_hose_qty)),
        Notes(PipedTableRow(7, PipedTableRow.PipedTableRowType.Notes, "", "", EXLDProcess.c_consum_notes)),
        Count(PipedTableRow(8, PipedTableRow.PipedTableRowType.Count, ""));

        companion object {
            fun tableRowFromPosition(position: Int): PipedTableRow? {
                when (position)
                {
                    Hyp.value.position -> return  Hyp.value
                    Bis.value.position -> return Bis.value
                    SwabsQty.value.position -> return SwabsQty.value
                    SwabsSize.value.position -> return SwabsSize.value
                    FlangesQty.value.position -> return FlangesQty.value
                    FlangesSize.value.position -> return FlangesSize.value
                    FireHose.value.position -> return FireHose.value
                    Notes.value.position -> return Notes.value
                    Count.value.position -> return Count.value
                }

                return null
            }
        }
    }


    enum class FlowrateChlorRows(val value: PipedTableRow)
    {
        DateTime(PipedTableRow(0, PipedTableRow.PipedTableRowType.TitleValue, "Date/Time", "", EXLDChlorFlowrates.COLUMN_CHLOR_TIMESTAMP)),
        Flowrate(PipedTableRow(1, PipedTableRow.PipedTableRowType.TitleValue, "Flowrate (Ltrs/min)", "", EXLDChlorFlowrates.COLUMN_CHLOR_FLOWRATE)),
        Strength(PipedTableRow(2, PipedTableRow.PipedTableRowType.TitleValue, "Chlorine Strength", "", EXLDChlorFlowrates.COLUMN_CHLOR_STRENGTH)),
        Photo(PipedTableRow(3, PipedTableRow.PipedTableRowType.MainPicture, "Photo", "", EXLDChlorFlowrates.COLUMN_CHLOR_PHOTO)),
        Count(PipedTableRow(4, PipedTableRow.PipedTableRowType.Count, ""));

        companion object {
            fun tableRowFromPosition(position: Int): PipedTableRow? {
                when (position)
                {
                    DateTime.value.position -> return DateTime.value
                    Flowrate.value.position -> return Flowrate.value
                    Strength.value.position -> return Strength.value
                    Photo.value.position -> return  Photo.value
                    Count.value.position -> return Count.value
                }

                return null
            }
        }
    }

    enum class FlowrateDecRows(val value: PipedTableRow)
    {
        DateTime(PipedTableRow(0, PipedTableRow.PipedTableRowType.TitleValue, "Date/Time", "", EXLDDecFlowrates.COLUMN_DEC_TIMESTAMP)),
        Flowrate(PipedTableRow(1, PipedTableRow.PipedTableRowType.TitleValue, "Flowrate (Ltrs/min)", "", EXLDDecFlowrates.COLUMN_DEC_FLOWRATE)),
        Strength(PipedTableRow(2, PipedTableRow.PipedTableRowType.TitleValue, "Chlorine Strength", "", EXLDDecFlowrates.COLUMN_DEC_STRENGTH)),
        Discharge(PipedTableRow(3, PipedTableRow.PipedTableRowType.TitleValue, "Level at Discharge", "", EXLDDecFlowrates.COLUMN_DEC_DISCHARGE)),
        Photo(PipedTableRow(4, PipedTableRow.PipedTableRowType.MainPicture, "Photo", "", EXLDDecFlowrates.COLUMN_DEC_PHOTO)),
        Count(PipedTableRow(5, PipedTableRow.PipedTableRowType.Count, ""));

        companion object {
            fun tableRowFromPosition(position: Int): PipedTableRow? {
                when (position)
                {
                    DateTime.value.position -> return DateTime.value
                    Flowrate.value.position -> return Flowrate.value
                    Strength.value.position -> return Strength.value
                    Photo.value.position -> return  Photo.value
                    Discharge.value.position -> return Discharge.value
                    Count.value.position -> return Count.value
                }

                return null
            }
        }
    }

    enum class ChlorRows(val value: PipedTableRow)
    {
        ChlorDetailsHeader(PipedTableRow(0, PipedTableRow.PipedTableRowType.SectionHeader, "CHLORINATION DETAILS")),
        StartChlorinating(PipedTableRow(1, PipedTableRow.PipedTableRowType.DateSetLocation, "Start Chlorinating", "", EXLDProcess.c_pt_chlor_start_time)),
        FlowratesHeader(PipedTableRow(2, PipedTableRow.PipedTableRowType.PauseSectionHeader, "FLOWRATES")),
        ChlorDetailsContdHeader(PipedTableRow(3, PipedTableRow.PipedTableRowType.SectionHeader, "CHLORINATION DETAILS CONTD")),
        MainChlorinated(PipedTableRow(4, PipedTableRow.PipedTableRowType.DateSet, "Main Chlorinated", "", EXLDProcess.c_pt_chlor_main_chlorinated)),
        ChlorineEndStrength(PipedTableRow(5, PipedTableRow.PipedTableRowType.TitleValue, "Chlorine End Strength", "", EXLDProcess.c_pt_chlor_end_strength)),
        TotalWater(PipedTableRow(6, PipedTableRow.PipedTableRowType.TitleValue, "Total Volume Chlorinated", "", EXLDProcess.c_pt_chlor_volume)),
        PhotoSectionHeader(PipedTableRow(7, PipedTableRow.PipedTableRowType.SectionHeader, "PHOTOS")),
        EndPhoto(PipedTableRow(8, PipedTableRow.PipedTableRowType.MainPicture, "End Photo", "", EXLDProcess.c_pt_chlor_end_photo)),
        NotesSectionHeader(PipedTableRow(9, PipedTableRow.PipedTableRowType.SectionHeader, "NOTES")),
        Notes(PipedTableRow(10, PipedTableRow.PipedTableRowType.Notes, "", "", EXLDProcess.c_pt_chlor_notes)),
        FooterSection(PipedTableRow(11, PipedTableRow.PipedTableRowType.SectionHeader, "")),
        Count(PipedTableRow(12, PipedTableRow.PipedTableRowType.Count, ""));

        companion object {
            fun tableRowFromPosition(position: Int): PipedTableRow? {
                when (position) {
                    ChlorDetailsHeader.value.position -> return ChlorDetailsHeader.value
                    StartChlorinating.value.position -> return StartChlorinating.value
                    FlowratesHeader.value.position -> return FlowratesHeader.value
                    ChlorDetailsContdHeader.value.position -> return ChlorDetailsContdHeader.value
                    MainChlorinated.value.position -> return MainChlorinated.value
                    ChlorineEndStrength.value.position -> return ChlorineEndStrength.value
                    TotalWater.value.position -> return TotalWater.value
                    PhotoSectionHeader.value.position -> return PhotoSectionHeader.value
                    EndPhoto.value.position -> return EndPhoto.value
                    NotesSectionHeader.value.position -> return NotesSectionHeader.value
                    Notes.value.position -> return Notes.value
                    FooterSection.value.position -> return  FooterSection.value
                    Count.value.position -> return Count.value
                }

                return null
            }
        }
    }

    enum class DecRows(val value: PipedTableRow)
    {
        DeChlorDetailsHeader(PipedTableRow(0, PipedTableRow.PipedTableRowType.SectionHeader, "DECHLORINATION DETAILS")),
        StartDeChlorinating(PipedTableRow(1, PipedTableRow.PipedTableRowType.DateSetLocation, "Start DeChlorinating", "", EXLDProcess.c_pt_dec_start)),
        FlowratesHeader(PipedTableRow(2, PipedTableRow.PipedTableRowType.PauseSectionHeader, "FLOWRATES")),
        DeChlorDetailsContdHeader(PipedTableRow(3, PipedTableRow.PipedTableRowType.SectionHeader, "DECHLORINATION DETAILS CONTD")),
        MainDeChlorinated(PipedTableRow(4, PipedTableRow.PipedTableRowType.DateSet, "Main Dechlorinated", "", EXLDProcess.c_pt_dec_dechlorinated)),
        TotalWater(PipedTableRow(5, PipedTableRow.PipedTableRowType.TitleValue, "Total Volume Dechlorinated", "", EXLDProcess.c_pt_dec_volume)),
        NotesSectionHeader(PipedTableRow(6, PipedTableRow.PipedTableRowType.SectionHeader, "NOTES")),
        Notes(PipedTableRow(7, PipedTableRow.PipedTableRowType.Notes, "", "", EXLDProcess.c_pt_dec_notes)),
        FooterSection(PipedTableRow(8, PipedTableRow.PipedTableRowType.SectionHeader, "")),
        Count(PipedTableRow(9, PipedTableRow.PipedTableRowType.Count, ""));

        companion object {
            fun tableRowFromPosition(position: Int): PipedTableRow? {
                when (position)
                {
                    DeChlorDetailsHeader.value.position -> return DeChlorDetailsHeader.value
                    StartDeChlorinating.value.position -> return StartDeChlorinating.value
                    FlowratesHeader.value.position -> return FlowratesHeader.value
                    DeChlorDetailsContdHeader.value.position -> return  DeChlorDetailsContdHeader.value
                    MainDeChlorinated.value.position -> return MainDeChlorinated.value
                    TotalWater.value.position -> return TotalWater.value
                    NotesSectionHeader.value.position -> return NotesSectionHeader.value
                    Notes.value.position -> return Notes.value
                    FooterSection.value.position -> return FooterSection.value
                    Count.value.position -> return Count.value
                }

                return null
            }
        }
    }

    enum class FlushingRows(val value: PipedTableRow)
    {
        FlushingDetailsHeader(PipedTableRow(0, PipedTableRow.PipedTableRowType.SectionHeader, "FLUSHING DETAILS")),
        StartedFlushing(PipedTableRow(1, PipedTableRow.PipedTableRowType.DateSetLocation, "Started Flushing", "", EXLDProcess.c_pt_flush_started)),
        FlowratesHeader(PipedTableRow(2, PipedTableRow.PipedTableRowType.PauseSectionHeader, "FLOWRATES")),
        FlushingDetailsContdHeader(PipedTableRow(3, PipedTableRow.PipedTableRowType.SectionHeader, "FLUSHING DETAILS CONTD")),
        FinishedFlushing(PipedTableRow(4, PipedTableRow.PipedTableRowType.DateSet, "Finished Flushing", "", EXLDProcess.c_pt_flush_completed)),
        TotalFlushingTime(PipedTableRow(5, PipedTableRow.PipedTableRowType.TitleValue, "Total Flushing Time", "", "flushing_time")),
        TotalWater(PipedTableRow(6, PipedTableRow.PipedTableRowType.TitleValue, "Total Water Volume (Ltrs)", "", EXLDProcess.c_pt_flush_total_water)),
        NotesSectionHeader(PipedTableRow(7, PipedTableRow.PipedTableRowType.SectionHeader, "NOTES")),
        Notes(PipedTableRow(8, PipedTableRow.PipedTableRowType.Notes, "", "", EXLDProcess.c_pt_flush_notes)),
        FooterSection(PipedTableRow(9, PipedTableRow.PipedTableRowType.SectionHeader, "")),
        Count(PipedTableRow(10, PipedTableRow.PipedTableRowType.Count, ""));

        companion object {
            fun tableRowFromPosition(position: Int): PipedTableRow?
            {
                when (position)
                {
                    FlushingDetailsHeader.value.position -> return FlushingDetailsHeader.value
                    StartedFlushing.value.position -> return StartedFlushing.value
                    FlowratesHeader.value.position -> return FlowratesHeader.value
                    FlushingDetailsContdHeader.value.position -> return FlushingDetailsContdHeader.value
                    FinishedFlushing.value.position -> return FinishedFlushing.value
                    TotalFlushingTime.value.position -> return TotalFlushingTime.value
                    TotalWater.value.position -> return TotalWater.value
                    NotesSectionHeader.value.position -> return NotesSectionHeader.value
                    Notes.value.position -> return Notes.value
                    FooterSection.value.position -> return FooterSection.value
                    Count.value.position -> return Count.value
                }

                return null
            }
        }
    }

    enum class FlushingRows2(val value: PipedTableRow)
    {
        FlushingDetailsHeader(PipedTableRow(0, PipedTableRow.PipedTableRowType.SectionHeader, "FLUSHING DETAILS")),
        StartedFlushing(PipedTableRow(1, PipedTableRow.PipedTableRowType.DateSetLocation, "Started Flushing", "", EXLDProcess.c_pt_flush_started2)),
        FlowratesHeader(PipedTableRow(2, PipedTableRow.PipedTableRowType.PauseSectionHeader, "FLOWRATES")),
        FlushingDetailsContdHeader(PipedTableRow(3, PipedTableRow.PipedTableRowType.SectionHeader, "FLUSHING DETAILS CONTD")),
        FinishedFlushing(PipedTableRow(4, PipedTableRow.PipedTableRowType.DateSet, "Finished Flushing", "", EXLDProcess.c_pt_flush_completed2)),
        TotalFlushingTime(PipedTableRow(5, PipedTableRow.PipedTableRowType.TitleValue, "Total Flushing Time", "", "flushing_time2")),
        TotalWater(PipedTableRow(6, PipedTableRow.PipedTableRowType.TitleValue, "Total Water Volume (Ltrs)", "", EXLDProcess.c_pt_flush_total_water2)),
        NotesSectionHeader(PipedTableRow(7, PipedTableRow.PipedTableRowType.SectionHeader, "NOTES")),
        Notes(PipedTableRow(8, PipedTableRow.PipedTableRowType.Notes, "", "", EXLDProcess.c_pt_flush_notes2)),
        FooterSection(PipedTableRow(9, PipedTableRow.PipedTableRowType.SectionHeader, "")),
        Count(PipedTableRow(10, PipedTableRow.PipedTableRowType.Count, ""));

        companion object {
            fun tableRowFromPosition(position: Int): PipedTableRow?
            {
                when (position)
                {
                    FlushingDetailsHeader.value.position -> return FlushingDetailsHeader.value
                    StartedFlushing.value.position -> return StartedFlushing.value
                    FlowratesHeader.value.position -> return FlowratesHeader.value
                    FlushingDetailsContdHeader.value.position -> return FlushingDetailsContdHeader.value
                    FinishedFlushing.value.position -> return FinishedFlushing.value
                    TotalFlushingTime.value.position -> return TotalFlushingTime.value
                    TotalWater.value.position -> return TotalWater.value
                    NotesSectionHeader.value.position -> return NotesSectionHeader.value
                    Notes.value.position -> return Notes.value
                    FooterSection.value.position -> return FooterSection.value
                    Count.value.position -> return Count.value
                }

                return null
            }
        }
    }

    enum class FillingRows(val value: PipedTableRow)
    {
        FillingDetailsHeader(PipedTableRow(0, PipedTableRow.PipedTableRowType.SectionHeader, "FILLNG DETAILS")),
        StartFilling(PipedTableRow(1, PipedTableRow.PipedTableRowType.DateSetLocation, "Start Filling", "", EXLDProcess.c_filling_started)),
        FlowratesHeader(PipedTableRow(2, PipedTableRow.PipedTableRowType.PauseSectionHeader, "FLOWRATES")),
        FillingDetailsContdHeader(PipedTableRow(3, PipedTableRow.PipedTableRowType.SectionHeader, "FILLING DETAILS CONTD")),
        MainFull(PipedTableRow(4, PipedTableRow.PipedTableRowType.DateSet, "Main Full", "", EXLDProcess.c_filling_stopped)),
        TotalFillingTime(PipedTableRow(5, PipedTableRow.PipedTableRowType.TitleValue, "Total Filling Time", "", "filling_time")),
        TotalWater(PipedTableRow(6, PipedTableRow.PipedTableRowType.TitleValue, "Total Water Volume (Ltrs)", "", EXLDProcess.c_filling_total_water_volume)),
        NotesSectionHeader(PipedTableRow(7, PipedTableRow.PipedTableRowType.SectionHeader, "NOTES")),
        Notes(PipedTableRow(8, PipedTableRow.PipedTableRowType.Notes, "", "", EXLDProcess.c_filling_notes)),
        FooterSection(PipedTableRow(9, PipedTableRow.PipedTableRowType.SectionHeader, "")),
        Count(PipedTableRow(10, PipedTableRow.PipedTableRowType.Count, ""));

        companion object {
            fun tableRowFromPosition(position: Int): PipedTableRow?
            {
                when (position)
                {
                    FillingDetailsHeader.value.position -> return FillingDetailsHeader.value
                    StartFilling.value.position -> return StartFilling.value
                    FlowratesHeader.value.position -> return FlowratesHeader.value
                    FillingDetailsContdHeader.value.position -> return FillingDetailsContdHeader.value
                    MainFull.value.position -> return MainFull.value
                    TotalFillingTime.value.position -> return TotalFillingTime.value
                    TotalWater.value.position -> return TotalWater.value
                    NotesSectionHeader.value.position -> return NotesSectionHeader.value
                    Notes.value.position -> return Notes.value
                    FooterSection.value.position -> return FooterSection.value
                    Count.value.position -> return Count.value
                }

                return null
            }
        }
    }

    enum class SwabbingRows(val value: PipedTableRow)
    {
        SwabbingDetailsHeader(PipedTableRow(0, PipedTableRow.PipedTableRowType.SectionHeader, "SWABBING DETAILS")),
        SwabLoaded(PipedTableRow(1, PipedTableRow.PipedTableRowType.DateSetLocation, "Swab Loaded", "", EXLDProcess.c_swab_loaded)),
        StartedSwabRun(PipedTableRow(2, PipedTableRow.PipedTableRowType.DateSet, "Started Swab Run", "", EXLDProcess.c_swab_run_started)),
        FlowratesHeader(PipedTableRow(3, PipedTableRow.PipedTableRowType.PauseSectionHeader, "FLOWRATES")),
        SwabbingDetailsContdHeader(PipedTableRow(4, PipedTableRow.PipedTableRowType.SectionHeader, "SWABBING DETAILS CONTD")),
        SwabHome(PipedTableRow(5, PipedTableRow.PipedTableRowType.DateSet, "Swab Home", "", EXLDProcess.c_swab_home)),
        SwabRemoved(PipedTableRow(6, PipedTableRow.PipedTableRowType.DateSetLocation, "Swab Removed", "", EXLDProcess.c_swab_removed)),
        TotalWater(PipedTableRow(7, PipedTableRow.PipedTableRowType.TitleValue, "Total Water Volume (Ltrs)", "", EXLDProcess.c_swab_total_water)),
        PhotoSectionHeader(PipedTableRow(8, PipedTableRow.PipedTableRowType.SectionHeader, "PHOTOS")),
        ConditionPhoto(PipedTableRow(9, PipedTableRow.PipedTableRowType.MainPicture, "Condition Photo", "", EXLDProcess.c_swab_photo)),
        NotesSectionHeader(PipedTableRow(10, PipedTableRow.PipedTableRowType.SectionHeader, "NOTES")),
        Notes(PipedTableRow(11, PipedTableRow.PipedTableRowType.Notes, "", "", EXLDProcess.c_swab_notes)),
        FooterSection(PipedTableRow(12, PipedTableRow.PipedTableRowType.SectionHeader, "")),
        Count(PipedTableRow(13, PipedTableRow.PipedTableRowType.Count, ""));

        companion object {
            fun tableRowFromPosition(position: Int): PipedTableRow?
            {
                when (position)
                {
                    SwabbingDetailsHeader.value.position -> return SwabbingDetailsHeader.value
                    SwabLoaded.value.position -> return SwabLoaded.value
                    StartedSwabRun.value.position -> return StartedSwabRun.value
                    FlowratesHeader.value.position -> return FlowratesHeader.value
                    SwabHome.value.position -> return SwabHome.value
                    SwabbingDetailsContdHeader.value.position -> return SwabbingDetailsContdHeader.value
                    SwabRemoved.value.position -> return SwabRemoved.value
                    TotalWater.value.position -> return TotalWater.value
                    ConditionPhoto.value.position -> return ConditionPhoto.value
                    Count.value.position -> return Count.value
                    PhotoSectionHeader.value.position -> return PhotoSectionHeader.value
                    NotesSectionHeader.value.position -> return NotesSectionHeader.value
                    Notes.value.position -> return Notes.value
                    FooterSection.value.position -> return FooterSection.value
                }

                return null
            }
        }
    }

    //endregion

    override fun getItemCount(): Int {

        val p = AppGlobals.instance.activeProcess
        when (pipedTask)
        {
            StandardRecyclerAdapter.PipedTask.Swabbing -> return SwabbingRows.Count.value.position + EXLDSwabFlowrates.getSwabbingFlowrates(ctx, p.columnId).size
            StandardRecyclerAdapter.PipedTask.Filling -> return FillingRows.Count.value.position + EXLDFillingFlowrates.getFillingFlowrates(ctx, p.columnId).size
            StandardRecyclerAdapter.PipedTask.Flushing -> return FlushingRows.Count.value.position + EXLDFlushFlowrates.getFlushFlowrates(ctx, p.columnId, 1).size
            StandardRecyclerAdapter.PipedTask.Flushing2 -> return FlushingRows2.Count.value.position + EXLDFlushFlowrates.getFlushFlowrates(ctx, p.columnId, 2).size
            PipedTask.Chlorination -> return ChlorRows.Count.value.position + EXLDChlorFlowrates.getChlorFlowrates(ctx, p.columnId).size
            PipedTask.DeChlorination -> return DecRows.Count.value.position + EXLDDecFlowrates.getDecFlowrates(ctx, p.columnId).size
            PipedTask.Surveying -> return EXLDSurveyNotes.getSurveyNotes(ctx, p.columnId).size

            PipedTask.ChlorFlowrate -> return FlowrateChlorRows.Count.value.position
            PipedTask.DecFlowrate -> return FlowrateDecRows.Count.value.position
            PipedTask.Consumables -> return ConsumableRows.Count.value.position
            PipedTask.Sampling -> return SamplingRows.Count.value.position + EXLDSamplingData.getSamplingFlowrates(ctx, p.columnId).size
            PipedTask.SamplingFlowrate -> return SamplFlowrateRows.Count.value.position
        }

        return 0
    }

    override fun getItemViewType(position: Int): Int {

        if (isFlowratePosition(position).first)
        {
            return PipedTableRow.PipedTableRowType.Flowrate.value
        }

        if (pipedTask == PipedTask.Surveying)
        {
            return PipedTableRow.PipedTableRowType.DateSetLocation.value
        }

        // Remove the flowrates count from the position if after flowrate position

        val p = AppGlobals.instance.activeProcess
        var tableRow: PipedTableRow? = null
        if (pipedTask == PipedTask.Swabbing)
        {
            val flowrateStartRow = 4
            val frCount = EXLDSwabFlowrates.getSwabbingFlowrates(ctx, p.columnId).size
            var workingPosition = position
            if (position >= (frCount + flowrateStartRow))
            {
                workingPosition = workingPosition - frCount
            }

            tableRow = SwabbingRows.tableRowFromPosition(workingPosition)!!
        }

        if (pipedTask == PipedTask.Filling)
        {
            val flowrateStartRow = 3
            val frCount = EXLDFillingFlowrates.getFillingFlowrates(ctx, p.columnId).size
            var workingPosition = position
            if (position >= (frCount + flowrateStartRow))
            {
                workingPosition = workingPosition - frCount
            }

            tableRow = FillingRows.tableRowFromPosition(workingPosition)!!
        }

        if (pipedTask == PipedTask.Flushing)
        {
            val flowrateStartRow = 3
            val frCount = EXLDFlushFlowrates.getFlushFlowrates(ctx, p.columnId, 1).size
            var workingPosition = position
            if (position >= (frCount + flowrateStartRow))
            {
                workingPosition = workingPosition - frCount
            }

            tableRow = FlushingRows.tableRowFromPosition(workingPosition)!!
        }

        if (pipedTask == PipedTask.Flushing2)
        {
            val flowrateStartRow = 3
            val frCount = EXLDFlushFlowrates.getFlushFlowrates(ctx, p.columnId, 2).size
            var workingPosition = position
            if (position >= (frCount + flowrateStartRow))
            {
                workingPosition = workingPosition - frCount
            }

            tableRow = FlushingRows2.tableRowFromPosition(workingPosition)!!
        }

        if (pipedTask == PipedTask.Chlorination)
        {
            val flowrateStartRow = 3
            val frCount = EXLDChlorFlowrates.getChlorFlowrates(ctx, p.columnId).size
            var workingPosition = position
            if (position >= (frCount + flowrateStartRow))
            {
                workingPosition = workingPosition - frCount
            }

            tableRow = ChlorRows.tableRowFromPosition(workingPosition)!!
        }

        if (pipedTask == PipedTask.DeChlorination)
        {
            val flowrateStartRow = 3
            val frCount = EXLDDecFlowrates.getDecFlowrates(ctx, p.columnId).size
            var workingPosition = position
            if (position >= (frCount + flowrateStartRow))
            {
                workingPosition = workingPosition - frCount
            }

            tableRow = DecRows.tableRowFromPosition(workingPosition)!!
        }

        if (pipedTask == PipedTask.Sampling)
        {
            val flowrateStartRow = 1
            val frCount = EXLDSamplingData.getSamplingFlowrates(ctx, p.columnId).size
            var workingPosition = position
            if (position >= (frCount + flowrateStartRow))
            {
                workingPosition = workingPosition - frCount
            }

            tableRow = SamplingRows.tableRowFromPosition(workingPosition)!!
        }

        if (pipedTask == PipedTask.ChlorFlowrate)
        {
            tableRow = FlowrateChlorRows.tableRowFromPosition(position)!!
        }

        if (pipedTask == PipedTask.DecFlowrate)
        {
            tableRow = FlowrateDecRows.tableRowFromPosition(position)!!
        }

        if (pipedTask == PipedTask.Consumables)
        {
            tableRow = ConsumableRows.tableRowFromPosition(position)!!
        }

        if (pipedTask == PipedTask.SamplingFlowrate)
        {
            tableRow = SamplFlowrateRows.tableRowFromPosition(position)!!
        }


        // . . . for each PipedTask

        if (tableRow != null)
        {
            return tableRow!!.rowType.value
        }
        else
        {
            return  PipedTableRow.PipedTableRowType.Unknown.value
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder? {
        when (viewType)
        {
            PipedTableRow.PipedTableRowType.SectionHeader.value -> {
                val view = LayoutInflater.from(parent?.context).inflate(R.layout.view_holder_standard_header, parent, false)
                return ViewHolderStandardHeader(view)
            }

            PipedTableRow.PipedTableRowType.DateSet.value -> {
                val view = LayoutInflater.from(parent?.context).inflate(R.layout.view_holder_date_set, parent, false)
                return ViewHolderDateSet(view)
            }

            PipedTableRow.PipedTableRowType.DateSetLocation.value -> {
                val view = LayoutInflater.from(parent?.context).inflate(R.layout.view_holder_date_set, parent, false)
                return ViewHolderDateSet(view)
            }

            PipedTableRow.PipedTableRowType.TestStatus.value -> {
                val view = LayoutInflater.from(parent?.context).inflate(R.layout.view_holder_test_status, parent, false)
                return ViewHolderTestStatus(view)
            }

            PipedTableRow.PipedTableRowType.AddFlowrateButton.value -> {

            }

            PipedTableRow.PipedTableRowType.Flowrate.value -> {
                val view = LayoutInflater.from(parent?.context).inflate(R.layout.view_holder_flowrate, parent, false)
                return ViewHolderFlowrate(view)
            }

            PipedTableRow.PipedTableRowType.TitleValue.value -> {
                val view = LayoutInflater.from(parent?.context).inflate(R.layout.view_holder_title_value, parent, false)
                return ViewHolderTitleValue(view)
            }

            PipedTableRow.PipedTableRowType.Notes.value -> {
                val view = LayoutInflater.from(parent?.context).inflate(R.layout.view_holder_one_line_text, parent, false)
                return ViewHolderOneLineText(view)
            }

            PipedTableRow.PipedTableRowType.MainPicture.value -> {
                val view = LayoutInflater.from(parent?.context).inflate(R.layout.view_holder_picture, parent, false)
                return ViewHolderPicture(view)
            }

            PipedTableRow.PipedTableRowType.PauseSectionHeader.value -> {
                val view = LayoutInflater.from(parent?.context).inflate(R.layout.view_holder_pause_section_header, parent, false)
                return ViewHolderPauseSectionHeader(view)
            }

        }

        return null
    }

    fun isFlowratePosition(position: Int): Pair<Boolean, Int>
    {
        val p = AppGlobals.instance.activeProcess
        var flowrateCount = 0
        var flowrateStartRow = 0

        if (pipedTask == PipedTask.Swabbing)
        {
            flowrateCount = EXLDSwabFlowrates.getSwabbingFlowrates(ctx, p.columnId).size
            flowrateStartRow = 4
        }

        if (pipedTask == PipedTask.Filling)
        {
            flowrateCount = EXLDFillingFlowrates.getFillingFlowrates(ctx, p.columnId).size
            flowrateStartRow = 3
        }

        if (pipedTask == PipedTask.Flushing)
        {
            flowrateCount = EXLDFlushFlowrates.getFlushFlowrates(ctx, p.columnId, 1).size
            flowrateStartRow = 3
        }

        if (pipedTask == PipedTask.Flushing2)
        {
            flowrateCount = EXLDFlushFlowrates.getFlushFlowrates(ctx, p.columnId, 2).size
            flowrateStartRow = 3
        }

        if (pipedTask == PipedTask.Chlorination)
        {
            flowrateCount = EXLDChlorFlowrates.getChlorFlowrates(ctx, p.columnId).size
            flowrateStartRow = 3
        }

        if (pipedTask == PipedTask.DeChlorination)
        {
            flowrateCount = EXLDDecFlowrates.getDecFlowrates(ctx, p.columnId).size
            flowrateStartRow = 3
        }

        if (pipedTask == PipedTask.Sampling)
        {
            flowrateCount = EXLDSamplingData.getSamplingFlowrates(ctx, p.columnId).size
            flowrateStartRow = 1
        }

        if (pipedTask == PipedTask.ChlorFlowrate || pipedTask == PipedTask.DecFlowrate || pipedTask == PipedTask.Consumables || pipedTask == PipedTask.SamplingFlowrate)
        {
            return Pair(false, 0)
        }

        if (position >= flowrateStartRow && position < flowrateStartRow + flowrateCount)
        {
            // It's a flowrate
            val flowratePosition = position - flowrateStartRow
            return Pair(true, flowratePosition)
        }
        else
        {
            return Pair(false, 0)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {

        val p = AppGlobals.instance.activeProcess
        var tableRow: PipedTableRow? = null

        /* Exceptions that don't follow the normal rules
           i.e. Surveying which is just one type of row and just a list of notes
         */

        if (pipedTask == PipedTask.Surveying)
        {
            val sn = EXLDSurveyNotes.getSurveyNotes(ctx, p.columnId).get(position)
            val viewHolder = holder as ViewHolderDateSet
            viewHolder.tvLocation?.text = NumbersHelper.latLongString(sn.sn_lat, sn.sn_long)
            viewHolder.tvTitle?.text = DateHelper.dbDateStringFormattedWithSeconds(sn.sn_timestamp)
            viewHolder.tvValue?.text = sn.sn_note
            viewHolder.btnSet?.visibility = View.GONE

            if (sn.sn_photo.length > 2)
            {
                viewHolder.itemView.ivPicture?.visibility = View.VISIBLE
                val imageUri = AppGlobals.uriForSavedImage(sn.sn_photo)
                viewHolder.ivPicture?.setImageURI(imageUri)
            }
            else
            {
                viewHolder.itemView.ivPicture?.visibility = View.GONE
            }

            viewHolder.itemView.setOnClickListener {
                surveyNoteInterface?.didRequestImage(sn)
            }

            return
        }

        /* Flowrate positions, for all tasks */

        if (pipedTask == PipedTask.Swabbing)
        {
            val flowrateStartRow = 4
            val frCount = EXLDSwabFlowrates.getSwabbingFlowrates(ctx, p.columnId).size
            var workingPosition = position
            if (position >= (frCount + flowrateStartRow))
            {
                workingPosition = workingPosition - frCount
            }
            tableRow = SwabbingRows.tableRowFromPosition(workingPosition)
        }

        if (pipedTask == PipedTask.Filling)
        {
            val flowrateStartRow = 3
            val frCount = EXLDFillingFlowrates.getFillingFlowrates(ctx, p.columnId).size
            var workingPosition = position
            if (position >= (frCount + flowrateStartRow))
            {
                workingPosition = workingPosition - frCount
            }
            tableRow = FillingRows.tableRowFromPosition(workingPosition)
        }

        if (pipedTask == PipedTask.Flushing)
        {
            val flowrateStartRow = 3
            val frCount = EXLDFlushFlowrates.getFlushFlowrates(ctx, p.columnId, 1).size
            var workingPosition = position
            if (position >= (frCount + flowrateStartRow))
            {
                workingPosition = workingPosition - frCount
            }
            tableRow = FlushingRows.tableRowFromPosition(workingPosition)
        }

        if (pipedTask == PipedTask.Flushing2)
        {
            val flowrateStartRow = 3
            val frCount = EXLDFlushFlowrates.getFlushFlowrates(ctx, p.columnId, 2).size
            var workingPosition = position
            if (position >= (frCount + flowrateStartRow))
            {
                workingPosition = workingPosition - frCount
            }
            tableRow = FlushingRows2.tableRowFromPosition(workingPosition)
        }

        if (pipedTask == PipedTask.Chlorination)
        {
            val flowrateStartRow = 3
            val frCount = EXLDChlorFlowrates.getChlorFlowrates(ctx, p.columnId).size
            var workingPosition = position
            if (position >= (frCount + flowrateStartRow))
            {
                workingPosition = workingPosition - frCount
            }
            tableRow = ChlorRows.tableRowFromPosition(workingPosition)
        }

        if (pipedTask == PipedTask.DeChlorination)
        {
            val flowrateStartRow = 3
            val frCount = EXLDDecFlowrates.getDecFlowrates(ctx, p.columnId).size
            var workingPosition = position
            if (position >= (frCount + flowrateStartRow))
            {
                workingPosition = workingPosition - frCount
            }
            tableRow = DecRows.tableRowFromPosition(workingPosition)
        }

        if (pipedTask == PipedTask.Sampling)
        {
            val flowrateStartRow = 1
            val frCount = EXLDSamplingData.getSamplingFlowrates(ctx, p.columnId).size
            var workingPosition = position
            if (position >= (frCount + flowrateStartRow))
            {
                workingPosition = workingPosition - frCount
            }
            tableRow = SamplingRows.tableRowFromPosition(workingPosition)
        }

        if (pipedTask == PipedTask.ChlorFlowrate)
        {
            tableRow = FlowrateChlorRows.tableRowFromPosition(position)!!
        }

        if (pipedTask == PipedTask.DecFlowrate)
        {
            tableRow = FlowrateDecRows.tableRowFromPosition(position)!!
        }

        if (pipedTask == PipedTask.Consumables)
        {
            tableRow = ConsumableRows.tableRowFromPosition(position)!!
        }

        if (pipedTask == PipedTask.SamplingFlowrate)
        {
            tableRow = SamplFlowrateRows.tableRowFromPosition(position)!!
        }

        // etc . . .

        /* Flowrates */

        if (isFlowratePosition(position).first)
        {
            var dateString = ""
            var value = 0.0
            var ignoreDefaultStyling = false

            // It's a flowrate
            when (pipedTask)
            {
                PipedTask.Swabbing -> {
                    val flowrates = EXLDSwabFlowrates.getSwabbingFlowrates(ctx, p.columnId)
                    val flowrate = flowrates[isFlowratePosition(position).second]
                    dateString = DateHelper.dbDateStringFormattedWithSeconds(flowrate.swab_created)
                    value = flowrate.swab_flowrate
                }

                PipedTask.Filling -> {
                    val flowrates = EXLDFillingFlowrates.getFillingFlowrates(ctx, p.columnId)
                    val flowrate = flowrates[isFlowratePosition(position).second]
                    dateString = DateHelper.dbDateStringFormattedWithSeconds(flowrate.filling_created)
                    value = flowrate.filling_flowrate
                }

                PipedTask.Flushing -> {
                    val flowrates = EXLDFlushFlowrates.getFlushFlowrates(ctx, p.columnId, 1)
                    val flowrate = flowrates[isFlowratePosition(position).second]
                    dateString = DateHelper.dbDateStringFormattedWithSeconds(flowrate.flush_created)
                    value = flowrate.flush_flowrate
                }

                PipedTask.Flushing2 -> {
                    val flowrates = EXLDFlushFlowrates.getFlushFlowrates(ctx, p.columnId, 2)
                    val flowrate = flowrates[isFlowratePosition(position).second]
                    dateString = DateHelper.dbDateStringFormattedWithSeconds(flowrate.flush_created)
                    value = flowrate.flush_flowrate
                }

                PipedTask.Chlorination -> {
                    val flowrates = EXLDChlorFlowrates.getChlorFlowrates(ctx, p.columnId)
                    val flowrate = flowrates[isFlowratePosition(position).second]
                    dateString = DateHelper.dbDateStringFormattedWithSeconds(flowrate.chlor_timestamp)
                    val strength = flowrate.chlor_strength.formatForDecPlaces(2)
                    val valueText = "Flow: ${flowrate.chlor_flowrate}, Strength: $strength"

                    val viewHolder = holder as ViewHolderFlowrate
                    viewHolder.titleText?.text = dateString
                    viewHolder.valueText?.text = valueText
                    viewHolder.itemView.setOnClickListener {
                        delegate?.didRequestFlowrate(isFlowratePosition(position).second)
                    }

                    ignoreDefaultStyling = true
                }

                PipedTask.DeChlorination -> {
                    val flowrates = EXLDDecFlowrates.getDecFlowrates(ctx, p.columnId)
                    val flowrate = flowrates[isFlowratePosition(position).second]
                    dateString = DateHelper.dbDateStringFormattedWithSeconds(flowrate.dec_timestamp)
                    val strength = flowrate.dec_strength.formatForDecPlaces(2)
                    val discharge = flowrate.dec_discharge.formatForDecPlaces(2)
                    val valueText = "Flow: ${flowrate.dec_flowrate}, Strength: $strength, Discharge: $discharge"

                    val viewHolder = holder as ViewHolderFlowrate
                    viewHolder.titleText?.text = dateString
                    viewHolder.valueText?.text = valueText
                    viewHolder.itemView.setOnClickListener {
                        delegate?.didRequestFlowrate(isFlowratePosition(position).second)
                    }
                    ignoreDefaultStyling = true
                }

                PipedTask.Sampling -> {
                    val flowrates = EXLDSamplingData.getSamplingFlowrates(ctx, p.columnId)
                    val flowrate = flowrates[isFlowratePosition(position).second]
                    dateString = DateHelper.dbDateStringFormattedWithSeconds(flowrate.sampl_timestamp)

                    val viewHolder = holder as ViewHolderFlowrate
                    viewHolder.titleText?.text = dateString
                    viewHolder.valueText?.text = flowrate.sampl_sample_id
                    viewHolder.itemView.setOnClickListener {
                        delegate?.didRequestFlowrate(isFlowratePosition(position).second)
                    }
                    ignoreDefaultStyling = true
                }
            }

            if (!ignoreDefaultStyling) {
                val viewHolder = holder as ViewHolderFlowrate
                viewHolder.titleText?.text = dateString
                viewHolder.valueText?.text = value.toString()
            }

            return
        }

        /* Row Types */

        if (tableRow != null)
        {
            when (tableRow.rowType)
            {
                /* Section Headers */

                PipedTableRow.PipedTableRowType.SectionHeader -> {
                    val viewHolder = holder as ViewHolderStandardHeader
                    viewHolder.headerText?.text = tableRow.title
                }

                /* Notes */

                PipedTableRow.PipedTableRowType.Notes -> {
                    val viewHolder = holder as ViewHolderOneLineText
                    when (tableRow.field)
                    {
                        EXLDProcess.c_swab_notes -> viewHolder.mainText?.text = p.swab_notes
                        EXLDProcess.c_filling_notes -> viewHolder.mainText?.text = p.filling_notes
                        EXLDProcess.c_pt_flush_notes -> viewHolder.mainText?.text = p.pt_flush_notes
                        EXLDProcess.c_pt_flush_notes2 -> viewHolder.mainText?.text = p.pt_flush_notes2
                        EXLDProcess.c_pt_chlor_notes -> viewHolder.mainText?.text = p.pt_chlor_notes
                        EXLDProcess.c_pt_dec_notes -> viewHolder.mainText?.text = p.pt_dec_notes
                        EXLDProcess.c_consum_notes -> viewHolder.mainText?.text = p.consum_notes
                        EXLDProcess.c_pt_sampl_notes -> viewHolder.mainText?.text = p.pt_sampl_notes
                    }

                    if (viewHolder.mainText!!.text!!.length < 1)
                    {
                        viewHolder.mainText?.text = "(none)"
                    }

                    viewHolder.itemView.setOnClickListener {
                        delegate?.didRequestNotes(tableRow.field)
                    }
                }

                /* Picture */

                PipedTableRow.PipedTableRowType.MainPicture -> {
                    val viewHolder = holder as ViewHolderPicture
                    viewHolder.titleText?.text = tableRow.title
                    var ignoreSetViewHolder = false

                    when (tableRow.field)
                    {
                        EXLDProcess.c_swab_photo -> {
                            if (p.swab_photo.length < 2)
                            {
                                // No picture
                                viewHolder.picture?.visibility = View.GONE
                                viewHolder.valueText?.visibility = View.VISIBLE
                            }
                            else
                            {
                                // Have picture
                                viewHolder.valueText?.visibility = View.GONE
                                viewHolder.picture?.visibility = View.VISIBLE

                                val imageUri = AppGlobals.uriForSavedImage(p.swab_photo)
                                viewHolder.picture?.setImageURI(imageUri)
                            }
                        }

                        EXLDProcess.c_pt_chlor_end_photo -> {
                            if (p.pt_chlor_end_photo.length < 2)
                            {
                                // No picture
                                viewHolder.picture?.visibility = View.GONE
                                viewHolder.valueText?.visibility = View.VISIBLE
                            }
                            else
                            {
                                // Have picture
                                viewHolder.valueText?.visibility = View.GONE
                                viewHolder.picture?.visibility = View.VISIBLE

                                val imageUri = AppGlobals.uriForSavedImage(p.pt_chlor_end_photo)
                                viewHolder.picture?.setImageURI(imageUri)
                            }
                        }

                        EXLDChlorFlowrates.COLUMN_CHLOR_PHOTO -> {
                            val fr = AppGlobals.instance.drillChlorFlowrate!!
                            if (fr.chlor_photo.length < 2)
                            {
                                // No picture
                                viewHolder.picture?.visibility = View.GONE
                                viewHolder.valueText?.visibility = View.VISIBLE
                            }
                            else
                            {
                                // Have picture
                                viewHolder.valueText?.visibility = View.GONE
                                viewHolder.picture?.visibility = View.VISIBLE

                                val imageUri = AppGlobals.uriForSavedImage(fr.chlor_photo)
                                viewHolder.picture?.setImageURI(imageUri)
                            }

                            viewHolder.itemView.setOnClickListener {
                                chlorInterface?.didRequestChlorImage(fr)
                            }
                            ignoreSetViewHolder = true
                        }

                        EXLDDecFlowrates.COLUMN_DEC_PHOTO -> {
                            val fr = AppGlobals.instance.drillDecFlowrate!!
                            if (fr.dec_photo.length < 2)
                            {
                                // No picture
                                viewHolder.picture?.visibility = View.GONE
                                viewHolder.valueText?.visibility = View.VISIBLE
                            }
                            else
                            {
                                // Have picture
                                viewHolder.valueText?.visibility = View.GONE
                                viewHolder.picture?.visibility = View.VISIBLE

                                val imageUri = AppGlobals.uriForSavedImage(fr.dec_photo)
                                viewHolder.picture?.setImageURI(imageUri)
                            }

                            viewHolder.itemView.setOnClickListener {
                                decInterface?.didRequestDeChlorImage(fr)
                            }
                            ignoreSetViewHolder = true
                        }

                        EXLDSamplingData.COLUMN_SAMP_PHOTO -> {
                            val fr = AppGlobals.instance.drillSamplFlorwate!!
                            if (fr.sampl_photo.length < 2)
                            {
                                // No picture
                                viewHolder.picture?.visibility = View.GONE
                                viewHolder.valueText?.visibility = View.VISIBLE
                            }
                            else
                            {
                                // Have picture
                                viewHolder.valueText?.visibility = View.GONE
                                viewHolder.picture?.visibility = View.VISIBLE

                                val imageUri = AppGlobals.uriForSavedImage(fr.sampl_photo)
                                viewHolder.picture?.setImageURI(imageUri)
                            }

                            viewHolder.itemView.setOnClickListener {
                                sampInterface?.didRequestSampleImage(fr)
                            }
                            ignoreSetViewHolder = true
                        }
                    }

                    if (!ignoreSetViewHolder) {
                        viewHolder.itemView.setOnClickListener {
                            delegate?.didRequestMainImage(tableRow.field)
                        }
                    }

                }

                /* Title Value */
                PipedTableRow.PipedTableRowType.TitleValue -> {
                    val viewHolder = holder as ViewHolderTitleValue
                    viewHolder.titleText?.text = tableRow.title

                    when (tableRow.field)
                    {
                        /* Sample Flowrate Details */

                        EXLDSamplingData.COLUMN_SAMP_SAMPLE_ID -> {
                            viewHolder.valueText?.text = AppGlobals.instance.drillSamplFlorwate!!.sampl_sample_id

                            if (AppGlobals.instance.drillSamplFlorwate!!.sampl_sample_id.length < 1)
                            {
                                viewHolder.valueText?.text = "(none)"
                            }

                            viewHolder.itemView.setOnClickListener {
                                val alert = AlertHelper(ctx)
                                alert.dialogForTextInput("Sample ID", AppGlobals.instance.drillSamplFlorwate!!.sampl_sample_id, {
                                    AppGlobals.instance.drillSamplFlorwate!!.sampl_sample_id = it
                                    AppGlobals.instance.drillSamplFlorwate!!.save(ctx)
                                    notifyDataSetChanged()
                                })
                            }
                        }

                        EXLDSamplingData.COLUMN_SAMP_DESC -> {
                            viewHolder.valueText?.text = AppGlobals.instance.drillSamplFlorwate!!.sampl_desc

                            if (AppGlobals.instance.drillSamplFlorwate!!.sampl_desc.length < 1)
                            {
                                viewHolder.valueText?.text = "(none)"
                            }

                            viewHolder.itemView.setOnClickListener {
                                val alert = AlertHelper(ctx)
                                alert.dialogForTextInput("Sample Description", AppGlobals.instance.drillSamplFlorwate!!.sampl_desc, {
                                    AppGlobals.instance.drillSamplFlorwate!!.sampl_desc = it
                                    AppGlobals.instance.drillSamplFlorwate!!.save(ctx)
                                    notifyDataSetChanged()
                                })
                            }
                        }

                        EXLDSamplingData.COLUMN_SAMP_LOCATION -> {
                            viewHolder.valueText?.text = AppGlobals.instance.drillSamplFlorwate!!.sampl_location

                            if (AppGlobals.instance.drillSamplFlorwate!!.sampl_location.length < 1)
                            {
                                viewHolder.valueText?.text = "(none)"
                            }

                            viewHolder.itemView.setOnClickListener {
                                val alert = AlertHelper(ctx)
                                alert.dialogForTextInput("Location", AppGlobals.instance.drillSamplFlorwate!!.sampl_location, {
                                    AppGlobals.instance.drillSamplFlorwate!!.sampl_location = it
                                    AppGlobals.instance.drillSamplFlorwate!!.save(ctx)
                                    notifyDataSetChanged()
                                })
                            }
                        }

                        EXLDSamplingData.COLUMN_SAMP_CHLOR_FREE -> {
                            viewHolder.valueText?.text = AppGlobals.instance.drillSamplFlorwate!!.sampl_chlor_free.formatForDecPlaces(2)

                            viewHolder.itemView.setOnClickListener {
                                val alert = AlertHelper(ctx)
                                alert.dialogForTextInput("Chlorine Free (ppm)", AppGlobals.instance.drillSamplFlorwate!!.sampl_chlor_free.formatForDecPlaces(2), {

                                    val theValue = it.toDoubleOrNull()
                                    if (theValue != null)
                                    {
                                        AppGlobals.instance.drillSamplFlorwate!!.sampl_chlor_free = theValue
                                        AppGlobals.instance.drillSamplFlorwate!!.save(ctx)
                                        notifyDataSetChanged()
                                    }

                                })
                            }
                        }

                        EXLDSamplingData.COLUMN_SAMP_CHLOR_TOTAL -> {
                            viewHolder.valueText?.text = AppGlobals.instance.drillSamplFlorwate!!.sampl_chlor_total.formatForDecPlaces(2)

                            viewHolder.itemView.setOnClickListener {
                                val alert = AlertHelper(ctx)
                                alert.dialogForTextInput("Chlorine Total (ppm)", AppGlobals.instance.drillSamplFlorwate!!.sampl_chlor_total.formatForDecPlaces(2), {

                                    val theValue = it.toDoubleOrNull()
                                    if (theValue != null)
                                    {
                                        AppGlobals.instance.drillSamplFlorwate!!.sampl_chlor_total = theValue
                                        AppGlobals.instance.drillSamplFlorwate!!.save(ctx)
                                        notifyDataSetChanged()
                                    }

                                })
                            }
                        }

                        EXLDSamplingData.COLUMN_SAMP_TURBIDITY -> {
                            viewHolder.valueText?.text = AppGlobals.instance.drillSamplFlorwate!!.sampl_turbidity.formatForDecPlaces(2)

                            viewHolder.itemView.setOnClickListener {
                                val alert = AlertHelper(ctx)
                                alert.dialogForTextInput("Turbidity (NTU)", AppGlobals.instance.drillSamplFlorwate!!.sampl_turbidity.formatForDecPlaces(2), {

                                    val theValue = it.toDoubleOrNull()
                                    if (theValue != null)
                                    {
                                        AppGlobals.instance.drillSamplFlorwate!!.sampl_turbidity = theValue
                                        AppGlobals.instance.drillSamplFlorwate!!.save(ctx)
                                        notifyDataSetChanged()
                                    }

                                })
                            }
                        }

                        EXLDSamplingData.COLUMN_SAMP_WATER_TEMP -> {
                            viewHolder.valueText?.text = AppGlobals.instance.drillSamplFlorwate!!.sampl_water_temp.toString()

                            viewHolder.itemView.setOnClickListener {
                                val alert = AlertHelper(ctx)
                                alert.dialogForTextInput("Water Temperature", AppGlobals.instance.drillSamplFlorwate!!.sampl_water_temp.toString(), {

                                    val theValue = it.toIntOrNull()
                                    if (theValue != null)
                                    {
                                        AppGlobals.instance.drillSamplFlorwate!!.sampl_water_temp = theValue
                                        AppGlobals.instance.drillSamplFlorwate!!.save(ctx)
                                        notifyDataSetChanged()
                                    }
                                })
                            }
                        }

                        EXLDSamplingData.COLUMN_SAMP_OTHER_INFO -> {
                            viewHolder.valueText?.text = AppGlobals.instance.drillSamplFlorwate!!.sampl_other_info

                            if (AppGlobals.instance.drillSamplFlorwate!!.sampl_other_info.length < 1)
                            {
                                viewHolder.valueText?.text = "(none)"
                            }

                            viewHolder.itemView.setOnClickListener {
                                val alert = AlertHelper(ctx)
                                alert.dialogForTextInput("Other Info", AppGlobals.instance.drillSamplFlorwate!!.sampl_other_info, {
                                    AppGlobals.instance.drillSamplFlorwate!!.sampl_other_info = it
                                    AppGlobals.instance.drillSamplFlorwate!!.save(ctx)
                                    notifyDataSetChanged()
                                })
                            }
                        }

                        /* Sampling */

                        EXLDProcess.c_pt_sampl_given_to -> {
                            viewHolder.valueText?.text = p.pt_sampl_given_to

                            if (p.pt_sampl_given_to.length < 1)
                            {
                                viewHolder.valueText?.text = "(none)"
                            }

                            viewHolder.itemView.setOnClickListener {
                                val alert = AlertHelper(ctx)
                                alert.dialogForTextInput("Samples Given To", p.pt_sampl_given_to, {
                                    p.pt_sampl_given_to = it
                                    p.save(ctx)
                                    notifyDataSetChanged()
                                })
                            }
                        }


                        /* Consumables */

                        EXLDProcess.c_consum_sodium_hypoclorite -> {
                            viewHolder.valueText?.text = p.consum_sodium_hypoclorite.toString()
                            viewHolder.itemView.setOnClickListener {
                                val alert = AlertHelper(ctx)
                                alert.dialogForTextInput("Sodium Hypoclorite (Ltrs)", p.consum_sodium_hypoclorite.toString(), {
                                    val inputValue = it.toIntOrNull()
                                    if (inputValue != null)
                                    {
                                        p.consum_sodium_hypoclorite = inputValue!!
                                        p.save(ctx)
                                        notifyDataSetChanged()
                                    }
                                })
                            }
                        }

                        EXLDProcess.c_consum_sodium_bisulphate -> {
                            viewHolder.valueText?.text = p.consum_sodium_bisulphate.toString()
                            viewHolder.itemView.setOnClickListener {
                                val alert = AlertHelper(ctx)
                                alert.dialogForTextInput("Sodium Bisulphate (Ltrs)", p.consum_sodium_bisulphate.toString(), {
                                    val inputValue = it.toIntOrNull()
                                    if (inputValue != null)
                                    {
                                        p.consum_sodium_bisulphate = inputValue!!
                                        p.save(ctx)
                                        notifyDataSetChanged()
                                    }
                                })
                            }
                        }

                        EXLDProcess.c_consum_swabs_qty -> {
                            viewHolder.valueText?.text = p.consum_swabs_qty.toString()
                            viewHolder.itemView.setOnClickListener {
                                val alert = AlertHelper(ctx)
                                alert.dialogForTextInput("Swabs Qty", p.consum_swabs_qty.toString(), {
                                    val inputValue = it.toIntOrNull()
                                    if (inputValue != null)
                                    {
                                        p.consum_swabs_qty = inputValue!!
                                        p.save(ctx)
                                        notifyDataSetChanged()
                                    }
                                })
                            }
                        }

                        EXLDProcess.c_consum_swabs_size -> {
                            viewHolder.valueText?.text = p.consum_swabs_size.toString()
                            viewHolder.itemView.setOnClickListener {
                                val alert = AlertHelper(ctx)
                                alert.dialogForTextInput("Swabs Size", p.consum_swabs_size.formatForDecPlaces(2), {
                                    val inputValue = it.toDoubleOrNull()
                                    if (inputValue != null)
                                    {
                                        p.consum_swabs_size = inputValue!!
                                        p.save(ctx)
                                        notifyDataSetChanged()
                                    }
                                })
                            }
                        }

                        EXLDProcess.c_consum_flanges_qty -> {
                            viewHolder.valueText?.text = p.consum_flanges_qty.toString()
                            viewHolder.itemView.setOnClickListener {
                                val alert = AlertHelper(ctx)
                                alert.dialogForTextInput("Flanges Qty", p.consum_flanges_qty.toString(), {
                                    val inputValue = it.toIntOrNull()
                                    if (inputValue != null)
                                    {
                                        p.consum_flanges_qty = inputValue!!
                                        p.save(ctx)
                                        notifyDataSetChanged()
                                    }
                                })
                            }
                        }

                        EXLDProcess.c_consum_flanges_size -> {
                            viewHolder.valueText?.text = p.consum_flanges_size.toString()
                            viewHolder.itemView.setOnClickListener {
                                val alert = AlertHelper(ctx)
                                alert.dialogForTextInput("Flanges Size", p.consum_flanges_size.formatForDecPlaces(2), {
                                    val inputValue = it.toDoubleOrNull()
                                    if (inputValue != null)
                                    {
                                        p.consum_flanges_size = inputValue!!
                                        p.save(ctx)
                                        notifyDataSetChanged()
                                    }
                                })
                            }
                        }

                        EXLDProcess.c_consum_additional_fire_hose_qty -> {
                            viewHolder.valueText?.text = p.consum_additional_fire_hose_qty.toString()
                            viewHolder.itemView.setOnClickListener {
                                val alert = AlertHelper(ctx)
                                alert.dialogForTextInput("Additional Fire Hose", p.consum_additional_fire_hose_qty.toString(), {
                                    val inputValue = it.toIntOrNull()
                                    if (inputValue != null)
                                    {
                                        p.consum_additional_fire_hose_qty = inputValue!!
                                        p.save(ctx)
                                        notifyDataSetChanged()
                                    }
                                })
                            }
                        }


                        /* Chlor Flowrates */

                        EXLDChlorFlowrates.COLUMN_CHLOR_TIMESTAMP -> {
                            viewHolder.valueText?.text = DateHelper.dbDateStringFormattedWithSeconds(AppGlobals.instance.drillChlorFlowrate!!.chlor_timestamp)
                        }

                        EXLDChlorFlowrates.COLUMN_CHLOR_FLOWRATE -> {
                            viewHolder.valueText?.text = AppGlobals.instance.drillChlorFlowrate!!.chlor_flowrate.formatForDecPlaces(2)
                            viewHolder.itemView.setOnClickListener {
                                val alert = AlertHelper(ctx)
                                alert.dialogForTextInput("Flowrate", AppGlobals.instance.drillChlorFlowrate!!.chlor_flowrate.formatForDecPlaces(2), {
                                    val inputValue = it.toDoubleOrNull()
                                    if (inputValue != null)
                                    {
                                        AppGlobals.instance.drillChlorFlowrate!!.chlor_flowrate = inputValue!!
                                        AppGlobals.instance.drillChlorFlowrate!!.save(ctx)
                                        notifyDataSetChanged()
                                    }
                                })
                            }
                        }

                        EXLDChlorFlowrates.COLUMN_CHLOR_STRENGTH -> {
                            viewHolder.valueText?.text = AppGlobals.instance.drillChlorFlowrate!!.chlor_strength.formatForDecPlaces(2)
                            viewHolder.itemView.setOnClickListener {
                                val alert = AlertHelper(ctx)
                                alert.dialogForTextInput("Chlorine End Strength", AppGlobals.instance.drillChlorFlowrate!!.chlor_strength.formatForDecPlaces(2), {
                                    val inputValue = it.toDoubleOrNull()
                                    if (inputValue != null)
                                    {
                                        AppGlobals.instance.drillChlorFlowrate!!.chlor_strength = inputValue!!
                                        AppGlobals.instance.drillChlorFlowrate!!.save(ctx)
                                        notifyDataSetChanged()
                                    }
                                })
                            }
                        }

                        /* Dec Flowrates */

                        EXLDDecFlowrates.COLUMN_DEC_TIMESTAMP -> {
                            viewHolder.valueText?.text = DateHelper.dbDateStringFormattedWithSeconds(AppGlobals.instance.drillDecFlowrate!!.dec_timestamp)
                        }

                        EXLDDecFlowrates.COLUMN_DEC_FLOWRATE -> {
                            viewHolder.valueText?.text = AppGlobals.instance.drillDecFlowrate!!.dec_flowrate.formatForDecPlaces(2)
                            viewHolder.itemView.setOnClickListener {
                                val alert = AlertHelper(ctx)
                                alert.dialogForTextInput("Flowrate", AppGlobals.instance.drillDecFlowrate!!.dec_flowrate.formatForDecPlaces(2), {
                                    val inputValue = it.toDoubleOrNull()
                                    if (inputValue != null)
                                    {
                                        AppGlobals.instance.drillDecFlowrate!!.dec_flowrate = inputValue!!
                                        AppGlobals.instance.drillDecFlowrate!!.save(ctx)
                                        notifyDataSetChanged()
                                    }
                                })
                            }
                        }

                        EXLDDecFlowrates.COLUMN_DEC_STRENGTH -> {
                        viewHolder.valueText?.text = AppGlobals.instance.drillDecFlowrate!!.dec_strength.formatForDecPlaces(2)
                        viewHolder.itemView.setOnClickListener {
                            val alert = AlertHelper(ctx)
                            alert.dialogForTextInput("Chlorine Strength", AppGlobals.instance.drillDecFlowrate!!.dec_strength.formatForDecPlaces(2), {
                                val inputValue = it.toDoubleOrNull()
                                if (inputValue != null)
                                {
                                    AppGlobals.instance.drillDecFlowrate!!.dec_strength = inputValue!!
                                    AppGlobals.instance.drillDecFlowrate!!.save(ctx)
                                    notifyDataSetChanged()
                                }
                            })
                        }
                    }

                        EXLDDecFlowrates.COLUMN_DEC_DISCHARGE -> {
                            viewHolder.valueText?.text = AppGlobals.instance.drillDecFlowrate!!.dec_discharge.formatForDecPlaces(2)
                            viewHolder.itemView.setOnClickListener {
                                val alert = AlertHelper(ctx)
                                alert.dialogForTextInput("Level at Discharge", AppGlobals.instance.drillDecFlowrate!!.dec_discharge.formatForDecPlaces(2), {
                                    val inputValue = it.toDoubleOrNull()
                                    if (inputValue != null)
                                    {
                                        AppGlobals.instance.drillDecFlowrate!!.dec_discharge = inputValue!!
                                        AppGlobals.instance.drillDecFlowrate!!.save(ctx)
                                        notifyDataSetChanged()
                                    }
                                })
                            }
                        }

                        /* Total Water */

                        EXLDProcess.c_swab_total_water -> {

                            if (shouldCalculateTotalWater)
                            {
                                val totalWater = EXLDSwabFlowrates.totalWaterVolume(ctx, p.columnId, getPauseTypeString())
                                p.swab_total_water = totalWater
                                p.save(ctx)
                                shouldCalculateTotalWater = false
                            }
                            viewHolder.valueText?.text = p.swab_total_water.formatForDecPlaces(0)
                        }

                        EXLDProcess.c_filling_total_water_volume -> {
                            if (shouldCalculateTotalWater)
                            {
                                val totalWater = EXLDFillingFlowrates.totalWaterVolume(ctx, p.columnId, getPauseTypeString())
                                p.filling_total_water_volume = totalWater
                                p.save(ctx)
                                shouldCalculateTotalWater = false
                            }
                            viewHolder.valueText?.text = p.filling_total_water_volume.formatForDecPlaces(0)
                        }

                        EXLDProcess.c_pt_flush_total_water -> {
                            if (shouldCalculateTotalWater)
                            {
                                val totalWater = EXLDFlushFlowrates.totalWaterVolume(ctx, p.columnId, getPauseTypeString(), 1)
                                p.pt_flush_total_water = totalWater
                                p.save(ctx)
                                shouldCalculateTotalWater = false
                            }
                            viewHolder.valueText?.text = p.pt_flush_total_water.formatForDecPlaces(0)
                        }

                        EXLDProcess.c_pt_flush_total_water2 -> {
                            if (shouldCalculateTotalWater)
                            {
                                val totalWater = EXLDFlushFlowrates.totalWaterVolume(ctx, p.columnId, getPauseTypeString(), 2)
                                p.pt_flush_total_water2 = totalWater
                                p.save(ctx)
                                shouldCalculateTotalWater = false
                            }
                            viewHolder.valueText?.text = p.pt_flush_total_water2.formatForDecPlaces(0)
                        }

                        EXLDProcess.c_pt_chlor_volume -> {
                            if (shouldCalculateTotalWater)
                            {
                                val totalWater = EXLDChlorFlowrates.totalWaterVolume(ctx, p.columnId, getPauseTypeString())
                                p.pt_chlor_volume = totalWater
                                p.save(ctx)
                                shouldCalculateTotalWater = false
                            }
                            viewHolder.valueText?.text = p.pt_chlor_volume.formatForDecPlaces(0)
                        }

                        EXLDProcess.c_pt_dec_volume -> {
                            if (shouldCalculateTotalWater)
                            {
                                val totalWater = EXLDDecFlowrates.totalWaterVolume(ctx, p.columnId, getPauseTypeString())
                                p.pt_dec_volume = totalWater
                                p.save(ctx)
                                shouldCalculateTotalWater = false
                            }
                            viewHolder.valueText?.text = p.pt_dec_volume.formatForDecPlaces(0)
                        }

                        /* Chlorination */

                        EXLDProcess.c_pt_chlor_end_strength -> {
                            viewHolder.valueText?.text = p.pt_chlor_end_strength.formatForDecPlaces(2)
                            viewHolder.itemView.setOnClickListener {
                                val alert = AlertHelper(ctx)
                                alert.dialogForTextInput("Chlorine End Strength", p.pt_chlor_end_strength.formatForDecPlaces(2), {
                                    val inputValue = it.toDoubleOrNull()
                                    if (inputValue != null)
                                    {
                                        p.pt_chlor_end_strength = inputValue!!
                                        p.save(ctx)
                                        notifyDataSetChanged()
                                    }
                                })
                            }
                        }


                        /* Filling Times */

                        "filling_time" -> {
                            val fillingStarted = DateHelper.dbStringToDateOrNull(p.filling_started)
                            val fillingStopped = DateHelper.dbStringToDateOrNull(p.filling_stopped)

                            if (fillingStarted != null && fillingStopped != null)
                            {
                                val totalMillis = fillingStopped!!.time - fillingStarted!!.time
                                viewHolder.valueText?.text = DateHelper.timeDifferenceFormattedForCountdown(totalMillis)
                            }
                            else
                            {
                                viewHolder.valueText?.text = DateHelper.timeDifferenceFormattedForCountdown(0)
                            }
                        }

                        "flushing_time" -> {
                            val fillingStarted = DateHelper.dbStringToDateOrNull(p.pt_flush_started)
                            val fillingStopped = DateHelper.dbStringToDateOrNull(p.pt_flush_completed)

                            if (fillingStarted != null && fillingStopped != null)
                            {
                                val totalMillis = fillingStopped!!.time - fillingStarted!!.time
                                viewHolder.valueText?.text = DateHelper.timeDifferenceFormattedForCountdown(totalMillis)
                            }
                            else
                            {
                                viewHolder.valueText?.text = DateHelper.timeDifferenceFormattedForCountdown(0)
                            }
                        }

                        "flushing_time2" -> {
                            val fillingStarted = DateHelper.dbStringToDateOrNull(p.pt_flush_started2)
                            val fillingStopped = DateHelper.dbStringToDateOrNull(p.pt_flush_completed2)

                            if (fillingStarted != null && fillingStopped != null)
                            {
                                val totalMillis = fillingStopped!!.time - fillingStarted!!.time
                                viewHolder.valueText?.text = DateHelper.timeDifferenceFormattedForCountdown(totalMillis)
                            }
                            else
                            {
                                viewHolder.valueText?.text = DateHelper.timeDifferenceFormattedForCountdown(0)
                            }
                        }
                    }
                }

                /* Pause Headers */

                PipedTableRow.PipedTableRowType.PauseSectionHeader -> {
                    val viewHolder = holder as ViewHolderPauseSectionHeader

                    var isRunning = false
                    if (pipedTask == PipedTask.Swabbing)
                    {
                        isRunning = operationIsInProgress(p.swab_run_started, p.swab_home)
                    }
                    if (pipedTask == PipedTask.Filling)
                    {
                        isRunning = operationIsInProgress(p.filling_started, p.filling_stopped)
                    }
                    if (pipedTask == PipedTask.Flushing)
                    {
                        isRunning = operationIsInProgress(p.pt_flush_started, p.pt_flush_completed)
                    }
                    if (pipedTask == PipedTask.Flushing2)
                    {
                        isRunning = operationIsInProgress(p.pt_flush_started2, p.pt_flush_completed2)
                    }

                    if (pipedTask == PipedTask.Chlorination)
                    {
                        isRunning = operationIsInProgress(p.pt_chlor_start_time, p.pt_chlor_main_chlorinated)
                    }

                    if (pipedTask == PipedTask.DeChlorination)
                    {
                        isRunning = operationIsInProgress(p.pt_dec_start, p.pt_dec_dechlorinated)
                    }

                    if (pipedTask == PipedTask.Sampling)
                    {
                        viewHolder.btnPause?.visibility = View.GONE
                        viewHolder.btnAddFlowrate?.text = "Add Data"
                        viewHolder.btnAddFlowrate?.setOnClickListener {
                            delegate?.didRequestFlowrate(-1)
                        }
                        return
                    }

                    // Formatting
                    if (taskIsPaused)
                    {
                        viewHolder.btnPause?.text = "Restart"
                        viewHolder.btnPause?.setOnClickListener {
                            restartButtonPressed()
                        }

                        viewHolder.btnAddFlowrate?.visibility = View.GONE
                    }
                    else
                    {

                        viewHolder.btnAddFlowrate?.visibility = View.VISIBLE
                        viewHolder.btnAddFlowrate?.setOnClickListener {
                            addFlowratePressed()
                        }

                        if (isRunning)
                        {
                            viewHolder.btnPause?.visibility = View.VISIBLE
                            viewHolder.btnPause?.text = "Pause"
                            viewHolder.btnPause?.setOnClickListener {
                                pauseButtonPressed()
                            }
                        }
                        else
                        {
                            viewHolder.btnPause?.visibility = View.GONE
                        }
                    }
                }

                /* Date Set Location */

                PipedTableRow.PipedTableRowType.DateSetLocation -> {
                    val viewHolder = holder as ViewHolderDateSet
                    viewHolder.tvTitle?.text = tableRow.title
                    viewHolder.tvLocation?.visibility = View.VISIBLE

                    when (tableRow.field)
                    {
                        EXLDProcess.c_swab_loaded -> {
                            formatSwabLoaded(viewHolder)
                        }

                        EXLDProcess.c_swab_removed -> {
                            formatSwabRemoved(viewHolder)
                        }

                        EXLDProcess.c_filling_started -> {
                            formatFillingStarted(viewHolder)
                        }

                        EXLDProcess.c_pt_flush_started -> {
                            formatFlushingStarted(viewHolder)
                        }

                        EXLDProcess.c_pt_flush_started2 -> {
                            formatFlushingStarted2(viewHolder)
                        }

                        EXLDProcess.c_pt_chlor_start_time -> {
                            formatStartChlorinating(viewHolder)
                        }

                        EXLDProcess.c_pt_dec_start -> {
                            formatStartDechlorinating(viewHolder)
                        }

                        EXLDProcess.c_pt_sampl_taken_to_address -> {
                            formatSamplingTakenTo(viewHolder)
                        }

                    }
                }

                /* Date Set */

                PipedTableRow.PipedTableRowType.DateSet -> {
                    val viewHolder = holder as ViewHolderDateSet
                    viewHolder.tvTitle?.text = tableRow.title
                    viewHolder.tvLocation?.visibility = View.GONE

                    var theDate = ""

                    when (tableRow.field)
                    {
                        EXLDProcess.c_swab_run_started -> theDate = p.swab_run_started
                        EXLDProcess.c_swab_home -> {
                            theDate = p.swab_home
                            closePauseSessions()
                        }
                        EXLDProcess.c_filling_stopped -> {
                            theDate = p.filling_stopped
                            closePauseSessions()
                        }
                        EXLDProcess.c_pt_flush_completed -> {
                            theDate = p.pt_flush_completed
                            closePauseSessions()
                        }
                        EXLDProcess.c_pt_flush_completed2 -> {
                            theDate = p.pt_flush_completed2
                            closePauseSessions()
                        }
                        EXLDProcess.c_pt_chlor_main_chlorinated -> {
                            theDate = p.pt_chlor_main_chlorinated
                            closePauseSessions()
                        }
                        EXLDProcess.c_pt_dec_dechlorinated -> {
                            theDate = p.pt_dec_dechlorinated
                            closePauseSessions()
                        }
                        EXLDProcess.c_pt_sampl_given_time -> {
                            theDate = p.pt_sampl_given_time
                            viewHolder.tvValue?.text = DateHelper.dbDateStringFormattedWithSeconds(theDate)
                            viewHolder.btnSet?.text = "Set"
                            viewHolder.btnSet?.setOnClickListener {
                                p.pt_sampl_given_time = DateHelper.dateToDBString(Date())
                                p.save(ctx)
                                notifyDataSetChanged()
                            }

                            return
                        }
                        EXLDSamplingData.COLUMN_SAMP_LAT -> {
                            viewHolder.tvValue?.text = NumbersHelper.latLongString(AppGlobals.instance.drillSamplFlorwate!!.sampl_lat, AppGlobals.instance.drillSamplFlorwate!!.sampl_long)
                            viewHolder.btnSet?.text = "Set"
                            viewHolder.btnSet?.setOnClickListener {
                                AppGlobals.instance.drillSamplFlorwate!!.sampl_lat = AppGlobals.instance.lastLat
                                AppGlobals.instance.drillSamplFlorwate!!.sampl_long = AppGlobals.instance.lastLng
                                AppGlobals.instance.drillSamplFlorwate!!.save(ctx)
                                notifyDataSetChanged()
                            }

                            return
                        }
                    }

                    if (theDate.length > 1)
                    {
                        viewHolder.tvValue?.text = DateHelper.dbDateStringFormattedWithSeconds(theDate)
                        viewHolder.btnSet?.text = "Reset"
                    }
                    else
                    {
                        viewHolder.tvValue?.text = "(none)"
                        viewHolder.btnSet?.text = "Set"
                    }

                    viewHolder.btnSet?.setOnClickListener {
                        val now = DateHelper.dateToDBString(Date())
                        when (tableRow.field)
                        {
                            EXLDProcess.c_swab_run_started -> p.swab_run_started = now
                            EXLDProcess.c_swab_home -> p.swab_home = now
                            EXLDProcess.c_filling_stopped -> p.filling_stopped = now
                            EXLDProcess.c_pt_flush_completed -> p.pt_flush_completed = now
                            EXLDProcess.c_pt_flush_completed2 -> p.pt_flush_completed2 = now
                            EXLDProcess.c_pt_chlor_main_chlorinated -> p.pt_chlor_main_chlorinated = now
                            EXLDProcess.c_pt_dec_dechlorinated -> p.pt_dec_dechlorinated = now
                        }

                        p.save(ctx)
                        notifyDataSetChanged()
                    }
                }

                PipedTableRow.PipedTableRowType.TestStatus -> {
                    // Only relevant for sampling flowrates
                    val fr = AppGlobals.instance.drillSamplFlorwate!!
                    val TEST_STATUS_NOT_SET = 0
                    val TEST_STAUTS_PASS = 1
                    val TEST_STATUS_FAIL = 2
                    val viewHolder = holder as ViewHolderTestStatus

                    viewHolder.rdFail?.isChecked = false
                    viewHolder.rdNotSet?.isChecked = true
                    viewHolder.rdPass?.isChecked = false
                    viewHolder.tvFailMessage?.visibility = View.GONE

                    viewHolder.tvFailMessage?.text = fr.sampl_failnotes
                    if (fr.sampl_failnotes.length < 1)
                    {
                        viewHolder.tvFailMessage?.text = "[Tap to enter fail notes]"
                    }

                    viewHolder.tvFailMessage?.setOnClickListener {
                        val alert = AlertHelper(ctx)
                        alert.dialogForTextInput("Fail Notes", fr.sampl_failnotes, {

                            fr.sampl_failnotes = it
                            fr.save(ctx)

                            ctx.runOnUiThread {
                                if (fr.sampl_failnotes.length > 0) {
                                    viewHolder.tvFailMessage?.text = fr.sampl_failnotes
                                }
                                else
                                {
                                    viewHolder.tvFailMessage?.text = "[Tap to enter fail notes]"
                                }
                            }
                        })
                    }

                    if (fr.sampl_test_status == TEST_STATUS_FAIL)
                    {
                        viewHolder.rdFail?.isChecked = true
                        viewHolder.tvFailMessage?.visibility = View.VISIBLE
                    }

                    if (fr.sampl_test_status == TEST_STAUTS_PASS)
                    {
                        viewHolder.rdPass?.isChecked = true
                    }

                    if (fr.sampl_test_status == TEST_STATUS_NOT_SET)
                    {
                        viewHolder.rdNotSet?.isChecked = true
                    }


                    viewHolder.radioGroup?.setOnCheckedChangeListener { group, checkedId ->
                        when (checkedId)
                        {
                            viewHolder.rdPass!!.id -> {
                                fr.sampl_test_status = TEST_STAUTS_PASS
                                ctx.runOnUiThread {
                                    viewHolder.tvFailMessage?.visibility = View.GONE
                                }
                            }

                            viewHolder.rdNotSet!!.id -> {
                                fr.sampl_test_status = TEST_STATUS_NOT_SET
                                ctx.runOnUiThread {
                                    viewHolder.tvFailMessage?.visibility = View.GONE
                                }
                            }

                            viewHolder.rdFail!!.id -> {
                                fr.sampl_test_status = TEST_STATUS_FAIL
                                ctx.runOnUiThread {
                                    viewHolder.tvFailMessage?.visibility = View.VISIBLE
                                }

                            }
                        }

                        fr.save(ctx)
                        //notifyDataSetChanged()
                    }

                }
            }
        }
    }

    fun formatSamplingTakenTo(viewHolder: ViewHolderDateSet)
    {
        val p = AppGlobals.instance.activeProcess
        val lat = p.pt_sampl_taken_to_lat
        val lng = p.pt_sampl_taken_to_long

        viewHolder.tvLocation?.text = NumbersHelper.latLongString(lat, lng)
        viewHolder.tvValue?.text = p.pt_sampl_taken_to_address

        if (p.pt_sampl_taken_to_address.length < 1)
        {
            viewHolder.tvValue?.text = "(none)"
        }

        viewHolder.btnSet?.setOnClickListener {
            p.pt_sampl_taken_to_lat = AppGlobals.instance.lastLat
            p.pt_sampl_taken_to_long = AppGlobals.instance.lastLng
            p.save(ctx)
            notifyDataSetChanged()
        }

        viewHolder.itemView.setOnClickListener {
            val alert = AlertHelper(ctx)
            alert.dialogForTextInput("Taken To", p.pt_sampl_taken_to_address, {
                p.pt_sampl_taken_to_address = it
                p.save(ctx)
                notifyDataSetChanged()
            })
        }
    }

    fun formatStartDechlorinating(viewHolder: ViewHolderDateSet)
    {
        val p = AppGlobals.instance.activeProcess
        val theDate = p.pt_dec_start
        val lat = p.pt_dec_start_lat
        val lng = p.pt_dec_start_long

        viewHolder.btnSet?.setOnClickListener {
            if (viewHolder.btnSet?.text == "Set") {
                resetDeChlor()
            }
            else
            {
                val alert = AlertHelper(ctx)
                alert.dialogForOKAlert("Reset Task", "Are you sure you want to reset this task (including resetting flowrates)?", ::undoDeChlor)

            }
            notifyDataSetChanged()
        }

        viewHolder.tvLocation?.text = NumbersHelper.latLongString(lat, lng)
        if (theDate.length > 1)
        {
            viewHolder.tvValue?.text = DateHelper.dbDateStringFormattedWithSeconds(theDate)
        }
        else
        {
            viewHolder.tvValue?.text = "(none)"
        }

        if (p.pt_dec_start.length > 1 && p.pt_dec_dechlorinated.length == 0)
        {
            viewHolder.btnSet?.text = "Reset"
        }
        else
        {
            viewHolder.btnSet?.text = "Set"
        }

        if (lat == 0.0 && lng == 0.0)
        {
            viewHolder.tvLocation?.visibility = View.GONE
        }
    }


    fun formatStartChlorinating(viewHolder: ViewHolderDateSet)
    {
        val p = AppGlobals.instance.activeProcess
        val theDate = p.pt_chlor_start_time
        val lat = p.pt_chlor_start_lat
        val lng = p.pt_chlor_start_long

        viewHolder.btnSet?.setOnClickListener {
            if (viewHolder.btnSet?.text == "Set") {
                resetChlor()
            }
            else
            {
                val alert = AlertHelper(ctx)
                alert.dialogForOKAlert("Reset Task", "Are you sure you want to reset this task (including resetting flowrates)?", ::undoChlor)

            }
            notifyDataSetChanged()
        }

        viewHolder.tvLocation?.text = NumbersHelper.latLongString(lat, lng)
        if (theDate.length > 1)
        {
            viewHolder.tvValue?.text = DateHelper.dbDateStringFormattedWithSeconds(theDate)
        }
        else
        {
            viewHolder.tvValue?.text = "(none)"
        }

        if (p.pt_chlor_start_time.length > 1 && p.pt_chlor_main_chlorinated.length == 0)
        {
            viewHolder.btnSet?.text = "Reset"
        }
        else
        {
            viewHolder.btnSet?.text = "Set"
        }

        if (lat == 0.0 && lng == 0.0)
        {
            viewHolder.tvLocation?.visibility = View.GONE
        }
    }

    fun formatFlushingStarted(viewHolder: ViewHolderDateSet)
    {
        val p = AppGlobals.instance.activeProcess
        val theDate = p.pt_flush_started
        val lat = p.pt_flush_start_lat
        val lng = p.pt_flush_start_long

        viewHolder.btnSet?.setOnClickListener {
            if (viewHolder.btnSet?.text == "Set") {
                resetFlushing()
            }
            else
            {
                val alert = AlertHelper(ctx)
                alert.dialogForOKAlert("Reset Task", "Are you sure you want to reset this task (including resetting flowrates)?", ::undoFilling)

            }
            notifyDataSetChanged()
        }

        viewHolder.tvLocation?.text = NumbersHelper.latLongString(lat, lng)
        if (theDate.length > 1)
        {
            viewHolder.tvValue?.text = DateHelper.dbDateStringFormattedWithSeconds(theDate)
        }
        else
        {
            viewHolder.tvValue?.text = "(none)"
        }

        if (p.pt_flush_started.length > 1 && p.pt_flush_completed.length == 0)
        {
            viewHolder.btnSet?.text = "Reset"
        }
        else
        {
            viewHolder.btnSet?.text = "Set"
        }

        if (lat == 0.0 && lng == 0.0)
        {
            viewHolder.tvLocation?.visibility = View.GONE
        }
    }

    fun formatFlushingStarted2(viewHolder: ViewHolderDateSet)
    {
        val p = AppGlobals.instance.activeProcess
        val theDate = p.pt_flush_started2
        val lat = p.pt_flush_start_lat2
        val lng = p.pt_flush_start_long2

        viewHolder.btnSet?.setOnClickListener {
            if (viewHolder.btnSet?.text == "Set") {
                resetFlushing2()
            }
            else
            {
                val alert = AlertHelper(ctx)
                alert.dialogForOKAlert("Reset Task", "Are you sure you want to reset this task (including resetting flowrates)?", ::undoFlushing2)

            }
            notifyDataSetChanged()
        }

        viewHolder.tvLocation?.text = NumbersHelper.latLongString(lat, lng)
        if (theDate.length > 1)
        {
            viewHolder.tvValue?.text = DateHelper.dbDateStringFormattedWithSeconds(theDate)
        }
        else
        {
            viewHolder.tvValue?.text = "(none)"
        }

        if (p.pt_flush_started2.length > 1 && p.pt_flush_completed2.length == 0)
        {
            viewHolder.btnSet?.text = "Reset"
        }
        else
        {
            viewHolder.btnSet?.text = "Set"
        }

        if (lat == 0.0 && lng == 0.0)
        {
            viewHolder.tvLocation?.visibility = View.GONE
        }
    }

    fun formatFillingStarted(viewHolder: ViewHolderDateSet)
    {
        val p = AppGlobals.instance.activeProcess
        val theDate = p.filling_started
        val lat = p.filling_lat
        val lng = p.filling_long

        viewHolder.btnSet?.setOnClickListener {
            if (viewHolder.btnSet?.text == "Set") {
                resetFilling()
            }
            else
            {
                val alert = AlertHelper(ctx)
                alert.dialogForOKAlert("Reset Task", "Are you sure you want to reset this task (including resetting flowrates)?", ::undoFlushing)

            }
            notifyDataSetChanged()
        }

        viewHolder.tvLocation?.text = NumbersHelper.latLongString(lat, lng)
        if (theDate.length > 1)
        {
            viewHolder.tvValue?.text = DateHelper.dbDateStringFormattedWithSeconds(theDate)
        }
        else
        {
            viewHolder.tvValue?.text = "(none)"
        }

        if (p.filling_started.length > 1 && p.filling_stopped.length == 0)
        {
            viewHolder.btnSet?.text = "Reset"
        }
        else
        {
            viewHolder.btnSet?.text = "Set"
        }

        if (lat == 0.0 && lng == 0.0)
        {
            viewHolder.tvLocation?.visibility = View.GONE
        }
    }


    fun formatSwabLoaded(viewHolder: ViewHolderDateSet)
    {
        val p = AppGlobals.instance.activeProcess
        val theDate = p.swab_loaded
        val lat = p.swab_latitude
        val lng = p.swab_longitude

        viewHolder.btnSet?.setOnClickListener {
            if (viewHolder.btnSet?.text == "Set") {
                resetSwabbing()
            }
            else
            {
                val alert = AlertHelper(ctx)
                alert.dialogForOKAlert("Reset Task", "Are you sure you want to reset this task (including resetting flowrates)?", ::undoSwabbing)

            }
            notifyDataSetChanged()
        }

        viewHolder.tvLocation?.text = NumbersHelper.latLongString(lat, lng)
        if (theDate.length > 1)
        {
            viewHolder.tvValue?.text = DateHelper.dbDateStringFormattedWithSeconds(theDate)
        }
        else
        {
            viewHolder.tvValue?.text = "(none)"
        }

        if (p.swab_loaded.length > 1 && p.swab_removed.length == 0)
        {
            viewHolder.btnSet?.text = "Reset"
        }
        else
        {
            viewHolder.btnSet?.text = "Set"
        }

        if (lat == 0.0 && lng == 0.0)
        {
            viewHolder.tvLocation?.visibility = View.GONE
        }
    }

    fun formatSwabRemoved(viewHolder: ViewHolderDateSet)
    {
        val p = AppGlobals.instance.activeProcess
        val theDate = p.swab_removed
        val lat = p.swab_removed_lat
        val lng = p.swab_removed_long

        viewHolder.btnSet?.setOnClickListener {
            if (viewHolder.btnSet?.text == "Set")
            {
                p.swab_removed = DateHelper.dateToDBString(Date())
                p.swab_removed_lat = AppGlobals.instance.lastLat
                p.swab_removed_long = AppGlobals.instance.lastLng
            }
            else
            {
                p.swab_removed = ""
                p.swab_removed_lat = 0.0
                p.swab_removed_long = 0.0
            }
            p.save(ctx)
            notifyDataSetChanged()
        }

        viewHolder.tvLocation?.text = NumbersHelper.latLongString(lat, lng)
        if (theDate.length > 1)
        {
            viewHolder.tvValue?.text = DateHelper.dbDateStringFormattedWithSeconds(theDate)
        }
        else
        {
            viewHolder.tvValue?.text = "(none)"
        }

        if (p.swab_removed.length > 1)
        {
            viewHolder.btnSet?.text = "Reset"
        }
        else
        {
            viewHolder.btnSet?.text = "Set"
        }

        if (lat == 0.0 && lng == 0.0)
        {
            viewHolder.tvLocation?.visibility = View.GONE
        }
    }

    fun resetChlor()
    {
        val p = AppGlobals.instance.activeProcess
        p.pt_chlor_start_time = DateHelper.dateToDBString(Date())
        p.pt_chlor_main_chlorinated = ""
        p.pt_chlor_end_strength = 0.0
        p.pt_chlor_volume = 0.0
        p.pt_chlor_start_lat = AppGlobals.instance.lastLat
        p.pt_chlor_start_long = AppGlobals.instance.lastLng
        p.save(ctx)

        resetPauses()
        EXLDChlorFlowrates.deleteFlowrates(ctx, p.columnId)
        notifyDataSetChanged()
    }

    fun resetDeChlor()
    {
        val p = AppGlobals.instance.activeProcess
        p.pt_dec_start = DateHelper.dateToDBString(Date())
        p.pt_dec_dechlorinated = ""
        p.pt_dec_volume = 0.0
        p.pt_dec_start_lat = AppGlobals.instance.lastLat
        p.pt_dec_start_long = AppGlobals.instance.lastLng
        p.save(ctx)

        resetPauses()
        EXLDDecFlowrates.deleteFlowrates(ctx, p.columnId)
        notifyDataSetChanged()
    }

    fun resetSwabbing()
    {
        val p = AppGlobals.instance.activeProcess
        p.swab_loaded = DateHelper.dateToDBString(Date())
        p.swab_run_started = ""
        p.swab_home = ""
        p.swab_removed = ""
        p.swab_total_water = 0.0
        p.swab_latitude = AppGlobals.instance.lastLat
        p.swab_longitude = AppGlobals.instance.lastLng
        p.swab_removed_lat = 0.0
        p.swab_removed_long = 0.0
        p.save(ctx)

        resetPauses()
        EXLDSwabFlowrates.deleteFlowrates(ctx, AppGlobals.instance.activeProcess.columnId)
        notifyDataSetChanged()
    }

    fun undoChlor()
    {
        val p = AppGlobals.instance.activeProcess
        p.pt_chlor_start_time = ""
        p.pt_chlor_start_lat = 0.0
        p.pt_chlor_start_long = 0.0
        p.pt_chlor_main_chlorinated = ""
        p.pt_chlor_end_strength = 0.0
        p.pt_chlor_volume = 0.0
        p.save(ctx)

        notifyDataSetChanged()
    }

    fun undoDeChlor()
    {
        val p = AppGlobals.instance.activeProcess
        p.pt_dec_start = ""
        p.pt_dec_dechlorinated = ""
        p.pt_dec_start_lat = 0.0
        p.pt_dec_start_long = 0.0
        p.pt_dec_volume = 0.0
        p.save(ctx)

        notifyDataSetChanged()
    }


    fun undoSwabbing()
    {
        val p = AppGlobals.instance.activeProcess
        p.swab_loaded = ""
        p.swab_latitude = 0.0
        p.swab_longitude = 0.0
        p.save(ctx)

        notifyDataSetChanged()
    }

    fun resetFilling()
    {
        val p = AppGlobals.instance.activeProcess
        p.filling_started = DateHelper.dateToDBString(Date())
        p.filling_stopped = ""
        p.filling_lat = AppGlobals.instance.lastLat
        p.filling_long = AppGlobals.instance.lastLng
        p.filling_total_water_volume = 0.0
        p.save(ctx)

        resetPauses()
        EXLDFillingFlowrates.deleteFlowrates(ctx, p.columnId)
        notifyDataSetChanged()
    }

    fun resetFlushing()
    {
        val p = AppGlobals.instance.activeProcess
        p.pt_flush_started = DateHelper.dateToDBString(Date())
        p.pt_flush_completed = ""
        p.pt_flush_start_lat = AppGlobals.instance.lastLat
        p.pt_flush_start_long = AppGlobals.instance.lastLng
        p.pt_flush_total_water = 0.0
        p.save(ctx)

        resetPauses()
        EXLDFlushFlowrates.deleteFlowrates(ctx, p.columnId, 1)
        notifyDataSetChanged()
    }

    fun resetFlushing2()
    {
        val p = AppGlobals.instance.activeProcess
        p.pt_flush_started2 = DateHelper.dateToDBString(Date())
        p.pt_flush_completed2 = ""
        p.pt_flush_start_lat2 = AppGlobals.instance.lastLat
        p.pt_flush_start_long2 = AppGlobals.instance.lastLng
        p.pt_flush_total_water2 = 0.0
        p.save(ctx)

        resetPauses()
        EXLDFlushFlowrates.deleteFlowrates(ctx, p.columnId, 2)
        notifyDataSetChanged()
    }

    fun undoFilling()
    {
        val p = AppGlobals.instance.activeProcess
        p.filling_started = ""
        p.filling_lat = 0.0
        p.filling_long = 0.0
        p.save(ctx)

        notifyDataSetChanged()
    }

    fun undoFlushing()
    {
        val p = AppGlobals.instance.activeProcess
        p.pt_flush_started = ""
        p.pt_flush_start_lat = 0.0
        p.pt_flush_start_long = 0.0
        p.save(ctx)

        notifyDataSetChanged()
    }

    fun undoFlushing2()
    {
        val p = AppGlobals.instance.activeProcess
        p.pt_flush_started2 = ""
        p.pt_flush_start_lat2 = 0.0
        p.pt_flush_start_long2 = 0.0
        p.save(ctx)

        notifyDataSetChanged()
    }

    // Find these methods in TableViewBase.PauseSessions
    fun loadPauseSessions()
    {
        var pauseType = getPauseTypeString()
        if (pauseType.length > 1)
        {
            pauseSessions = ctx.database.use {
                select(EXLDPauseSessions.TABLE_NAME)
                        .whereArgs("${EXLDPauseSessions.COLUMN_PAUSE_PROCESS_ID} = ${AppGlobals.instance.activeProcess.columnId} AND ${EXLDPauseSessions.COLUMN_PAUSE_TYPE} = '$pauseType'")
                        .orderBy(EXLDPauseSessions.COLUMN_ID)
                        .exec {
                            parseList<EXLDPauseSessions>(classParser())
                        }
            }
        }
    }

    // Ensures tht orphan pauses are closed
    fun closePauseSessions()
    {
        var pauseType = getPauseTypeString()
        val pauses = ctx.database.use {
            select(EXLDPauseSessions.TABLE_NAME)
                    .whereArgs("${EXLDPauseSessions.COLUMN_PAUSE_PROCESS_ID} = ${AppGlobals.instance.activeProcess.columnId} AND ${EXLDPauseSessions.COLUMN_PAUSE_TYPE} = '$pauseType'")
                    .orderBy(EXLDPauseSessions.COLUMN_ID)
                    .exec {
                        parseList<EXLDPauseSessions>(classParser())
                    }
        }

        val p = AppGlobals.instance.activeProcess
        for (pause in pauses)
        {
            val pauseEnd = DateHelper.dbStringToDateOrNull(pause.pause_end)
            if (pauseEnd == null)
            {
                when (pause.pause_type)
                {
                    EXLDPauseSessions.PAUSE_TYPE_FILLING -> pause.pause_end = p.filling_stopped
                    EXLDPauseSessions.PAUSE_TYPE_SWABBING -> pause.pause_end = p.swab_home
                    EXLDPauseSessions.PAUSE_TYPE_CHLOR -> pause.pause_end = p.pt_chlor_main_chlorinated
                    EXLDPauseSessions.PAUSE_TYPE_DECHLOR -> pause.pause_end = p.pt_dec_dechlorinated
                    EXLDPauseSessions.PAUSE_TYPE_FLUSH -> pause.pause_end = p.pt_flush_completed
                    EXLDPauseSessions.PAUSE_TYPE_FLUSH2 -> pause.pause_end = p.pt_flush_completed2
                }
            }

            pause.save(ctx)
        }

        taskIsPaused = false
    }

    fun resetPauses()
    {
        currentPause = null
        taskIsPaused = false
        pauseSessions = null

        // Delete any existing pauses of the correct type
        var pauseType = getPauseTypeString()
        if (pauseType.length > 1)
        {
            ctx.database.use {
                delete(EXLDPauseSessions.TABLE_NAME, "${EXLDPauseSessions.COLUMN_PAUSE_PROCESS_ID} = ${AppGlobals.instance.activeProcess.columnId} AND ${EXLDPauseSessions.COLUMN_PAUSE_TYPE} = '$pauseType'")
            }
        }

        updateTotalWater()
    }

    
    fun pauseButtonPressed()
    {
        currentPause = EXLDPauseSessions()
        currentPause!!.pause_process_id = AppGlobals.instance.activeProcess.columnId
        currentPause!!.pause_type = getPauseTypeString()
        currentPause!!.pause_start = DateHelper.dateToDBString(Date())
        currentPause!!.pause_flowrate = getCurrentFlowrate()
        taskIsPaused = true
        currentPause!!.save(ctx)
        notifyDataSetChanged()
    }

    fun restartButtonPressed()
    {
        currentPause!!.pause_end = DateHelper.dateToDBString(Date())
        taskIsPaused = false
        currentPause!!.save(ctx)
        updateTotalWater()
        notifyDataSetChanged()
    }

    fun addFlowratePressed()
    {
        var guardLowerDate = ""
        var guardUpperDate = ""

        val p = AppGlobals.instance.activeProcess

        if (pipedTask == PipedTask.Swabbing)
        {
            guardLowerDate = p.swab_run_started
            guardUpperDate = p.swab_home
        }

        if (pipedTask == PipedTask.Filling)
        {
            guardLowerDate = p.filling_started
            guardUpperDate = p.filling_stopped
        }

        if (pipedTask == PipedTask.Flushing)
        {
            guardLowerDate = p.pt_flush_started
            guardUpperDate = p.pt_flush_completed
        }

        if (pipedTask == PipedTask.Flushing2)
        {
            guardLowerDate = p.pt_flush_started2
            guardUpperDate = p.pt_flush_completed2
        }

        if (pipedTask == PipedTask.Chlorination)
        {
            guardLowerDate = p.pt_chlor_start_time
            guardUpperDate = p.pt_chlor_main_chlorinated
        }

        if (pipedTask == PipedTask.DeChlorination)
        {
            guardLowerDate = p.pt_dec_start
            guardUpperDate = p.pt_dec_dechlorinated
        }

        val lowerDate = DateHelper.dbStringToDateOrNull(guardLowerDate)
        val upperDate = DateHelper.dbStringToDateOrNull(guardUpperDate)

        if (lowerDate == null || upperDate != null)
        {
            val alert = AlertHelper(ctx)
            alert.dialogForOKAlertNoAction("Task Not Running", "The task must be running to enter a flowrate")
            return
        }

        if (pipedTask == PipedTask.Chlorination)
        {
            delegate?.didRequestFlowrate(-1)
            return
        }

        if (pipedTask == PipedTask.DeChlorination)
        {
            delegate?.didRequestFlowrate(-1)
            return
        }

        val alert = AlertHelper(ctx)
        alert.dialogForTextInput("Enter Flowrate", "0", {
            val doubleValue = it.toDoubleOrNull()
            if (doubleValue != null)
            {
                createNewFlowrate(doubleValue)
            }
        })
    }

    fun createNewFlowrate(value: Double)
    {
        val p = AppGlobals.instance.activeProcess
        when (pipedTask)
        {
            PipedTask.Swabbing -> {

                val initialFrCount = EXLDSwabFlowrates.getSwabbingFlowrates(ctx, p.columnId).size
                val fr = EXLDSwabFlowrates.createFlowrate(ctx, value, p.columnId)
                if (initialFrCount == 0) {
                    if (DateHelper.dbStringToDateOrNull(p.swab_run_started) != null)
                    {
                        fr.swab_created = p.swab_run_started
                    }
                }
                fr.save(ctx)
            }

            PipedTask.Filling -> {
                val initialFrCount = EXLDFillingFlowrates.getFillingFlowrates(ctx, p.columnId).size
                val fr = EXLDFillingFlowrates.createFlowrate(ctx, value, p.columnId)
                if (initialFrCount == 0) {
                    if (DateHelper.dbStringToDateOrNull(p.filling_started) != null)
                    {
                        fr.filling_created = p.filling_started
                    }
                }
                fr.save(ctx)
            }

            PipedTask.Flushing -> {
                val initialFrCount = EXLDFlushFlowrates.getFlushFlowrates(ctx, p.columnId, 1).size
                val fr = EXLDFlushFlowrates.createFlowrate(ctx, value, p.columnId, 1)
                if (initialFrCount == 0) {
                    if (DateHelper.dbStringToDateOrNull(p.pt_flush_started) != null)
                    {
                        fr.flush_created = p.pt_flush_started
                    }
                }
                fr.save(ctx)
            }

            PipedTask.Flushing2 -> {
                val initialFrCount = EXLDFlushFlowrates.getFlushFlowrates(ctx, p.columnId, 2).size
                val fr = EXLDFlushFlowrates.createFlowrate(ctx, value, p.columnId, 2)
                if (initialFrCount == 0) {
                    if (DateHelper.dbStringToDateOrNull(p.pt_flush_started2) != null)
                    {
                        fr.flush_created = p.pt_flush_started2
                    }
                }
                fr.save(ctx)
            }

            PipedTask.Chlorination -> {
                delegate?.didRequestFlowrate(-1)
            }

            PipedTask.DeChlorination -> {
                val initialFrCount = EXLDDecFlowrates.getDecFlowrates(ctx, p.columnId).size
                val fr = EXLDDecFlowrates.createFlowrate(ctx, p.columnId)
                if (initialFrCount == 0) {
                    if (DateHelper.dbStringToDateOrNull(p.pt_dec_start) != null)
                    {
                        fr.dec_timestamp = p.pt_dec_start
                    }
                }
                fr.save(ctx)
            }
        }

        updateTotalWater()
        notifyDataSetChanged()
    }



    fun operationIsInProgress(startDateString: String, finishDateString: String): Boolean
    {
        val startDate = DateHelper.dbStringToDateOrNull(startDateString)
        val finishDate = DateHelper.dbStringToDateOrNull(finishDateString)

        if (startDate != null)
        {
            if (finishDate != null)
            {
                if (startDate.time < finishDate.time)
                {
                    return false
                }
                else
                {
                    return true
                }
            }
            else
            {
                return  true
            }
        }
        else
        {
            return false
        }
    }

    fun operationIsCompleted(startDateString: String, finishDateString: String): Boolean
    {
        val startDate = DateHelper.dbStringToDateOrNull(startDateString)
        val finishDate = DateHelper.dbStringToDateOrNull(finishDateString)

        if (startDate != null)
        {
            if (finishDate != null)
            {
                if (finishDate.time > startDate.time)
                {
                    return true
                }
            }
        }

        return false
    }

    fun getCurrentFlowrate(): Double
    {
        val p = AppGlobals.instance.activeProcess
        when (pipedTask)
        {
            PipedTask.Swabbing -> {
                val flowrates = EXLDSwabFlowrates.getSwabbingFlowrates(ctx, p.columnId)
                if (flowrates.size > 0)
                {
                    return flowrates.first().swab_flowrate
                }
                else
                {
                    return 0.0
                }
            }

            PipedTask.Filling -> {
                val flowrates = EXLDFillingFlowrates.getFillingFlowrates(ctx, p.columnId)
                if (flowrates.size > 0)
                {
                    return flowrates.first().filling_flowrate
                }
                else
                {
                    return 0.0
                }
            }

            PipedTask.Flushing -> {
                val flowrates = EXLDFlushFlowrates.getFlushFlowrates(ctx, p.columnId, 1)
                if (flowrates.size > 0)
                {
                    return flowrates.first().flush_flowrate
                }
                else
                {
                    return 0.0
                }
            }

            PipedTask.Flushing2 -> {
                val flowrates = EXLDFlushFlowrates.getFlushFlowrates(ctx, p.columnId, 2)
                if (flowrates.size > 0)
                {
                    return flowrates.first().flush_flowrate
                }
                else
                {
                    return 0.0
                }
            }

            PipedTask.Chlorination -> {
                val flowrates = EXLDChlorFlowrates.getChlorFlowrates(ctx, p.columnId)
                if (flowrates.size > 0)
                {
                    return flowrates.first().chlor_flowrate
                }
                else
                {
                    return 0.0
                }
            }

            PipedTask.DeChlorination -> {
                val flowrates = EXLDDecFlowrates.getDecFlowrates(ctx, p.columnId)
                if (flowrates.size > 0)
                {
                    return flowrates.first().dec_flowrate
                }
                else
                {
                    return 0.0
                }
            }
        }

        return 0.0
    }

    fun getPauseTypeString(): String
    {
        var pauseType = ""
        when (pipedTask)
        {
            PipedTask.Swabbing -> pauseType = EXLDPauseSessions.PAUSE_TYPE_SWABBING
            PipedTask.Filling -> pauseType = EXLDPauseSessions.PAUSE_TYPE_FILLING
            PipedTask.Chlorination -> pauseType = EXLDPauseSessions.PAUSE_TYPE_CHLOR
            PipedTask.DeChlorination -> pauseType = EXLDPauseSessions.PAUSE_TYPE_DECHLOR
            PipedTask.Flushing -> pauseType = EXLDPauseSessions.PAUSE_TYPE_FLUSH
            PipedTask.Flushing2 -> pauseType = EXLDPauseSessions.PAUSE_TYPE_FLUSH2
        }

        return pauseType
    }

    fun updateTotalWater()
    {
        shouldCalculateTotalWater = true
        notifyDataSetChanged()
    }

}

class PipedTableRow(val position: Int, val rowType: PipedTableRowType, val title: String, val table: String = "", val field: String = "", val flowrateId: Int = -1) {

    enum class PipedTableRowType(val value: Int) {
        DateSet(0), DateSetLocation(1), SectionHeader(2), AddFlowrateButton(3), Flowrate(4), Notes(5), PauseSectionHeader(6), Count(7), Unknown(8),
        TitleValue(9), MainPicture(10), TestStatus(11)
    }
}
