package de.gwdg.wifitool.frontend.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import de.gwdg.wifitool.frontend.fragments.OrganizationProfileFragment

class MainPagerAdapter(private val fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    private val organizationProfileFragment = OrganizationProfileFragment()


    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0, 1, 2 -> organizationProfileFragment
            else -> throw IllegalStateException("Position $position is not supported.");
        }
    }
}