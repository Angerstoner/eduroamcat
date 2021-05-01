package de.gwdg.wifitool.frontend.fragments

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.gwdg.wifitool.backend.WifiConfig
import de.gwdg.wifitool.databinding.FragmentWelcomeBinding
import de.gwdg.wifitool.frontend.activities.MainActivity
import de.gwdg.wifitool.frontend.components.WifiSettingsDialog

const val REQUEST_CODE_LOCATION_PERMISSION = 101
val PERMISSION_ARRAY_LOCATION = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

class WelcomeFragment : Fragment() {
    private lateinit var binding: FragmentWelcomeBinding
    private lateinit var parentActivity: MainActivity
    private lateinit var wifiConfig: WifiConfig
    private val logTag = "WelcomeFragment"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        this.binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        parentActivity = activity as MainActivity
        wifiConfig = WifiConfig(parentActivity)
        parentActivity.allowNext()
        return this.binding.root
    }

    //TODO: test this
    override fun onResume() {
        binding.oldConfigInfoText.visibility = if (hasPreviousEduroamConfig()) View.VISIBLE else View.GONE
        val nextAction = {
            if (userHasToDeleteConfig()) {
                // users has to delete this existing config by themselves
                requestAppPermissions()
                WifiSettingsDialog().show(childFragmentManager, null)
            } else {
                // ask only for location permission to obtain feedback in the end
                requestAppPermissions()
            }
        }
        parentActivity.addNextButtonAction(nextAction)
        super.onResume()
    }


    private fun userHasToDeleteConfig(): Boolean {
        return when {
            Build.VERSION.SDK_INT <= Build.VERSION_CODES.P -> {
                // warn user if an existing eduroam connection was detected
                // users has to delete this existing config by themselves
                if (hasPreviousEduroamConfig()) {
                    true
                } else {
                    return !wifiConfig.isWifiEnabled()
                }
            }
            Build.VERSION.SDK_INT == Build.VERSION_CODES.Q -> {
                // on Android 10 the app can not detect if an eduroam config exists and
                // therefore needs to show this to every user
                true
            }
            else -> {
                // on Android 11 the app can overwrite existing eduroam connections
                false
            }
        }
    }


    /**
     * Checks if an eduroam connection was already added in the past
     *
     * see [WifiConfig.hasEduroamConfiguration]
     */
    private fun hasPreviousEduroamConfig(): Boolean {
        return wifiConfig.hasEduroamConfiguration()
    }


    /**
     * Requests needed permissions if Android M or higher is used
     * Android L and lower use only the AndroidManifest to grant permissions
     */
    private fun requestAppPermissions() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(PERMISSION_ARRAY_LOCATION, REQUEST_CODE_LOCATION_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION && permissions.contentEquals(PERMISSION_ARRAY_LOCATION)) {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                if (grantResults.size == 1 && grantResults.contains(-1)) {
                    Log.i(logTag, "Location permission not granted. User has to delete eduroam network.")
                    WifiSettingsDialog().show(childFragmentManager, null)
                }
            }
        }
    }
}