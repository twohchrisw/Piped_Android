package pipedkotlin.android.cobalttechno.com.pipedkotlin

class CobaltXmlRow {
    var fields = ArrayList<CobaltXmlField>()

    public fun getFieldForKey(key: String): CobaltXmlField?
    {
        for (field: CobaltXmlField in fields)
        {
            if (field.key.equals(key))
            {
                return field
            }
        }

        return null
    }
}

class CobaltXmlField {
    var key = ""
    var value = ""
}