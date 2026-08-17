package com.example.quizlet.ui.study

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LectureDetailScreen(
    lectureTitle: String,
    items: List<FlashcardItem>,
    onBack: () -> Unit,
    onAddItem: (String, String) -> Unit,
    onEditItem: (Int, String, String) -> Unit,
    onDeleteItem: (Int) -> Unit,
    onImportJson: (String) -> Result<Unit>,
    onStartFlashcards: () -> Unit,
    onStartSpacedRepetition: () -> Unit,
    onStartQuiz: () -> Unit,
    onStartFillInBlank: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var editingIndex by remember { mutableStateOf<Int?>(null) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { snackbarHostState.showSnackbar(it); snackbarMessage = null }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(lectureTitle, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { showImportDialog = true }) { Icon(Icons.Default.FileDownload, contentDescription = null) }
                    IconButton(onClick = { showAddDialog = true }) { Icon(Icons.Default.Add, contentDescription = null) }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = selectedTab == 0, onClick = { selectedTab = 0 }, icon = { Icon(Icons.Default.List, contentDescription = null) }, label = { Text("Danh sách (${items.size})") })
                NavigationBarItem(selected = selectedTab == 1, onClick = { selectedTab = 1 }, icon = { Icon(Icons.Default.School, contentDescription = null) }, label = { Text("Học & Kiểm tra") })
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (selectedTab == 0) {
                if (items.isEmpty()) {
                    Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
    if (showAddDialog || editingIndex != null) {
        val isEditing = editingIndex != null
        var foreignText by remember { mutableStateOf(if (isEditing) items[editingIndex!!].foreign else "") }
        var nativeText by remember { mutableStateOf(if (isEditing) items[editingIndex!!].native else "") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false; editingIndex = null },
            title = { Text(if (isEditing) "Sửa từ vựng" else "Thêm từ vựng mới") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = foreignText, onValueChange = { foreignText = it }, label = { Text("Từ gốc") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = nativeText, onValueChange = { nativeText = it }, label = { Text("Nghĩa") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (foreignText.isNotBlank() && nativeText.isNotBlank()) {
                        if (isEditing) { onEditItem(editingIndex!!, foreignText, nativeText); snackbarMessage = "Đã cập nhật" }
                        else { onAddItem(foreignText, nativeText); snackbarMessage = "Đã thêm" }
                        showAddDialog = false; editingIndex = null
                    }
                }) { Text("Lưu") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false; editingIndex = null }) { Text("Hủy") } }
        )
    }

    if (showImportDialog) {
        var jsonInput by remember { mutableStateOf(LectureContentHelper.sampleJson) }
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import từ JSON") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Dán JSON bài giảng:", fontSize = 13.sp)
                    OutlinedTextField(value = jsonInput, onValueChange = { jsonInput = it }, modifier = Modifier.fillMaxWidth().height(140.dp), label = { Text("JSON") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (onImportJson(jsonInput).isSuccess) { snackbarMessage = "Import thành công!"; showImportDialog = false }
                    else { snackbarMessage = "JSON không hợp lệ" }
                }) { Text("Import") }
            },
            dismissButton = { TextButton(onClick = { showImportDialog = false }) { Text("Hủy") } }
        )
    }
}

@Composable
fun StudyModeCard(title: String, description: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(48.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        }
    }
}
