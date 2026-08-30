package com.darkx.wavenow.models

data class User(
    val id: String? = null,
    @field:com.google.gson.annotations.SerializedName("_id") val _id: String? = null,
    val username: String,
    val phone: String? = null,
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val about: String? = null,
    val isOnline: Boolean = false,
    val lastSeen: String? = null
) {
    // Server sometimes returns "id", sometimes "_id" depending on endpoint
    fun resolvedId(): String = id ?: _id ?: ""
}
