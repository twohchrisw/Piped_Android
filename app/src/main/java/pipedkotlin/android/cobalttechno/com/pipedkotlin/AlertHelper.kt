package pipedkotlin.android.cobalttechno.com.pipedkotlin

import android.app.AlertDialog
import android.content.Context
import android.text.InputType
import android.widget.EditText

open class AlertHelper(context: Context) {

    val builder = AlertDialog.Builder(context)
    val dialogEditText = EditText(context)
    var textResult: String = ""

    // See LoginActivity.getCompanyId for a simple examle of passing a named function
    public fun dialogForTextInput(title: String, defaultValue: String, positiveAction: (value: String) -> Unit)
    {
        dialogEditText.inputType = InputType.TYPE_CLASS_TEXT
        dialogEditText.setText(defaultValue)
        builder.setTitle(title)
        builder.setView(dialogEditText)

        builder.setPositiveButton("OK", { dialog, i ->
            textResult = dialogEditText.text.toString()
            positiveAction.invoke(textResult)
            dialog.dismiss()
        })

        builder.setNegativeButton("Cancel", { dialog, i ->
            dialog.dismiss()
        })

        builder.create().show()
    }


}
