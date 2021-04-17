package de.gwdg.wifitool.frontend.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.gwdg.wifitool.databinding.FragmentCredentialsBinding
import de.gwdg.wifitool.frontend.activities.MainActivity

class CredentialFragment : Fragment() {
    private lateinit var binding: FragmentCredentialsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        this.binding = FragmentCredentialsBinding.inflate(inflater, container, false)
        (activity as MainActivity).allowNext()
        return this.binding.root
    }
}