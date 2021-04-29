package de.gwdg.wifitool.frontend.fragments

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.SupplicantState
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.gwdg.wifitool.R
import de.gwdg.wifitool.databinding.FragmentFeedbackBinding
import de.gwdg.wifitool.frontend.activities.MainActivity


class FeedbackFragment : Fragment() {
    private val logTag = "FeedbackFragment"
    private lateinit var binding: FragmentFeedbackBinding
    private lateinit var parentActivity: MainActivity


    private val feedbackReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                context?.let {
                    if (intent.action == WifiManager.NETWORK_STATE_CHANGED_ACTION) {
                        val wifiManager =
                            parentActivity.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                        displayConnectionStatus(wifiManager.connectionInfo)
                    }
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        this.binding = FragmentFeedbackBinding.inflate(inflater, container, false)

        try {
            parentActivity = activity as MainActivity
        } catch (e: NullPointerException) {
            Log.e(logTag, "Context/Activity missing, could not init Fragment.\n ${e.stackTrace}")
        }

        return this.binding.root
    }

    override fun onResume() {
        // TODO: replace following workaround by a real fix
        // this is needed because the button gets re-activated by the TextWatcher in the CredentialFragment
        parentActivity.blockNext()


        if (hasLocationPermission()) {
            // connection feedback possible
            // register receivers for connection status updates
            initBroadcastReceiver()
        } else {
            // no connection feedback possible, only check WifiConfig feedback
            binding.connectionStatusFeedbackTextView.text =
                getString(R.string.feedback_connection_missing_permission_text)
            //TODO: check if connection was added successfully
        }

        super.onResume()
    }

    private fun hasLocationPermission(): Boolean {
        // permission check if Android M (6.0) or above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return parentActivity.checkSelfPermission(Manifest.permission.CHANGE_WIFI_STATE) ==
                    PackageManager.PERMISSION_GRANTED
        }
        // permission granted via manifest if below Android M
        return true
    }

    private fun initBroadcastReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        parentActivity.registerReceiver(feedbackReceiver, intentFilter)
    }

    fun displayConnectionStatus(connectionInfo: WifiInfo) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            // user has to click notification and then android 10 will decide if
            // eduroam is the best available network
            binding.connectionStatusFeedbackTextView.text =
                getString(R.string.feedback_connection_status_android_10_text)
        } else {
            if (connectionInfo.supplicantState == SupplicantState.COMPLETED) {
                if (connectionInfo.ssid == "\"eduroam\"") {
                    // connection successful
                    Log.i(logTag, "eduroam connection established")
                    binding.connectionStatusFeedbackTextView.text =
                        getString(R.string.feedback_connection_status_connected_text)
                    binding.uninstallInfoTextView.visibility = View.VISIBLE
                    parentActivity.unregisterReceiver(feedbackReceiver)
                } else {
                    // connected to different wifi
                    Log.i(logTag, "eduroam connection failed. Please check availability and user data")
                    binding.connectionStatusFeedbackTextView.text =
                        getString(R.string.feedback_connection_status_disconnected_text)
                }
            } else if (connectionInfo.supplicantState == SupplicantState.DISCONNECTED) {
                // connection failed
                Log.i(logTag, "eduroam connection failed. Please check availability and user data")
                binding.connectionStatusFeedbackTextView.text =
                    getString(R.string.feedback_connection_status_disconnected_text)
            }
        }
    }

}