package de.unigoe.eduroamcat.backend.models

data class IdentityProvider(
    private val entityId: Long,
    private val country: String,
    private val title: String
) {
    override fun toString(): String {
        return "$title ($country, $entityId)"
    }
}