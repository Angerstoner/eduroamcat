package de.gwdg.wifitool.frontend.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        this.binding = FragmentProfileBinding.inflate(inflater, container, false)
        parentActivity = activity as MainActivity
        profileApi = parentActivity.profileApi
        try {
            initProfileInfoBox()
            initProfileSelectionSpinner()
        } catch (e: NullPointerException) {
            Log.e(logTag, "Context/Activity missing, could not init Fragment.\n ${e.stackTrace}")
        }

        return this.binding.root
    }


    override fun onResume() {
        identityProviderId = loadIdentityProviderId()
        if (identityProviderId != -1L) {
            profileApi.updateIdentityProviderProfiles(identityProviderId)
            parentActivity.allowNext()
        } else {
            Log.e(logTag, "Invalid Identity Provider. Cannot continue.")
        }

        super.onResume()
    }

    private fun initProfileInfoBox() {
        binding.profileInformationCard.observeProfileAttributes(this, profileApi)
        binding.profileInformationCard.setOnClickListener {
            binding.profileInformationCard.openProfileInformationDialog(
                childFragmentManager
            )
        }
    }

    private fun initProfileSelectionSpinner() {
        profileArrayAdapter = ProfileArrayAdapter(activity!!, R.layout.spinner_profile_dropdown)
        binding.profileSpinner.adapter = profileArrayAdapter

        binding.profileSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedProfile = profileArrayAdapter.getItem(position)
                binding.profileInformationCard.setTitleRefresh()
                profileApi.updateProfileAttributes(selectedProfile)
                saveProfile(selectedProfile)
            }

            // do nothing
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        profileApi.getIdentityProviderProfilesLiveData()
            .observe(this) { profiles ->
                profileArrayAdapter.setProfiles(profiles)
                // hide spinner/dropdown and label if there is nothing to select
                binding.profileSpinnerLabel.visibility = if (profiles.size == 1) View.GONE else View.VISIBLE
                binding.profileSpinner.visibility = if (profiles.size == 1) View.GONE else View.VISIBLE

                // populate infobox with first item and add observer to liveData used for profile preview
                val defaultProfile = profileArrayAdapter.getItem(0)
                profileApi.updateProfileAttributes(defaultProfile)
                saveProfile(defaultProfile)
            }
    }

    private fun loadIdentityProviderId(): Long {
        val sharedPref =
            parentActivity.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        return sharedPref.getLong(getString(R.string.preference_identity_provider_id), -1L)
    }


    /**
     * Stores selected Profile to app preferences.
     *
     * Value used in [CredentialFragment]
     */
    private fun saveProfile(profile: Profile) {
        val sharedPref =
            parentActivity.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putLong(getString(R.string.preference_profile_id), profile.profileId)
            putString(getString(R.string.preference_profile_name), profile.displayLabel)
            apply()
        }
    }
}