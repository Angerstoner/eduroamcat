package de.gwdg.wifitool.backend.models

class Profile(
    val profileId: Long,
    private val displayLabel: String,
    private val identityProviderId: Long,
    private val identityProviderName: String
) {
    override fun toString(): String {
        return displayLabel
    }
}