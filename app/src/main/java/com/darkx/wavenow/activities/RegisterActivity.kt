package com.darkx.wavenow.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.darkx.wavenow.databinding.ActivityRegisterBinding
import com.darkx.wavenow.models.RegisterRequest
import com.darkx.wavenow.network.RetrofitClient
import com.darkx.wavenow.network.SocketManager
import com.darkx.wavenow.utils.TokenManager
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)

        binding.btnRegister.setOnClickListener { attemptRegister() }
        binding.goToLogin.setOnClickListener { finish() }
    }

    private fun attemptRegister() {
        val displayName = binding.inputDisplayName.text.toString().trim()
        val username = binding.inputUsername.text.toString().trim()
        val phone = binding.inputPhone.text.toString().trim()
        val password = binding.inputPassword.text.toString().trim()

        if (username.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(com.darkx.wavenow.R.string.error_fields_required), Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getApi(this@RegisterActivity).register(
                    RegisterRequest(username, phone, password, displayName.ifEmpty { username })
                )

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    tokenManager.saveToken(body.token)
                    tokenManager.saveUser(body.user)

                    SocketManager.connect(this@RegisterActivity)

                    startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@RegisterActivity, "Registration failed: username or phone may already be taken", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@RegisterActivity, getString(com.darkx.wavenow.R.string.error_connection), Toast.LENGTH_SHORT).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !loading
    }
}
