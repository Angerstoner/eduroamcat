package de.gwdg.wifitool.frontend.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import de.gwdg.wifitool.backend.models.Profile

class ProfileArrayAdapter(context: Context, resource: Int) :
    ArrayAdapter<Profile>(context, resource) {
    private var profileList: ArrayList<Profile> = ArrayList()

    override fun getCount(): Int = profileList.size
    override fun getItem(position: Int): Profile = profileList[position]
    override fun getPosition(item: Profile?): Int = profileList.indexOf(item)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val listItem = convertView
            ?: LayoutInflater.from(context)
                .inflate(android.R.layout.simple_spinner_dropdown_item, parent, false)

        listItem.findViewById<TextView>(android.R.id.text1).text = profileList[position].toString()
        return listItem
    }

    fun setProfiles(profiles: ArrayList<Profile>) {
        profileList = profiles
        notifyDataSetChanged()
    }
}