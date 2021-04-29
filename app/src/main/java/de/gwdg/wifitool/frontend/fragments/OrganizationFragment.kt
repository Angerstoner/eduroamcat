package de.gwdg.wifitool.frontend.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.android.volley.Response
import de.gwdg.wifitool.R
import de.gwdg.wifitool.backend.ProfileApi
import de.gwdg.wifitool.backend.models.IdentityProvider
import de.gwdg.wifitool.databinding.FragmentOrganizationBinding
import de.gwdg.wifitool.frontend.activities.MainActivity
import de.gwdg.wifitool.frontend.adapters.IdentityProviderArrayAdapter
import de.gwdg.wifitool.frontend.components.IdentityProviderDownloadErrorDialog

const val SEARCH_INPUT_THRESHOLD = 2
const val SEARCH_DROPDOWN_HEIGHT_HIDDEN = 0

class OrganizationFragment : Fragment() {
    private val logTag = "OrganizationFragment"
    private lateinit var binding: FragmentOrganizationBinding

    private lateinit var parentActivity: MainActivity
    private lateinit var identityProviderArrayAdapter: IdentityProviderArrayAdapter
    private lateinit var profileApi: ProfileApi
    private var savedIdentityProvider: IdentityProvider? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        this.binding = FragmentOrganizationBinding.inflate(inflater, container, false)

        try {
            parentActivity = activity as MainActivity
            profileApi = parentActivity.profileApi
            initAutoCompleteSearch()
        } catch (e: NullPointerException) {
            Log.e(logTag, "Context/Activity missing, could not init Fragment. \n${e.stackTrace}")
        }

        return this.binding.root
    }

    override fun onResume() {
        savedIdentityProvider = loadIdentityProvider()
        savedIdentityProvider?.let { initSavedIdentityProvider(it) }
        super.onResume()
    }

    // TODO: move to custom search view class
    private fun initAutoCompleteSearch() {
        identityProviderArrayAdapter =
            IdentityProviderArrayAdapter(parentActivity.baseContext, android.R.layout.simple_list_item_1)
        binding.identitySearchEditText.setAdapter(identityProviderArrayAdapter)


        binding.identitySearchEditText.setOnItemClickListener { _, _, position, id ->
            Log.i(logTag, "Click on $position with id $id")
            onIdentityProviderClick(position)
        }

        //TODO: move download to welcome fragment
        profileApi.getAllIdentityProviders(onIdentityProviderListDownloadError).observe(this, { identityProviders ->
            identityProviderArrayAdapter.setIdentityProviders(identityProviders)
        })

        with(binding.identitySearchEditText) {
            addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // tried with internal threshold of AutoCompleteTextView,
                    // wont work because internal threshold seems to be always 1, even when explicitly set
                    dropDownHeight = if (s != null && s.length >= SEARCH_INPUT_THRESHOLD) {
                        identityProviderArrayAdapter.filter.filter(s)
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    } else {
                        SEARCH_DROPDOWN_HEIGHT_HIDDEN
                    }
                    parentActivity.blockNext()
                }


                // do nothing
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                // do nothing
                override fun afterTextChanged(s: Editable?) {}
            })
        }

    }

    private fun initSavedIdentityProvider(identityProvider: IdentityProvider) {
        binding.identitySearchEditText.setText(identityProvider.toString())
        parentActivity.allowNext()
    }


    /**
     * Called when clicking on an Identity Provider from the list
     */
    private fun onIdentityProviderClick(pos: Int) {
        val idp = identityProviderArrayAdapter.getClickedItem(pos)
        saveIdentityProvider(idp)
        parentActivity.allowNext()
    }


    private fun loadIdentityProvider(): IdentityProvider? {
        val sharedPref =
            parentActivity.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        with(sharedPref) {
            val id = getLong(getString(R.string.preference_identity_provider_id), -1L)
            val title = getString(getString(R.string.preference_identity_provider_name), null) ?: ""
            val country = getString(getString(R.string.preference_identity_provider_country), null) ?: ""
            if (id != -1L && title != "") {
                return IdentityProvider(id, country, title)
            }
        }
        Log.i(logTag, "No saved Identity Provider found. Starting App for first use.")
        return null
    }

    /**
     * Stores selected Identity Provider to app preferences.
     *
     * Value used in [ProfileFragment]
     */
    private fun saveIdentityProvider(idp: IdentityProvider) {
        val sharedPref =
            parentActivity.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putLong(getString(R.string.preference_identity_provider_id), idp.entityId)
            putString(getString(R.string.preference_identity_provider_name), idp.title)
            putString(getString(R.string.preference_identity_provider_country), idp.country)
            apply()
        }
    }

    private val onIdentityProviderListDownloadError =
        Response.ErrorListener {
            IdentityProviderDownloadErrorDialog().show(childFragmentManager, null)
            parentActivity.blockNext()
        }
}