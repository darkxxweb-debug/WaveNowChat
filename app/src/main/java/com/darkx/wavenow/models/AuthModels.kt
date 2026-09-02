package com.darkx.wavenow.models

data class RegisterRequest(
    val username: String,
    val phone: String,
    val password: String,
    val displayName: String? = null
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class AuthResponse(
    val message: String,
    val token: String,
    val user: User
)

data class CreateChatRequest(
    val participantId: String
)

data class CreateGroupRequest(
    val name: String,
    val avatarUrl: String? = null,
    val participantIds: List<String> = emptyList()
)

data class CreateChannelRequest(
    val name: String,
    val description: String? = null,
    val avatarUrl: String? = null
)

data class FcmTokenRequest(
    val fcmToken: String
)

data class MembersUpdateRequest(
    val add: List<String> = emptyList(),
    val remove: List<String> = emptyList()
)

data class SendMessageRequest(
    val chatId: String,
    val content: String,
    val type: String = "text",
    val replyTo: String? = null
)
