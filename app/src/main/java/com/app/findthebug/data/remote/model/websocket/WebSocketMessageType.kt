package com.app.findthebug.data.remote.model.websocket

object WebSocketMessageType {
    // Request types
    const val CREATE_LOBBY = "CREATE_LOBBY"
    const val JOIN_AS_PLAYER = "JOIN_AS_PLAYER"
    const val JOIN_AS_MASTER = "JOIN_AS_MASTER"
    const val GET_LOBBY_INFO = "GET_LOBBY_INFO"
    const val START_GAME = "START_GAME"
    const val GAME_ACTION = "GAME_ACTION"
    const val SUBMIT_SOLUTION = "SUBMIT_SOLUTION"
    const val SAVE_NOTE = "SAVE_NOTE"
    const val VALIDATE_SOLUTION = "VALIDATE_SOLUTION"
    const val LEAVE_LOBBY = "LEAVE_LOBBY"

    // Response types
    const val LOBBY_CREATED = "LOBBY_CREATED"
    const val JOINED_LOBBY = "JOINED_LOBBY"
    const val LOBBY_INFO = "LOBBY_INFO"
    const val LOBBY_UPDATE = "LOBBY_UPDATE"
    const val GAME_STARTED = "GAME_STARTED"
    const val GAME_STATE_UPDATE = "GAME_STATE_UPDATE"
    const val CLUE_REVEALED = "CLUE_REVEALED"
    const val SOLUTION_FOR_REVIEW = "SOLUTION_FOR_REVIEW"
    const val GAME_VICTORY = "GAME_VICTORY"
    const val GAME_OVER = "GAME_OVER"
    const val SOLUTION_REJECTED = "SOLUTION_REJECTED"
    const val TURN_SKIPPED = "TURN_SKIPPED"
    const val ERROR = "ERROR"

    fun isResponseType(type: String): Boolean = type in listOf(
        LOBBY_CREATED, JOINED_LOBBY, LOBBY_INFO, LOBBY_UPDATE,
        GAME_STARTED, GAME_STATE_UPDATE, CLUE_REVEALED,
        SOLUTION_FOR_REVIEW, GAME_VICTORY, GAME_OVER,
        SOLUTION_REJECTED, TURN_SKIPPED, ERROR
    )

    fun isRequestType(type: String): Boolean = type in listOf(
        CREATE_LOBBY, JOIN_AS_PLAYER, JOIN_AS_MASTER,
        GET_LOBBY_INFO, START_GAME, GAME_ACTION,
        SUBMIT_SOLUTION, SAVE_NOTE, VALIDATE_SOLUTION,
        LEAVE_LOBBY
    )
}