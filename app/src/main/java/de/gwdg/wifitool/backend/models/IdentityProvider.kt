package de.gwdg.wifitool.backend.models

data class IdentityProvider(
    val entityId: Long,
    val country: String,
    val title: String
) {

    private val keywords = arrayListOf<String>()
    override fun toString(): String {
        return "$title ($country)"
    }

    public fun addKeywords(vararg keywords: String) {
        this.keywords.addAll(keywords)
    }

    public fun hasKeyword(keyword: String): Boolean {
        return keywords.any { it.contains(keyword, ignoreCase = true) }
    }


}