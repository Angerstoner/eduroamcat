package de.unigoe.eduroamcat.frontend.activities

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import de.unigoe.eduroamcat.R
import de.unigoe.eduroamcat.backend.ORGANIZATION_ID
import de.unigoe.eduroamcat.backend.ProfileApi


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestAppPermissions()
        ProfileApi(this).downloadProfile(ORGANIZATION_ID)
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
}


