package de.gwdg.wifitool.frontend.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.gwdg.wifitool.backend.ProfileApi
import de.gwdg.wifitool.databinding.FragmentProfileBinding
import de.gwdg.wifitool.frontend.adapters.IdentityProviderArrayAdapter

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding

    private lateinit var identityProviderArrayAdapter: IdentityProviderArrayAdapter
    private lateinit var profileApi: ProfileApi


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        this.binding = FragmentProfileBinding.inflate(inflater, container, false)

//        profileApi = ProfileApi(activity!!.applicationContext)

        return this.binding.root
    }

//
//    private fun initProfileSelectionSpinner() {
//        profileArrayAdapter = ProfileArrayAdapter(
//            this,
//            android.R.layout.simple_spinner_item
//        )
//        binding.profileSpinner.adapter = profileArrayAdapter
//    }


//    private fun showProfilesForIdentityProvider(identityProvider: IdentityProvider) {
//        ProfileApi(this).getIdentityProviderProfiles(identityProvider)
//            .observe(this, Observer { profiles ->
//                profileArrayAdapter.setProfiles(profiles)
//                binding.profileSpinner.visibility = if (profileArrayAdapter.count == 0) GONE else VISIBLE
//                binding.profileSpinner.isEnabled = (profileArrayAdapter.count > 1)
//                binding.hiddenConstraintLayout.visibility =
//                    if (profileArrayAdapter.count == 0) GONE else VISIBLE
//            })
//    }

    // TODO: refactor end
}