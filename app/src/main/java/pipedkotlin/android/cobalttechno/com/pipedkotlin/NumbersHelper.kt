package pipedkotlin.android.cobalttechno.com.pipedkotlin

class NumbersHelper {

    companion object {
        public fun latLongString(lat: Double, lng: Double): String
        {
            return "Lat: " + lat.formatForDecPlaces(3) + " Long: " + lng.formatForDecPlaces(3)
        }
    }
}