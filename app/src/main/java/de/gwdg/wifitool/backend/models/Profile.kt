package de.gwdg.wifitool.backend.models

class Profile(
    val profileId: Long,
    val displayLabel: String,
    val identityProviderId: Long,
    val identityProviderName: String
) {
    override fun toString(): String {
        return displayLabel
    }
}