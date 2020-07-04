package de.unigoe.eduroamcat.backend.models

enum class EapType(val label: String, val ianaId: Int) {
    // OUTER TYPES
    EAP_TLS("EAP-TLS", 13),
    EAP_TTLS("EAP-TTLS", 21),
    PEAP("PEAP", 25),

    // INNER TYPES
    PAP("PAP", 1),
    GTC("GTC", 6),
    MSCHAPv2("MSCHAPv2", 26);

    companion object {
        fun getEapType(ianaId: Int) = values().single() { it.ianaId == ianaId }
    }
}