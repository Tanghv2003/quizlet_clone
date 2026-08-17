package com.example.quizlet.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quizlet.data.FolderData
import com.example.quizlet.data.LectureData

private sealed class DialogState {
    data object None : DialogState()
    data object AddFolder : DialogState()
    data object AddLecture : DialogState()
    data class RenameFolder(val folder: FolderData) : DialogState()
    data class RenameLecture(val lecture: LectureData) : DialogState()
    data class DeleteFolder(val folder: FolderData) : DialogState()
    data class DeleteLecture(val lecture: LectureData) : DialogState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderLibraryScreen(
    modifier: Modifier = Modifier,
    viewModel: FolderViewModel,
    onLectureSelected: (String, LectureData) -> Unit   // đã đổi tên
) {
    val pathStack by viewModel.pathStack.collectAsState()
    val subfolders by viewModel.subfolders.collectAsState()
    val lectures by viewModel.lectures.collectAsState()
    val currentFolderId by viewModel.currentFolderId.collectAsState()

    var dialogState by remember { mutableStateOf<DialogState>(DialogState.None) }
    var showAddMenu by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Breadcrumb
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                pathStack.forEachIndexed { index, folder ->
                    val isLast = index == pathStack.lastIndex
                    Text(
                        text = folder.name,
                        fontSize = 15.sp,
                        fontWeight = if (isLast) FontWeight.Bold else FontWeight.Normal,
                        color = if (isLast) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(enabled = !isLast) {
                            viewModel.goToBreadcrumb(index)
                        }
                    )
                    if (!isLast) {
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }
                }
            }

            if (subfolders.isEmpty() && lectures.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Thư mục trống. Nhấn nút + để thêm thư mục hoặc bài giảng.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(32.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(subfolders, key = { "folder_${it.id}" }) { folder ->
                        FolderRow(
                            folder = folder,
                            onOpen = { viewModel.openFolder(folder) },
                            onRename = { dialogState = DialogState.RenameFolder(folder) },
                            onDelete = { dialogState = DialogState.DeleteFolder(folder) }
                        )
                    }
                    items(lectures, key = { "lecture_${it.id}" }) { lecture ->
                        LectureRow(
                            lecture = lecture,
                            onRename = { dialogState = DialogState.RenameLecture(lecture) },
                            onDelete = { dialogState = DialogState.DeleteLecture(lecture) },
                            onLectureSelected = { onLectureSelected(currentFolderId, lecture) }
                        )
                    }
                }
            }
        }

        // FAB
        Box(modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp)) {
            FloatingActionButton(onClick = { showAddMenu = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Thêm mới")
            }
            DropdownMenu(expanded = showAddMenu, onDismissRequest = { showAddMenu = false }) {
                DropdownMenuItem(
                    text = { Text("Thư mục mới") },
                    leadingIcon = { Icon(Icons.Filled.CreateNewFolder, contentDescription = null) },
                    onClick = {
                        showAddMenu = false
                        dialogState = DialogState.AddFolder
                    }
                )
                DropdownMenuItem(
                    text = { Text("Bài giảng mới") },
                    leadingIcon = { Icon(Icons.Filled.NoteAdd, contentDescription = null) },
                    onClick = {
                        showAddMenu = false
                        dialogState = DialogState.AddLecture
                    }
                )
            }
        }
    }

    // Các dialog
    when (val state = dialogState) {
        DialogState.AddFolder -> NameInputDialog(
            title = "Thư mục mới",
            initialValue = "",
            confirmLabel = "Tạo",
            onConfirm = { name -> viewModel.createFolder(name) },
            onDismiss = { dialogState = DialogState.None }
        )
        DialogState.AddLecture -> NameInputDialog(
            title = "Bài giảng mới",
            initialValue = "",
            confirmLabel = "Tạo",
            onConfirm = { name -> viewModel.createLecture(name) },
            onDismiss = { dialogState = DialogState.None }
        )
        is DialogState.RenameFolder -> NameInputDialog(
            title = "Đổi tên thư mục",
            initialValue = state.folder.name,
            confirmLabel = "Lưu",
            onConfirm = { name -> viewModel.renameFolder(state.folder, name) },
            onDismiss = { dialogState = DialogState.None }
        )
        is DialogState.RenameLecture -> NameInputDialog(
            title = "Đổi tên bài giảng",
            initialValue = state.lecture.title,
            confirmLabel = "Lưu",
            onConfirm = { name -> viewModel.renameLecture(state.lecture, name) },
            onDismiss = { dialogState = DialogState.None }
        )
        is DialogState.DeleteFolder -> ConfirmDeleteDialog(
            message = "Xóa thư mục \"${state.folder.name}\"? Toàn bộ thư mục con và bài giảng bên trong sẽ bị xóa theo.",
            onConfirm = { viewModel.deleteFolder(state.folder) },
            onDismiss = { dialogState = DialogState.None }
        )
        is DialogState.DeleteLecture -> ConfirmDeleteDialog(
            message = "Xóa bài giảng \"${state.lecture.title}\"?",
            onConfirm = { viewModel.deleteLecture(state.lecture) },
            onDismiss = { dialogState = DialogState.None }
        )
        DialogState.None -> Unit
    }
}

@Composable
private fun FolderRow(
    folder: FolderData,
    onOpen: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Folder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = folder.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Filled.Edit, contentDescription = "Tùy chọn")
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Đổi tên") },
                        leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                        onClick = { showMenu = false; onRename() }
                    )
                    if (!folder.isRoot) {
                        DropdownMenuItem(
                            text = { Text("Xóa") },
                            leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                            onClick = { showMenu = false; onDelete() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LectureRow(
    lecture: LectureData,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onLectureSelected: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onLectureSelected() },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Description,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = lecture.title,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Filled.Edit, contentDescription = "Tùy chọn")
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Đổi tên") },
                        leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                        onClick = { showMenu = false; onRename() }
                    )
                    DropdownMenuItem(
                        text = { Text("Xóa") },
                        leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                        onClick = { showMenu = false; onDelete() }
                    )
                }
            }
        }
    }
}

@Composable
private fun NameInputDialog(
    title: String,
    initialValue: String,
    confirmLabel: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                placeholder = { Text("Nhập tên...") }
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (text.isNotBlank()) {
                        onConfirm(text)
                        onDismiss()
                    }
                },
                enabled = text.isNotBlank()
            ) { Text(confirmLabel) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy") }
        }
    )
}

@Composable
private fun ConfirmDeleteDialog(
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Xác nhận xóa") },
        text = { Text(message) },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(); onDismiss() },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) { Text("Xóa") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy") }
        }
    )
}