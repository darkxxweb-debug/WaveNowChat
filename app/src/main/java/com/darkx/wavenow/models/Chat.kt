package com.darkx.wavenow.models

data class Chat(
    @field:com.google.gson.annotations.SerializedName("_id") val id: String,
    val type: String = "direct", // "direct" | "group" | "channel"
    val name: String? = null,
    val avatarUrl: String? = null,
    val description: String? = null,
    val owner: User? = null,
    val participants: List<User> = emptyList(),
    val admins: List<String>? = null,
    val lastMessage: Message? = null,
    val updatedAt: String? = null
) {
    fun isGroup(): Boolean = type == "group"
    fun isChannel(): Boolean = type == "channel"
    fun isDirect(): Boolean = type == "direct"

    // For a 1-on-1 chat, get the "other" person to display in the list
    fun otherParticipant(myUserId: String): User? {
        if (!isDirect()) return null
        return participants.firstOrNull { it.resolvedId() != myUserId }
    }

    fun displayName(myUserId: String): String {
        if (!isDirect()) return name ?: if (isChannel()) "Channel" else "Group"
        val other = otherParticipant(myUserId)
        return other?.displayName ?: other?.username ?: "Unknown"
    }

    fun displayAvatar(myUserId: String): String? {
        if (!isDirect()) return avatarUrl
        return otherParticipant(myUserId)?.avatarUrl
    }

    fun isAdmin(userId: String): Boolean = admins?.contains(userId) == true
}
