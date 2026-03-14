package com.app.findthebug.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.app.findthebug.core.common.ActionType
import com.app.findthebug.core.common.ClueType
import com.app.findthebug.core.common.TargetType
import com.app.findthebug.domain.model.Clue

@Composable
fun ActionMenu(
    targetName: String,
    targetId: String,
    targetType: TargetType,
    remainingPF: Int,
    discoveredClues: List<Clue>,
    onActionSelected: (ActionType) -> Unit,
    onDismiss: () -> Unit
) {
    val hasInspected = discoveredClues.any { it.targetId == targetId && it.type == ClueType.CODE }
    val hasBreakpoint = discoveredClues.any { it.targetId == targetId && it.type == ClueType.BREAKPOINT }

    val actions = when (targetType) {
        TargetType.MODULE -> listOf(ActionType.READ_DOCUMENTATION)
        TargetType.FUNCTION -> listOf(
            ActionType.READ_DOCUMENTATION,
            ActionType.INSERT_LOG,
            ActionType.INVESTIGATE_FUNCTION,
            ActionType.SET_BREAKPOINT,
            ActionType.RUN_UNIT_TESTS
        )
        TargetType.CONNECTION -> listOf(
            ActionType.INSERT_LOG,
            ActionType.RUN_INTEGRATION_TESTS
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFF1C2126), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(targetName, color = Color(0xFF00B7C3), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("FERRAMENTAS TÉCNICAS", color = Color(0xFF9AA5B0), fontSize = 10.sp, fontWeight = FontWeight.Black)

                Spacer(modifier = Modifier.height(20.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.heightIn(max = 400.dp)) {
                    items(actions) { action ->
                        val isDone = when(action) {
                            ActionType.READ_DOCUMENTATION -> discoveredClues.any { it.targetId == targetId && it.type == ClueType.DOCUMENTATION }
                            ActionType.INSERT_LOG -> discoveredClues.any { it.targetId == targetId && it.type == ClueType.LOG }
                            ActionType.INVESTIGATE_FUNCTION -> hasInspected
                            ActionType.SET_BREAKPOINT -> hasBreakpoint
                            ActionType.RUN_UNIT_TESTS -> discoveredClues.any { it.targetId == targetId && it.type == ClueType.UNIT_TEST_RESULT }
                            ActionType.RUN_INTEGRATION_TESTS -> discoveredClues.any { it.targetId == targetId && it.type == ClueType.INTEGRATION_TEST_RESULT }
                            else -> false
                        }

                        val dynamicCost = when (action) {
                            ActionType.INVESTIGATE_FUNCTION -> if (hasBreakpoint) 1 else 2
                            ActionType.SET_BREAKPOINT -> if (hasInspected) 1 else 2
                            else -> action.cost
                        }

                        ActionButton(
                            name = getCleanActionName(action),
                            cost = dynamicCost,
                            isDone = isDone,
                            enabled = remainingPF >= dynamicCost,
                            onClick = { onActionSelected(action); onDismiss() }
                        )
                    }
                }
                TextButton(onClick = onDismiss, modifier = Modifier.padding(top = 8.dp)) {
                    Text("CANCELAR", color = Color.White.copy(0.5f), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ActionButton(name: String, cost: Int, isDone: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(54.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isDone) Color(0xFF1F2429) else Color(0xFF2E3A44),
            disabledContainerColor = Color(0xFF15191C)
        ),
        border = if (isDone) BorderStroke(1.dp, Color(0xFF00B7C3).copy(0.3f)) else null
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(name, color = if (enabled) Color.White else Color(0xFF5A5F64), fontSize = 14.sp)
                if (isDone) {
                    Text("  [VISTO]", color = Color(0xFF00B7C3), fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            }
            Text("$cost PF", color = if (enabled) Color(0xFF00B7C3) else Color(0xFF3A4248), fontWeight = FontWeight.Black)
        }
    }
}

private fun getCleanActionName(action: ActionType) = when (action) {
    ActionType.READ_DOCUMENTATION -> "Ler Documentação"
    ActionType.INSERT_LOG -> "Inserir Log"
    ActionType.INVESTIGATE_FUNCTION -> "Inspecionar Código"
    ActionType.SET_BREAKPOINT -> "Definir Breakpoint"
    ActionType.RUN_UNIT_TESTS -> "Teste Unitário"
    ActionType.RUN_INTEGRATION_TESTS -> "Teste Integração"
    else -> "Ação"
}