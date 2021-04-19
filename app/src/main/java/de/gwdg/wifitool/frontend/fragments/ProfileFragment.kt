package de.gwdg.wifitool.frontend.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import de.gwdg.wifitool.backend.ProfileApi
import de.gwdg.wifitool.backend.models.IdentityProvider
import de.gwdg.wifitool.databinding.FragmentProfileBinding
import de.gwdg.wifitool.frontend.activities.MainActivity
import de.gwdg.wifitool.frontend.adapters.IdentityProviderArrayAdapter
import de.gwdg.wifitool.frontend.adapters.ProfileArrayAdapter

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding

    private lateinit var identityProviderArrayAdapter: IdentityProviderArrayAdapter
    private lateinit var profileApi: ProfileApi
    private lateinit var profileArrayAdapter: ProfileArrayAdapter


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        this.binding = FragmentProfileBinding.inflate(inflater, container, false)

        profileApi = ProfileApi(activity!!.applicationContext)
        (activity as MainActivity).allowNext()
        initProfileSelectionSpinner()
        val idp = IdentityProvider(5055, "DE", "GWDG Goettingen")
        showProfilesForIdentityProvider(idp)

        return this.binding.root
    }

    //
    private fun initProfileSelectionSpinner() {
        profileArrayAdapter = ProfileArrayAdapter(activity!!, android.R.layout.simple_spinner_item)
        binding.profileSpinner.adapter = profileArrayAdapter
    }


    private fun showProfilesForIdentityProvider(identityProvider: IdentityProvider) {
        ProfileApi(activity!!).getIdentityProviderProfiles(identityProvider)
            .observe(this, Observer { profiles ->
                profileArrayAdapter.setProfiles(profiles)
            })
    }

    // TODO: refactor end
}