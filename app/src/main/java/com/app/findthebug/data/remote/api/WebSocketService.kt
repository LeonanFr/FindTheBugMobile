package com.app.findthebug.data.remote.api

import android.util.Log
import com.app.findthebug.core.common.Constants
import com.app.findthebug.core.common.Result
import com.app.findthebug.data.remote.model.websocket.WebSocketMessage
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okhttp3.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSocketService @Inject constructor() {
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(WebSocketMessage::class.java, WebSocketMessage.Adapter())
        .create()

    private var webSocket: WebSocket? = null
    private var client: OkHttpClient? = null
    private var isConnecting = false

    private val _messages = MutableSharedFlow<WebSocketMessage>(extraBufferCapacity = 64)
    val messages: Flow<WebSocketMessage> = _messages.asSharedFlow()

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    sealed class ConnectionState {
        object DISCONNECTED : ConnectionState()
        object CONNECTING : ConnectionState()
        object CONNECTED : ConnectionState()
        data class ERROR(val message: String) : ConnectionState()
    }

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun connect() {
        if (connectionState.value is ConnectionState.CONNECTED || isConnecting) {
            return
        }

        isConnecting = true
        _connectionState.value = ConnectionState.CONNECTING

        coroutineScope.launch {
            try {
                webSocket?.cancel()
                cleanup()

                val wsClient = OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .pingInterval(30, TimeUnit.SECONDS)
                    .build()

                client = wsClient
                val request = Request.Builder().url(Constants.WS_URL).build()
                webSocket = wsClient.newWebSocket(request, createWebSocketListener())

                withTimeoutOrNull(10000) {
                    connectionState.filter { it is ConnectionState.CONNECTED }.first()
                } ?: run {
                    if (isConnecting) {
                        _connectionState.value = ConnectionState.ERROR("Timeout na conexão")
                        disconnect()
                    }
                }
            } catch (e: Exception) {
                _connectionState.value = ConnectionState.ERROR("Falha: ${e.message}")
                isConnecting = false
            }
        }
    }

    fun disconnect() {
        isConnecting = false
        webSocket?.cancel()
        cleanup()
        _connectionState.value = ConnectionState.DISCONNECTED
    }

    private fun cleanup() {
        webSocket = null
        client?.dispatcher?.executorService?.shutdownNow()
        client = null
    }

    private fun createWebSocketListener(): WebSocketListener {
        return object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocketService", "WebSocket connected")
                _connectionState.value = ConnectionState.CONNECTED
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocketService", "Received: $text")
                coroutineScope.launch {
                    try {
                        val message = gson.fromJson(text, WebSocketMessage::class.java)
                        _messages.tryEmit(message)
                    } catch (e: JsonSyntaxException) {
                        Log.e("WebSocketService", "Failed to parse message: $text", e)
                    } catch (e: Exception) {
                        Log.e("WebSocketService", "Error emitting message", e)
                    }
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocketService", "Closing: $code - $reason")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocketService", "Closed: $code - $reason")
                _connectionState.value = ConnectionState.DISCONNECTED
                cleanup()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocketService", "WebSocket failure", t)
                _connectionState.value = ConnectionState.ERROR("Connection failed: ${t.message}")
            }
        }
    }

    private fun scheduleReconnect() {
        coroutineScope.launch {
            delay(Constants.WS_RECONNECT_DELAY)
            if (connectionState.value !is ConnectionState.CONNECTED) {
                connect()
            }
        }
    }
    fun sendMessage(message: WebSocketMessage): Boolean {
        return try {
            if (connectionState.value !is ConnectionState.CONNECTED) {
                Log.w("WebSocketService", "Cannot send message, not connected")
                return false
            }

            val json = gson.toJson(message)
            Log.d("WebSocketService", "Sending: $json")
            val result = webSocket?.send(json) ?: false

            if (!result) {
                Log.e("WebSocketService", "Failed to send message: socket closed or buffer full")
                _connectionState.value = ConnectionState.DISCONNECTED
                webSocket = null
            }

            result
        } catch (e: Exception) {
            Log.e("WebSocketService", "Error sending message - Pipe may be broken", e)
            _connectionState.value = ConnectionState.ERROR(e.message ?: "Broken Pipe")
            webSocket = null
            false
        }
    }

    suspend inline fun <reified T : WebSocketMessage> sendMessageAndWaitForResponse(
        request: WebSocketMessage,
        timeoutMillis: Long = 10000
    ): Result<T> = withContext(Dispatchers.IO) {
        if (!sendMessage(request)) {
            return@withContext Result.Error("Failed to send message")
        }

        try {
            withTimeout(timeoutMillis) {
                messages.filter { message ->
                    message is T || message is WebSocketMessage.ErrorResponse
                }.first().let { message ->
                    when (message) {
                        is T -> Result.Success(message)
                        is WebSocketMessage.ErrorResponse -> Result.Error(message.message)
                        else -> Result.Error("Unexpected message type")
                    }
                }
            }
        } catch (e: TimeoutCancellationException) {
            Result.Error("Timeout waiting for response")
        } catch (e: Exception) {
            Result.Error("Error: ${e.message}")
        }
    }

    suspend inline fun <reified T : WebSocketMessage> waitForMessage(
        noinline predicate: (T) -> Boolean = { true },
        timeoutMillis: Long = 10000
    ): T? = withContext(Dispatchers.IO) {
        try {
            withTimeout(timeoutMillis) {
                messages
                    .filterIsInstance<T>()
                    .filter(predicate)
                    .first()
            }
        } catch (_: TimeoutCancellationException) {
            null
        } catch (_: Exception) {
            null
        }
    }

    fun isConnected(): Boolean = connectionState.value is ConnectionState.CONNECTED
}