package pipedkotlin.android.cobalttechno.com.pipedkotlin

public class AppGlobals private constructor() {

    // Singleton setup
    private object Holder { val INSTANCE = AppGlobals() }

    companion object {
        val instance: AppGlobals by lazy { Holder.INSTANCE }
    }

    var companyId = ""
    var activeProcess = EXLDProcess()

}