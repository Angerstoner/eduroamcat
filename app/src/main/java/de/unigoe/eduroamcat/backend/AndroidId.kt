package de.unigoe.eduroamcat.backend

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
enum class AndroidId(val androidId: String, vararg val applicableApiLevels: Int) {
    LOLLIPOP("android_lollipop", Build.VERSION_CODES.LOLLIPOP, Build.VERSION_CODES.LOLLIPOP_MR1),
    MARSHMALLOW("android_marshmallow", Build.VERSION_CODES.M),
    NOUGAT("android_nougat", Build.VERSION_CODES.N, Build.VERSION_CODES.N_MR1),
    OREO("android_oreo", Build.VERSION_CODES.O, Build.VERSION_CODES.O_MR1),
    PIE("android_pie", Build.VERSION_CODES.P),
    Q("android_q", Build.VERSION_CODES.Q);

    companion object {
        fun getAndroidId() =
            try {
                values().single { it.applicableApiLevels.contains(Build.VERSION.SDK_INT) }.androidId
            } catch (e: NoSuchElementException) {
                val default = values().last()
                Log.i("AndroidId", "Android Version not found in enum, defaulting to $default")
                default
            }
    }
}