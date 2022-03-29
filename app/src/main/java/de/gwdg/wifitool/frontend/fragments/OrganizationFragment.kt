package de.gwdg.wifitool.frontend.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.android.volley.Response
import de.gwdg.wifitool.backend.ProfileApi
import de.gwdg.wifitool.backend.models.IdentityProvider
import de.gwdg.wifitool.databinding.FragmentOrganizationBinding
import de.gwdg.wifitool.frontend.PREFERENCE_FILE_KEY
import de.gwdg.wifitool.frontend.PREFERENCE_IDENTITY_PROVIDER_COUNTRY
import de.gwdg.wifitool.frontend.PREFERENCE_IDENTITY_PROVIDER_ID
import de.gwdg.wifitool.frontend.PREFERENCE_IDENTITY_PROVIDER_NAME
import de.gwdg.wifitool.frontend.activities.MainActivity
import de.gwdg.wifitool.frontend.components.IdentityProviderDownloadErrorDialog


class OrganizationFragment : Fragment() {
    private val logTag = "OrganizationFragment"
    private lateinit var binding: FragmentOrganizationBinding

    private lateinit var parentActivity: MainActivity
    private lateinit var profileApi: ProfileApi
    private var savedIdentityProvider: IdentityProvider? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        this.binding = FragmentOrganizationBinding.inflate(inflater, container, false)

        try {
            parentActivity = activity as MainActivity
            profileApi = parentActivity.profileApi
        } catch (e: NullPointerException) {
            Log.e(logTag, "Context/Activity missing, could not init Fragment. \n${e.stackTrace}")
        }

        return this.binding.root
    }

    override fun onResume() {
        savedIdentityProvider = loadIdentityProvider()
        savedIdentityProvider?.let { initSavedIdentityProvider(it) }
        binding.identityProviderSearch.observeIdentityProviders(this, profileApi, onIdentityProviderListDownloadError)
        super.onResume()
    }


    private val onIdentityProviderListDownloadError =
        Response.ErrorListener {
            IdentityProviderDownloadErrorDialog().show(childFragmentManager, null)
            parentActivity.blockNext()
            Log.e(logTag, "Error Error Error")
        }


    // TODO: either move this to searchview or move searchview save-methods here
    // or even to viewmodel
    private fun initSavedIdentityProvider(identityProvider: IdentityProvider) {
        binding.identityProviderSearch.setText(identityProvider.toString())
        parentActivity.allowNext()
    }


    private fun loadIdentityProvider(): IdentityProvider? {
        val sharedPref =
            parentActivity.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
        with(sharedPref) {
            val id = getLong(PREFERENCE_IDENTITY_PROVIDER_ID, -1L)
            val title = getString(PREFERENCE_IDENTITY_PROVIDER_NAME, null) ?: ""
            val country = getString(PREFERENCE_IDENTITY_PROVIDER_COUNTRY, null) ?: ""
            if (id != -1L && title != "") {
                return IdentityProvider(id, country, title)
            }
        }
        Log.i(logTag, "No saved Identity Provider found. Starting App for first use.")
        return null
    }


}