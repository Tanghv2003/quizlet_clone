package com.example.quizlet.ui.study

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.quizlet.data.SpacedRepetitionData
import kotlin.random.Random

enum class StudyQuestionType {
    QUIZ, WRITE
}

data class StudyCardState(
    val item: FlashcardItem,
    val questionType: StudyQuestionType,
    val firstTryCorrect: Boolean = true
)

@Composable
fun LearningStatusTag(repetitions: Int) {
    val (text, containerColor, contentColor) = when {
        repetitions >= 2 -> Triple("Đã nắm vững", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
        repetitions == 1 -> Triple("Đang học", MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
        else -> Triple("Chưa học", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
    }
    
    Surface(
        shape = RoundedCornerShape(50),
        color = containerColor,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpacedRepetitionScreen(
    dueItems: List<FlashcardItem>,
    allItems: List<FlashcardItem>,
    cardProgressMap: Map<String, SpacedRepetitionData>,
    onReviewCard: (String, Int) -> Unit,
    onBack: () -> Unit
) {
    var hasStarted by remember { mutableStateOf(false) }
    var askInForeign by remember { mutableStateOf(true) } // true = Ask in Foreign, false = Ask in Native
    var useAllItems by remember { mutableStateOf(false) }

    val sessionItems = remember(useAllItems, dueItems, allItems) {
        if (useAllItems) allItems else dueItems
    }

    var remainingItems by remember(sessionItems) { mutableStateOf(sessionItems) }
    var activeQueue by remember { mutableStateOf(listOf<StudyCardState>()) }
    var completedCount by remember { mutableIntStateOf(0) }
    val totalCount = remember(sessionItems) { sessionItems.size }

    // Pull next batch of 5 items when active queue is empty
    LaunchedEffect(activeQueue, remainingItems) {
        if (activeQueue.isEmpty() && remainingItems.isNotEmpty()) {
            val batch = remainingItems.take(5)
            remainingItems = remainingItems.drop(5)
            activeQueue = batch.map { item ->
                // 90% Write (Điền từ), 10% Quiz (Trắc nghiệm)
                val format = if (Random.nextFloat() < 0.10f) StudyQuestionType.QUIZ else StudyQuestionType.WRITE
                StudyCardState(
                    item = item,
                    questionType = format
                )
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Học từ vựng", fontWeight = FontWeight.Bold) },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (sessionItems.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🎉 Hoàn thành!",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Hôm nay bạn không có từ vựng nào cần ôn tập.",
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        if (allItems.isNotEmpty()) {
                            Button(
                                onClick = { useAllItems = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Ôn tập lại tất cả (${allItems.size} từ)")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        OutlinedButton(
                            onClick = onBack,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Quay lại")
                        }
                    }
                }
            } else if (!hasStarted) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Cấu hình học phần",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Số lượng từ cần học: $totalCount từ",
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Định dạng: 90% Điền từ, 10% Trắc nghiệm",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Chọn ngôn ngữ câu hỏi:",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { askInForeign = true }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(selected = askInForeign, onClick = { askInForeign = true })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Hỏi bằng từ gốc (Foreign -> Native)")
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { askInForeign = false }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(selected = !askInForeign, onClick = { askInForeign = false })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Hỏi bằng nghĩa (Native -> Foreign)")
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = { hasStarted = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Bắt đầu học")
                        }
                    }
                }
            } else if (activeQueue.isEmpty() && remainingItems.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🎉 Hoàn thành bài học!",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Bạn đã hoàn thành việc ôn tập tất cả các từ trong lô này.",
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onBack,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Quay lại thư viện")
                        }
                    }
                }
            } else {
                val currentCard = activeQueue.first()
                val item = currentCard.item

                val promptText = if (askInForeign) item.foreign else item.native
                val correctAnswer = if (askInForeign) item.native else item.foreign

                val shuffledOptions = remember(item, allItems, askInForeign) {
                    val correct = correctAnswer
                    val wrongs = allItems
                        .filter { (if (askInForeign) it.native else it.foreign) != correct }
                        .map { if (askInForeign) it.native else it.foreign }
                        .shuffled()
                        .take(3)
                    (listOf(correct) + wrongs).shuffled()
                }

                var selectedOption by remember(currentCard) { mutableStateOf<String?>(null) }
                var typedAnswer by remember(currentCard) { mutableStateOf("") }
                var isAnswered by remember(currentCard) { mutableStateOf(false) }
                var isCorrectAnswer by remember(currentCard) { mutableStateOf(false) }

                Text(
                    text = "Tiến độ: $completedCount / $totalCount từ",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { if (totalCount > 0) completedCount.toFloat() / totalCount else 0f },
                    modifier = Modifier.fillMaxWidth().height(8.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (currentCard.questionType == StudyQuestionType.QUIZ) "Trắc nghiệm" else "Điền từ",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            val reps = cardProgressMap[item.foreign]?.repetitions ?: 0
                            LearningStatusTag(repetitions = reps)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = promptText,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                if (currentCard.questionType == StudyQuestionType.QUIZ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        shuffledOptions.forEach { option ->
                            val isSelected = selectedOption == option
                            val buttonColor = when {
                                isAnswered && option == correctAnswer -> ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                isAnswered && isSelected && !isCorrectAnswer -> ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                isSelected -> ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                                else -> ButtonDefaults.filledTonalButtonColors()
                            }

                            Button(
                                onClick = {
                                    if (!isAnswered) {
                                        selectedOption = option
                                    }
                                },
                                colors = buttonColor,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(text = option, fontSize = 16.sp, modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (!isAnswered) {
                        Button(
                            onClick = {
                                if (selectedOption != null) {
                                    isCorrectAnswer = selectedOption == correctAnswer
                                    isAnswered = true
                                }
                            },
                            enabled = selectedOption != null,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Kiểm tra")
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = typedAnswer,
                        onValueChange = { if (!isAnswered) typedAnswer = it },
                        label = { Text("Nhập câu trả lời") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = if (isAnswered) {
                            if (isCorrectAnswer) {
                                OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.error,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.error
                                )
                            }
                        } else {
                            OutlinedTextFieldDefaults.colors()
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (!isAnswered) {
                        Button(
                            onClick = {
                                if (typedAnswer.isNotBlank()) {
                                    isCorrectAnswer = typedAnswer.trim().lowercase() == correctAnswer.trim().lowercase()
                                    isAnswered = true
                                }
                            },
                            enabled = typedAnswer.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Kiểm tra")
                        }
                    }
                }

                if (isAnswered) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCorrectAnswer)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                            Text(
                                text = if (isCorrectAnswer) "Chính xác! 🎉" else "Sai rồi! 😢",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = if (isCorrectAnswer)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onErrorContainer
                            )
                            if (!isCorrectAnswer) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Đáp án đúng: $correctAnswer",
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    if (isCorrectAnswer) {
                                        // Update SM-2 database via ViewModel
                                        onReviewCard(item.foreign, if (currentCard.firstTryCorrect) 2 else 0)
                                        completedCount++
                                        activeQueue = activeQueue.drop(1)
                                    } else {
                                        // Toggle question format (90% Write, 10% Quiz) and append to back of queue
                                        val nextType = if (Random.nextFloat() < 0.10f)
                                            StudyQuestionType.QUIZ
                                        else
                                            StudyQuestionType.WRITE
                                        val updatedCard = currentCard.copy(
                                            firstTryCorrect = false,
                                            questionType = nextType
                                        )
                                        activeQueue = activeQueue.drop(1) + updatedCard
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isCorrectAnswer)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Tiếp tục")
                            }
                        }
                    }
                }
            }
        }
    }
}