package com.darkx.wavenow.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.darkx.wavenow.databinding.ActivityCreateChannelBinding
import com.darkx.wavenow.models.CreateChannelRequest
import com.darkx.wavenow.network.RetrofitClient
import kotlinx.coroutines.launch

class CreateChannelActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateChannelBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateChannelBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnCreate.setOnClickListener { createChannel() }
    }

    private fun createChannel() {
        val name = binding.inputChannelName.text?.toString()?.trim() ?: ""
        val description = binding.inputChannelDescription.text?.toString()?.trim() ?: ""

        if (name.isEmpty()) {
            Toast.makeText(this, "Weka jina la channel", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val api = RetrofitClient.getApi(this@CreateChannelActivity)
                val response = api.createChannel(CreateChannelRequest(name = name, description = description))
                if (response.isSuccessful) {
                    Toast.makeText(this@CreateChannelActivity, "Channel imeundwa", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@CreateChannelActivity, "Imeshindwa kuunda channel", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CreateChannelActivity, "Hitilafu: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
