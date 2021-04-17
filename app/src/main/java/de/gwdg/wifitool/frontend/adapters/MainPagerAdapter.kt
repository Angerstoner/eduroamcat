package de.gwdg.wifitool.frontend.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import de.gwdg.wifitool.frontend.fragments.CredentialFragment
import de.gwdg.wifitool.frontend.fragments.FeedbackFragment
import de.gwdg.wifitool.frontend.fragments.OrganizationFragment
import de.gwdg.wifitool.frontend.fragments.ProfileFragment

class MainPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    private val organizationFragment = OrganizationFragment()
    private val profileFragment = ProfileFragment()
    private val credentialFragment = CredentialFragment()
    private val feedbackFragment = FeedbackFragment()


    override fun getItemCount(): Int {
        return 4
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> organizationFragment
            1 -> profileFragment
            2 -> credentialFragment
            3 -> feedbackFragment
            else -> throw IllegalStateException("Position $position is not supported.");
        }
    }
}