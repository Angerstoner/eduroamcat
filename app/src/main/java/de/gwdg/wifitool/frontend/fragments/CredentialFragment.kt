package de.gwdg.wifitool.frontend.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiEnterpriseConfig
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
import de.gwdg.wifitool.backend.WifiConfig
import de.gwdg.wifitool.backend.models.Profile
import de.gwdg.wifitool.backend.models.ProfileAttributes
import de.gwdg.wifitool.backend.util.EapConfigParser
import de.gwdg.wifitool.backend.util.WifiEnterpriseConfigurator
import de.gwdg.wifitool.databinding.FragmentCredentialsBinding
import de.gwdg.wifitool.frontend.activities.MainActivity

class CredentialFragment : Fragment() {
    private val logTag = "CredentialFragment"
    private lateinit var parentActivity: MainActivity
    private lateinit var profileApi: ProfileApi
    private lateinit var binding: FragmentCredentialsBinding
    private var profile: Profile? = null
    private var profileDownloadFinished = false
    private var profileDownloadPath = ""


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        this.binding = FragmentCredentialsBinding.inflate(inflater, container, false)
        try {
            parentActivity = activity as MainActivity
            profileApi = parentActivity.profileApi
        } catch (e: NullPointerException) {
            Log.e(logTag, "Context/Activity missing, could not init Fragment.\n ${e.stackTrace}")
        }
        return this.binding.root
    }

    override fun onResume() {
        initEditTexts()
        profile = getStoredProfile()
        if (profile != null) {
            Log.i(logTag, "Downloading profile $profile")
            startProfileConfigDownload(profile!!.profileId)
            initProfileInfoBox()
            updateNextButton()
        } else if (parentActivity.eapConfigParser != null) {
            initProfileInfoBoxFromEapConfig(parentActivity.eapConfigParser!!)
            updateNextButton()
        } else {
            Log.e(logTag, "Invalid Profile. Cannot continue.")
        }
        super.onResume()
    }

    private fun startProfileConfigDownload(profileId: Long) {
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
            parentActivity.addNextButtonAction { connectToWifi(profileDownloadPath) }
            parentActivity.changeNextButtonText(getString(R.string.next_button_connect))
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

    private fun initProfileInfoBox() {
        binding.profileInformationCard.observeProfileAttributes(this, profileApi)
        binding.profileInformationCard.setOnClickListener {
            binding.profileInformationCard.openProfileInformationDialog(
                childFragmentManager
            )
        }
    }

    private fun initProfileInfoBoxFromEapConfig(eapConfigParser: EapConfigParser) {
        val identityProviderName = eapConfigParser.getProviderDisplayName()
        val identityProviderMail = eapConfigParser.getHelpdeskEmailAddress()
        val identityProviderPhone = eapConfigParser.getHelpdeskPhoneNumber()
        val identityProviderUrl = eapConfigParser.getHelpdeskWebAddress()
        val identityProviderDesc = eapConfigParser.getProviderDescription()
        val profileAttributes = ProfileAttributes(
            -1, identityProviderName,
            identityProviderMail, identityProviderPhone, identityProviderUrl, identityProviderDesc
        )
        binding.profileInformationCard.setProfileAttributes(this, profileAttributes)

    }


    private fun isConnectAllowed(): Boolean {
        return binding.usernameEditText.text.toString() != ""
                && binding.passwordEditText.text.toString() != ""
                && (profileDownloadFinished || parentActivity.eapConfigParser != null)
    }

    private fun getStoredProfile(): Profile? {
        val sharedPref =
            parentActivity.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        with(sharedPref) {
            val identityProviderName = this.getString(getString(R.string.preference_identity_provider_name), null)
            val identityProviderId = this.getLong(getString(R.string.preference_identity_provider_id), -1L)
            val profileName = this.getString(getString(R.string.preference_profile_name), null)
            val profileId = this.getLong(getString(R.string.preference_profile_id), -1L)

            if (identityProviderId != -1L && profileId != -1L && profileName != null && identityProviderName != null) {
                return Profile(profileId, profileName, identityProviderId, identityProviderName)
            }
        }
        return null
    }

    private fun getConfigFilename(profileId: Long): String {
        val sharedPref =
            parentActivity.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val identityProviderName = sharedPref.getString(getString(R.string.preference_identity_provider_name), "")
        return "eduroam-${identityProviderName}_${profileId}.eap-config"
            .replace("[<>:\"/\\\\|?*, ]".toRegex(), "_")
    }


    private fun connectToWifi(configFilename: String) {
        val wifiEnterpriseConfigurator = WifiEnterpriseConfigurator()

        if (parentActivity.eapConfigParser == null) {
            parentActivity.eapConfigParser = EapConfigParser(configFilename)
        }

        val enterpriseConfig = wifiEnterpriseConfigurator.getConfigFromFile(parentActivity.eapConfigParser!!).first()
        if (enterpriseConfig.eapMethod != WifiEnterpriseConfig.Eap.PWD)
            enterpriseConfig.identity = binding.usernameEditText.text.toString().replace(" ", "")
        enterpriseConfig.password = binding.passwordEditText.text.toString()

        val ssid = parentActivity.eapConfigParser!!.getSsidPairs()

        val wifiConfig = WifiConfig(parentActivity)

        // TODO: this value is used for communication and is only implemented as a temporary solution
        // TODO: do communication in a proper way (e.g. via ViewModel)
        parentActivity.wifiConfigResults = wifiConfig.connectToEapNetwork(enterpriseConfig, ssid)
    }
}