package de.gwdg.wifitool.frontend.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.gwdg.wifitool.R
import de.gwdg.wifitool.databinding.FragmentOrganizationProfileBinding

class OrganizationProfileFragment : Fragment() {
    private lateinit var binding: FragmentOrganizationProfileBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        this.binding = FragmentOrganizationProfileBinding.inflate(inflater, container, false)


        return this.binding.root
    }
}