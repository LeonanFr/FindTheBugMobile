package com.app.findthebug.presentation.investigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.findthebug.R
import com.app.findthebug.core.common.ActionType
import com.app.findthebug.core.common.TargetType
import com.app.findthebug.domain.model.ConnectionNode
import com.app.findthebug.domain.model.FunctionNode
import com.app.findthebug.domain.model.ModuleNode
import com.app.findthebug.presentation.components.ActionMenu
import com.app.findthebug.presentation.components.BackButton
import com.app.findthebug.presentation.components.ClueNoteDialog
import com.app.findthebug.presentation.components.DayCounter
import com.app.findthebug.presentation.components.EvidencePanel
import com.app.findthebug.presentation.components.PFBadge
import com.app.findthebug.presentation.components.TimerDisplay
import com.app.findthebug.presentation.viewmodel.CasesViewModel
import com.app.findthebug.presentation.viewmodel.GameViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestigationScreen(
    caseId: String,
    gameViewModel: GameViewModel,
    casesViewModel: CasesViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onSubmitSolution: () -> Unit
) {
    val gameState by gameViewModel.currentGameState.collectAsStateWithLifecycle()
    val session by gameViewModel.currentSession.collectAsStateWithLifecycle()
    val caseDetails by casesViewModel.selectedCase.collectAsStateWithLifecycle()
    val isLoadingCase by casesViewModel.isLoading.collectAsStateWithLifecycle()
    val errorCase by casesViewModel.errorMessage.collectAsStateWithLifecycle()
    val currentPlayerName by gameViewModel.currentPlayerName.collectAsStateWithLifecycle()
    val clues = gameState?.discoveredClues ?: emptyList()

    val isMaster = currentPlayerName == session?.masterPlayerId
    val isMyTurn = gameState?.currentTurnPlayer == currentPlayerName && !isMaster
    val remainingPF = gameState?.remainingPoints ?: 0
    val currentDay = gameState?.currentDay ?: 1
    val maxDays = 5
    val daysLeft = (maxDays - currentDay + 1).coerceAtLeast(0)

    LaunchedEffect(caseId) {
        if (caseDetails?.id != caseId) {
            casesViewModel.loadCaseDetails(caseId)
        }
    }

    var showNoteDialog by remember { mutableStateOf(false) }
    var currentClueId by remember { mutableStateOf("") }
    var currentClueContent by remember { mutableStateOf("") }
    var noteTimer by remember { mutableIntStateOf(60) }

    LaunchedEffect(Unit) {
        gameViewModel.revealedClue.collectLatest { (clueId, content) ->
            if (!isMaster) {
                currentClueId = clueId
                currentClueContent = content
                noteTimer = 60
                showNoteDialog = true
            }
        }
    }

    var selectedTargetId by remember { mutableStateOf<String?>(null) }
    var selectedTargetType by remember { mutableStateOf<TargetType?>(null) }
    var selectedTargetName by remember { mutableStateOf<String?>(null) }
    var showActionMenu by remember { mutableStateOf(false) }
    var expandedModule by remember { mutableStateOf<String?>(null) }

    fun onTargetSelected(targetId: String, targetType: TargetType, targetName: String) {
        if (isMaster) return
        selectedTargetId = targetId
        selectedTargetType = targetType
        selectedTargetName = targetName
        showActionMenu = true
    }

    fun executeAction(actionType: ActionType) {
        val targetId = selectedTargetId ?: return
        gameViewModel.executeAction(actionType.value, targetId)
        showActionMenu = false
        selectedTargetId = null
        selectedTargetType = null
        selectedTargetName = null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1F2429))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                BackButton(onClick = onBack)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = caseDetails?.title ?: "Investigação",
                    color = Color(0xFFE9EEF1),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                EvidencePanel(
                    clues = clues,
                    currentPlayerName = currentPlayerName ?: "",
                    onNoteClick = { clue ->
                        if (!isMaster) {
                            currentClueId = clue.id
                            currentClueContent = clue.content
                            noteTimer = 60
                            showNoteDialog = true
                        }
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (isMaster) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF2E3A44), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Modo Mestre",
                            color = Color(0xFFB583FF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Button(
                        onClick = onSubmitSolution,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFDE1B1B),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Declarar Solução", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                PFBadge(points = remainingPF)
                DayCounter(daysLeft = daysLeft)
                Text(
                    text = "Vez de: ${gameState?.currentTurnPlayer ?: "..."}",
                    color = if (isMyTurn) Color(0xFF8AB4F8) else Color(0xFF9AA5B0),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoadingCase) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF00B7C3))
                }
            } else if (errorCase != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = errorCase!!, color = Color(0xFFFF6B6B), fontSize = 16.sp)
                }
            } else if (caseDetails != null) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            text = "Módulos",
                            color = Color(0xFFB0B8C0),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(caseDetails!!.systemTopology.modules) { module ->
                        ModuleItem(
                            module = module,
                            isExpanded = expandedModule == module.name,
                            onToggleExpand = { expandedModule = if (expandedModule == module.name) null else module.name },
                            functions = caseDetails!!.systemTopology.functions.filter { it.parentId == module.name },
                            onModuleAction = { targetId, targetType, targetName ->
                                onTargetSelected(targetId, targetType, targetName)
                            },
                            isMaster = isMaster
                        )
                    }
                    if (caseDetails!!.systemTopology.connections.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Conexões",
                                color = Color(0xFFB0B8C0),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        items(caseDetails!!.systemTopology.connections) { connection ->
                            ConnectionItem(
                                connection = connection,
                                onClick = {
                                    onTargetSelected(connection.id, TargetType.CONNECTION, "${connection.from} → ${connection.to}")
                                },
                                isMaster = isMaster
                            )
                        }
                    }
                }
            }
        }
    }

    if (showNoteDialog && !isMaster) {
        ClueNoteDialog(
            clueContent = currentClueContent,
            initialNote = clues.find { it.id == currentClueId }?.playerNotes?.get(currentPlayerName) ?: "",
            timeLeft = noteTimer,
            onSave = { note ->
                gameViewModel.saveNote(currentClueId, note)
                showNoteDialog = false
            },
            onDismiss = { showNoteDialog = false }
        )
        TimerDisplay(
            initialSeconds = noteTimer,
            isActive = showNoteDialog,
            onFinish = { showNoteDialog = false }
        )
    }

    if (showActionMenu && !isMaster && selectedTargetId != null && selectedTargetType != null) {
        ActionMenu(
            targetName = selectedTargetName ?: "",
            targetType = selectedTargetType!!,
            remainingPF = remainingPF,
            onActionSelected = { actionType -> executeAction(actionType) },
            onDismiss = { showActionMenu = false }
        )
    }
}

