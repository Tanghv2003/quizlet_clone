package com.example.quizlet.ui.study

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quizlet.data.FlashcardItem
import com.example.quizlet.utils.SoundManager
import com.example.quizlet.utils.rememberTextToSpeech
import com.example.quizlet.utils.TextToSpeechHelper
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction

enum class TestType {
    MULTIPLE_CHOICE, WRITTEN
}

enum class QuestionSide {
    FOREIGN, NATIVE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    items: List<FlashcardItem>,
    onBack: () -> Unit
) {
    var isConfigured by remember { mutableStateOf(false) }
    var testType by remember { mutableStateOf(TestType.MULTIPLE_CHOICE) }
    var questionSide by remember { mutableStateOf(QuestionSide.FOREIGN) }

    val tts = rememberTextToSpeech()

    if (items.isEmpty()) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Kiểm tra", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = {
                            SoundManager.playClick()
                            onBack()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Không có từ vựng để làm bài kiểm tra.")
            }
        }
    } else if (!isConfigured) {
        TestSetupScreen(
            onStart = { type, side ->
                testType = type
                questionSide = side
                isConfigured = true
            },
            onBack = onBack
        )
    } else {
        when (testType) {
            TestType.MULTIPLE_CHOICE -> {
                MultipleChoiceTestContent(
                    items = items,
                    questionSide = questionSide,
                    onBack = onBack,
                    tts = tts
                )
            }
            TestType.WRITTEN -> {
                WrittenTestContent(
                    items = items,
                    questionSide = questionSide,
                    onBack = onBack,
                    tts = tts
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestSetupScreen(
    onStart: (TestType, QuestionSide) -> Unit,
    onBack: () -> Unit
) {
    var selectedType by remember { mutableStateOf(TestType.MULTIPLE_CHOICE) }
    var selectedSide by remember { mutableStateOf(QuestionSide.FOREIGN) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Thiết lập kiểm tra", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        SoundManager.playClick()
                        onBack()
                    }) {
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
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Hỏi bằng
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Hỏi bằng:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    SoundManager.playClick()
                                    selectedSide = QuestionSide.FOREIGN
                                }
                                .padding(8.dp)
                        ) {
                            RadioButton(
                                selected = selectedSide == QuestionSide.FOREIGN,
                                onClick = {
                                    SoundManager.playClick()
                                    selectedSide = QuestionSide.FOREIGN
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Từ gốc (Foreign)")
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    SoundManager.playClick()
                                    selectedSide = QuestionSide.NATIVE
                                }
                                .padding(8.dp)
                        ) {
                            RadioButton(
                                selected = selectedSide == QuestionSide.NATIVE,
                                onClick = {
                                    SoundManager.playClick()
                                    selectedSide = QuestionSide.NATIVE
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Nghĩa (Native)")
                        }
                    }
                }
            }

            // Loại câu hỏi
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Loại câu hỏi:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    SoundManager.playClick()
                                    selectedType = TestType.MULTIPLE_CHOICE
                                }
                                .padding(8.dp)
                        ) {
                            RadioButton(
                                selected = selectedType == TestType.MULTIPLE_CHOICE,
                                onClick = {
                                    SoundManager.playClick()
                                    selectedType = TestType.MULTIPLE_CHOICE
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Trắc nghiệm (Multiple Choice)")
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    SoundManager.playClick()
                                    selectedType = TestType.WRITTEN
                                }
                                .padding(8.dp)
                        ) {
                            RadioButton(
                                selected = selectedType == TestType.WRITTEN,
                                onClick = {
                                    SoundManager.playClick()
                                    selectedType = TestType.WRITTEN
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Tự luận / Điền từ (Written)")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    SoundManager.playClick()
                    onStart(selectedType, selectedSide)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Bắt đầu kiểm tra", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultipleChoiceTestContent(
    items: List<FlashcardItem>,
    questionSide: QuestionSide,
    onBack: () -> Unit,
    tts: TextToSpeechHelper
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var isAnswered by remember { mutableStateOf(false) }
    var shuffledOptions by remember { mutableStateOf<List<String>>(emptyList()) }
    var quizComplete by remember { mutableStateOf(false) }

    val currentItem = items[currentIndex]
    val questionText = if (questionSide == QuestionSide.FOREIGN) currentItem.foreign else currentItem.native
    val correctAnswer = if (questionSide == QuestionSide.FOREIGN) currentItem.native else currentItem.foreign

    LaunchedEffect(currentIndex, items, questionSide) {
        if (items.isNotEmpty()) {
            val wrongs = items.filter { 
                val valToCompare = if (questionSide == QuestionSide.FOREIGN) it.native else it.foreign
                valToCompare != correctAnswer 
            }
            .map { if (questionSide == QuestionSide.FOREIGN) it.native else it.foreign }
            .distinct()
            .shuffled()
            .take(3)

            val options = (listOf(correctAnswer) + wrongs).distinct().shuffled()
            shuffledOptions = options
            selectedOption = null
            isAnswered = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Kiểm tra - Trắc nghiệm", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        SoundManager.playClick()
                        onBack()
                    }) {
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
            if (quizComplete) {
                TestResultCard(score = score, total = items.size, onBack = onBack)
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = questionText,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = {
                                SoundManager.playClick()
                                tts.speak(questionText, isForeign = (questionSide == QuestionSide.FOREIGN))
                            }) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Pronounce",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                shuffledOptions.forEach { option ->
                    val isCorrect = option == correctAnswer
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
                                if (isCorrect) {
                                    score++
                                    SoundManager.playCorrect()
                                } else {
                                    SoundManager.playIncorrect()
                                }
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
                        SoundManager.playClick()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WrittenTestContent(
    items: List<FlashcardItem>,
    questionSide: QuestionSide,
    onBack: () -> Unit,
    tts: TextToSpeechHelper
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    var userAnswer by remember { mutableStateOf("") }
    var isAnswered by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }
    var quizComplete by remember { mutableStateOf(false) }

    val currentItem = items[currentIndex]
    val questionText = if (questionSide == QuestionSide.FOREIGN) currentItem.foreign else currentItem.native
    val correctAnswer = if (questionSide == QuestionSide.FOREIGN) currentItem.native else currentItem.foreign

    LaunchedEffect(currentIndex) {
        userAnswer = ""
        isAnswered = false
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Kiểm tra - Tự luận", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        SoundManager.playClick()
                        onBack()
                    }) {
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
            if (quizComplete) {
                TestResultCard(score = score, total = items.size, onBack = onBack)
            } else {
                Text(
                    text = "Câu ${currentIndex + 1} / ${items.size}",
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = questionText,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = {
                                SoundManager.playClick()
                                tts.speak(questionText, isForeign = (questionSide == QuestionSide.FOREIGN))
                            }) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Pronounce",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (questionSide == QuestionSide.FOREIGN) "Nhập nghĩa của từ trên" else "Nhập từ gốc tương ứng",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = userAnswer,
                        onValueChange = { userAnswer = it },
                        label = { Text("Câu trả lời của bạn") },
                        modifier = Modifier.weight(1f),
                        enabled = !isAnswered,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = if (isAnswered) ImeAction.Next else ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (userAnswer.isNotBlank() && !isAnswered) {
                                    isAnswered = true
                                    val isCorrect = userAnswer.trim().equals(correctAnswer.trim(), ignoreCase = true)
                                    if (isCorrect) {
                                        score++
                                        SoundManager.playCorrect()
                                    } else {
                                        SoundManager.playIncorrect()
                                    }
                                }
                            },
                            onNext = {
                                if (isAnswered) {
                                    SoundManager.playClick()
                                    if (currentIndex < items.size - 1) {
                                        currentIndex++
                                    } else {
                                        quizComplete = true
                                    }
                                }
                            }
                        )
                    )

                    Button(
                        onClick = {
                            if (isAnswered) {
                                SoundManager.playClick()
                                if (currentIndex < items.size - 1) {
                                    currentIndex++
                                } else {
                                    quizComplete = true
                                }
                            } else {
                                isAnswered = true
                                val isCorrect = userAnswer.trim().equals(correctAnswer.trim(), ignoreCase = true)
                                if (isCorrect) {
                                    score++
                                    SoundManager.playCorrect()
                                } else {
                                    SoundManager.playIncorrect()
                                }
                            }
                        },
                        enabled = userAnswer.isNotBlank() || isAnswered,
                        modifier = Modifier.height(56.dp)
                    ) {
                        Text(if (!isAnswered) "Check" else "Next")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isAnswered) {
                    val isCorrect = userAnswer.trim().equals(correctAnswer.trim(), ignoreCase = true)
                    Text(
                        text = if (isCorrect) "✅ Chính xác!" else "❌ Sai. Đáp án đúng: $correctAnswer",
                        color = if (isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun TestResultCard(
    score: Int,
    total: Int,
    onBack: () -> Unit
) {
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
                text = "Bạn trả lời đúng $score/$total câu",
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            val percent = if (total > 0) (score.toFloat() / total * 100).toInt() else 0
            Text(
                text = "Tỉ lệ: $percent%",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = {
                SoundManager.playClick()
                onBack()
            }) {
                Text("Quay lại")
            }
        }
    }
}
