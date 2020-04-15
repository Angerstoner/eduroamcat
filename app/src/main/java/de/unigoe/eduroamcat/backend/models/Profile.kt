package de.unigoe.eduroamcat.backend.models

class Profile(
    private val profileId: Long,
    private val displayLabel: String,
    private val identityProvider: IdentityProvider
) {
    override fun toString(): String {
        return "$displayLabel (${identityProvider.title}, $profileId)"
    }
}