@Composable
fun ModuleItem(
    module: ModuleNode,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    functions: List<FunctionNode>,
    onModuleAction: (String, TargetType, String) -> Unit,
    isMaster: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF232A30)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !isMaster) { onToggleExpand() }
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_module),
                    contentDescription = null,
                    tint = Color(0xFF8AB4F8),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = module.name,
                    color = Color(0xFFE9EEF1),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                if (!isMaster) {
                    IconButton(
                        onClick = { onModuleAction(module.name, TargetType.MODULE, module.name) }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_action),
                            contentDescription = "Ações",
                            tint = Color(0xFF9AA5B0)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Funções internas",
                    color = Color(0xFFB0B8C0),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 36.dp)
                )
                functions.forEach { function ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isMaster) {
                                onModuleAction(function.name, TargetType.FUNCTION, function.name)
                            }
                            .padding(start = 36.dp, top = 8.dp, bottom = 8.dp, end = 16.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_function),
                            contentDescription = null,
                            tint = Color(0xFFB583FF),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = function.name,
                            color = Color(0xFFE9EEF1),
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1f)
                        )
                        if (!isMaster) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_action_small),
                                contentDescription = "Ações",
                                tint = Color(0xFF9AA5B0),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConnectionItem(
    connection: ConnectionNode,
    onClick: () -> Unit,
    isMaster: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isMaster) { onClick() }
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF232A30)
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_connection),
                contentDescription = null,
                tint = Color(0xFFF9A825),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "${connection.from} → ${connection.to}",
                color = Color(0xFFE9EEF1),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            if (!isMaster) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_action_small),
                    contentDescription = "Ações",
                    tint = Color(0xFF9AA5B0),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}