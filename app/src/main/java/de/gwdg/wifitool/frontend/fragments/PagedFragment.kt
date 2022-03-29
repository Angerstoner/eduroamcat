package de.gwdg.wifitool.frontend.fragments

import android.content.Context
import androidx.fragment.app.Fragment
import de.gwdg.wifitool.frontend.activities.MainActivity

abstract class PagedFragment : Fragment() {
    private val logTag = "PagedFragment (Base)"
    protected lateinit var parentActivity: MainActivity

    override fun onAttach(context: Context) {
        parentActivity = activity as MainActivity
        super.onAttach(context)
    }

    protected fun blockNext() {
        parentActivity.blockNext()
    }

    protected fun allowNext() {
        parentActivity.allowNext()
    }

    fun addNextButtonAction(action: () -> Unit) = parentActivity.addNextButtonAction(action)

    fun changeNextButtonText(newText: String) = parentActivity.changeNextButtonText(newText)
}