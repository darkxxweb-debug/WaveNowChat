package com.darkx.wavenow.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.darkx.wavenow.adapters.MessageAdapter
import com.darkx.wavenow.databinding.ActivityChatBinding
import com.darkx.wavenow.local.AppDatabase
import com.darkx.wavenow.local.toEntity
import com.darkx.wavenow.local.toMessage
import com.darkx.wavenow.models.Message
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
    private lateinit var db: AppDatabase

    private var chatId: String = ""
    private var myUserId: String = ""
    private var otherUserId: String? = null
    private var chatType: String = "direct"
    private var canPost: Boolean = true // kwa channel: admin/owner pekee

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        tokenManager = TokenManager(this)
        myUserId = tokenManager.getUser()?.resolvedId() ?: ""
        db = AppDatabase.getInstance(this)

        chatId = intent.getStringExtra("chatId") ?: ""
        otherUserId = intent.getStringExtra("otherUserId")
        chatType = intent.getStringExtra("chatType") ?: "direct"
        val chatName = intent.getStringExtra("chatName") ?: "Chat"

        supportActionBar?.title = chatName
        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = MessageAdapter(mutableListOf(), myUserId)
        binding.recyclerMessages.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        binding.recyclerMessages.adapter = adapter

        binding.btnSend.setOnClickListener { sendMessage() }

        SocketManager.joinChat(chatId)
        listenForIncomingMessages()
        listenForMessageStatus()

        // Onyesha kwanza ujumbe uliohifadhiwa local (haraka, hata bila internet)
        loadLocalHistory()

        if (chatType == "channel") checkChannelPostingPermission()

        loadMessageHistory()
    }

    override fun onDestroy() {
        super.onDestroy()
        SocketManager.off("new_message")
        SocketManager.off("message_status")
    }

    private fun checkChannelPostingPermission() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getApi(this@ChatActivity).getChat(chatId)
                val chat = response.body() ?: return@launch
                canPost = chat.isAdmin(myUserId)
                if (!canPost) {
                    binding.inputMessage.isEnabled = false
                    binding.inputMessage.hint = "Admin pekee ndiye anaweza kutuma kwenye channel hii"
                    binding.btnSend.visibility = View.GONE
                }
            } catch (_: Exception) { /* tuache default: ruhusiwa kuandika */ }
        }
    }

    private fun loadLocalHistory() {
        lifecycleScope.launch {
            val local = db.messageDao().getMessages(chatId)
            if (local.isNotEmpty()) {
                adapter.setMessages(local.map { it.toMessage() })
                scrollToBottom()
            }
        }
    }

    private fun loadMessageHistory() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getApi(this@ChatActivity).getMessages(chatId)
                if (response.isSuccessful) {
                    val messages = response.body() ?: emptyList()
                    // Server inaonyesha tu ujumbe ambao bado haujafika kwa washiriki wote.
                    // Tunauonyesha na kuuhifadhi local, kisha kumjulisha server "nimepokea"
                    // ili aweze kuufuta (mradi tayari uko salama hapa simu).
                    messages.forEach { msg ->
                        db.messageDao().insert(msg.toEntity(chatId, myUserId))
                        if (msg.sender?.resolvedId() != myUserId) {
                            msg.id?.let { SocketManager.markMessageDelivered(it, chatId) }
                        }
                    }
                    val merged = db.messageDao().getMessages(chatId)
                    adapter.setMessages(merged.map { it.toMessage() })
                    scrollToBottom()
                }
            } catch (_: Exception) {
                // Historia ya server imeshindwa kupakia — ile ya local inabaki kuonekana
            }
        }
    }

    private fun sendMessage() {
        if (chatType == "channel" && !canPost) {
            Toast.makeText(this, "Admin/mmiliki wa channel pekee ndiye anaweza kutuma", Toast.LENGTH_SHORT).show()
            return
        }

        val text = binding.inputMessage.text.toString().trim()
        if (text.isEmpty() || chatId.isEmpty()) return

        binding.inputMessage.text?.clear()

        // Tuma kwa real-time kupitia socket (server pia inaihifadhi MongoDB kwa muda,
        // hadi ifike kwa washiriki wote wa chat)
        SocketManager.sendMessage(chatId, text)
    }

    private fun listenForIncomingMessages() {
        SocketManager.onNewMessage { args ->
            val data = args.firstOrNull() as? JSONObject ?: return@onNewMessage
            runOnUiThread {
                try {
                    val message = Gson().fromJson(data.toString(), Message::class.java)
                    if (message.chat == chatId) {
                        lifecycleScope.launch {
                            // Hifadhi local KWANZA — hii ndiyo inayothibitisha ujumbe
                            // hautapotea hata baada ya server kuufuta.
                            db.messageDao().insert(message.toEntity(chatId, myUserId))
                            adapter.addMessage(message)
                            scrollToBottom()

                            // Mwambie server "nimepokea na kuhifadhi" — ukishafika kwa wote,
                            // ataufuta DB yake moja kwa moja.
                            if (message.sender?.resolvedId() != myUserId) {
                                message.id?.let { SocketManager.markMessageDelivered(it, chatId) }
                            }
                        }
                    }
                } catch (_: Exception) {
                    // Ignore malformed payloads
                }
            }
        }
    }

    private fun listenForMessageStatus() {
        // Hii ni taarifa tu kwamba server imeufuta ujumbe kwenye DB yake — hauathiri
        // kilichohifadhiwa hapa local kwenye simu, kwa hiyo hatufanyi lolote kwa UI.
        SocketManager.onMessageStatus { }
    }

    private fun scrollToBottom() {
        binding.recyclerMessages.post {
            if (adapter.itemCount > 0) {
                binding.recyclerMessages.scrollToPosition(adapter.itemCount - 1)
            }
        }
    }
}
