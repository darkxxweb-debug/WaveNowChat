package com.darkx.wavenow.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.darkx.wavenow.adapters.ChatListAdapter
import com.darkx.wavenow.databinding.ActivityMainBinding
import com.darkx.wavenow.models.Chat
import com.darkx.wavenow.models.CreateChatRequest
import com.darkx.wavenow.network.RetrofitClient
import com.darkx.wavenow.network.SocketManager
import com.darkx.wavenow.utils.TokenManager
import kotlinx.coroutines.launch
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var tokenManager: TokenManager
    private lateinit var adapter: ChatListAdapter
    private var myUserId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        tokenManager = TokenManager(this)
        myUserId = tokenManager.getUser()?.resolvedId() ?: ""

        adapter = ChatListAdapter(emptyList(), myUserId) { chat ->
            openChat(chat)
        }
        binding.recyclerChats.layoutManager = LinearLayoutManager(this)
        binding.recyclerChats.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener { loadChats() }

        // Search: as the user types a username, offer to start a chat with the first match
        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim() ?: ""
                if (query.length >= 3) searchAndOfferChat(query)
            }
        })

        SocketManager.connect(this)
        listenForRealtimeUpdates()
        loadChats()
    }

    override fun onResume() {
        super.onResume()
        SocketManager.connect(this)
        loadChats()
    }

    private fun loadChats() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getApi(this@MainActivity).getChats()
                if (response.isSuccessful) {
                    val chats = response.body() ?: emptyList()
                    adapter.updateData(chats)
                    binding.emptyState.visibility = if (chats.isEmpty()) View.VISIBLE else View.GONE
                }
            } catch (_: Exception) {
                // Silent fail on refresh; user can pull-to-refresh again
            } finally {
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun searchAndOfferChat(query: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getApi(this@MainActivity).searchUsers(query)
                val users = response.body() ?: return@launch
                val match = users.firstOrNull { it.username.equals(query, ignoreCase = true) } ?: return@launch

                val chatResponse = RetrofitClient.getApi(this@MainActivity)
                    .createChat(CreateChatRequest(participantId = match.resolvedId()))
                chatResponse.body()?.let { openChat(it) }
            } catch (_: Exception) {
                // ignore - user can keep typing
            }
        }
    }

    private fun openChat(chat: Chat) {
        val other = chat.otherParticipant(myUserId)
        val intent = android.content.Intent(this, ChatActivity::class.java).apply {
            putExtra("chatId", chat.id)
            putExtra("chatName", if (chat.isGroup) chat.groupName else (other?.displayName ?: other?.username))
            putExtra("otherUserId", other?.resolvedId())
        }
        startActivity(intent)
    }

    private fun listenForRealtimeUpdates() {
        // Refresh the chat list whenever a new message arrives anywhere,
        // and whenever a contact's online status changes
        SocketManager.onNewMessage { runOnUiThread { loadChats() } }
        SocketManager.onUserOnline { runOnUiThread { loadChats() } }
        SocketManager.onUserOffline { runOnUiThread { loadChats() } }
    }
}
