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

    @PUT("api/users/me/fcm-token")
    suspend fun updateFcmToken(@Body body: FcmTokenRequest): Response<Unit>

    // ---- Chats / Groups / Channels ----
    @GET("api/chats")
    suspend fun getChats(): Response<List<Chat>>

    @POST("api/chats")
    suspend fun createChat(@Body body: CreateChatRequest): Response<Chat>

    @POST("api/chats/group")
    suspend fun createGroup(@Body body: CreateGroupRequest): Response<Chat>

    @POST("api/chats/channel")
    suspend fun createChannel(@Body body: CreateChannelRequest): Response<Chat>

    @POST("api/chats/{id}/subscribe")
    suspend fun subscribeChannel(@Path("id") id: String): Response<Chat>

    @DELETE("api/chats/{id}/subscribe")
    suspend fun unsubscribeChannel(@Path("id") id: String): Response<Unit>

    @PUT("api/chats/{id}/members")
    suspend fun updateGroupMembers(@Path("id") id: String, @Body body: MembersUpdateRequest): Response<Chat>

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

    // App inaita hii mara ujumbe ushahifadhiwa local (Room DB) kwenye simu —
    // hii ndiyo inayosababisha ufutwe kwenye server DB ukishafika kwa wote.
    @POST("api/messages/{id}/delivered")
    suspend fun markDelivered(@Path("id") id: String): Response<Unit>
}
