package com.app.findthebug.presentation.investigation

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.findthebug.R
import com.app.findthebug.core.common.TargetType
import com.app.findthebug.domain.model.ConnectionNode
import com.app.findthebug.domain.model.FunctionNode
import com.app.findthebug.domain.model.ModuleNode
import com.app.findthebug.presentation.components.*
import com.app.findthebug.presentation.viewmodel.CasesViewModel
import com.app.findthebug.presentation.viewmodel.GameViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun InvestigationScreen(
    caseId: String,
    gameViewModel: GameViewModel,
    casesViewModel: CasesViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onSubmitSolution: () -> Unit,
    onNavigateHome: () -> Unit
) {
    val gameState by gameViewModel.currentGameState.collectAsStateWithLifecycle()
    val session by gameViewModel.currentSession.collectAsStateWithLifecycle()
    val caseDetails by casesViewModel.selectedCase.collectAsStateWithLifecycle()
    val isLoadingCase by casesViewModel.isLoading.collectAsStateWithLifecycle()
    val currentPlayerName by gameViewModel.currentPlayerName.collectAsStateWithLifecycle()
    val sessionEnded by gameViewModel.showSessionEndedDialog.collectAsStateWithLifecycle()
    var showBriefing by remember { mutableStateOf(false) }

    val isInitialDataLoaded = caseDetails != null && gameState != null
    val isActuallyLoading = isLoadingCase || !isInitialDataLoaded

    val clues = gameState?.discoveredClues ?: emptyList()

    val localName = currentPlayerName?.trim() ?: ""
    val serverTurnName = gameState?.currentTurnPlayer?.trim() ?: ""

    val selfPlayer = session?.players?.find {
        it.name.trim().equals(localName, ignoreCase = true)
    }

    val isMaster = selfPlayer?.role == com.app.findthebug.core.common.PlayerRole.MASTER

    val isMyTurn = serverTurnName.isNotEmpty() &&
            localName.isNotEmpty() &&
            serverTurnName.equals(localName, ignoreCase = true) &&
            !isMaster

    val canInteract = isInitialDataLoaded && !isMaster && isMyTurn

    LaunchedEffect(isInitialDataLoaded) {
        if (isInitialDataLoaded) {
            showBriefing = true
        }
    }

    LaunchedEffect(localName, serverTurnName) {
        if (localName.isEmpty()) {
            Log.e("InvestigationSync", "ERRO: Nome local está VAZIO. O ViewModel não carregou o nome.")
        }
        Log.d("InvestigationSync", "Comparando: Local='$localName' vs Server='$serverTurnName' | Turno: $isMyTurn")
    }

    val remainingPF = gameState?.remainingPoints ?: 0
    val daysLeft = (5 - (gameState?.currentDay ?: 1) + 1).coerceAtLeast(0)

    var showExitDialog by remember { mutableStateOf(false) }
    var isSidebarOpen by remember { mutableStateOf(false) }
    var showClueDialog by remember { mutableStateOf(false) }
    var activeClueId by remember { mutableStateOf("") }
    var activeClueContent by remember { mutableStateOf("") }
    var expandedModule by remember { mutableStateOf<String?>(null) }

    BackHandler { if (isSidebarOpen) isSidebarOpen = false else showExitDialog = true }

    LaunchedEffect(caseId) {
        if (caseDetails?.id != caseId) casesViewModel.loadCaseDetails(caseId)
    }

    LaunchedEffect(Unit) {
        gameViewModel.revealedClue.collectLatest { (id, content) ->
            if (!isMaster) {
                activeClueId = id
                activeClueContent = content
                showClueDialog = true
            }
        }
        gameViewModel.navigationEvent.collect { event ->
            if (event is GameViewModel.NavigationEvent.GoToHome) onNavigateHome()
        }
    }

    var selectedTargetId by remember { mutableStateOf<String?>(null) }
    var selectedTargetType by remember { mutableStateOf<TargetType?>(null) }
    var selectedTargetName by remember { mutableStateOf<String?>(null) }
    var showActionMenu by remember { mutableStateOf(false) }

    val density = LocalDensity.current
    val insets = WindowInsets.safeDrawing
    val horizontalPadding = with(density) { maxOf(insets.getLeft(this, LocalLayoutDirection.current), insets.getRight(this, LocalLayoutDirection.current)).toDp() }
    val verticalPadding = with(density) { maxOf(insets.getTop(this), insets.getBottom(this)).toDp() }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1F2429))) {
        if (isActuallyLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF00B7C3))
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = horizontalPadding, vertical = verticalPadding)) {

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    BackButton(onClick = { showExitDialog = true })
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(caseDetails!!.title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        val bannerColor = if (isMaster) Color(0xFFB583FF) else if (isMyTurn) Color(0xFF00B7C3) else Color(0xFF7F8C95)
                        Surface(color = bannerColor.copy(0.2f), shape = RoundedCornerShape(4.dp)) {
                            Text(
                                text = if (isMaster) "MODO MESTRE" else if (isMyTurn) "SUA VEZ" else "VEZ DE: ${gameState?.currentTurnPlayer?.uppercase()}",
                                color = bannerColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    IconButton(onClick = { showBriefing = true }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_briefing),
                            contentDescription = "Briefing",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp).padding(horizontal = 8.dp)
                        )
                    }

                    BadgedBox(
                        badge = { if(clues.isNotEmpty()) Badge(containerColor = Color(0xFF00B7C3)) { Text(clues.size.toString()) } }
                    ) {
                        IconButton(onClick = { isSidebarOpen = true }) {
                            Icon(painterResource(R.drawable.ic_evidence), "Evidências", tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                    }

                    if (!isMaster) {
                        Button(onClick = onSubmitSolution, enabled = canInteract, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDE1B1B)), modifier = Modifier.height(38.dp).padding(horizontal = 8.dp)) {
                            Text("SOLUÇÃO", fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }

                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    PFBadge(points = remainingPF)
                    DayCounter(daysLeft = daysLeft)
                    if (isMyTurn) {
                        Text("PULAR TURNO", color = Color(0xFF00B7C3).copy(0.7f), fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.clickable { gameViewModel.skipTurn() })
                    }
                }

                LazyVerticalGrid(columns = GridCells.Fixed(2), contentPadding = PaddingValues(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
                    item(span = { GridItemSpan(2) }) { Text("MÓDULOS", color = Color(0xFF5A6268), fontSize = 11.sp, fontWeight = FontWeight.Black) }

                    items(caseDetails!!.systemTopology.modules) { module: ModuleNode ->
                        ModuleCard(
                            module = module,
                            isExpanded = expandedModule == module.name,
                            functions = caseDetails!!.systemTopology.functions.filter { it.parentId == module.name },
                            onToggle = { expandedModule = if (expandedModule == module.name) null else module.name },
                            onAction = {
                                selectedTargetId = module.name
                                selectedTargetType = TargetType.MODULE
                                selectedTargetName = module.name
                                showActionMenu = true
                            },
                            onFunctionAction = { fn: FunctionNode ->
                                selectedTargetId = fn.name
                                selectedTargetType = TargetType.FUNCTION
                                selectedTargetName = fn.name
                                showActionMenu = true
                            },
                            canInteract = canInteract
                        )
                    }

                    item(span = { GridItemSpan(2) }) { Spacer(modifier = Modifier.height(12.dp)); Text("CONEXÕES", color = Color(0xFF5A6268), fontSize = 11.sp, fontWeight = FontWeight.Black) }

                    items(caseDetails!!.systemTopology.connections) { conn: ConnectionNode ->
                        ConnectionCard(
                            connection = conn,
                            onClick = {
                                selectedTargetId = conn.id
                                selectedTargetType = TargetType.CONNECTION
                                selectedTargetName = "${conn.from} → ${conn.to}"
                                showActionMenu = true
                            },
                            canInteract = canInteract
                        )
                    }
                }
            }
        }

        if (isSidebarOpen) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)).clickable { isSidebarOpen = false }) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
                    EvidenceSidebar(
                        isVisible = isSidebarOpen,
                        clues = clues,
                        currentPlayerName = localName,
                        onClose = { isSidebarOpen = false },
                        onSaveNote = { id, text -> gameViewModel.saveNote(id, text) }
                    )
                }
            }
        }

        if (showClueDialog) {
            ClueRevealedDialog(clueContent = activeClueContent, onSaveAndExit = { note -> gameViewModel.saveNote(activeClueId, note); showClueDialog = false })
        }

        if (showActionMenu && canInteract) {
            ActionMenu(
                targetName = selectedTargetName ?: "",
                targetId = selectedTargetId ?: "",
                targetType = selectedTargetType!!,
                remainingPF = remainingPF,
                discoveredClues = clues,
                onActionSelected = { action -> gameViewModel.executeAction(action.value, selectedTargetId!!) },
                onDismiss = { showActionMenu = false }
            )
        }
    }
    if (showBriefing && caseDetails != null) {
        BriefingDialog(
            title = caseDetails!!.title,
            description = caseDetails!!.description,
            questions = caseDetails!!.solutionQuestions,
            onDismiss = { showBriefing = false }
        )
    }
    if (showExitDialog) {
        Dialog(onDismissRequest = { showExitDialog = false }) {
            Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFF232A30)) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ABANDONAR INVESTIGAÇÃO?", color = Color.White, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { gameViewModel.leaveGame() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDE1B1B))) { Text("SAIR DA PARTIDA") }
                    TextButton(onClick = { showExitDialog = false }) { Text("CANCELAR", color = Color.White) }
                }
            }
        }
    }
}

