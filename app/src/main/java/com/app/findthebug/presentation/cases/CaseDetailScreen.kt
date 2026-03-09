package com.app.findthebug.presentation.cases

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.findthebug.presentation.components.BackButton
import com.app.findthebug.presentation.viewmodel.CasesViewModel
import com.app.findthebug.presentation.viewmodel.GameViewModel

data class CaseSection(
    val title: String
)

@Composable
fun CaseDetailScreen(
    caseId: String,
    gameViewModel: GameViewModel,
    onBack: () -> Unit
) {
    val casesViewModel: CasesViewModel = hiltViewModel()
    val gameState by gameViewModel.currentGameState.collectAsStateWithLifecycle()
    val selectedCase by casesViewModel.selectedCase.collectAsStateWithLifecycle()
    val isLoading by casesViewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by casesViewModel.errorMessage.collectAsStateWithLifecycle()

    LaunchedEffect(caseId) {
        casesViewModel.loadCaseDetails(caseId)
    }

    val sections = selectedCase?.systemTopology?.modules
        ?.map { CaseSection(it.name) }
        ?: emptyList()

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
                    text = selectedCase?.title ?: "Carregando...",
                    color = Color(0xFFE9EEF1),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = { },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDE1B1B),
                        contentColor = Color.White
                    )
                ) {
                    Text("Declarar Solução", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Text(
                    text = "Pontos de Função: ${gameState?.remainingPoints ?: 12}",
                    color = Color(0xFF00B7C3),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Dias Restantes: ${(6 - (gameState?.currentDay ?: 1)).coerceAtLeast(0)}",
                    color = Color(0xFFE9EEF1),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (sections.isNotEmpty()) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 200.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(sections) { section ->
                            SectionCard(section.title)
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isLoading) "Carregando..." else "Nenhum módulo encontrado",
                            color = Color(0xFF8A9095)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        onClick = { },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFF1C2126), RoundedCornerShape(10.dp))
                    ) {
                        Text("DOC", color = Color(0xFFE9EEF1), fontSize = 16.sp)
                    }
                    IconButton(
                        onClick = { },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFF1C2126), RoundedCornerShape(10.dp))
                    ) {
                        Text("i", color = Color(0xFFE9EEF1), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (!errorMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage!!,
                    color = Color(0xFFFF6B6B),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun SectionCard(title: String) {
    Box(
        modifier = Modifier
            .background(Color(0xFF232A30), RoundedCornerShape(12.dp))
            .padding(16.dp)
            .height(100.dp)
    ) {
        Text(
            text = title,
            color = Color(0xFFE9EEF1),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}