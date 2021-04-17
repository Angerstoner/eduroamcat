package de.gwdg.wifitool.frontend.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.gwdg.wifitool.databinding.FragmentFeedbackBinding

class FeedbackFragment : Fragment() {
    private lateinit var binding: FragmentFeedbackBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        this.binding = FragmentFeedbackBinding.inflate(inflater, container, false)
        return this.binding.root
    }
}