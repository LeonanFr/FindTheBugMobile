package com.app.findthebug.presentation.submission

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.findthebug.presentation.components.BackButton
import com.app.findthebug.presentation.viewmodel.CasesViewModel
import com.app.findthebug.presentation.viewmodel.GameViewModel
import kotlinx.coroutines.launch

@Composable
fun SubmissionScreen(
    caseId: String,
    gameViewModel: GameViewModel,
    onBack: () -> Unit,
    onSubmit: () -> Unit
) {
    val casesViewModel: CasesViewModel = hiltViewModel()
    val caseDetails by casesViewModel.selectedCase.collectAsStateWithLifecycle()
    val isLoading by gameViewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by gameViewModel.errorMessage.collectAsStateWithLifecycle()
    val currentGameState by gameViewModel.currentGameState.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val effectiveCaseId = remember(caseId, currentGameState) {
        if (caseId == "{caseId}" || caseId.isBlank()) {
            currentGameState?.case?.id ?: ""
        } else {
            caseId
        }
    }

    LaunchedEffect(effectiveCaseId) {
        if (effectiveCaseId.isNotBlank() && caseDetails?.id != effectiveCaseId) {
            casesViewModel.loadCaseDetails(effectiveCaseId)
        }
    }

    val questions = caseDetails?.solutionQuestions ?: emptyList()
    val answers = remember { mutableStateListOf<String>() }

    LaunchedEffect(questions.size) {
        answers.clear()
        answers.addAll(List(questions.size) { "" })
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1F2429))
            .safeDrawingPadding()
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
                    text = "Declarar Solução",
                    color = Color(0xFFE9EEF1),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (questions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFF00B7C3))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Carregando perguntas...",
                            color = Color(0xFF8A9095),
                            fontSize = 16.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    itemsIndexed(questions) { index, question ->
                        Column {
                            Text(
                                text = "${index + 1}. $question",
                                color = Color(0xFFE9EEF1),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = answers.getOrElse(index) { "" },
                                onValueChange = { newValue ->
                                    if (index < answers.size) {
                                        answers[index] = newValue
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Sua resposta...", color = Color(0xFF8A97A1)) },
                                enabled = !isLoading,
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = Color(0xFFE9EEF1),
                                    unfocusedTextColor = Color(0xFFE9EEF1),
                                    focusedContainerColor = Color(0xFF1C2126),
                                    unfocusedContainerColor = Color(0xFF1C2126),
                                    focusedIndicatorColor = Color(0xFF00B7C3),
                                    unfocusedIndicatorColor = Color(0xFF3A4A53)
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!errorMessage.isNullOrBlank()) {
                Text(
                    text = errorMessage!!,
                    color = Color(0xFFFF6B6B),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    scope.launch {
                        gameViewModel.submitSolution(answers.toList())
                        snackbarHostState.showSnackbar(
                            message = "Solução enviada para o mestre!",
                            duration = SnackbarDuration.Short
                        )
                        onSubmit()
                    }
                },
                enabled = !isLoading &&
                        questions.isNotEmpty() &&
                        answers.all { it.isNotBlank() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00B7C3),
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFF3A4A53)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Enviar para o Mestre", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
        )
    }
}