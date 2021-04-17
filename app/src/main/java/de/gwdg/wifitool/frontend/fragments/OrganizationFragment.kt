package de.gwdg.wifitool.frontend.fragments

import android.R
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import de.gwdg.wifitool.backend.ProfileApi
import de.gwdg.wifitool.databinding.FragmentOrganizationBinding
import de.gwdg.wifitool.frontend.activities.MainActivity
import de.gwdg.wifitool.frontend.adapters.IdentityProviderArrayAdapter
import de.gwdg.wifitool.frontend.adapters.ProfileArrayAdapter

class OrganizationFragment : Fragment() {
    private lateinit var binding: FragmentOrganizationBinding

    private lateinit var identityProviderArrayAdapter: IdentityProviderArrayAdapter
    private lateinit var profileApi: ProfileApi


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        this.binding = FragmentOrganizationBinding.inflate(inflater, container, false)

        profileApi = ProfileApi(activity!!.applicationContext)
        initIdentityProviderListView()
        initIdentityProviderSearchBox()

        return this.binding.root
    }


    // TODO: refactor start
    /**
     * Initializes list of all identity providers obtained by the [ProfileApi]
     */
    private fun initIdentityProviderListView() {
        identityProviderArrayAdapter =
            IdentityProviderArrayAdapter(
                activity!!.baseContext,
                R.layout.simple_list_item_1
            )

        binding.identityProviderListView.adapter = identityProviderArrayAdapter


        // TODO: select on click and show next button
        binding.identityProviderListView.setOnItemClickListener { _, _, position, _ ->
            val item = identityProviderArrayAdapter.getItem(position)
            (activity as MainActivity).allowNext()

        }

        profileApi.getAllIdentityProviders()
            .observe(this, Observer { identityProviders ->
                identityProviderArrayAdapter.setIdentityProviders(identityProviders)
            })
    }

    private fun initIdentityProviderSearchBox() {
        binding.identitySearchEditText.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                identityProviderArrayAdapter.filter.filter(s)
            }

            // do nothing
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            // do nothing
            override fun afterTextChanged(s: Editable?) {}
        })
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