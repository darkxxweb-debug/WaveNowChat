package com.darkx.wavenow.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.darkx.wavenow.databinding.ActivityLoginBinding
import com.darkx.wavenow.models.LoginRequest
import com.darkx.wavenow.network.RetrofitClient
import com.darkx.wavenow.network.SocketManager
import com.darkx.wavenow.utils.TokenManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)

        binding.btnLogin.setOnClickListener { attemptLogin() }
        binding.goToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun attemptLogin() {
        val username = binding.inputUsername.text.toString().trim()
        val password = binding.inputPassword.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(com.darkx.wavenow.R.string.error_fields_required), Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getApi(this@LoginActivity)
                    .login(LoginRequest(username, password))

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    // Save the JWT token — this is what connects the app to the server from now on
                    tokenManager.saveToken(body.token)
                    tokenManager.saveUser(body.user)

                    SocketManager.connect(this@LoginActivity)

                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "Login failed: check your credentials", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, getString(com.darkx.wavenow.R.string.error_connection), Toast.LENGTH_SHORT).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !loading
    }
}
