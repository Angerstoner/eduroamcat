package de.gwdg.wifitool.backend

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import de.gwdg.wifitool.backend.models.IdentityProvider
import de.gwdg.wifitool.backend.models.Profile
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList


const val API_URL_BASE = "https://cat.eduroam.org/user/API.php?action="
const val API_ACTION_LIST_PROFILES = API_URL_BASE + "listProfiles&idp=%d&lang=%s"
const val API_ACTION_LIST_IDENTITY_PROVIDERS = API_URL_BASE + "listAllIdentityProviders&lang=%s"
const val API_ACTION_DOWNLOAD_CONFIG = API_URL_BASE + "downloadInstaller&id=%s&profile=%d&lang=%s"

const val JSON_TAG_PROFILE_LIST_DATA = "data"

const val JSON_TAG_IDENTITY_PROVIDER_ID = "entityID"
const val JSON_TAG_IDENTITY_PROVIDER_COUNTRY = "country"
const val JSON_TAG_IDENTITY_PROVIDER_TITLE = "title"

const val JSON_TAG_PROFILE_ID = "profile"
const val JSON_TAG_PROFILE_LABEL = "display"


const val LOG_MESSAGE_MISSING_DATA = "Could not add %s (missing data)"

class ProfileApi(private val activityContext: Context) {
    private val tag = "ProfileApi"
    private val lang = Locale.getDefault().language
    private val identityProviderLiveData = MutableLiveData<ArrayList<IdentityProvider>>()
    private val profileLiveData = MutableLiveData<ArrayList<Profile>>()


    private val defaultErrorListener =
        Response.ErrorListener { error -> Log.e(tag, error.toString()) }


    // JSONArray does not provide an iterator, so we add one
    operator fun JSONArray.iterator(): Iterator<JSONObject> =
        (0 until length()).asSequence().map { get(it) as JSONObject }.iterator()

    /**
     * Downloads file from [uri] to [filename] and calls [onComplete] afterwards using the [DownloadManager]
     */
    private fun downloadToAppData(uri: Uri, filename: String, onComplete: BroadcastReceiver?) {
        // TODO: maybe use Volley and request simple strings
        val downloadManager =
            activityContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        activityContext.getExternalFilesDir(null)!!.mkdirs()

        if (onComplete != null) activityContext.registerReceiver(
            onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        )

        Log.d(tag, "Downloading $filename from $uri")
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
                Request.Method.GET, downloadUrl, null, responseListener, errorListener
            )
        queue.add(identityProviderListRequest)
    }

    /**
     * Downloads JSON Object from [downloadUrl].
     * Calls [responseListener] after download finished or [errorListener] if download fails
     */
    private fun downloadJsonObject(
        downloadUrl: String,
        responseListener: Response.Listener<JSONObject>,
        errorListener: Response.ErrorListener = defaultErrorListener
    ) {
        val queue = Volley.newRequestQueue(activityContext)
        val identityProviderListRequest = JsonObjectRequest(
            Request.Method.GET, downloadUrl, null, responseListener, errorListener
        )
        queue.add(identityProviderListRequest)
    }


    /**
     * Downloads the eap-config/profile for the given profile identified by its [Profile.profileId]
     *
     * see [ProfileApi.getAllIdentityProviders]
     */
    fun downloadProfileConfig(profile: Profile, filename: String, onDownloadFinished: BroadcastReceiver) {
        val profileDownloadUri = Uri.parse(
            API_ACTION_DOWNLOAD_CONFIG.format(AndroidId.getAndroidId(), profile.profileId, lang)
        )
        downloadToAppData(profileDownloadUri, filename, onDownloadFinished)
    }

    /**
     * Returns LiveData of Identity Provider List
     */
    fun getAllIdentityProviders(): LiveData<ArrayList<IdentityProvider>> {
        val identityProviderListUrl = API_ACTION_LIST_IDENTITY_PROVIDERS.format(lang)

        val responseListener = Response.Listener<JSONArray> { response -> parseIdentityProviderListJsonArray(response) }
        downloadJsonArray(identityProviderListUrl, responseListener)

        return identityProviderLiveData
    }

    /**
     * Returns LiveData of Profile List for given [identityProvider]
     */
    fun getIdentityProviderProfiles(identityProvider: IdentityProvider): LiveData<ArrayList<Profile>> {
        val profileListUrl = API_ACTION_LIST_PROFILES.format(identityProvider.entityId, lang)

        val responseListener = Response.Listener<JSONObject> { response ->
            parseProfileListJsonArray(JSONArray(response.getString(JSON_TAG_PROFILE_LIST_DATA)), identityProvider)
        }

        downloadJsonObject(profileListUrl, responseListener)
        return profileLiveData
    }


    /**
     * Parses [identityProviderJsonArray] into List of [IdentityProvider] as LiveData
     */
    private fun parseIdentityProviderListJsonArray(identityProviderJsonArray: JSONArray) {
        val identityProviderList = ArrayList<IdentityProvider>()
        identityProviderJsonArray.iterator().forEach {
            val entityId = it.getLong(JSON_TAG_IDENTITY_PROVIDER_ID)
            val country = it.getString(JSON_TAG_IDENTITY_PROVIDER_COUNTRY)
            val title = it.getString(JSON_TAG_IDENTITY_PROVIDER_TITLE)

            if (null !in listOf(entityId, country, title))
                identityProviderList.add(
                    IdentityProvider(
                        entityId,
                        country,
                        title
                    )
                )
            else
                Log.e(tag, LOG_MESSAGE_MISSING_DATA.format("IdentityProvider"))
        }
        identityProviderLiveData.postValue(identityProviderList)
    }


    /**
     * Parses [profileListJsonArray] into List of [Profile] as LiveData.
     * [identityProvider] is stored as [Profile] information
     */
    private fun parseProfileListJsonArray(profileListJsonArray: JSONArray, identityProvider: IdentityProvider) {
        val profileList = ArrayList<Profile>()
        profileListJsonArray.iterator().forEach {
            val profileId = it.getLong(JSON_TAG_PROFILE_ID)
            val displayLabel = it.getString(JSON_TAG_PROFILE_LABEL)
            if (null !in listOf(profileId, displayLabel))
                profileList.add(
                    Profile(
                        profileId,
                        displayLabel,
                        identityProvider
                    )
                )
            else
                Log.e(tag, LOG_MESSAGE_MISSING_DATA.format("Profile"))
        }
        profileLiveData.postValue(profileList)
    }


}
