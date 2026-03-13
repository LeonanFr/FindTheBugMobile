package com.app.findthebug.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.findthebug.core.common.ActionType
import com.app.findthebug.core.common.TargetType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionMenu(
    targetName: String,
    targetType: TargetType,
    remainingPF: Int,
    onActionSelected: (ActionType) -> Unit,
    onDismiss: () -> Unit
) {
    val actions = when (targetType) {
        TargetType.MODULE -> listOf(
            ActionType.READ_DOCUMENTATION,
            ActionType.INSERT_LOG,
            ActionType.RUN_UNIT_TESTS,
            ActionType.RUN_INTEGRATION_TESTS
        )
        TargetType.FUNCTION -> listOf(
            ActionType.READ_DOCUMENTATION,
            ActionType.INSERT_LOG,
            ActionType.INVESTIGATE_FUNCTION,
            ActionType.SET_BREAKPOINT,
            ActionType.RUN_UNIT_TESTS
        )
        TargetType.CONNECTION -> listOf(
            ActionType.RUN_INTEGRATION_TESTS,
            ActionType.INSERT_LOG
        )
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = Color(0xFF232A30)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = targetName,
                color = Color(0xFFE9EEF1),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Selecione uma ação",
                color = Color(0xFFB0B8C0),
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            actions.forEach { action ->
                val canAfford = remainingPF >= action.cost
                ActionButton(
                    action = action,
                    enabled = canAfford,
                    onClick = { onActionSelected(action) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3A4A53),
                    contentColor = Color.White
                )
            ) {
                Text("Cancelar")
            }
        }
    }
}

@Composable
private fun ActionButton(
    action: ActionType,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val actionName = when (action) {
        ActionType.READ_DOCUMENTATION -> "Ler documentação"
        ActionType.INSERT_LOG -> "Inserir log"
        ActionType.INVESTIGATE_FUNCTION -> "Investigar função"
        ActionType.SET_BREAKPOINT -> "Adicionar breakpoint"
        ActionType.RUN_UNIT_TESTS -> "Executar testes unitários"
        ActionType.RUN_INTEGRATION_TESTS -> "Executar testes de integração"
        ActionType.SUBMIT_SOLUTION -> "Submeter solução"
        ActionType.SKIP_TURN -> "Pular turno"
    }

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF2E3A44),
            contentColor = if (enabled) Color.White else Color(0xFF7F8C95),
            disabledContainerColor = Color(0xFF1F2A30)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = actionName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${action.cost} PF",
                color = if (enabled) Color(0xFF00B7C3) else Color(0xFF5F7A8C),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}