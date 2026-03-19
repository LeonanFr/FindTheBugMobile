package com.app.findthebug.core.common

object Constants {

    //URLS
    const val BASE_URL = "https://findthebug.onrender.com"
    const val WS_URL = "wss://findthebug.onrender.com/ws"

    // Game Constants
    const val MAX_PF_PER_DAY = 12
    const val MAX_DAYS = 5
    const val CLUE_NOTATION_TIMER_SECONDS = 60

    // Database
    const val DATABASE_NAME = "findthebug_db"

    // Preferences
    const val PREFERENCES_NAME = "findthebug_prefs"
    const val KEY_PLAYER_NAME = "player_name"
    const val KEY_SESSION_ID = "session_id"

    // Websocket timeouts
    const val WS_CONNECT_TIMEOUT = 10000L
    const val WS_RECONNECT_DELAY = 3000L

}