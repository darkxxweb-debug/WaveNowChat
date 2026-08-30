package com.darkx.wavenow.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.darkx.wavenow.databinding.ItemMessageReceivedBinding
import com.darkx.wavenow.databinding.ItemMessageSentBinding
import com.darkx.wavenow.models.Message

class MessageAdapter(
    private var messages: MutableList<Message>,
    private val myUserId: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_SENT = 1
        private const val TYPE_RECEIVED = 2
    }

    inner class SentViewHolder(val binding: ItemMessageSentBinding) : RecyclerView.ViewHolder(binding.root)
    inner class ReceivedViewHolder(val binding: ItemMessageReceivedBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int {
        val msg = messages[position]
        val senderId = msg.sender?.resolvedId() ?: ""
        return if (senderId == myUserId) TYPE_SENT else TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_SENT) {
            SentViewHolder(ItemMessageSentBinding.inflate(inflater, parent, false))
        } else {
            ReceivedViewHolder(ItemMessageReceivedBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = messages[position]
        val time = msg.createdAt?.take(16)?.replace("T", " ")?.takeLast(5) ?: ""

        when (holder) {
            is SentViewHolder -> {
                holder.binding.txtMessage.text = msg.content
                holder.binding.txtTime.text = time
            }
            is ReceivedViewHolder -> {
                holder.binding.txtMessage.text = msg.content
                holder.binding.txtTime.text = time
            }
        }
    }

    override fun getItemCount(): Int = messages.size

    fun setMessages(newMessages: List<Message>) {
        messages = newMessages.toMutableList()
        notifyDataSetChanged()
    }

    fun addMessage(message: Message) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }
}
