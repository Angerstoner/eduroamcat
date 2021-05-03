package de.gwdg.wifitool.backend.models

data class ProfileAttributes(
    val profileId: Long,
    val identityProviderName: String,
    val identityProviderMail: String,
    val identityProviderPhone: String,
    val identityProviderUrl: String,
    val identityProviderDescription: String
) {

}