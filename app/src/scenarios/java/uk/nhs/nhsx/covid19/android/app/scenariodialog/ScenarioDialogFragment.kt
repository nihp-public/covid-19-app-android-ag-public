package uk.nhs.nhsx.covid19.android.app.scenariodialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

abstract class ScenarioDialogFragment(
    private val positiveAction: (() -> Unit),
    private val dismissAction: (() -> Unit)? = null
) : DialogFragment() {
    protected abstract val title: String
    protected abstract val layoutId: Int
    protected var inflatedView: View? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it).apply {
                setTitle(title)
                setView(it.createView())

                setPositiveButton(android.R.string.ok) { _, _ ->
                    positiveAction.invoke()
                }
                setNegativeButton(android.R.string.cancel) { dialog, _ ->
                    dismissAction?.invoke()
                    dialog.dismiss()
                }
            }
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun Context.createView(): View =
        LayoutInflater.from(this).inflate(layoutId, null).apply {
            setUp(this)
        }.apply { inflatedView = this }

    protected fun Boolean.toViewState(): Int =
        if (this) View.VISIBLE else View.INVISIBLE

    abstract fun setUp(view: View)
}
