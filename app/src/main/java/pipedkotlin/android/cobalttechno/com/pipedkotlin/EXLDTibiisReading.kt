package pipedkotlin.android.cobalttechno.com.pipedkotlin

data class EXLDTibiisReading(val _id: Long,
                             val battery: Int = -1,
                             val createdOn: String = "",
                             val flowRate: Int = -1,
                             val logNumber: Int = -1,
                             val pressure: Int = -1,
                             val processId: Int = -1,
                             val readingType: String = "",
                             val testType: String = "",
                             val uploaded: Int = 0)
{
    companion object {

    }
}

