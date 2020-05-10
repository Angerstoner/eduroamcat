package de.unigoe.eduroamcat.backend.models

class EapConfig {
    var outerEapMethod: String? = null
    var serverCertificate: String? = null
    var serverId: String? = null
    var outerIdentity: String? = null
    var innerEapMethod: String? = null
    var ssid: String? = null

    override fun toString(): String {
        var string = ""
        if (outerEapMethod != null) string += "$outerEapMethod; "
        if (serverCertificate != null) string += "$serverCertificate; "
        if (serverId != null) string += "$serverId; "
        if (outerIdentity != null) string += "$outerIdentity; "
        if (innerEapMethod != null) string += "$innerEapMethod; "
        if (ssid != null) string += "$ssid; "
        return string
    }
}

