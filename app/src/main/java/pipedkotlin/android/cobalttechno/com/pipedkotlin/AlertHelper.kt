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
        dialogEditText.setSelectAllOnFocus(true)
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

    // Simple alert with no response
    public fun dialogForOKAlert(title: String, message: String, positiveAction: () -> Unit)
    {
        builder.setTitle(title)
        builder.setMessage(message)

        builder.setPositiveButton("OK", { dialog, i ->
            positiveAction.invoke()
            dialog.dismiss()
        })

        builder.setNegativeButton("Cancel", { dialog, i ->

        })

        builder.create().show()
    }

    // Simple alert with no response and no follow action
    public fun dialogForOKAlertNoAction(title: String, message: String)
    {
        builder.setTitle(title)
        builder.setMessage(message)

        builder.setPositiveButton("OK", { dialog, i ->
            dialog.dismiss()
        })

        builder.create().show()
    }


}
