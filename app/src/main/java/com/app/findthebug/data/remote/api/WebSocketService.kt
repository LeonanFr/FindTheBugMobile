package com.app.findthebug.data.remote.api

import android.util.Log
import com.app.findthebug.core.common.Constants
import com.app.findthebug.data.remote.model.websocket.WebSocketMessage
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

@Singleton
class WebSocketService @Inject constructor() {
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(WebSocketMessage::class.java, WebSocketMessage.Adapter())
        .create()

    @Volatile private var webSocket: WebSocket? = null
    @Volatile private var client: OkHttpClient? = null

    private val _messages = MutableSharedFlow<WebSocketMessage>(
        replay = 1,
        extraBufferCapacity = 64
    )

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

    private val pendingJson = ArrayDeque<String>()
    private val pendingLock = Any()
    private val reconnectScheduled = AtomicBoolean(false)

    fun connect() {
        val state = connectionState.value
        if (state is ConnectionState.CONNECTED || state is ConnectionState.CONNECTING) return

        _connectionState.value = ConnectionState.CONNECTING

        coroutineScope.launch {
            try {
                val wsClient = OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(0, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .pingInterval(30, TimeUnit.SECONDS)
                    .build()

                client = wsClient

                val request = Request.Builder()
                    .url(Constants.WS_URL)
                    .build()

                val listener = createWebSocketListener()
                webSocket = wsClient.newWebSocket(request, listener)

                val ok = withTimeoutOrNull(10_000) {
                    connectionState.filter { it is ConnectionState.CONNECTED }.first()
                    true
                } ?: false

                if (!ok) {
                    _connectionState.value = ConnectionState.ERROR("Connection timeout")
                    cleanup()
                }
            } catch (e: Exception) {
                _connectionState.value = ConnectionState.ERROR("Connection failed: ${e.message}")
                Log.e("WebSocketService", "Connection error", e)
                scheduleReconnect()
            }
        }
    }

    private fun createWebSocketListener(): WebSocketListener {
        return object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocketService", "WebSocket connected")
                this@WebSocketService.webSocket = webSocket
                _connectionState.value = ConnectionState.CONNECTED
                flushPending()
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
                scheduleReconnect()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocketService", "WebSocket failure", t)
                _connectionState.value = ConnectionState.ERROR("Connection failed: ${t.message}")
                cleanup()
                scheduleReconnect()
            }
        }
    }

    private fun scheduleReconnect() {
        if (!reconnectScheduled.compareAndSet(false, true)) return

        coroutineScope.launch {
            try {
                delay(Constants.WS_RECONNECT_DELAY)
                reconnectScheduled.set(false)

                val state = connectionState.value
                val hasPending = synchronized(pendingLock) { pendingJson.isNotEmpty() }

                if ((state !is ConnectionState.CONNECTED && state !is ConnectionState.CONNECTING) && hasPending) {
                    connect()
                }
            } catch (_: Exception) {
                reconnectScheduled.set(false)
            }
        }
    }

    fun sendMessage(message: WebSocketMessage): Boolean {
        return try {
            val json = gson.toJson(message)

            val state = connectionState.value
            val ws = webSocket

            if (state is ConnectionState.CONNECTED && ws != null) {
                val sent = ws.send(json)
                if (!sent) {
                    enqueue(json)
                    _connectionState.value = ConnectionState.ERROR("Send failed")
                    scheduleReconnect()
                }
                sent
            } else {
                enqueue(json)
                connect()
                true
            }
        } catch (e: Exception) {
            Log.e("WebSocketService", "Error sending message", e)
            false
        }
    }

    private fun enqueue(json: String) {
        synchronized(pendingLock) {
            pendingJson.addLast(json)
            val max = 256
            if (pendingJson.size > max) {
                repeat(pendingJson.size - max) { pendingJson.removeFirst() }
            }
        }
    }

    private fun flushPending() {
        val ws = webSocket ?: return
        if (connectionState.value !is ConnectionState.CONNECTED) return

        coroutineScope.launch {
            val backoffMs = 50L
            while (true) {
                val next: String? = synchronized(pendingLock) {
                    if (pendingJson.isEmpty()) null else pendingJson.removeFirst()
                }

                if (next == null) break

                val ok = try {
                    ws.send(next)
                } catch (_: Exception) {
                    false
                }

                if (!ok) {
                    enqueue(next)
                    _connectionState.value = ConnectionState.ERROR("Send failed")
                    scheduleReconnect()
                    delay(backoffMs)
                    min(backoffMs * 2, 1000L)
                    break
                }
            }
        }
    }

    suspend inline fun <reified T : WebSocketMessage> waitForMessage(
        noinline predicate: (T) -> Boolean = { true },
        timeoutMillis: Long = 10_000
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

    fun disconnect() {
        try {
            webSocket?.close(1000, "Normal closure")
        } catch (_: Exception) {
        }
        cleanup()
        _connectionState.value = ConnectionState.DISCONNECTED
    }

    private fun cleanup() {
        webSocket = null
        client?.dispatcher?.executorService?.shutdown()
        client = null
    }

    fun isConnected(): Boolean = connectionState.value is ConnectionState.CONNECTED
}
