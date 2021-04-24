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
    private var previousIdentityProviderList: ArrayList<IdentityProvider> = ArrayList()
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
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val searchText = constraint?.toString() ?: ""
                val filterResults = FilterResults()
                previousIdentityProviderList = ArrayList(identityProviderList)
                identityProviderList = ArrayList(originalIdentityProviderList)

                val newValues = if (searchText.contains("\\S".toRegex()))
                    identityProviderList.filter {
                        it.hasKeyword(searchText)
                    }
                else
                    identityProviderList

                filterResults.values = newValues
                filterResults.count = newValues.size
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults) {
                identityProviderList = (results.values as List<*>)
                    .filterIsInstance<IdentityProvider>() as ArrayList<IdentityProvider>
                if (results.count > 0) {
                    notifyDataSetChanged()
                } else {
                    notifyDataSetInvalidated()
                }
            }
        }
    }

    /**
     * This method has to use the version of the list prior to the last filtering
     *
     * This method should only be called when a item from the dropdown is clicked because clicking on such
     * an item will change the filter and therefore reset the positions of the list. So the previous version
     * of the list has to be used, so that the item on the provided [position] is the correct [IdentityProvider]
     */
    fun getClickedItem(position: Int): IdentityProvider = previousIdentityProviderList[position]


    fun setIdentityProviders(identityProviders: ArrayList<IdentityProvider>) {
        identityProviderList = identityProviders
        originalIdentityProviderList = (identityProviderList.clone() as List<*>)
            .filterIsInstance<IdentityProvider>() as ArrayList<IdentityProvider>
        notifyDataSetChanged()
    }

    fun moveIdentityProviderWithIdToTop(identityProviderId: Long) {
        // this uses a trick for moving the desired identity to the first position
        // False results to 0 and True to 1, that's why the Identity Provider with
        // the given Id will be moved to the front when sortByDescending is used.
        identityProviderList.sortByDescending { it.entityId == identityProviderId }
    }
}