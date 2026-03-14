package com.app.findthebug.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay

@Composable
fun ClueRevealedDialog(
    clueContent: String,
    onSaveAndExit: (String) -> Unit
) {
    var noteText by remember { mutableStateOf("") }
    var timeLeft by remember { mutableIntStateOf(60) }
    var currentPage by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
        onSaveAndExit(noteText)
    }

    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF0A0D0F)
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Text(
                        "SESSÃO DE ANÁLISE",
                        color = Color(0xFF00B7C3),
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${timeLeft}s ", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        CircularProgressIndicator(
                            progress = { timeLeft / 60f },
                            modifier = Modifier.size(18.dp),
                            color = if (timeLeft < 10) Color(0xFFFF6B6B) else Color(0xFF00B7C3),
                            strokeWidth = 2.dp
                        )
                    }
                }

                SecondaryTabRow(
                    selectedTabIndex = currentPage,
                    containerColor = Color.Transparent,
                    contentColor = Color(0xFF00B7C3),
                    divider = {}
                ) {
                    Tab(selected = currentPage == 0, onClick = { currentPage = 0 }) {
                        Text("LEITURA DE CÓDIGO", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
                    }
                    Tab(selected = currentPage == 1, onClick = { currentPage = 1 }) {
                        Text("NOTAS TÉCNICAS", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(vertical = 12.dp)) {
                    if (currentPage == 0) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = Color(0xFF050708),
                            border = BorderStroke(1.dp, Color(0xFF1F2429)),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = clueContent,
                                color = Color(0xFF00FF41),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 15.sp,
                                lineHeight = 22.sp,
                                modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp)
                            )
                        }
                    } else {
                        OutlinedTextField(
                            value = noteText,
                            onValueChange = { noteText = it },
                            modifier = Modifier.fillMaxSize(),
                            placeholder = { Text("Documente a falha aqui...", color = Color(0xFF3F4B55)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00B7C3),
                                unfocusedBorderColor = Color(0xFF1F2429),
                                focusedTextColor = Color.White
                            )
                        )
                    }
                }

                Button(
                    onClick = { onSaveAndExit(noteText) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B7C3)),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("SALVAR E SINCRONIZAR", fontWeight = FontWeight.Black)
                }
            }
        }
    }
}