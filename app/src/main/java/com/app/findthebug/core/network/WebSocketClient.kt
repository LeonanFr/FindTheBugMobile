package com.app.findthebug.core.network

import android.util.Log
import com.app.findthebug.core.common.Constants
import com.google.gson.Gson
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.*
import java.util.concurrent.TimeUnit

class WebSocketClient {
    private val gson = Gson()
    private var webSocket: WebSocket? = null
    private var listener: WebSocketListener? = null

    fun connect(): Flow<String> = callbackFlow {
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.SECONDS) // No timeout for WebSocket
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()

        val request = Request.Builder()
            .url(Constants.WS_URL)
            .build()

        listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocket", "Connected")
                trySend("CONNECTED")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocket", "Received: $text")
                trySend(text)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocket", "Closing: $code - $reason")
                close()
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocket", "Closed: $code - $reason")
                close()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocket", "Error", t)
                close(t)
            }
        }

        webSocket = client.newWebSocket(request, listener!!)

        awaitClose {
            webSocket?.close(1000, "Closing")
            client.dispatcher.executorService.shutdown()
        }
    }

    fun sendMessage(message: Any): Boolean {
        return try {
            val json = gson.toJson(message)
            Log.d("WebSocket", "Sending: $json")
            webSocket?.send(json)
            true
        } catch (e: Exception) {
            Log.e("WebSocket", "Error sending message", e)
            false
        }
    }

    fun disconnect() {
        webSocket?.close(1000, "Disconnecting")
        webSocket = null
        listener = null
    }

    fun isConnected(): Boolean = webSocket != null
}