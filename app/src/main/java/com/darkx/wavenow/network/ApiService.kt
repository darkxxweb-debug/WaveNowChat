package com.darkx.wavenow.network

import com.darkx.wavenow.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ---- Auth ----
    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<AuthResponse>

    @GET("api/auth/me")
    suspend fun getMe(): Response<User>

    // ---- Users ----
    @GET("api/users/search")
    suspend fun searchUsers(@Query("q") query: String): Response<List<User>>

    @GET("api/users/{id}")
    suspend fun getUser(@Path("id") id: String): Response<User>

    // ---- Chats ----
    @GET("api/chats")
    suspend fun getChats(): Response<List<Chat>>

    @POST("api/chats")
    suspend fun createChat(@Body body: CreateChatRequest): Response<Chat>

    @GET("api/chats/{id}")
    suspend fun getChat(@Path("id") id: String): Response<Chat>

    // ---- Messages ----
    @GET("api/messages/{chatId}")
    suspend fun getMessages(
        @Path("chatId") chatId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 30
    ): Response<List<Message>>

    @POST("api/messages")
    suspend fun sendMessage(@Body body: SendMessageRequest): Response<Message>
}
