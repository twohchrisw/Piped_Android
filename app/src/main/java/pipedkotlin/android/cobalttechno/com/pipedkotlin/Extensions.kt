package pipedkotlin.android.cobalttechno.com.pipedkotlin

public fun String.valueOrNone(): String
{
    if (this.isNullOrEmpty())
    {
        return "(none)"
    }
    else
    {
        return this
    }
}

public fun Double.formatForDecPlaces(places: Int): String
{
    val formatString = "%." + places + "f"
    return formatString.format(this)
}

public fun String.toCobaltInt(): Int
{
    val tempInt = this.toIntOrNull()
    if (tempInt == null)
    {
        return 0
    }

    return tempInt
}

public fun String.toCobaltDouble(): Double
{
    val tempDouble = this.toDoubleOrNull()
    if (tempDouble == null)
    {
        return 0.0
    }

    return tempDouble
}