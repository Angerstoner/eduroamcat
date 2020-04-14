package de.unigoe.eduroamcat.frontend.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import de.unigoe.eduroamcat.backend.models.IdentityProvider

class IdentityProviderArrayAdapter(context: Context, resource: Int) :
    ArrayAdapter<IdentityProvider>(context, resource) {
    private var identityProviderList: List<IdentityProvider> = ArrayList()


    override fun getCount(): Int = identityProviderList.size

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val listItem = convertView
            ?: LayoutInflater.from(context)
                .inflate(android.R.layout.simple_list_item_1, parent, false)

        listItem.findViewById<TextView>(android.R.id.text1).text =
            identityProviderList[position].toString()

        return listItem
    }

    fun setIdentityProviders(identityProviders: List<IdentityProvider>) {
        identityProviderList = identityProviders
        notifyDataSetChanged()
    }
}