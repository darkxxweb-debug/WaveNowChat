package com.darkx.wavenow.models

data class Chat(
    @field:com.google.gson.annotations.SerializedName("_id") val id: String,
    val isGroup: Boolean = false,
    val groupName: String? = null,
    val groupAvatar: String? = null,
    val participants: List<User> = emptyList(),
    val lastMessage: Message? = null,
    val updatedAt: String? = null
) {
    // For a 1-on-1 chat, get the "other" person to display in the list
    fun otherParticipant(myUserId: String): User? {
        if (isGroup) return null
        return participants.firstOrNull { it.resolvedId() != myUserId }
    }
}
