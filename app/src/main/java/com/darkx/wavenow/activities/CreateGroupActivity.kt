package com.darkx.wavenow.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.darkx.wavenow.databinding.ActivityCreateGroupBinding
import com.darkx.wavenow.models.CreateGroupRequest
import com.darkx.wavenow.network.RetrofitClient
import kotlinx.coroutines.launch

class CreateGroupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateGroupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateGroupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnCreate.setOnClickListener { createGroup() }
    }

    private fun createGroup() {
        val name = binding.inputGroupName.text?.toString()?.trim() ?: ""
        val usernamesRaw = binding.inputParticipants.text?.toString()?.trim() ?: ""

        if (name.isEmpty()) {
            Toast.makeText(this, "Weka jina la group", Toast.LENGTH_SHORT).show()
            return
        }

        val usernames = usernamesRaw.split(",").map { it.trim() }.filter { it.isNotEmpty() }

        lifecycleScope.launch {
            try {
                val api = RetrofitClient.getApi(this@CreateGroupActivity)

                // Tafuta ID ya kila mtumiaji kwa jina lake
                val participantIds = mutableListOf<String>()
                for (username in usernames) {
                    val res = api.searchUsers(username)
                    val match = res.body()?.firstOrNull { it.username.equals(username, ignoreCase = true) }
                    if (match != null) participantIds.add(match.resolvedId())
                }

                val response = api.createGroup(CreateGroupRequest(name = name, participantIds = participantIds))
                if (response.isSuccessful) {
                    Toast.makeText(this@CreateGroupActivity, "Group imeundwa", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@CreateGroupActivity, "Imeshindwa kuunda group", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CreateGroupActivity, "Hitilafu: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
