package de.gwdg.wifitool.frontend.components

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import de.gwdg.wifitool.R

class IdentityProviderDownloadErrorDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            builder.setMessage(R.string.dialog_identity_provider_download_failed)
                .setNeutralButton(R.string.dialog_button_okay) { _, _ ->
                    // User acknowledged error
                }
            // TODO: add retry button
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
