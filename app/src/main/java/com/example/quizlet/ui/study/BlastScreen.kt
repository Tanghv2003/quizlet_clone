package com.example.quizlet.ui.study

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Refresh
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlastScreen(
    items: List<FlashcardItem>,
    onBack: () -> Unit
) {
    var hasStarted by remember { mutableStateOf(false) }
    var showNativeFirst by remember { mutableStateOf(false) }
    var shuffledItems by remember(items) { mutableStateOf(items.shuffled()) }
    var pageIndex by remember { mutableIntStateOf(0) }
    val tts = rememberTextToSpeech()

    val pageSize = 4
    val totalPages = if (shuffledItems.isNotEmpty()) {
        (shuffledItems.size + pageSize - 1) / pageSize
    } else {
        0
    }

    val currentPageItems = remember(pageIndex, shuffledItems) {
        val start = pageIndex * pageSize
        val end = minOf(start + pageSize, shuffledItems.size)
        if (start < shuffledItems.size) {
            shuffledItems.subList(start, end)
        } else {
            emptyList()
        }
    }

    // Keep track of flipped state for the 4 slots on this page
    var flippedStates by remember { mutableStateOf(List(pageSize) { false }) }

    // Reset flipped states when moving to a different page or changing language priority
    LaunchedEffect(pageIndex, showNativeFirst) {
        flippedStates = List(pageSize) { false }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Khối hợp (4 thẻ)", fontWeight = FontWeight.Bold) },
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (items.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Không có từ vựng nào.")
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
                            text = "Thiết lập Khối hợp",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Số lượng từ vựng: ${items.size} từ",
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Chọn ngôn ngữ hiển thị trước:",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    SoundManager.playClick()
                                    showNativeFirst = false
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = !showNativeFirst,
                                onClick = {
                                    SoundManager.playClick()
                                    showNativeFirst = false
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
                                    showNativeFirst = true
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = showNativeFirst,
                                onClick = {
                                    SoundManager.playClick()
                                    showNativeFirst = true
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Nghĩa (Native)")
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = {
                                SoundManager.playClick()
                                shuffledItems = items.shuffled()
                                hasStarted = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Bắt đầu")
                        }
                    }
                }
            } else {
                Text(
                    text = "Nhóm ${pageIndex + 1} / $totalPages (${shuffledItems.size} từ)",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                // 2x2 Grid of cards
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    for (row in 0 until 2) {
                        Row(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            for (col in 0 until 2) {
                                val slotIndex = row * 2 + col
                                if (slotIndex < currentPageItems.size) {
                                    val item = currentPageItems[slotIndex]
                                    val isFlipped = flippedStates.getOrElse(slotIndex) { false }

                                    val isShowingNative = showNativeFirst xor isFlipped
                                    val displayText = if (isShowingNative) item.native else item.foreign
                                    val sideLabel = if (isShowingNative) "Nghĩa" else "Từ gốc"

                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clickable {
                                                SoundManager.playClick()
                                                flippedStates = flippedStates.mapIndexed { idx, value ->
                                                    if (idx == slotIndex) !value else value
                                                }
                                            },
                                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Box(modifier = Modifier.fillMaxSize()) {
                                            // Speaker icon
                                            IconButton(
                                                onClick = {
                                                    SoundManager.playClick()
                                                    tts.speak(displayText, isForeign = !isShowingNative)
                                                },
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.VolumeUp,
                                                    contentDescription = "Nghe",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }

                                            // Text
                                            Text(
                                                text = displayText,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Medium,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier
                                                    .align(Alignment.Center)
                                                    .padding(12.dp)
                                            )

                                            // Side label
                                            Text(
                                                text = sideLabel,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier
                                                    .align(Alignment.BottomCenter)
                                                    .padding(bottom = 8.dp)
                                            )
                                        }
                                    }
                                } else {
                                    // Empty slot
                                    Box(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Navigation buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            SoundManager.playClick()
                            if (pageIndex > 0) pageIndex--
                        },
                        enabled = pageIndex > 0,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Previous")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Nhóm trước", fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            SoundManager.playClick()
                            shuffledItems = items.shuffled()
                            pageIndex = 0
                            flippedStates = List(pageSize) { false }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Restart")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Chơi lại", fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            SoundManager.playClick()
                            if (pageIndex < totalPages - 1) pageIndex++
                        },
                        enabled = pageIndex < totalPages - 1,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Nhóm sau", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = "Next")
                    }
                }
            }
        }
    }
}
