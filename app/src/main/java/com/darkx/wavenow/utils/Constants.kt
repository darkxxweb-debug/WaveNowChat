package com.darkx.wavenow.utils

object Constants {
    /**
     * ============================================================
     *  MUHIMU: Weka hapa URL ya server yako baada ya ku-deploy Render
     * ============================================================
     * Mfano: "https://darkx-chat-server.onrender.com/"
     * (lazima iishie na "/" kwa Retrofit)
     */
    const val BASE_URL = "https://serveryawavenowchat.onrender.com"

    // Socket.io hutumia URL ile ile bila "/" mwishoni
    val SOCKET_URL: String get() = BASE_URL.removeSuffix("/")
}
