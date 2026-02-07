package com.app.findthebug.data.remote.model.response

import com.google.gson.annotations.SerializedName

data class CaseResponse(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("systemTopology") val systemTopology: SystemTopologyDto
)

data class SystemTopologyDto(
    @SerializedName("modules") val modules: List<ModuleDto>,
    @SerializedName("functions") val functions: List<FunctionDto>,
    @SerializedName("connections") val connections: List<ConnectionDto>
)

data class ModuleDto(
    @SerializedName("name") val name: String
)

data class FunctionDto(
    @SerializedName("name") val name: String,
    @SerializedName("parentId") val parentId: String
)

data class ConnectionDto(
    @SerializedName("id") val id: String,
    @SerializedName("from") val from: String,
    @SerializedName("to") val to: String
)