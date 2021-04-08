package de.gwdg.wifitool.frontend.activities

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiEnterpriseConfig
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import de.gwdg.wifitool.backend.ADD_WIFI_NETWORK_SUGGESTION_REQUEST_CODE
import de.gwdg.wifitool.backend.ProfileApi
import de.gwdg.wifitool.backend.WifiConfig
import de.gwdg.wifitool.backend.models.IdentityProvider
import de.gwdg.wifitool.backend.models.Profile
import de.gwdg.wifitool.backend.util.EapConfigParser
import de.gwdg.wifitool.backend.util.WifiEnterpriseConfigurator
import de.gwdg.wifitool.databinding.ActivityMainBinding
import de.gwdg.wifitool.frontend.adapters.IdentityProviderArrayAdapter
import de.gwdg.wifitool.frontend.adapters.ProfileArrayAdapter


class MainActivity : AppCompatActivity() {
    private val logTag = "MainActivity"
    private val profileApi = ProfileApi(this)
    private lateinit var binding: ActivityMainBinding
    private lateinit var identityProviderArrayAdapter: IdentityProviderArrayAdapter
    private lateinit var profileArrayAdapter: ProfileArrayAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

//        requestAppPermissions()
        initIdentityProviderListView()
        initIdentityProviderSearchBox()
        initProfileSelectionSpinner()
        initConnectButton()

//        downloadAndParseTest()
    }

    /**
     * Requests needed permissions if Android M or higher is used
     * Android L and lower use only the AndroidManifest to grant permissions
     */
    private fun requestAppPermissions() {
        val permissionsNeeded = arrayOf(ACCESS_FINE_LOCATION)
        val permissionsRequestCode = 1

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissionsNeeded, permissionsRequestCode)
        }
    }

    /**
     * Initializes list of all identity providers obtained by the [ProfileApi]
     */
    private fun initIdentityProviderListView() {
        identityProviderArrayAdapter =
            IdentityProviderArrayAdapter(
                this,
                android.R.layout.simple_list_item_1
            )

        binding.identityProviderListView.adapter = identityProviderArrayAdapter

        binding.identityProviderListView.setOnItemClickListener { _, _, position, _ ->
            val item = identityProviderArrayAdapter.getItem(position)
            identityProviderArrayAdapter.filter.filter(item.toString())
            showProfilesForIdentityProvider(item)
        }

        ProfileApi(this).getAllIdentityProviders()
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

    private fun initProfileSelectionSpinner() {
        profileArrayAdapter = ProfileArrayAdapter(
            this,
            android.R.layout.simple_spinner_item
        )
        binding.profileSpinner.adapter = profileArrayAdapter
    }

    private fun initConnectButton() {
        binding.connectButton.setOnClickListener {
            val selectedProfile = binding.profileSpinner.selectedItem as Profile

            val filename = getFilenameForProfile(selectedProfile)
            val onDownloadFinished: BroadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    val filenameWithPath = getExternalFilesDir(null).toString().plus("/").plus(filename)
                    connectToWifi(filenameWithPath)
                }
            }
            profileApi.downloadProfileConfig(selectedProfile, filename, onDownloadFinished)
        }
    }

    private fun showProfilesForIdentityProvider(identityProvider: IdentityProvider) {
        ProfileApi(this).getIdentityProviderProfiles(identityProvider)
            .observe(this, Observer { profiles ->
                profileArrayAdapter.setProfiles(profiles)
                binding.profileSpinner.visibility = if (profileArrayAdapter.count == 0) GONE else VISIBLE
                binding.profileSpinner.isEnabled = (profileArrayAdapter.count > 1)
                binding.hiddenConstraintLayout.visibility =
                    if (profileArrayAdapter.count == 0) GONE else VISIBLE
            })
    }

    private fun connectToWifi(configFilename: String) {
        val wifiEnterpriseConfigurator = WifiEnterpriseConfigurator()
        val configParser = EapConfigParser(configFilename)

        val enterpriseConfig = wifiEnterpriseConfigurator.getConfigFromFile(configParser).first()
        if (enterpriseConfig.eapMethod != WifiEnterpriseConfig.Eap.PWD)
            enterpriseConfig.identity = binding.usernameEditText.text.toString()
        enterpriseConfig.password = binding.passwordEditText.text.toString()

        val ssid = configParser.getSsidPairs()

        val wifiConfig = WifiConfig(this)
        wifiConfig.connectToEapNetwork(enterpriseConfig, ssid)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ADD_WIFI_NETWORK_SUGGESTION_REQUEST_CODE){
            Log.i(logTag, "Result was $resultCode")
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
    
    //TODO: move to util as this could be static
    private fun getFilenameForProfile(profile: Profile): String {
        return "eduroam-${profile.identityProvider}_${profile.profileId}_.eap-config"
            .replace("[<>:\"/\\\\|?*, ]".toRegex(), "_")
    }

}


