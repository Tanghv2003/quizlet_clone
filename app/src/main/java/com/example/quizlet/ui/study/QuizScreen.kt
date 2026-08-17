package com.example.quizlet.ui.study

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quizlet.data.FlashcardItem
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    items: List<FlashcardItem>,
    onBack: () -> Unit
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var isAnswered by remember { mutableStateOf(false) }
    var shuffledOptions by remember { mutableStateOf<List<String>>(emptyList()) }
    var quizComplete by remember { mutableStateOf(false) }

    LaunchedEffect(currentIndex, items) {
        if (items.isNotEmpty()) {
            val correct = items[currentIndex].native
            val wrongs = items.filter { it.native != correct }
                .shuffled()
                .take(3)
                .map { it.native }
            val options = (listOf(correct) + wrongs).shuffled()
            shuffledOptions = options
            selectedOption = null
            isAnswered = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Trắc nghiệm", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (items.isEmpty()) {
                Text("Không có từ vựng để làm bài kiểm tra")
            } else if (quizComplete) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🎉 Hoàn thành!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Bạn trả lời đúng $score/${items.size} câu",
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tỉ lệ: ${(score.toFloat() / items.size * 100).toInt()}%",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = onBack) {
                            Text("Quay lại")
                        }
                    }
                }
            } else {
                Text(
                    text = "Câu ${currentIndex + 1} / ${items.size}",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Nghĩa của từ:",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = items[currentIndex].foreign,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                shuffledOptions.forEach { option ->
                    val isCorrect = option == items[currentIndex].native
                    val backgroundColor = when {
                        isAnswered && isCorrect -> MaterialTheme.colorScheme.primaryContainer
                        isAnswered && selectedOption == option && !isCorrect -> MaterialTheme.colorScheme.errorContainer
                        isAnswered && selectedOption == option && isCorrect -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable(enabled = !isAnswered) {
                                selectedOption = option
                                isAnswered = true
                                if (isCorrect) score++
                            },
                        elevation = CardDefaults.cardElevation(defaultElevation = if (!isAnswered) 1.dp else 0.dp),
                        colors = CardDefaults.cardColors(containerColor = backgroundColor)
                    ) {
                        Text(
                            text = option,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = if (selectedOption == option || isCorrect) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (currentIndex < items.size - 1) {
                            currentIndex++
                        } else {
                            quizComplete = true
                        }
                    },
                    enabled = isAnswered,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (currentIndex < items.size - 1) "Câu tiếp theo" else "Xem kết quả")
                }
            }
        }
    }
}

