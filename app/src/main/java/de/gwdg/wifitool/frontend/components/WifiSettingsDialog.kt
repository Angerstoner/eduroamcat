package de.gwdg.wifitool.frontend.components

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import de.gwdg.wifitool.R
import de.gwdg.wifitool.backend.WifiConfig

class WifiSettingsDialog : DialogFragment() {
    // TODO: show different texts/buttons on Android 10 and when wifi is off
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            builder.setMessage(R.string.dialog_remove_old_eduroam)
                .setPositiveButton(R.string.dialog_button_open_wifi_settings) { _, _ ->
                    // Open Wi-Fi Settings
                    val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                    startActivity(intent)
                }
                .setNegativeButton(R.string.dialog_button_cancel) { _, _ ->
                    // User cancelled the dialog
                }
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    // dialog only needs to be shown if an existing config is detected or is not detectable
    fun isNeeded(requestingActivity: Activity): Boolean {
        return if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            // warn user if an existing eduroam connection was detected
            // users has to delete this existing config by themselves
            WifiConfig(requestingActivity).hasEduroamConfiguration()
        } else Build.VERSION.SDK_INT == Build.VERSION_CODES.Q
            // on Android 10 the app can not detect if an eduroam config exists and
            // therefore needs to show this to every user
            // from Android 11 the app can overwrite existing eduroam connections
    }
}
