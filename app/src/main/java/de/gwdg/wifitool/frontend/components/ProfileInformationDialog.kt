package de.gwdg.wifitool.frontend.components

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import de.gwdg.wifitool.R
import de.gwdg.wifitool.databinding.DialogProfileInformationBinding

const val BUNDLE_KEY_PROFILE_DIALOG_DISPLAY_NAME = "bundle_key_profile_dialog_display_name"
const val BUNDLE_KEY_PROFILE_DIALOG_HELPDESK_WEB = "bundle_key_profile_dialog_helpdesk_web"
const val BUNDLE_KEY_PROFILE_DIALOG_HELPDESK_MAIL = "bundle_key_profile_dialog_helpdesk_mail"
const val BUNDLE_KEY_PROFILE_DIALOG_HELPDESK_PHONE = "bundle_key_profile_dialog_helpdesk_phone"
const val BUNDLE_KEY_PROFILE_DIALOG_HELPDESK_DESCRIPTION = "bundle_key_profile_dialog_helpdesk_description"

class ProfileInformationDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { activity ->
            val binding = DialogProfileInformationBinding.inflate(activity.layoutInflater)
            arguments?.let { args ->
                val displayName = args.getString(BUNDLE_KEY_PROFILE_DIALOG_DISPLAY_NAME, "")
                val helpdeskWeb = args.getString(BUNDLE_KEY_PROFILE_DIALOG_HELPDESK_WEB, "")
                val helpdeskMail = args.getString(BUNDLE_KEY_PROFILE_DIALOG_HELPDESK_MAIL, "")
                val helpdeskPhone = args.getString(BUNDLE_KEY_PROFILE_DIALOG_HELPDESK_PHONE, "")
                val description = args.getString(BUNDLE_KEY_PROFILE_DIALOG_HELPDESK_DESCRIPTION, "")

                binding.displayNameTextView.text = displayName
                binding.helpdeskWebTextView.text = helpdeskWeb
                binding.helpdeskMailTextView.text = helpdeskMail
                binding.helpdeskPhoneTextView.text = helpdeskPhone
                binding.descriptionTextView.text = description

                AlertDialog.Builder(activity)
                    .setView(binding.root)
                    .setPositiveButton(R.string.dialog_button_okay) { _, _ -> }.create()
            }
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
