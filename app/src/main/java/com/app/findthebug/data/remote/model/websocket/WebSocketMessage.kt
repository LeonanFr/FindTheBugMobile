package com.app.findthebug.data.remote.model.websocket

import com.google.gson.*
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

sealed class WebSocketMessage {
    abstract val type: String

    // === REQUESTS ===
    data class CreateLobbyRequest(
        @SerializedName("type") override val type: String = "CREATE_LOBBY",
        @SerializedName("playerName") val playerName: String
    ) : WebSocketMessage()

    data class JoinAsPlayerRequest(
        @SerializedName("type") override val type: String = "JOIN_AS_PLAYER",
        @SerializedName("sessionId") val sessionId: String,
        @SerializedName("playerName") val playerName: String
    ) : WebSocketMessage()

    data class GetLobbyInfoRequest(
        @SerializedName("type") override val type: String = "GET_LOBBY_INFO",
        @SerializedName("sessionId") val sessionId: String
    ) : WebSocketMessage()

    data class StartGameRequest(
        @SerializedName("type") override val type: String = "START_GAME",
        @SerializedName("sessionId") val sessionId: String,
        @SerializedName("playerName") val playerName: String,
        @SerializedName("caseId") val caseId: String? = null
    ) : WebSocketMessage()

    data class GameActionRequest(
        @SerializedName("type") override val type: String = "GAME_ACTION",
        @SerializedName("sessionId") val sessionId: String,
        @SerializedName("playerId") val playerId: String,
        @SerializedName("actionType") val actionType: Int,
        @SerializedName("targetId") val targetId: String
    ) : WebSocketMessage()

    data class SubmitSolutionRequest(
        @SerializedName("type") override val type: String = "SUBMIT_SOLUTION",
        @SerializedName("sessionId") val sessionId: String,
        @SerializedName("answers") val answers: List<String>
    ) : WebSocketMessage()

    data class SaveNoteRequest(
        @SerializedName("type") override val type: String = "SAVE_NOTE",
        @SerializedName("sessionId") val sessionId: String,
        @SerializedName("playerId") val playerId: String,
        @SerializedName("clueId") val clueId: String,
        @SerializedName("content") val content: String
    ) : WebSocketMessage()

    data class ValidateSolutionRequest(
        @SerializedName("type") override val type: String = "VALIDATE_SOLUTION",
        @SerializedName("sessionId") val sessionId: String,
        @SerializedName("approved") val approved: Boolean
    ) : WebSocketMessage()

    data class LeaveLobbyRequest(
        @SerializedName("type") override val type: String = "LEAVE_LOBBY"
    ) : WebSocketMessage()

    // === RESPONSES ===
    data class LobbyCreatedResponse(
        @SerializedName("type") override val type: String,
        @SerializedName("sessionId") val sessionId: String,
        @SerializedName("playerName") val playerName: String,
        @SerializedName("role") val role: Int
    ) : WebSocketMessage()

    data class JoinedLobbyResponse(
        @SerializedName("type") override val type: String,
        @SerializedName("sessionId") val sessionId: String,
        @SerializedName("playerName") val playerName: String,
        @SerializedName("role") val role: Int? = null,
        @SerializedName("isRejoin") val isRejoin: Boolean? = null
    ) : WebSocketMessage()

    data class LobbyInfoResponse(
        @SerializedName("type") override val type: String,
        @SerializedName("exists") val exists: Boolean,
        @SerializedName("sessionId") val sessionId: String? = null,
        @SerializedName("players") val players: List<PlayerDto>? = null
    ) : WebSocketMessage()

    data class LobbyUpdateResponse(
        @SerializedName("type") override val type: String,
        @SerializedName("sessionId") val sessionId: String,
        @SerializedName("canStart") val canStart: Boolean,
        @SerializedName("players") val players: List<PlayerDto>
    ) : WebSocketMessage()

    data class LobbyDestroyedResponse(
        @SerializedName("type") override val type: String,
        @SerializedName("reason") val reason: String
    ) : WebSocketMessage()

    data class GameStartedResponse(
        @SerializedName("type") override val type: String,
        @SerializedName("sessionId") val sessionId: String,
        @SerializedName("caseId") val caseId: String
    ) : WebSocketMessage()

    data class GameStateUpdateResponse(
        @SerializedName("type") override val type: String,
        @SerializedName("sessionId") val sessionId: String,
        @SerializedName("currentDay") val currentDay: Int,
        @SerializedName("remainingPoints") val remainingPoints: Int,
        @SerializedName("currentTurnIndex") val currentTurnIndex: Int? = null,
        @SerializedName("currentTurnPlayer") val currentTurnPlayer: String? = null,
        @SerializedName("discoveredClues") val discoveredClues: List<DiscoveredClueDto> = emptyList()
    ) : WebSocketMessage()

