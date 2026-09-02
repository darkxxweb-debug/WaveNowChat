package com.darkx.wavenow.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.darkx.wavenow.models.Message
import com.darkx.wavenow.models.User

/**
 * Ujumbe unapohifadhiwa hapa, unabaki kwenye simu MILELE — hata kama
 * baadaye umefutwa kwenye server DB (baada ya kufika kwa washiriki wote).
 * Hii ndiyo "local persistence" ya app, sawa na jinsi WhatsApp inavyofanya
 * kazi ikiwa na E2E — server haihifadhi ujumbe muda mrefu.
 */
@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val senderId: String?,
    val senderName: String?,
    val senderAvatar: String?,
    val type: String,
    val content: String,
    val status: String,
    val createdAt: String?,
    val isMine: Boolean,
    // Je, tayari tumemtumia server ripoti ya "nimepokea" (delivered ack)?
    val deliveredAckSent: Boolean = false
)

fun Message.toEntity(chatId: String, myUserId: String): MessageEntity = MessageEntity(
    id = id ?: java.util.UUID.randomUUID().toString(),
    chatId = chatId,
    senderId = sender?.resolvedId(),
    senderName = sender?.displayName ?: sender?.username,
    senderAvatar = sender?.avatarUrl,
    type = type,
    content = content,
    status = status,
    createdAt = createdAt,
    isMine = sender?.resolvedId() == myUserId
)

fun MessageEntity.toMessage(): Message {
    val msg = Message(
        id = id,
        chat = chatId,
        sender = User(id = senderId, username = senderName ?: "", displayName = senderName, avatarUrl = senderAvatar),
        type = type,
        content = content,
        status = status,
        createdAt = createdAt
    )
    msg.isMine = isMine
    return msg
}
