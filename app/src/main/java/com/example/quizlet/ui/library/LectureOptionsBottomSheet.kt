package com.example.quizlet.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.quizlet.data.LectureData
import kotlinx.coroutines.launch

enum class LectureAction {
    FLASHCARD, SPACED_REPETITION, QUIZ, BLAST, MATCH, EDIT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LectureOptionsBottomSheet(
    lecture: LectureData,
    onAction: (LectureAction) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = lecture.title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            OptionItem(
                icon = Icons.Default.School,
                label = "Thẻ ghi nhớ",
                onClick = { onAction(LectureAction.FLASHCARD) }
            )
            OptionItem(
                icon = Icons.Default.Timer,
                label = "Học",
                onClick = { onAction(LectureAction.SPACED_REPETITION) }
            )
            OptionItem(
                icon = Icons.Default.Quiz,
                label = "Kiểm tra",
                onClick = { onAction(LectureAction.QUIZ) }
            )
            OptionItem(
                icon = Icons.Default.ViewList,
                label = "Khối hợp",
                onClick = { onAction(LectureAction.BLAST) }
            )
            OptionItem(
                icon = Icons.Default.Bolt,
                label = "Blast",
                onClick = { onAction(LectureAction.BLAST) }
            )
            OptionItem(
                icon = Icons.Default.Games,
                label = "Ghép thẻ",
                onClick = { onAction(LectureAction.MATCH) }
            )
            OptionItem(
                icon = Icons.Default.Edit,
                label = "Chỉnh sửa",
                onClick = { onAction(LectureAction.EDIT) }
            )
        }
    }
}

@Composable
private fun OptionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.width(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}
