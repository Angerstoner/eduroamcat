package de.gwdg.wifitool.frontend.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.gwdg.wifitool.databinding.FragmentWelcomeBinding
import de.gwdg.wifitool.frontend.components.WifiSettingsDialog


class WelcomeFragment : PagedFragment() {
    private lateinit var binding: FragmentWelcomeBinding
    private val logTag = "WelcomeFragment"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        this.binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        allowNext()
        return this.binding.root
    }


    override fun onResume() {
        val nextAction = {
            if (WifiSettingsDialog().isNeeded(parentActivity)) {
                Log.i(logTag, "Setting next action to show delete wifi dialog")
                WifiSettingsDialog().show(childFragmentManager, null)
            }
        }
        addNextButtonAction(nextAction)
        super.onResume()
    }
}