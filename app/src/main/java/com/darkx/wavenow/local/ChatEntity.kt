package com.darkx.wavenow.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Metadata ya chat/group/channel iliyohifadhiwa local kwa ajili ya orodha
 * ya "Chats" isionekane tupu wakati hakuna internet.
 */
@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val id: String,
    val type: String,
    val displayName: String,
    val avatarUrl: String?,
    val lastMessage: String?,
    val lastMessageTime: String?,
    val isFavourite: Boolean = false
)
