package de.gwdg.wifitool.frontend.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import de.gwdg.wifitool.frontend.fragments.*

class MainPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    private val welcomeFragment = WelcomeFragment()
    private val organizationFragment = OrganizationFragment()
    private val profileFragment = ProfileFragment()
    private val credentialFragment = CredentialFragment()
    private val feedbackFragment = FeedbackFragment()


    override fun getItemCount(): Int {
        return 5
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> welcomeFragment
            1 -> organizationFragment
            2 -> profileFragment
            3 -> credentialFragment
            4 -> feedbackFragment
            else -> throw IllegalStateException("Position $position is not supported.");
        }
    }
}