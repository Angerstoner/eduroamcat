package de.gwdg.wifitool.frontend.components

import android.content.Context
import android.content.ContextWrapper
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.android.volley.Response
import de.gwdg.wifitool.R
import de.gwdg.wifitool.backend.ProfileApi
import de.gwdg.wifitool.backend.models.IdentityProvider
import de.gwdg.wifitool.frontend.PREFERENCE_FILE_KEY
import de.gwdg.wifitool.frontend.PREFERENCE_IDENTITY_PROVIDER_COUNTRY
import de.gwdg.wifitool.frontend.PREFERENCE_IDENTITY_PROVIDER_ID
import de.gwdg.wifitool.frontend.PREFERENCE_IDENTITY_PROVIDER_NAME
import de.gwdg.wifitool.frontend.activities.MainActivity
import de.gwdg.wifitool.frontend.adapters.IdentityProviderArrayAdapter


const val SEARCH_INPUT_THRESHOLD = 2
const val SEARCH_DROPDOWN_HEIGHT_HIDDEN = 0

class IdentityProviderSearch @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) :
    ConstraintLayout(context, attrs, defStyle) {

    private val logTag = "IdentityProviderSearch"
    private val identitySearchEditText: AutoCompleteTextView
    private lateinit var identityProviderArrayAdapter: IdentityProviderArrayAdapter


    init {
        inflate(context, R.layout.view_identity_provider_search, this)
        identitySearchEditText = findViewById(R.id.identitySearchEditText)
        initAutoCompleteSearch()
    }

    // TODO: move to custom search view class
    private fun initAutoCompleteSearch() {
        identityProviderArrayAdapter =
            IdentityProviderArrayAdapter(context, R.layout.search_dropdown_item)
        identitySearchEditText.setAdapter(identityProviderArrayAdapter)


        identitySearchEditText.setOnItemClickListener { _, _, position, id ->
            Log.i(logTag, "Click on $position with id $id")
            onIdentityProviderClick(position)
        }

        with(identitySearchEditText) {
            addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // only filter user input, not auto completion-input
                    if (!isPerformingCompletion) {
                        // tried with internal threshold of AutoCompleteTextView,
                        // wont work because internal threshold seems to be always 1, even when explicitly set
                        dropDownHeight = if (s != null && s.length >= SEARCH_INPUT_THRESHOLD) {
                            identityProviderArrayAdapter.filter.filter(s)
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        } else {
                            SEARCH_DROPDOWN_HEIGHT_HIDDEN
                        }
                        getActivity()?.blockNext()
                    }
                }


                // do nothing
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                // do nothing
                override fun afterTextChanged(s: Editable?) {}
            })
        }

    }

    //    TODO: move to viewModel, start with app start
    fun observeIdentityProviders(parent: Fragment, profileApi: ProfileApi, errorListener: Response.ErrorListener) {
        profileApi.getAllIdentityProviders(errorListener).observe(parent) { identityProviders ->
            identityProviderArrayAdapter.setIdentityProviders(identityProviders)
        }
        (context as ContextWrapper).baseContext
    }

    /**
     * Called when clicking on an Identity Provider from the list
     */
    private fun onIdentityProviderClick(pos: Int) {
        val idp = identityProviderArrayAdapter.getItem(pos)
        Log.i(logTag, "Clicked $idp")
        saveIdentityProvider(idp)
        getActivity()?.allowNext()
    }

    /**
     *
     * TODO: set via observer in fragment
     * Stores selected Identity Provider to app preferences.
     *
     * Value used in [de.gwdg.wifitool.frontend.fragments.ProfileFragment]
     */
    private fun saveIdentityProvider(idp: IdentityProvider) {
        val sharedPref =
            getActivity()?.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
        with(sharedPref?.edit()) {
            this?.putLong(PREFERENCE_IDENTITY_PROVIDER_ID, idp.entityId)
            this?.putString(PREFERENCE_IDENTITY_PROVIDER_NAME, idp.title)
            this?.putString(PREFERENCE_IDENTITY_PROVIDER_COUNTRY, idp.country)
            this?.apply()
        }
    }

    fun setText(text: String) {
        identitySearchEditText.setText(text)
    }

    private fun getActivity(): MainActivity? {
        var context = context
        while (context is ContextWrapper) {
            if (context is MainActivity) {
                return context
            }
            context = context.baseContext
        }
        return null
    }

}