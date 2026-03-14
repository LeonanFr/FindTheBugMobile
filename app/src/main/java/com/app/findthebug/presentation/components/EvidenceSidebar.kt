package com.app.findthebug.presentation.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.findthebug.domain.model.Clue

@Composable
fun EvidenceSidebar(
    isVisible: Boolean,
    clues: List<Clue>,
    currentPlayerName: String,
    onClose: () -> Unit,
    onSaveNote: (String, String) -> Unit
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInHorizontally(initialOffsetX = { it }),
        exit = slideOutHorizontally(targetOffsetX = { it })
    ) {
        Surface(
            modifier = Modifier.fillMaxHeight().fillMaxWidth(0.85f),
            color = Color(0xFF15191C),
            shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
            tonalElevation = 12.dp
        ) {
            Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color(0xFF0A0D0F)).padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("CENTRAL DE EVIDÊNCIAS (${clues.size})", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, null, tint = Color(0xFF5A6268))
                    }
                }

                if (clues.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Nenhuma pista coletada.", color = Color(0xFF3F4B55))
                    }
                }

                LazyColumn(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(clues) { clue ->
                        EvidenceCardInSidebar(clue = clue, playerName = currentPlayerName, onUpdateNote = { text -> onSaveNote(clue.id, text) })
                    }
                }
            }
        }
    }
}

@Composable
fun EvidenceCardInSidebar(clue: Clue, playerName: String, onUpdateNote: (String) -> Unit) {
    var isEditing by remember { mutableStateOf(false) }
    var currentNote by remember { mutableStateOf(clue.playerNotes[playerName] ?: "") }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2429)),
        border = BorderStroke(1.dp, Color(0xFF2E3A44)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = Color(0xFF00B7C3).copy(0.1f), shape = RoundedCornerShape(2.dp)) {
                    Text(clue.type.name, color = Color(0xFF00B7C3), fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(clue.targetId, color = Color(0xFF9AA5B0), fontSize = 10.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(14.dp))

            clue.playerNotes.forEach { (author, note) ->
                Column(modifier = Modifier.padding(bottom = 10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (author == playerName) "SUA ANOTAÇÃO" else "NOTA DE: ${author.uppercase()}",
                            color = if (author == playerName) Color(0xFF00B7C3) else Color(0xFFB583FF),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    if (author == playerName && isEditing) {
                        OutlinedTextField(
                            value = currentNote,
                            onValueChange = { currentNote = it },
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                            textStyle = TextStyle(fontSize = 13.sp, color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00B7C3),
                                unfocusedBorderColor = Color(0xFF2E3A44),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            TextButton(onClick = { isEditing = false }) { Text("CANCELAR", fontSize = 11.sp) }
                            TextButton(onClick = { onUpdateNote(currentNote); isEditing = false }) { Text("SALVAR", fontSize = 11.sp) }
                        }
                    } else {
                        Text(
                            text = note.ifEmpty { "[Sem observações técnicas]" },
                            color = if (note.isEmpty()) Color(0xFF5A6268) else Color.White,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(start = 2.dp, top = 4.dp),
                            fontStyle = if (note.isEmpty()) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal
                        )
                    }
                }
            }

            if (!isEditing && clue.playerNotes.containsKey(playerName)) {
                IconButton(onClick = { isEditing = true }, modifier = Modifier.align(Alignment.End).size(24.dp)) {
                    Icon(Icons.Default.Edit, null, tint = Color(0xFF00B7C3).copy(0.7f), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}