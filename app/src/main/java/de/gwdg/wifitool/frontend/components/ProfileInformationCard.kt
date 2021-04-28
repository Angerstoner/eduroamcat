package de.gwdg.wifitool.frontend.components

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import de.gwdg.wifitool.R
import de.gwdg.wifitool.backend.ProfileApi

class ProfileInformationCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) :
    ConstraintLayout(context, attrs, defStyle) {
    private val profilePreviewLabel: TextView
    private val displayNameTextView: TextView
    private val helpdeskMailTextView: TextView
    private val helpdeskWebTextView: TextView
    private val helpdeskPhoneTextView: TextView
    private var profileDescription: String = ""

    init {
        inflate(context, R.layout.view_profile_information_card, this)

        profilePreviewLabel = findViewById(R.id.profilePreviewLabel)
        displayNameTextView = findViewById(R.id.displayNameTextView)
        helpdeskMailTextView = findViewById(R.id.helpdeskMailTextView)
        helpdeskWebTextView = findViewById(R.id.helpdeskWebTextView)
        helpdeskPhoneTextView = findViewById(R.id.helpdeskPhoneTextView)
    }

    fun observeProfileAttributes(parent: Fragment, profileApi: ProfileApi) {
        with(parent) {
            profileApi.getProfileAttributesLiveData().observe(parent, { profileAttributes ->
                with(profileAttributes) {
                    profilePreviewLabel.text = getString(R.string.profile_preview_label_text)
                    displayNameTextView.text = identityProviderName
                    helpdeskMailTextView.text = identityProviderMail
                    helpdeskPhoneTextView.text = identityProviderPhone
                    helpdeskWebTextView.text = identityProviderUrl
                    profileDescription = identityProviderDescription
                }
            })
        }
    }

    fun setTitleRefresh(parent: Fragment) {
        profilePreviewLabel.text = parent.getString(R.string.profile_preview_label_refreshing_text)
    }

}