package com.example.quizlet.ui.study

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quizlet.data.FlashcardItem
import com.example.quizlet.data.LectureContentHelper
import com.example.quizlet.utils.SoundManager
import com.example.quizlet.utils.rememberTextToSpeech

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LectureDetailScreen(
    lectureTitle: String,
    items: List<FlashcardItem>,
    onBack: () -> Unit,
    onAddItem: (String, String) -> Unit,
    onEditItem: (Int, String, String) -> Unit,
    onDeleteItem: (Int) -> Unit,
    onImportJson: (String) -> Result<Unit>
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var editingIndex by remember { mutableStateOf<Int?>(null) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val tts = rememberTextToSpeech()

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }

    // Dialog thêm/sửa
    if (showAddDialog || editingIndex != null) {
        val isEditing = editingIndex != null
        var foreignText by remember {
            mutableStateOf(
                if (isEditing) items[editingIndex!!].foreign else ""
            )
        }
        var nativeText by remember {
            mutableStateOf(
                if (isEditing) items[editingIndex!!].native else ""
            )
        }

        AlertDialog(
            onDismissRequest = {
                SoundManager.playClick()
                showAddDialog = false
                editingIndex = null
            },
            title = { Text(if (isEditing) "Sửa từ vựng" else "Thêm từ vựng mới") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = foreignText,
                        onValueChange = { foreignText = it },
                        label = { Text("Từ gốc") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = nativeText,
                        onValueChange = { nativeText = it },
                        label = { Text("Nghĩa") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        SoundManager.playClick()
                        if (foreignText.isNotBlank() && nativeText.isNotBlank()) {
                            if (isEditing) {
                                onEditItem(editingIndex!!, foreignText, nativeText)
                                snackbarMessage = "Đã cập nhật"
                            } else {
                                onAddItem(foreignText, nativeText)
                                snackbarMessage = "Đã thêm"
                            }
                            showAddDialog = false
                            editingIndex = null
                        }
                    }
                ) {
                    Text("Lưu")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        SoundManager.playClick()
                        showAddDialog = false
                        editingIndex = null
                    }
                ) {
                    Text("Hủy")
                }
            }
        )
    }

    // Dialog import JSON
    if (showImportDialog) {
        var jsonInput by remember { mutableStateOf(LectureContentHelper.sampleJson) }
        AlertDialog(
            onDismissRequest = {
                SoundManager.playClick()
                showImportDialog = false
            },
            title = { Text("Import từ JSON") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Dán JSON bài giảng:", fontSize = 13.sp)
                    OutlinedTextField(
                        value = jsonInput,
                        onValueChange = { jsonInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        label = { Text("JSON") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        SoundManager.playClick()
                        if (onImportJson(jsonInput).isSuccess) {
                            snackbarMessage = "Import thành công!"
                            showImportDialog = false
                        } else {
                            snackbarMessage = "JSON không hợp lệ"
                        }
                    }
                ) {
                    Text("Import")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    SoundManager.playClick()
                    showImportDialog = false
                }) {
                    Text("Hủy")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(lectureTitle, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = {
                        SoundManager.playClick()
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        SoundManager.playClick()
                        showImportDialog = true
                    }) {
                        Icon(Icons.Default.FileDownload, contentDescription = null)
                    }
                    IconButton(onClick = {
                        SoundManager.playClick()
                        showAddDialog = true
                    }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (items.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Chưa có từ vựng nào.",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Nhấn nút + để thêm hoặc import từ JSON.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(items) { index, item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.foreign,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = item.native,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Row {
                                    IconButton(onClick = {
                                        SoundManager.playClick()
                                        tts.speak(item.foreign, isForeign = true)
                                    }) {
                                        Icon(Icons.Default.VolumeUp, contentDescription = "Nghe")
                                    }
                                    IconButton(onClick = {
                                        SoundManager.playClick()
                                        editingIndex = index
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Sửa")
                                    }
                                    IconButton(onClick = {
                                        SoundManager.playClick()
                                        onDeleteItem(index)
                                        snackbarMessage = "Đã xóa"
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Xóa")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}