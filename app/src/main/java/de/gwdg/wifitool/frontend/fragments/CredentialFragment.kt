package de.gwdg.wifitool.frontend.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.gwdg.wifitool.R
import de.gwdg.wifitool.backend.ProfileApi
import de.gwdg.wifitool.databinding.FragmentCredentialsBinding
import de.gwdg.wifitool.databinding.FragmentProfileBinding
import de.gwdg.wifitool.frontend.activities.MainActivity
import java.lang.NullPointerException

class CredentialFragment : Fragment() {
    private val logTag = "CredentialFragment"
    private lateinit var parentActivity: MainActivity
    private lateinit var profileApi: ProfileApi
    private lateinit var binding: FragmentCredentialsBinding
    private var profileId = -1L


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        this.binding = FragmentCredentialsBinding.inflate(inflater, container, false)
        try {
            parentActivity = activity as MainActivity
            profileApi = ProfileApi(parentActivity.applicationContext)
            profileId = loadProfileId()
            if (profileId != -1L) {
                Log.i(logTag, "Downloading profile $profileId")
                //TODO: download eap-config for profile to device
//                profileApi.downloadProfileConfig()
                initEditTexts()
            } else {
                Log.e(logTag, "Invalid Profile. Cannot continue.")
            }
        } catch (e: NullPointerException) {
            Log.e(logTag, "Context/Activity missing, could not init Fragment.\n ${e.stackTrace}")
        }
        return this.binding.root
    }

    override fun onResume() {
        profileId = loadProfileId()
        if (profileId != -1L) {
            Log.i(logTag, "Downloading profile $profileId")
            // TODO: download eap-config for profile to device
            // profileApi.downloadProfileConfig()
        } else {
            Log.e(logTag, "Invalid Profile. Cannot continue.")
        }
        super.onResume()
    }

    private val credentialTextChangeListener = object : TextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (binding.usernameEditText.text.toString() != "" && binding.passwordEditText.text.toString() != "") {
                parentActivity.allowNext()
            } else {
                parentActivity.blockNext()
            }
        }

        // do nothing
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        // do nothing
        override fun afterTextChanged(s: Editable?) {}
    }

    private fun initEditTexts() {
        binding.usernameEditText.addTextChangedListener(credentialTextChangeListener)
        binding.passwordEditText.addTextChangedListener(credentialTextChangeListener)
    }


    private fun loadProfileId(): Long {
        return 1L
    }
}