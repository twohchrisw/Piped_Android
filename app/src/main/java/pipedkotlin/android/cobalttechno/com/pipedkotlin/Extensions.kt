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