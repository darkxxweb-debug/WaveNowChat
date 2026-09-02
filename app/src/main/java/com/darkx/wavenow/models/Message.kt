package com.darkx.wavenow.models

data class Message(
    @field:com.google.gson.annotations.SerializedName("_id") val id: String? = null,
    val chat: String,
    val sender: User? = null,
    val type: String = "text",
    val content: String,
    val status: String = "sent",
    val deliveredTo: List<String>? = null,
    val readBy: List<String>? = null,
    val replyTo: String? = null,
    val createdAt: String? = null
) {
    // Set locally right after sending, before server confirms - used for optimistic UI
    var isMine: Boolean = false
}
