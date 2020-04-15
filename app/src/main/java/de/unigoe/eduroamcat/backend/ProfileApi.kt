package de.unigoe.eduroamcat.backend

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import de.unigoe.eduroamcat.backend.models.IdentityProvider
import de.unigoe.eduroamcat.backend.models.Profile
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
    private val profileLiveData = MutableLiveData<ArrayList<Profile>>()

    private val onProfileDownloadComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctxt: Context, intent: Intent) {
            Log.i(tag, "Download complete")
        }
    }

    private val defaultErrorListener =
        Response.ErrorListener { error -> Log.e(tag, error.toString()) }


    // JSONArray does not provide an iterator, so we add one
    operator fun JSONArray.iterator(): Iterator<JSONObject> =
        (0 until length()).asSequence().map { get(it) as JSONObject }.iterator()

    /**
     * Downloads file from [uri] to [filename] and calls [onComplete] afterwards using the [DownloadManager]
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
     * Downloads JSON Array from [downloadUrl].
     * Calls [responseListener] after download finished or [errorListener] if download fails
     */
    private fun downloadJsonArray(
        downloadUrl: String,
        responseListener: Response.Listener<JSONArray>,
        errorListener: Response.ErrorListener = defaultErrorListener
    ) {
        val queue = Volley.newRequestQueue(activityContext)
        val identityProviderListRequest =
            JsonArrayRequest(
                Request.Method.GET, downloadUrl, null,
                responseListener,
                errorListener
            )
        queue.add(identityProviderListRequest)
    }


    /**
     * Downloads the eap-config/profile for the given profile identified by its [profileId]
     *
     * see [ProfileApi.getAllIdentityProviders]
     */
    fun downloadProfile(profileId: Int) {
        val lang = Locale.getDefault().language
        val profileDownloadUri = Uri.parse(
            API_URL_BASE + "downloadInstaller" + "&id=${AndroidId.getAndroidId()}" +
                    "&profile=$profileId" + "&lang=$lang"
        )
        downloadToAppData(
            profileDownloadUri,
            "eduroam-${profileId}_${lang}.eap-config",
            onProfileDownloadComplete
        )
    }

    /**
     * Returns LiveData of Identity Provider List
     */
    fun getAllIdentityProviders(): LiveData<ArrayList<IdentityProvider>> {
        val lang = Locale.getDefault().language
        val identityProviderListUrl = API_URL_BASE + "listAllIdentityProviders&" + "lang=$lang"

        val responseListener =
            Response.Listener<JSONArray> { response -> parseIdentityProviderListJsonArray(response) }
        downloadJsonArray(identityProviderListUrl, responseListener)

        return identityProviderLiveData
    }

    /**
     * Returns LiveData of Profile List for given [identityProvider]
     */
    fun getProfilesForIdentityProvider(identityProvider: IdentityProvider): LiveData<ArrayList<Profile>> {
        val lang = Locale.getDefault().language
        val profileListUrl = API_URL_BASE + "listProfiles&" +
                "idp=${identityProvider.entityId}" + "&lang=$lang"

        val responseListener =
            Response.Listener<JSONArray> { response ->
                parseProfileListJsonArray(
                    response,
                    identityProvider
                )
            }
        downloadJsonArray(profileListUrl, responseListener)

        return profileLiveData
    }


    /**
     * Parses [identityProviderJsonArray] into List of [IdentityProvider] as LiveData
     */
    private fun parseIdentityProviderListJsonArray(identityProviderJsonArray: JSONArray) {
        val identityProviderList = ArrayList<IdentityProvider>()
        identityProviderJsonArray.iterator().forEach {
            // TODO: add parse-exception
            val entityId = it.get("entityID").toString().toLong()
            val country = it.get("country").toString()
            val title = it.get("title").toString()
            identityProviderList.add(IdentityProvider(entityId, country, title))
        }
        identityProviderLiveData.postValue(identityProviderList)
    }


    /**
     * Parses [profileListJsonArray] into List of [Profile] as LiveData.
     * [identityProvider] is stored as [Profile] information
     */
    private fun parseProfileListJsonArray(
        profileListJsonArray: JSONArray,
        identityProvider: IdentityProvider
    ) {
        val profileList = ArrayList<Profile>()
        profileListJsonArray.iterator().forEach {
            // TODO: add parse-exception
            val profileId = it.get("profile").toString().toLong()
            val displayLabel = it.get("display").toString()
            profileList.add(Profile(profileId, displayLabel, identityProvider))
        }
        profileLiveData.postValue(profileList)
    }


}
