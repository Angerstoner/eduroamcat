package de.gwdg.wifitool.frontend.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.gwdg.wifitool.databinding.FragmentWelcomeBinding
import de.gwdg.wifitool.frontend.activities.MainActivity

class WelcomeFragment : Fragment() {
    // TODO: explain that location permission is needed on android 9 and lower for removing old configs
    // TODO: ask for location permission if android 9 or lower

    private lateinit var binding: FragmentWelcomeBinding
    private lateinit var parentActivity: MainActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        this.binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        parentActivity = activity as MainActivity
        parentActivity.allowNext()
        return this.binding.root
    }

}