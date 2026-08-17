package com.example.quizlet.ui.study

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FillInBlankScreen(
    items: List<FlashcardItem>,
    onBack: () -> Unit
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    var userAnswer by remember { mutableStateOf("") }
    var isAnswered by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }
    var showResult by remember { mutableStateOf(false) }

    val currentItem = if (items.isNotEmpty()) items[currentIndex] else null

    LaunchedEffect(currentIndex) {
        userAnswer = ""
        isAnswered = false
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Điền từ", fontWeight = FontWeight.Bold) },
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
                Text("Không có từ vựng để luyện tập")
            } else if (showResult) {
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
                            text = "📝 Hoàn thành!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Bạn điền đúng $score/${items.size} từ",
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
                    text = "${currentIndex + 1} / ${items.size}",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentItem?.foreign ?: "",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Nhập nghĩa của từ trên",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = userAnswer,
                    onValueChange = { userAnswer = it },
                    label = { Text("Câu trả lời của bạn") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isAnswered,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (isAnswered) {
                    val isCorrect = userAnswer.trim().equals(currentItem?.native, ignoreCase = true)
                    Text(
                        text = if (isCorrect) "✅ Chính xác!" else "❌ Sai. Đáp án: ${currentItem?.native}",
                        color = if (isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Button(
                    onClick = {
                        if (isAnswered) {
                            if (currentIndex < items.size - 1) {
                                currentIndex++
                            } else {
                                showResult = true
                            }
                        } else {
                            isAnswered = true
                            if (userAnswer.trim().equals(currentItem?.native, ignoreCase = true)) {
                                score++
                            }
                        }
                    },
                    enabled = !isAnswered || currentIndex < items.size - 1 || showResult == false,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (!isAnswered) "Kiểm tra" else "Tiếp theo")
                }

                if (isAnswered && currentIndex == items.size - 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { showResult = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Xem kết quả")
                    }
                }
            }
        }
    }
}
