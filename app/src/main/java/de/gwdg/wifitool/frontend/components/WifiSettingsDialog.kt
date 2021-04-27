package de.gwdg.wifitool.frontend.components

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import de.gwdg.wifitool.R

class WifiSettingsDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            builder.setMessage(R.string.dialog_remove_old_eduroam)
                .setPositiveButton(R.string.open_wifi_settings) { dialog, id ->
                    // Open Wi-Fi Settings
                    val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                    startActivity(intent)
                }
                .setNegativeButton(
                    R.string.cancel
                ) { dialog, id ->
                    // User cancelled the dialog
                }
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
