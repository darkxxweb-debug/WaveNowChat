package com.darkx.wavenow.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.darkx.wavenow.R
import com.darkx.wavenow.databinding.ItemChatBinding
import com.darkx.wavenow.models.Chat

class ChatListAdapter(
    private var chats: List<Chat>,
    private val myUserId: String,
    private val onClick: (Chat) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(val binding: ItemChatBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chats[position]
        val other = chat.otherParticipant(myUserId)

        val displayName = if (chat.isGroup) chat.groupName ?: "Group" else (other?.displayName ?: other?.username ?: "Unknown")
        holder.binding.txtName.text = displayName
        holder.binding.txtLastMessage.text = chat.lastMessage?.content ?: "Say hi 👋"

        chat.lastMessage?.createdAt?.let {
            holder.binding.txtTime.text = it.take(16).replace("T", " ").takeLast(5)
        } ?: run { holder.binding.txtTime.text = "" }

        holder.binding.onlineDot.visibility = if (!chat.isGroup && other?.isOnline == true) View.VISIBLE else View.GONE

        val avatarUrl = if (chat.isGroup) chat.groupAvatar else other?.avatarUrl
        if (!avatarUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(avatarUrl)
                .placeholder(R.drawable.ic_person)
                .circleCrop()
                .into(holder.binding.imgAvatar)
        } else {
            holder.binding.imgAvatar.setImageResource(R.drawable.ic_person)
        }

        holder.itemView.setOnClickListener { onClick(chat) }
    }

    override fun getItemCount(): Int = chats.size

    fun updateData(newChats: List<Chat>) {
        chats = newChats
        notifyDataSetChanged()
    }
}
