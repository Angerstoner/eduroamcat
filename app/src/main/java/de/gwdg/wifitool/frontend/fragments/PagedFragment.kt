package de.gwdg.wifitool.frontend.fragments

import androidx.fragment.app.Fragment
import de.gwdg.wifitool.frontend.activities.MainActivity

abstract class PagedFragment : Fragment() {
    private val logTag = "PagedFragment (Base)"
    private var parentActivity: MainActivity? = null


    public fun setParentActivity(activity: MainActivity) {
        parentActivity = activity
    }

    public fun blockNext() {
        parentActivity?.blockNext()
    }

    public fun allowNext() {
        parentActivity?.allowNext()
    }


}