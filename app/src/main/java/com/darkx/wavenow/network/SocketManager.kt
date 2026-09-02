package com.darkx.wavenow.network

import android.content.Context
import android.util.Log
import com.darkx.wavenow.utils.Constants
import com.darkx.wavenow.utils.TokenManager
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONObject

/**
 * Manages the single Socket.io connection for the whole app.
 * Authenticates using the same JWT token obtained from /api/auth/login
 * (see server/socket/index.js -> io.use() middleware).
 */
object SocketManager {

    private const val TAG = "WaveNowSocket"
    private var socket: Socket? = null

    fun connect(context: Context) {
        if (socket?.connected() == true) return

        val token = TokenManager(context.applicationContext).getToken()
        if (token.isNullOrEmpty()) {
            Log.e(TAG, "No token found - cannot connect socket")
            return
        }

        try {
            val options = IO.Options()
            options.auth = mapOf("token" to token)
            options.reconnection = true
            options.reconnectionAttempts = Int.MAX_VALUE
            options.reconnectionDelay = 2000

            socket = IO.socket(Constants.SOCKET_URL, options)
            socket?.connect()

            socket?.on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "Connected to WaveNow server")
            }
            socket?.on(Socket.EVENT_DISCONNECT) {
                Log.d(TAG, "Disconnected from server")
            }
            socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                Log.e(TAG, "Connection error: ${args.firstOrNull()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Socket init failed: ${e.message}")
        }
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
    }

    fun getSocket(): Socket? = socket

    // ---- Emit helpers (matches server socket/index.js events) ----

    fun joinChat(chatId: String) {
        socket?.emit("join_chat", chatId)
    }

    fun sendMessage(chatId: String, content: String, type: String = "text") {
        val data = JSONObject().apply {
            put("chatId", chatId)
            put("content", content)
            put("type", type)
        }
        socket?.emit("send_message", data)
    }

    fun sendTyping(chatId: String) {
        val data = JSONObject().apply { put("chatId", chatId) }
        socket?.emit("typing", data)
    }

    fun sendStopTyping(chatId: String) {
        val data = JSONObject().apply { put("chatId", chatId) }
        socket?.emit("stop_typing", data)
    }

    fun markMessageRead(messageId: String, chatId: String) {
        val data = JSONObject().apply {
            put("messageId", messageId)
            put("chatId", chatId)
        }
        socket?.emit("message_read", data)
    }

    // Mtumiaji ameshahifadhi ujumbe local (Room DB) — mwambie server.
    // Ukishafika kwa washiriki wote, server itaufuta kwenye DB yake.
    fun markMessageDelivered(messageId: String, chatId: String) {
        val data = JSONObject().apply {
            put("messageId", messageId)
            put("chatId", chatId)
        }
        socket?.emit("message_delivered", data)
    }

    // ---- Listener helpers ----

    fun onNewMessage(listener: Emitter.Listener) {
        socket?.on("new_message", listener)
    }

    fun onUserOnline(listener: Emitter.Listener) {
        socket?.on("user_online", listener)
    }

    fun onUserOffline(listener: Emitter.Listener) {
        socket?.on("user_offline", listener)
    }

    fun onTyping(listener: Emitter.Listener) {
        socket?.on("typing", listener)
    }

    fun onStopTyping(listener: Emitter.Listener) {
        socket?.on("stop_typing", listener)
    }

    fun onMessageRead(listener: Emitter.Listener) {
        socket?.on("message_read", listener)
    }

    fun onMessageStatus(listener: Emitter.Listener) {
        socket?.on("message_status", listener)
    }

    fun off(event: String) {
        socket?.off(event)
    }
}
