package de.gwdg.wifitool.frontend.fragments

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import de.gwdg.wifitool.databinding.FragmentFeedbackBinding
import de.gwdg.wifitool.frontend.activities.MainActivity
import java.lang.NullPointerException

class FeedbackFragment : Fragment() {
    private val logTag = "FeedbackFragment"
    private lateinit var binding: FragmentFeedbackBinding
    private lateinit var parentActivity: MainActivity


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        this.binding = FragmentFeedbackBinding.inflate(inflater, container, false)

        try {
            parentActivity = activity as MainActivity
        } catch (e: NullPointerException) {
            Log.e(logTag, "Context/Activity missing, could not init Fragment.\n ${e.stackTrace}")
        }

        return this.binding.root
    }

    //TODO: this is test code. remove
    override fun onResume() {
        val wifiManager = parentActivity.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        Thread.sleep(2000)
        wifiManager.connectionInfo
        super.onResume()
    }
}