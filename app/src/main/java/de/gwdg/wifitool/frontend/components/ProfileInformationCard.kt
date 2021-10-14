package de.gwdg.wifitool.frontend.components

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.card.MaterialCardView
import de.gwdg.wifitool.R
import de.gwdg.wifitool.backend.ProfileApi
import de.gwdg.wifitool.backend.models.ProfileAttributes

class ProfileInformationCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) :
    ConstraintLayout(context, attrs, defStyle) {
    private val profilePreviewCard: MaterialCardView
    private val profilePreviewLabel: TextView
    private val displayNameTextView: TextView
    private val helpdeskMailTextView: TextView
    private val helpdeskWebTextView: TextView
    private val helpdeskPhoneTextView: TextView
    private var profileDescription: String = ""

    init {
        inflate(context, R.layout.view_profile_information_card, this)

        profilePreviewCard = findViewById(R.id.profilePreviewCard)
        profilePreviewLabel = findViewById(R.id.profilePreviewLabel)
        displayNameTextView = findViewById(R.id.displayNameTextView)
        helpdeskWebTextView = findViewById(R.id.helpdeskWebTextView)
        helpdeskMailTextView = findViewById(R.id.helpdeskMailTextView)
        helpdeskPhoneTextView = findViewById(R.id.helpdeskPhoneTextView)
    }

    fun observeProfileAttributes(parent: Fragment, profileApi: ProfileApi) {
        profileApi.getProfileAttributesLiveData().observe(parent, { profileAttributes ->
            setProfileAttributes(parent, profileAttributes)
        })
    }

    fun setProfileAttributes(parent: Fragment, profileAttributes: ProfileAttributes) {
        with(parent) {
            with(profileAttributes) {
                profilePreviewLabel.text = getString(R.string.profile_preview_label_text)
                displayNameTextView.text = identityProviderName
                helpdeskWebTextView.text = identityProviderUrl
                helpdeskMailTextView.text = identityProviderMail
                helpdeskPhoneTextView.text = identityProviderPhone
                profileDescription = identityProviderDescription
            }
        }
    }

    fun setTitleRefresh() {
        profilePreviewLabel.text = context.getString(R.string.profile_preview_label_refreshing_text)
    }

    override fun setOnClickListener(l: OnClickListener?) {
        profilePreviewCard.setOnClickListener(l)
    }

    fun openProfileInformationDialog(fragmentManager: FragmentManager) {
        val profileInformationDialog = ProfileInformationDialog()
        val args = Bundle()
        args.putString(BUNDLE_KEY_PROFILE_DIALOG_DISPLAY_NAME, displayNameTextView.text.toString())
        args.putString(BUNDLE_KEY_PROFILE_DIALOG_HELPDESK_MAIL, helpdeskMailTextView.text.toString())
        args.putString(BUNDLE_KEY_PROFILE_DIALOG_HELPDESK_PHONE, helpdeskPhoneTextView.text.toString())
        args.putString(BUNDLE_KEY_PROFILE_DIALOG_HELPDESK_WEB, helpdeskWebTextView.text.toString())
        args.putString(BUNDLE_KEY_PROFILE_DIALOG_HELPDESK_DESCRIPTION, profileDescription)
        profileInformationDialog.arguments = args
        profileInformationDialog.show(fragmentManager, null)
    }
}