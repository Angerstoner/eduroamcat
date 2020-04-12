package de.unigoe.eduroamcat.backend

import android.R
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.util.Log


// TEST CODE START -> TODO: remove after initial testing
const val ORGANIZATION_ID = 5042
const val LANG = "en"
// TEST CODE END


const val TAG = "ProfileDownloader"

class ProfileDownloader(private val context: Context) {
    private var lastDownload = -1L
    private val downloadManager = context.getSystemService(DOWNLOAD_SERVICE) as DownloadManager

    var onComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctxt: Context, intent: Intent) {
            Log.i(TAG, "Download complete")
        }
    }

    fun downloadProfile() {
        val profileDownloadUri = Uri.parse(
            "https://cat.eduroam.org/user/API.php?action=downloadInstaller" +
                    "&id=${AndroidId.getAndroidId()}" +
                    "&profile=$ORGANIZATION_ID" +
                    "&lang=$LANG"
        )

        context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        context.getExternalFilesDir(null)!!.mkdirs()
        lastDownload = downloadManager.enqueue(
            DownloadManager.Request(profileDownloadUri)
                .setDestinationInExternalFilesDir(
                    context,
                    null,
                    "eduroam-$ORGANIZATION_ID.eap-config"
                )
        )
    }


}

/**
 * Enum which determines the 'id' part of the eap-config request URL
 *
 * request url:  https://cat.eduroam.org/user/API.php?action=downloadInstaller&id=ANDROID_ID&profile=ID&lang=LANG
 *
 * Example for Android P (9.0):
 * https://cat.eduroam.org/user/API.php?action=downloadInstaller&id=android_pie&profile=5042&lang=en
 */
enum class AndroidId(val androidId: String, vararg val applicableApiLevels: Int) {
    LOLLIPOP("android_lollipop", Build.VERSION_CODES.LOLLIPOP, Build.VERSION_CODES.LOLLIPOP_MR1),
    MARSHMALLOW("android_marshmallow", Build.VERSION_CODES.M),
    NOUGAT("android_nougat", Build.VERSION_CODES.N, Build.VERSION_CODES.N_MR1),
    OREO("android_oreo", Build.VERSION_CODES.O, Build.VERSION_CODES.O_MR1),
    PIE("android_pie", Build.VERSION_CODES.P),
    Q("android_q", Build.VERSION_CODES.Q);

    companion object {
        fun getAndroidId() =
            values().single { it.applicableApiLevels.contains(Build.VERSION.SDK_INT) }.androidId
    }
}
