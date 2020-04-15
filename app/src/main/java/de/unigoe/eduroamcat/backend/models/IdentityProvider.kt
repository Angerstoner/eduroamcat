package de.unigoe.eduroamcat.backend.models

data class IdentityProvider(
    val entityId: Long,
    val country: String,
    val title: String
) {
    override fun toString(): String {
        return "$title ($country, $entityId)"
    }
}