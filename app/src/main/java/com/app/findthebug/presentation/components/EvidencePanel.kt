package com.app.findthebug.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.app.findthebug.R
import com.app.findthebug.core.common.ClueType
import com.app.findthebug.domain.model.Clue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvidencePanel(
    clues: List<Clue>,
    currentPlayerName: String,
    onNoteClick: (Clue) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    IconButton(
        onClick = { showSheet = true },
        modifier = modifier.size(40.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_evidence),
            contentDescription = "Evidências",
            tint = Color(0xFFE9EEF1)
        )
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            containerColor = Color(0xFF232A30)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Evidências (${clues.size})",
                        color = Color(0xFFE9EEF1),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { showSheet = false }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar",
                            tint = Color(0xFF9AA5B0)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (clues.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nenhuma evidência descoberta",
                            color = Color(0xFF8A9095),
                            fontSize = 16.sp
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(clues) { clue ->
                            EvidenceCard(
                                clue = clue,
                                currentPlayerName = currentPlayerName,
                                onNoteClick = { onNoteClick(clue) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EvidenceCard(
    clue: Clue,
    currentPlayerName: String,
    onNoteClick: () -> Unit
) {
    val typeColor = when (clue.type) {
        ClueType.DOCUMENTATION -> Color(0xFF8AB4F8)
        ClueType.LOG -> Color(0xFFF9A825)
        ClueType.CODE -> Color(0xFFB583FF)
        ClueType.BREAKPOINT -> Color(0xFFFF6B6B)
        ClueType.UNIT_TEST_RESULT -> Color(0xFF00B7C3)
        ClueType.INTEGRATION_TEST_RESULT -> Color(0xFFFFD966)
    }

    val typeName = when (clue.type) {
        ClueType.DOCUMENTATION -> "Documentação"
        ClueType.LOG -> "Log"
        ClueType.CODE -> "Código"
        ClueType.BREAKPOINT -> "Breakpoint"
        ClueType.UNIT_TEST_RESULT -> "Teste Unitário"
        ClueType.INTEGRATION_TEST_RESULT -> "Teste de Integração"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF2E3A44), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(typeColor, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = typeName,
                color = typeColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = clue.targetId,
                color = Color(0xFF9AA5B0),
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = clue.content,
            color = Color(0xFFE9EEF1),
            fontSize = 14.sp,
            maxLines = 3
        )

        if (clue.playerNotes.containsKey(currentPlayerName)) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Sua anotação: ${clue.playerNotes[currentPlayerName]}",
                color = Color(0xFF8AB4F8),
                fontSize = 12.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onNoteClick,
            modifier = Modifier.align(Alignment.End),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF3A4A53),
                contentColor = Color.White
            )
        ) {
            Text(
                text = if (clue.playerNotes.containsKey(currentPlayerName)) "Editar anotação" else "Adicionar anotação",
                fontSize = 12.sp
            )
        }
    }
}