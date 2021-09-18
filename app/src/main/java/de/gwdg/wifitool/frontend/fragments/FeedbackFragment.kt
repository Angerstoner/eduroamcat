package de.gwdg.wifitool.frontend.fragments

import android.Manifest
import android.app.Activity.RESULT_OK
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
import android.provider.Settings.ADD_WIFI_RESULT_ADD_OR_UPDATE_FAILED
import android.provider.Settings.EXTRA_WIFI_NETWORK_RESULT_LIST
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.gwdg.wifitool.R
import de.gwdg.wifitool.backend.ADD_WIFI_NETWORK_SUGGESTION_REQUEST_CODE
import de.gwdg.wifitool.backend.WifiConfig
import de.gwdg.wifitool.backend.WifiConfig.WifiConfigResult.*
import de.gwdg.wifitool.databinding.FragmentFeedbackBinding
import de.gwdg.wifitool.frontend.activities.MainActivity


class FeedbackFragment : Fragment() {
    private val logTag = "FeedbackFragment"
    private lateinit var binding: FragmentFeedbackBinding
    private lateinit var parentActivity: MainActivity
    private var connectionTried = false


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
        val configAddResult = parentActivity.wifiConfigResults[0]
        updateNextButton(configAddResult)
        if (hasLocationPermission() &&
            listOf(ANDROID_BELOW_Q_SUCCESS, ANDROID_Q_SUCCESS, ANDROID_R_NO_RESULT).contains(configAddResult)
        ) {
            if (configAddResult == ANDROID_R_NO_RESULT) {
                // connection feedback has to be set by [onActivityResult] on Android 11+
                binding.connectionAddFeedbackTextView.text = getString(R.string.feedback_connection_add_no_result_text)
            } else {
                // connection feedback possible
                binding.connectionAddFeedbackTextView.text = getString(R.string.feedback_connection_add_success_text)
            }
            // register receivers for connection status updates
            initBroadcastReceiver()
        } else {
            // no connection feedback possible, only check WifiConfig feedback
            if (listOf(ANDROID_BELOW_Q_FAIL, ANDROID_Q_FAIL).contains(configAddResult)) {
                binding.connectionAddFeedbackTextView.text =
                    getString(R.string.feedback_connection_add_error_text)
                binding.connectionStatusFeedbackTextView.visibility = View.GONE
            } else {
                binding.connectionStatusFeedbackTextView.text =
                    getString(R.string.feedback_connection_missing_permission_text)
            }
        }

        super.onResume()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ADD_WIFI_NETWORK_SUGGESTION_REQUEST_CODE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Log.i(logTag, "Result was $resultCode")
            if (resultCode == RESULT_OK) {
                if (data != null && data.hasExtra(EXTRA_WIFI_NETWORK_RESULT_LIST)) {
                    val addWifiNetworkResultList = data.getIntegerArrayListExtra(EXTRA_WIFI_NETWORK_RESULT_LIST)!!
                    if (addWifiNetworkResultList.contains(ADD_WIFI_RESULT_ADD_OR_UPDATE_FAILED)) {
                        binding.connectionAddFeedbackTextView.text =
                            getString(R.string.feedback_connection_add_error_text)
                        updateNextButton(ANDROID_R_FAIL)
                    } else {
                        binding.connectionAddFeedbackTextView.text =
                            getString(R.string.feedback_connection_add_success_text)
                        updateNextButton(ANDROID_R_SUCCESS)
                    }
                }
            } else {
                binding.connectionAddFeedbackTextView.text =
                    getString(R.string.feedback_connection_add_user_canceled_text)
                updateNextButton(ANDROID_R_FAIL)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
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
            binding.uninstallInfoTextView.visibility = View.VISIBLE
        } else {
            if (connectionInfo.supplicantState == SupplicantState.ASSOCIATING) {
                // first phase of trying to establish connection
                if (connectionInfo.ssid == "\"eduroam\"") {
                    connectionTried = true
                }
            }

            // second/last phase of trying to establish connection
            if (connectionInfo.supplicantState == SupplicantState.COMPLETED) {
                if (connectionInfo.ssid == "\"eduroam\"") {
                    // connection successful
                    Log.i(logTag, "eduroam connection established")
                    binding.connectionStatusFeedbackTextView.text =
                        getString(R.string.feedback_connection_status_connected_text)
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                        binding.uninstallInfoTextView.visibility = View.VISIBLE
                    }
                    parentActivity.unregisterReceiver(feedbackReceiver)
                } else {
                    // connected to different wifi
                    Log.i(logTag, "eduroam connection failed. Please check availability and user data")
                    binding.connectionStatusFeedbackTextView.text =
                        getString(R.string.feedback_connection_status_disconnected_text)
                }
            } else if (connectionInfo.supplicantState == SupplicantState.DISCONNECTED && connectionTried) {
                // connection failed
                Log.i(logTag, "eduroam connection failed. Please check availability and user data")
                binding.connectionStatusFeedbackTextView.text =
                    getString(R.string.feedback_connection_status_disconnected_text)
            }
        }
    }

    private fun updateNextButton(configAddResult: WifiConfig.WifiConfigResult) {
        if (listOf(ANDROID_BELOW_Q_SUCCESS, ANDROID_Q_SUCCESS, ANDROID_R_SUCCESS).contains(configAddResult)) {
            parentActivity.addNextButtonAction { parentActivity.finishAffinity() }
            parentActivity.changeNextButtonText(getString(R.string.next_button_close))
            parentActivity.allowNext()
        } else {
            parentActivity.blockNext()
        }
    }

}