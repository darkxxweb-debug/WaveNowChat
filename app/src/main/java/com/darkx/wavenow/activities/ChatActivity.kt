package com.darkx.wavenow.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.darkx.wavenow.adapters.MessageAdapter
import com.darkx.wavenow.databinding.ActivityChatBinding
import com.darkx.wavenow.models.Message
import com.darkx.wavenow.models.SendMessageRequest
import com.darkx.wavenow.network.RetrofitClient
import com.darkx.wavenow.network.SocketManager
import com.darkx.wavenow.utils.TokenManager
import com.google.gson.Gson
import kotlinx.coroutines.launch
import org.json.JSONObject

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var tokenManager: TokenManager
    private lateinit var adapter: MessageAdapter

    private var chatId: String = ""
    private var myUserId: String = ""
    private var otherUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        tokenManager = TokenManager(this)
        myUserId = tokenManager.getUser()?.resolvedId() ?: ""

        chatId = intent.getStringExtra("chatId") ?: ""
        otherUserId = intent.getStringExtra("otherUserId")
        val chatName = intent.getStringExtra("chatName") ?: "Chat"

        supportActionBar?.title = chatName
        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = MessageAdapter(mutableListOf(), myUserId)
        binding.recyclerMessages.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        binding.recyclerMessages.adapter = adapter

        binding.btnSend.setOnClickListener { sendMessage() }

        // Join this chat's socket room so we receive messages instantly
        SocketManager.joinChat(chatId)
        listenForIncomingMessages()

        loadMessageHistory()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop listening for this chat's messages to avoid duplicate listeners
        // when the user opens multiple chats one after another
        SocketManager.off("new_message")
    }

    private fun loadMessageHistory() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getApi(this@ChatActivity).getMessages(chatId)
                if (response.isSuccessful) {
                    val messages = response.body() ?: emptyList()
                    adapter.setMessages(messages)
                    scrollToBottom()
                }
            } catch (_: Exception) {
                // History failed to load — user can still send new messages
            }
        }
    }

    private fun sendMessage() {
        val text = binding.inputMessage.text.toString().trim()
        if (text.isEmpty() || chatId.isEmpty()) return

        binding.inputMessage.text?.clear()

        // Send in real time via socket (server also persists it to MongoDB)
        SocketManager.sendMessage(chatId, text)
    }

    private fun listenForIncomingMessages() {
        SocketManager.onNewMessage { args ->
            val data = args.firstOrNull() as? JSONObject ?: return@onNewMessage
            runOnUiThread {
                try {
                    val message = Gson().fromJson(data.toString(), Message::class.java)
                    // Only add if it belongs to the chat currently open
                    if (message.chat == chatId) {
                        adapter.addMessage(message)
                        scrollToBottom()
                    }
                } catch (_: Exception) {
                    // Ignore malformed payloads
                }
            }
        }
    }

    private fun scrollToBottom() {
        binding.recyclerMessages.post {
            if (adapter.itemCount > 0) {
                binding.recyclerMessages.scrollToPosition(adapter.itemCount - 1)
            }
        }
    }
}
