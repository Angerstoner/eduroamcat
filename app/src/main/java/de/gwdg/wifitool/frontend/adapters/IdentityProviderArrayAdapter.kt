package de.gwdg.wifitool.frontend.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import de.gwdg.wifitool.backend.models.IdentityProvider

class IdentityProviderArrayAdapter(context: Context, resource: Int) :
    ArrayAdapter<IdentityProvider>(context, resource) {
    private var identityProviderList: ArrayList<IdentityProvider> = ArrayList()
    private var originalIdentityProviderList: ArrayList<IdentityProvider> = ArrayList()

    override fun getCount(): Int = identityProviderList.size
    override fun getItem(position: Int): IdentityProvider = identityProviderList[position]
    override fun getPosition(item: IdentityProvider?): Int = identityProviderList.indexOf(item)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val listItem = convertView
            ?: LayoutInflater.from(context)
                .inflate(android.R.layout.simple_list_item_1, parent, false)

        listItem.findViewById<TextView>(android.R.id.text1).text =
            identityProviderList[position].toString()

        return listItem
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                val searchText = constraint.toString()
                val filterResults = FilterResults()

                identityProviderList = (originalIdentityProviderList.clone() as List<*>)
                    .filterIsInstance<IdentityProvider>() as ArrayList<IdentityProvider>

                filterResults.values = if (searchText.contains("\\S".toRegex()))
                    identityProviderList.filter {
                        it.toString().contains(searchText, ignoreCase = true)
                    }
                else
                    identityProviderList

                return filterResults
            }

            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                identityProviderList = (results.values as List<*>)
                    .filterIsInstance<IdentityProvider>() as ArrayList<IdentityProvider>
                notifyDataSetChanged()
            }
        }
    }

    fun setIdentityProviders(identityProviders: ArrayList<IdentityProvider>) {
        identityProviderList = identityProviders
        originalIdentityProviderList = (identityProviderList.clone() as List<*>)
            .filterIsInstance<IdentityProvider>() as ArrayList<IdentityProvider>
        notifyDataSetChanged()
    }
}