package de.gwdg.wifitool.frontend.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import de.gwdg.wifitool.R
import de.gwdg.wifitool.backend.ProfileApi
import de.gwdg.wifitool.backend.models.Profile
import de.gwdg.wifitool.databinding.FragmentProfileBinding
import de.gwdg.wifitool.frontend.activities.MainActivity
import de.gwdg.wifitool.frontend.adapters.ProfileArrayAdapter
import java.lang.NullPointerException

class ProfileFragment : Fragment() {
    private val logTag = "ProfileFragment"
    private lateinit var binding: FragmentProfileBinding
    private lateinit var parentActivity: MainActivity
    private lateinit var profileApi: ProfileApi
    private lateinit var profileArrayAdapter: ProfileArrayAdapter
    private var identityProviderId = -1L

    //TODO: move this somewhere it is called everytime the fragment is (re)opened
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        this.binding = FragmentProfileBinding.inflate(inflater, container, false)

        try {
            parentActivity = activity as MainActivity
            profileApi = ProfileApi(parentActivity.applicationContext)
            identityProviderId = loadIdentityProviderId()
            if (identityProviderId != -1L) {
                initProfileSelectionSpinner()
                initProfileInfoBox()
            } else {
                Log.e(logTag, "Invalid Identity Provider. Cannot continue.")
            }
        } catch (e: NullPointerException) {
            Log.e(logTag, "Context/Activity missing, could not init Fragment. \n${e.stackTrace}")
        }
        return this.binding.root
    }

    private fun loadIdentityProviderId(): Long {
        val sharedPref =
            parentActivity.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        return sharedPref.getLong(getString(R.string.preference_identity_provider_id), -1L)
    }

    private fun initProfileInfoBox() {
        binding.profileSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedProfile = profileArrayAdapter.getItem(position)
                binding.profilePreviewLabel.text = getString(R.string.profile_preview_label_refreshing_text)
                profileApi.updateProfileAttributes(selectedProfile)
            }

            // do nothing
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun updateProfileInfoBox(profile: Profile) {
        profileApi.getProfileAttributes(profile).observe(this, Observer { profileAttributes ->
            with(profileAttributes) {
                binding.profilePreviewLabel.text = getString(R.string.profile_preview_label_text)
                binding.displayNameTextView.text = identityProviderName
                binding.helpdeskMailTextView.text = identityProviderMail
                binding.helpdeskPhoneTextView.text = identityProviderPhone
                binding.helpdeskWebTextView.text = identityProviderUrl
            }
        })
    }

    private fun initProfileSelectionSpinner() {
        profileArrayAdapter = ProfileArrayAdapter(activity!!, R.layout.spinner_profile_dropdown)
        binding.profileSpinner.adapter = profileArrayAdapter

        ProfileApi(activity!!).getIdentityProviderProfiles(identityProviderId)
            .observe(this, Observer { profiles ->
                profileArrayAdapter.setProfiles(profiles)

                // populate infobox with first item and add observer to liveData used for profile preview
                updateProfileInfoBox(profileArrayAdapter.getItem(0))
            })
    }

    //TODO: save selected profile to preferences
    //TODO: download eap-config for profile to device
}