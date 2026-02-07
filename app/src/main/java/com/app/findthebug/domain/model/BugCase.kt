package com.app.findthebug.domain.model

data class BugCase(
    val id: String,
    val title: String,
    val description: String,
    val shortDescription: String= "",
    val systemTopology: SystemTopology,
    val solutionQuestions: List<String> = emptyList(),
    val correctAnswers: List<String> = emptyList()
)

data class SystemTopology(
    val modules: List<ModuleNode>,
    val functions: List<FunctionNode>,
    val connections: List<ConnectionNode>
)

data class ModuleNode(val name: String)
data class FunctionNode(val name: String, val parentId: String)
data class ConnectionNode(val id: String, val from: String, val to: String)
