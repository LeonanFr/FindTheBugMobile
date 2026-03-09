package com.app.findthebug.navigation

sealed class Screen(val route: String){
    object Home : Screen("home")
    object Lobby : Screen("lobby")
    object CreateLobby : Screen("createLobby")
    object JoinLobby : Screen("joinLobby")
    object DebugScenario : Screen("debugScenario")
    object CaseDetail : Screen("caseDetail/{caseId}"){
        fun passCaseId(caseId: String) = "caseDetail/$caseId"
    }
}