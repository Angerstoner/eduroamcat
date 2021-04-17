package de.gwdg.wifitool.frontend.activities

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import de.gwdg.wifitool.R
import de.gwdg.wifitool.databinding.ActivityMainBinding
import de.gwdg.wifitool.frontend.adapters.MainPagerAdapter


//    private lateinit var identityProviderArrayAdapter: IdentityProviderArrayAdapter
//    private val profileApi = ProfileApi(this)
//    private lateinit var profileArrayAdapter: ProfileArrayAdapter

class MainActivity : AppCompatActivity() {
    private val logTag = "MainActivity"
    private lateinit var binding: ActivityMainBinding
    private lateinit var pagerAdapter: MainPagerAdapter
    private lateinit var dotImageViews: Array<ImageView>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        initPager()
        bindDots()
        bindNavigationButtons()

//        requestAppPermissions()
//        initIdentityProviderListView()
//        initIdentityProviderSearchBox()
//        initProfileSelectionSpinner()
//        initConnectButton()

//        downloadAndParseTest()
    }

    private fun bindNavigationButtons() {
        binding.nextButton.setOnClickListener { goNext() }
        binding.backButton.setOnClickListener { goBack() }
    }

    private fun initPager() {
        pagerAdapter = MainPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter
        binding.viewPager.isUserInputEnabled = false
    }


    private fun bindDots() {
        this.dotImageViews = arrayOf(
            binding.dot1ImageView,
            binding.dot2ImageView,
            binding.dot3ImageView,
            binding.dot4ImageView
        )
        highlightDot(binding.viewPager.currentItem)
        binding.viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                highlightDot(position)
            }
        })
    }

    private fun highlightDot(position: Int) {
        for (index in dotImageViews.indices) {
            if (index == position) {
                dotImageViews[index].setImageResource(R.drawable.dot)
                dotImageViews[index].animate().alpha(0.7f).scaleX(1.2f).scaleY(1.2f)
            } else {
                dotImageViews[index].setImageResource(R.drawable.dot_outline)
                dotImageViews[index].animate().alpha(0.5f).scaleX(1f).scaleY(1f)
            }
        }
    }

    fun allowNext() {
        binding.nextButton.visibility = View.VISIBLE
    }

    fun blockNext() {
        binding.nextButton.visibility = View.GONE
    }

    fun allowBack() {
        binding.backButton.visibility = View.VISIBLE
    }

    fun blockBack() {
        binding.backButton.visibility = View.GONE
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

    private fun goNext() {
        binding.viewPager.currentItem += 1
        allowBack()
//        blockNext()
    }

    private fun goBack() {
        if (binding.viewPager.currentItem == 0) {
            return
        }
        binding.viewPager.currentItem -= 1
        allowNext()
    }


//    private fun initConnectButton() {
//        binding.connectButton.setOnClickListener {
//            val selectedProfile = binding.profileSpinner.selectedItem as Profile
//
//            val filename = getFilenameForProfile(selectedProfile)
//            val onDownloadFinished: BroadcastReceiver = object : BroadcastReceiver() {
//                override fun onReceive(context: Context?, intent: Intent?) {
//                    val filenameWithPath = getExternalFilesDir(null).toString().plus("/").plus(filename)
//                    connectToWifi(filenameWithPath)
//                }
//            }
//            profileApi.downloadProfileConfig(selectedProfile, filename, onDownloadFinished)
//        }
//    }
//

//
//    private fun connectToWifi(configFilename: String) {
//        val wifiEnterpriseConfigurator = WifiEnterpriseConfigurator()
//        val configParser = EapConfigParser(configFilename)
//
//        val enterpriseConfig = wifiEnterpriseConfigurator.getConfigFromFile(configParser).first()
//        if (enterpriseConfig.eapMethod != WifiEnterpriseConfig.Eap.PWD)
//            enterpriseConfig.identity = binding.usernameEditText.text.toString()
//        enterpriseConfig.password = binding.passwordEditText.text.toString()
//
//        val ssid = configParser.getSsidPairs()
//
//        val wifiConfig = WifiConfig(this)
//        wifiConfig.connectToEapNetwork(enterpriseConfig, ssid)
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        if (requestCode == ADD_WIFI_NETWORK_SUGGESTION_REQUEST_CODE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            Log.i(logTag, "Result was $resultCode")
//            if (resultCode == RESULT_OK && data != null && data.hasExtra(EXTRA_WIFI_NETWORK_RESULT_LIST)) {
//                val addWifiNetworkResultList = data.getIntegerArrayListExtra(EXTRA_WIFI_NETWORK_RESULT_LIST)!!
//                addWifiNetworkResultList.forEachIndexed { i, it ->
//                    Log.i(logTag, "Network result for $i was $it")
//                }
//            }
//        }
//        super.onActivityResult(requestCode, resultCode, data)
//    }
//
//    //TODO: move to util as this could be static
//    private fun getFilenameForProfile(profile: Profile): String {
//        return "eduroam-${profile.identityProvider}_${profile.profileId}_.eap-config"
//            .replace("[<>:\"/\\\\|?*, ]".toRegex(), "_")
//    }

}


