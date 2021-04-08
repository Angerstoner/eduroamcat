package de.gwdg.wifitool.backend

import android.os.Build
import android.util.Log

/**
 * Enum which determines the 'id' part of the eap-config request URL
 *
 * request url:  https://cat.eduroam.org/user/API.php?action=downloadInstaller&id=ANDROID_ID&profile=ID&lang=LANG
 *
 * Example for Android P (9.0):
 * https://cat.eduroam.org/user/API.php?action=downloadInstaller&id=android_pie&profile=5042&lang=en
 */
enum class AndroidDeviceGroup(val androidId: String, vararg val applicableApiLevels: Int) {
    ANDROID_RECENT("android_recent", Build.VERSION_CODES.R),
    ANDROID_8_10(
        "android_8_10", Build.VERSION_CODES.O, Build.VERSION_CODES.O_MR1,
        Build.VERSION_CODES.P, Build.VERSION_CODES.Q
    ),
    ANDROID_4_7(
        "android_4_7", Build.VERSION_CODES.LOLLIPOP, Build.VERSION_CODES.LOLLIPOP_MR1,
        Build.VERSION_CODES.M, Build.VERSION_CODES.N, Build.VERSION_CODES.N_MR1
    );

    companion object {
        fun getAndroidDeviceGroup() =
            try {
                values().single { it.applicableApiLevels.contains(Build.VERSION.SDK_INT) }.androidId
            } catch (e: NoSuchElementException) {
                val default = values().last()
                Log.i("AndroidId", "Android Version not found in enum, defaulting to $default")
                default
            }
    }
}