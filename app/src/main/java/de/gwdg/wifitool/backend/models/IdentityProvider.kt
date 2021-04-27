package de.gwdg.wifitool.backend.models

data class IdentityProvider(
    val entityId: Long,
    val country: String,
    val title: String
) {

    private val keywords = arrayListOf<String>()
    override fun toString(): String = if (country != "") "$title ($country)" else title

    fun addKeywords(vararg keywords: String) {
        this.keywords.addAll(keywords)
    }

    fun hasKeyword(keyword: String): Boolean = keywords.any { it.contains(keyword, ignoreCase = true) }
}