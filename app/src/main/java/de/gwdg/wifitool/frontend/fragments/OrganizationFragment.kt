package de.gwdg.wifitool.frontend.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ListView
import de.gwdg.wifitool.R
import de.gwdg.wifitool.backend.ProfileApi
import de.gwdg.wifitool.backend.models.IdentityProvider
import de.gwdg.wifitool.databinding.FragmentOrganizationBinding
import de.gwdg.wifitool.frontend.activities.MainActivity
import de.gwdg.wifitool.frontend.adapters.IdentityProviderArrayAdapter
import java.lang.NullPointerException

const val SEARCH_INPUT_THRESHOLD = 2
const val SEARCH_DROPDOWN_HEIGHT_HIDDEN = 0
const val SEARCH_DROPDOWN_HEIGHT_VISIBLE_MAX = 400
const val SEARCH_DROPDOWN_HEIGHT_VISIBLE_ITEM = 105

class OrganizationFragment : Fragment() {
    private val logTag = "OrganizationFragment"
    private lateinit var binding: FragmentOrganizationBinding

    private lateinit var parentActivity: MainActivity
    private lateinit var identityProviderArrayAdapter: IdentityProviderArrayAdapter
    private lateinit var profileApi: ProfileApi


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        this.binding = FragmentOrganizationBinding.inflate(inflater, container, false)

        try {
            parentActivity = activity as MainActivity
            profileApi = ProfileApi(parentActivity.applicationContext)
            initAutoCompleteSearch()
        } catch (e: NullPointerException) {
            Log.e(logTag, "Context/Activity missing, could not init Fragment. \n${e.stackTrace}")
        }

        return this.binding.root
    }

    // TODO: move to custom search view class
    private fun initAutoCompleteSearch() {
        identityProviderArrayAdapter =
            IdentityProviderArrayAdapter(parentActivity.baseContext, android.R.layout.simple_list_item_1)
        binding.identitySearchEditText.setAdapter(identityProviderArrayAdapter)

        binding.identitySearchEditText.setOnItemClickListener { _, _, position, _ ->
            Log.i(logTag, "Click on $position")
            onIdentityProviderClick(position)
        }
        profileApi.getAllIdentityProviders().observe(this, { identityProviders ->
            identityProviderArrayAdapter.setIdentityProviders(identityProviders)
            initSavedIdentityProvider(loadIdentityProviderId())
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
                }


                // do nothing
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                // do nothing
                override fun afterTextChanged(s: Editable?) {}
            })
        }

    }

//
//
//    /**
//     * Initializes EditText which can search the Identity Provider list
//     */
//    private fun initIdentityProviderSearchBox() {
//        binding.identitySearchEditText.addTextChangedListener(object : TextWatcher {
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                identityProviderArrayAdapter.filter.filter(s)
//            }
//
//            // do nothing
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//
//            // do nothing
//            override fun afterTextChanged(s: Editable?) {}
//        })
//    }

    private fun initSavedIdentityProvider(identityProviderId: Long) {
        if (identityProviderId != -1L) {
            identityProviderArrayAdapter.moveIdentityProviderWithIdToTop(identityProviderId)
            // TODO: highlight/select first item of list
        } else {
            Log.i(logTag, "No saved Identity Provider found. Starting App for first use.")
        }
    }


    /**
     * Called when clicking on an Identity Provider from the list
     */
    private fun onIdentityProviderClick(pos: Int) {
        val idp = identityProviderArrayAdapter.getItem(pos)
        saveIdentityProvider(idp)
        parentActivity.allowNext()
    }


    private fun loadIdentityProviderId(): Long {
        val sharedPref =
            parentActivity.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        return sharedPref.getLong(getString(R.string.preference_identity_provider_id), -1L)
    }

    /**
     * Stores selected Identity Provider to app preferences.
     *
     * Value used in [ProfileFragment]
     * TODO: implement checking and loading of previously saved IdPs when loading the OrganizationFragment
     */
    private fun saveIdentityProvider(idp: IdentityProvider) {
        val sharedPref =
            parentActivity.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putLong(getString(R.string.preference_identity_provider_id), idp.entityId)
            putString(getString(R.string.preference_identity_provider_name), idp.title)
            apply()
        }
    }
}