package de.unigoe.eduroamcat.frontend.activities

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import de.unigoe.eduroamcat.R
import de.unigoe.eduroamcat.backend.ProfileApi
import de.unigoe.eduroamcat.frontend.adapters.IdentityProviderArrayAdapter
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private val tag = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestAppPermissions()
        initIdentityProviderListView()

    }

    /**
     * Requests needed permissions if Android M or higher is used
     * Android L and lower use only the AndroidManifest to grant permissions
     */
    private fun requestAppPermissions() {
        val permissionsNeeded = arrayOf(ACCESS_FINE_LOCATION)
        val permissionsRequestCode = 1

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissionsNeeded, permissionsRequestCode)
        }
    }


    private fun initIdentityProviderListView() {
        val identityProviderArrayAdapter =
            IdentityProviderArrayAdapter(
                this,
                android.R.layout.simple_list_item_1
            )

        identityProviderListView.adapter = identityProviderArrayAdapter

        ProfileApi(this).getAllIdentityProviders()
            .observe(this, Observer { identityProviders ->
                identityProviderArrayAdapter.setIdentityProviders(identityProviders)
            })
    }


}


