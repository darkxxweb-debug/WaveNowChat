package com.darkx.wavenow.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.darkx.wavenow.R
import com.darkx.wavenow.adapters.ChatListAdapter
import com.darkx.wavenow.databinding.ActivityMainBinding
import com.darkx.wavenow.models.Chat
import com.darkx.wavenow.models.CreateChatRequest
import com.darkx.wavenow.models.FcmTokenRequest
import com.darkx.wavenow.network.RetrofitClient
import com.darkx.wavenow.network.SocketManager
import com.darkx.wavenow.utils.TokenManager
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var tokenManager: TokenManager
    private lateinit var adapter: ChatListAdapter
    private var myUserId: String = ""
    private var allChats: List<Chat> = emptyList()
    private var currentFilter: String = "all" // all | group | channel

    private val notifPermissionLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { /* haijalishi jibu — app itaendelea kufanya kazi bila push ikikataliwa */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)
        myUserId = tokenManager.getUser()?.resolvedId() ?: ""

        adapter = ChatListAdapter(emptyList(), myUserId) { chat -> openChat(chat) }
        binding.recyclerChats.layoutManager = LinearLayoutManager(this)
        binding.recyclerChats.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener { loadChats() }

        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim() ?: ""
                if (query.length >= 3) searchAndOfferChat(query) else applyFilter()
            }
        })

        binding.tabGroup.setOnCheckedChangeListener { _, checkedId ->
            currentFilter = when (checkedId) {
                R.id.tabGroups -> "group"
                R.id.tabChannels -> "channel"
                else -> "all"
            }
            applyFilter()
        }

        binding.fabNew.setOnClickListener { showNewMenu() }

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navChats -> true
                else -> {
                    Toast.makeText(this, "Inakuja hivi karibuni", Toast.LENGTH_SHORT).show()
                    false
                }
            }
        }

        askNotificationPermissionIfNeeded()
        registerFcmToken()

        SocketManager.connect(this)
        listenForRealtimeUpdates()
        loadChats()
    }

    override fun onResume() {
        super.onResume()
        SocketManager.connect(this)
        loadChats()
    }

    private fun showNewMenu() {
        val popup = PopupMenu(this, binding.fabNew)
        popup.menu.add(0, 1, 0, getString(R.string.new_chat))
        popup.menu.add(0, 2, 1, getString(R.string.new_group))
        popup.menu.add(0, 3, 2, getString(R.string.new_channel))
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                2 -> startActivity(Intent(this, CreateGroupActivity::class.java))
                3 -> startActivity(Intent(this, CreateChannelActivity::class.java))
                else -> Toast.makeText(this, "Andika jina la mtumiaji kwenye search", Toast.LENGTH_SHORT).show()
            }
            true
        }
        popup.show()
    }

    private fun askNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun registerFcmToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            lifecycleScope.launch {
                try {
                    RetrofitClient.getApi(this@MainActivity).updateFcmToken(FcmTokenRequest(token))
                } catch (_: Exception) { /* itajaribu tena baadaye */ }
            }
        }
    }

    private fun loadChats() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getApi(this@MainActivity).getChats()
                if (response.isSuccessful) {
                    allChats = response.body() ?: emptyList()
                    applyFilter()
                }
            } catch (_: Exception) {
                // Silent fail on refresh; user can pull-to-refresh again
            } finally {
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun applyFilter() {
        val filtered = when (currentFilter) {
            "group" -> allChats.filter { it.isGroup() }
            "channel" -> allChats.filter { it.isChannel() }
            else -> allChats
        }
        adapter.updateData(filtered)
        binding.emptyState.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
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
        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra("chatId", chat.id)
            putExtra("chatName", chat.displayName(myUserId))
            putExtra("chatType", chat.type)
            putExtra("otherUserId", chat.otherParticipant(myUserId)?.resolvedId())
        }
        startActivity(intent)
    }

    private fun listenForRealtimeUpdates() {
        SocketManager.onNewMessage { runOnUiThread { loadChats() } }
        SocketManager.onUserOnline { runOnUiThread { loadChats() } }
        SocketManager.onUserOffline { runOnUiThread { loadChats() } }
    }
}