@Composable
fun ModuleCard(
    module: ModuleNode,
    isExpanded: Boolean,
    functions: List<FunctionNode>,
    onToggle: () -> Unit,
    onAction: () -> Unit,
    onFunctionAction: (FunctionNode) -> Unit,
    canInteract: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF232A30))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(R.drawable.ic_module), null, tint = Color(0xFF8AB4F8), modifier = Modifier.size(24.dp))
                Text(module.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f).padding(start = 10.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (canInteract) {
                    IconButton(onClick = onAction, modifier = Modifier.size(32.dp)) {
                        Icon(painterResource(R.drawable.ic_action), null, tint = Color(0xFF00B7C3), modifier = Modifier.size(20.dp))
                    }
                }
            }
            Text(
                text = if (isExpanded) "RECOLHER" else "VER FUNÇÕES (${functions.size})",
                color = Color(0xFF00B7C3),
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(top = 10.dp).clickable { onToggle() }
            )
            if (isExpanded) {
                functions.forEach { fn ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable(enabled = canInteract) { onFunctionAction(fn) }.padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(painterResource(R.drawable.ic_function), null, tint = Color(0xFFB583FF), modifier = Modifier.size(16.dp))
                        Text(fn.name, color = Color.White, fontSize = 14.sp, modifier = Modifier.padding(start = 10.dp), maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
fun ConnectionCard(connection: ConnectionNode, onClick: () -> Unit, canInteract: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(enabled = canInteract) { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF232A30))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(R.drawable.ic_connection), null, tint = Color(0xFFF9A825), modifier = Modifier.size(20.dp))
            Text("${connection.from} → ${connection.to}", color = Color.White, fontSize = 13.sp, modifier = Modifier.padding(start = 10.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}