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

const val REQUEST_CODE_LOCATION_PERMISSION = 101
val PERMISSION_ARRAY_LOCATION = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)


class WelcomeFragment : Fragment() {
    private lateinit var binding: FragmentWelcomeBinding
    private lateinit var parentActivity: MainActivity
    private val logTag = "WelcomeFragment"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        this.binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        parentActivity = activity as MainActivity
        parentActivity.allowNext()
        return this.binding.root
    }

    override fun onResume() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (hasPreviousEduroamConfig()) {
                binding.locationInfoTextView.visibility = View.VISIBLE
                parentActivity.addNextButtonAction { requestAppPermissions() }
            }
        }
        super.onResume()
    }

    /**
     * Checks if an eduroam connection was already added in the past
     *
     * see [WifiConfig.hasEduroamConfiguration]
     */
    private fun hasPreviousEduroamConfig(): Boolean {
        return WifiConfig(parentActivity).hasEduroamConfiguration()
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
            if (grantResults.size == 1 && grantResults.contains(-1)) {
                Log.i(logTag, "Location permission not granted. User has to delete eduroam network.")
                //TODO: show dialog to user with options to open wifi settings
            }
        }
    }
}