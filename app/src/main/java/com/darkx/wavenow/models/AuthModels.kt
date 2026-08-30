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
    val participantId: String? = null,
    val isGroup: Boolean = false,
    val groupName: String? = null,
    val participantIds: List<String>? = null
)

data class SendMessageRequest(
    val chatId: String,
    val content: String,
    val type: String = "text",
    val replyTo: String? = null
)
