package com.app.findthebug.core.common

enum class PlayerRole(val value: Int){
    PLAYER(0), HOST(1), MASTER(2);

    companion object {
        fun fromInt(value: Int) = entries.firstOrNull { it.value == value } ?: PLAYER
    }
}

enum class ActionType(val value: Int, val cost: Int){
    READ_DOCUMENTATION(0, 1),
    INSERT_LOG(1, 1),
    INVESTIGATE_FUNCTION(2, 2),
    SET_BREAKPOINT(3, 2),
    RUN_UNIT_TESTS(4, 2),
    RUN_INTEGRATION_TESTS(5, 3),
    SUBMIT_SOLUTION(6, 1),
    SKIP_TURN(7, 0);

    companion object {
        fun fromInt(value: Int) = entries.firstOrNull { it.value == value }
    }
}

enum class ClueType(val value: Int) {
    DOCUMENTATION(0),
    LOG(1),
    CODE(2),
    BREAKPOINT(3),
    UNIT_TEST_RESULT(4),
    INTEGRATION_TEST_RESULT(5);

    companion object {
        fun fromInt(value: Int) = entries.firstOrNull { it.value == value } ?: DOCUMENTATION
    }
}

enum class TargetType(val value: Int) {
    MODULE(0), FUNCTION(1), CONNECTION(2);

    companion object {
        fun fromInt(value: Int) = entries.firstOrNull { it.value == value } ?: MODULE
    }
}

enum class GamePhase(val value: Int) {
    LOBBY(0),
    INVESTIGATION(1),
    SUDDEN_DEATH(2),
    REVIEW(3),
    FINISHED(4);

    companion object {
        fun fromInt(value: Int) = entries.firstOrNull { it.value == value } ?: LOBBY
    }
}