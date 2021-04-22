package de.gwdg.wifitool.frontend.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import de.gwdg.wifitool.R
import de.gwdg.wifitool.backend.ProfileApi
import de.gwdg.wifitool.backend.models.Profile
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
    private var profileDownloadFinished = false
    private var profileDownloadPath = ""


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        this.binding = FragmentCredentialsBinding.inflate(inflater, container, false)
        try {
            parentActivity = activity as MainActivity
            profileApi = ProfileApi(parentActivity.applicationContext)
            initEditTexts()
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
            startProfileConfigDownload()
        } else {
            Log.e(logTag, "Invalid Profile. Cannot continue.")
        }
        super.onResume()
    }

    private fun startProfileConfigDownload() {
        val filename = getConfigFilename(profileId)
        val onDownloadFinished: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                profileDownloadPath = parentActivity.getExternalFilesDir(null).toString().plus("/").plus(filename)
                profileDownloadFinished = true
                updateNextButton()
            }
        }
        profileApi.downloadProfileConfig(profileId, filename, onDownloadFinished)
    }

    private val credentialTextChangeListener = object : TextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            updateNextButton()
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

    private fun updateNextButton() {
        if (isConnectAllowed()) {
            parentActivity.allowNext()
        } else {
            if (binding.usernameEditText.text.toString() != ""
                && binding.passwordEditText.text.toString() != ""
            ) {
                Toast.makeText(parentActivity, "Please wait, Profile download is not finished yet", Toast.LENGTH_LONG)
                    .show()
            }
            parentActivity.blockNext()
        }
    }

    private fun isConnectAllowed(): Boolean {
        return binding.usernameEditText.text.toString() != ""
                && binding.passwordEditText.text.toString() != ""
                && profileDownloadFinished
    }

    private fun loadProfileId(): Long {
        val sharedPref =
            parentActivity.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        return sharedPref.getLong(getString(R.string.preference_profile_id), -1L)
    }

    private fun getConfigFilename(profileId: Long): String {
        val sharedPref =
            parentActivity.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val identityProviderName = sharedPref.getString(getString(R.string.preference_identity_provider_name), "")
        return "eduroam-${identityProviderName}_${profileId}.eap-config"
            .replace("[<>:\"/\\\\|?*, ]".toRegex(), "_")
    }

}