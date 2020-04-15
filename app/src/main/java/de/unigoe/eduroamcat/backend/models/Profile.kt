package de.unigoe.eduroamcat.backend.models

class Profile(
    val profileId: Long,
    val displayLabel: String,
    val identityProvider: IdentityProvider
) {
    override fun toString(): String {
        return "$displayLabel (${identityProvider.title}, $profileId)"
    }
}