    data class ClueRevealedResponse(
        @SerializedName("type") override val type: String,
        @SerializedName("clueId") val clueId: String,
        @SerializedName("content") val content: String,
        @SerializedName("duration") val duration: Int,
        @SerializedName("investigator") val investigator: String
    ) : WebSocketMessage()

    data class SolutionForReviewResponse(
        @SerializedName("type") override val type: String,
        @SerializedName("sessionId") val sessionId: String,
        @SerializedName("teamAnswers") val teamAnswers: List<String>,
        @SerializedName("questions") val questions: List<String>,
        @SerializedName("correctAnswers") val correctAnswers: List<String>
    ) : WebSocketMessage()

    data class GameVictoryResponse(
        @SerializedName("type") override val type: String
    ) : WebSocketMessage()

    data class GameOverResponse(
        @SerializedName("type") override val type: String
    ) : WebSocketMessage()

    data class SolutionRejectedResponse(
        @SerializedName("type") override val type: String,
        @SerializedName("message") val message: String
    ) : WebSocketMessage()

    data class TurnSkippedResponse(
        @SerializedName("type") override val type: String,
        @SerializedName("previousPlayer") val previousPlayer: String,
        @SerializedName("reason") val reason: String
    ) : WebSocketMessage()

    data class ErrorResponse(
        @SerializedName("type") override val type: String,
        @SerializedName("message") val message: String
    ) : WebSocketMessage()

    // === GSON ADAPTER ===
    class Adapter : JsonDeserializer<WebSocketMessage> {
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): WebSocketMessage {
            val jsonObject = json.asJsonObject
            val type = jsonObject.get("type").asString

            return when (type) {
                "LOBBY_CREATED" -> context.deserialize(jsonObject, LobbyCreatedResponse::class.java)
                "JOINED_LOBBY" -> context.deserialize(jsonObject, JoinedLobbyResponse::class.java)
                "LOBBY_INFO" -> context.deserialize(jsonObject, LobbyInfoResponse::class.java)
                "LOBBY_UPDATE" -> context.deserialize(jsonObject, LobbyUpdateResponse::class.java)
                "LOBBY_DESTROYED" -> context.deserialize(jsonObject, LobbyDestroyedResponse::class.java)
                "GAME_STARTED" -> context.deserialize(jsonObject, GameStartedResponse::class.java)
                "GAME_STATE_UPDATE" -> context.deserialize(jsonObject, GameStateUpdateResponse::class.java)
                "CLUE_REVEALED" -> context.deserialize(jsonObject, ClueRevealedResponse::class.java)
                "SOLUTION_FOR_REVIEW" -> context.deserialize(jsonObject, SolutionForReviewResponse::class.java)
                "GAME_VICTORY" -> context.deserialize(jsonObject, GameVictoryResponse::class.java)
                "GAME_OVER" -> context.deserialize(jsonObject, GameOverResponse::class.java)
                "SOLUTION_REJECTED" -> context.deserialize(jsonObject, SolutionRejectedResponse::class.java)
                "TURN_SKIPPED" -> context.deserialize(jsonObject, TurnSkippedResponse::class.java)
                "ERROR" -> context.deserialize(jsonObject, ErrorResponse::class.java)

                "CREATE_LOBBY" -> context.deserialize(jsonObject, CreateLobbyRequest::class.java)
                "JOIN_AS_PLAYER" -> context.deserialize(jsonObject, JoinAsPlayerRequest::class.java)
                "GET_LOBBY_INFO" -> context.deserialize(jsonObject, GetLobbyInfoRequest::class.java)
                "START_GAME" -> context.deserialize(jsonObject, StartGameRequest::class.java)
                "GAME_ACTION" -> context.deserialize(jsonObject, GameActionRequest::class.java)
                "SUBMIT_SOLUTION" -> context.deserialize(jsonObject, SubmitSolutionRequest::class.java)
                "SAVE_NOTE" -> context.deserialize(jsonObject, SaveNoteRequest::class.java)
                "VALIDATE_SOLUTION" -> context.deserialize(jsonObject, ValidateSolutionRequest::class.java)
                "LEAVE_LOBBY" -> context.deserialize(jsonObject, LeaveLobbyRequest::class.java)

                else -> throw JsonParseException("Unknown WebSocket message type: $type")
            }
        }
    }
}

// DTOs auxiliares
data class PlayerDto(
    @SerializedName("name") val name: String,
    @SerializedName("role") val role: Int
)

data class DiscoveredClueDto(
    @SerializedName("id") val id: String,
    @SerializedName("targetId") val targetId: String,
    @SerializedName("type") val type: Int,
    @SerializedName("content") val content: String,
    @SerializedName("playerNotes") val playerNotes: Map<String, String> = emptyMap()
)