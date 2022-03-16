package de.gwdg.wifitool.frontend.fragments

import android.Manifest
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
import kotlin.math.log

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

    override fun onResume() {
        val nextAction = {
            if (WifiSettingsDialog().isNeeded(parentActivity)) {
                Log.i(logTag, "Setting next action to show delete wifi dialog")
                WifiSettingsDialog().show(childFragmentManager, null)
            }
        }
        parentActivity.addNextButtonAction(nextAction)
        super.onResume()
    }
}