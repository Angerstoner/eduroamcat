package de.unigoe.eduroamcat.backend

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import de.unigoe.eduroamcat.backend.models.IdentityProvider
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

// TEST CODE START -> TODO: remove after initial testing
const val ORGANIZATION_ID = 5042
const val LANG = "en"
// TEST CODE END

const val API_URL_BASE = "https://cat.eduroam.org/user/API.php?action="

class ProfileApi(private val activityContext: Context) {
    private val tag = "ProfileApi"
    private val identityProviderLiveData = MutableLiveData<ArrayList<IdentityProvider>>()

    private val onProfileDownloadComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctxt: Context, intent: Intent) {
            Log.i(tag, "Download complete")
        }
    }


    /**
     * Util for downloading files using the download manager
     *
     * @param uri download URI/address
     * @param filename filename under which the downloaded file should be saved
     * @param onComplete BroadcastReceiver which should be triggered after download is complete
     */
    private fun downloadToAppData(uri: Uri, filename: String, onComplete: BroadcastReceiver?) {
        // TODO: maybe use Volley
        val downloadManager =
            activityContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        activityContext.getExternalFilesDir(null)!!.mkdirs()

        if (onComplete != null) activityContext.registerReceiver(
            onComplete,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        )

        downloadManager.enqueue(
            DownloadManager
                .Request(uri)
                .setDestinationInExternalFilesDir(activityContext, null, filename)
        )
    }

    /**
     * Downloads the eap-config/profile for the given organization identified by its ID
     *
     * @param organizationId: ID of identity provider, which can be obtained via listAllIdentityProviders
     * see {@link de.unigoe.eduroamcat.backend.ProfileApi#getAllIdentityProviders}
     */
    fun downloadProfile(organizationId: Int) {
        val lang = Locale.getDefault().language
        val profileDownloadUri = Uri.parse(
            API_URL_BASE + "downloadInstaller" + "&id=${AndroidId.getAndroidId()}" +
                    "&profile=$organizationId" + "&lang=$lang"
        )
        downloadToAppData(
            profileDownloadUri,
            "eduroam-${organizationId}_${lang}.eap-config",
            onProfileDownloadComplete
        )
    }

    // TODO: javadoc
    fun getAllIdentityProviders(): LiveData<ArrayList<IdentityProvider>> {
        val lang = Locale.getDefault().language
        val identityProviderListUrl = API_URL_BASE + "listAllIdentityProviders&" + "lang=$lang"

        val queue = Volley.newRequestQueue(activityContext)
        val identityProviderListRequest =
            JsonArrayRequest(Request.Method.GET, identityProviderListUrl, null,
                Response.Listener { response -> parseIdentityProviderArray(response) },
                Response.ErrorListener { error -> Log.e(tag, error.toString()) })

        queue.add(identityProviderListRequest)

        return identityProviderLiveData
    }

    // TODO: javadoc
    private fun parseIdentityProviderArray(identityProviderJSONArray: JSONArray) {
        // JSONArray does not provide an iterator, so we add one
        operator fun JSONArray.iterator(): Iterator<JSONObject> =
            (0 until length()).asSequence().map { get(it) as JSONObject }.iterator()

        val identityProviderList = ArrayList<IdentityProvider>()
        identityProviderJSONArray.iterator().forEach {
            // TODO: add parse-exception
            val entityId = it.get("entityID").toString().toLong()
            val country = it.get("country").toString()
            val title = it.get("title").toString()
            identityProviderList.add(IdentityProvider(entityId, country, title))
        }
        identityProviderLiveData.postValue(identityProviderList)
    }
}
