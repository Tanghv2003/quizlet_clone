package com.example.quizlet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.quizlet.data.LectureContentHelper
import com.example.quizlet.data.LectureData
import com.example.quizlet.ui.library.FolderLibraryScreen
import com.example.quizlet.ui.library.FolderViewModel
import com.example.quizlet.ui.library.FolderViewModelFactory
import com.example.quizlet.ui.library.LectureAction
import com.example.quizlet.ui.library.LectureOptionsBottomSheet
import com.example.quizlet.ui.study.FlashcardStudyScreen
import com.example.quizlet.ui.study.LectureDetailScreen
import com.example.quizlet.ui.study.LectureDetailViewModel
import com.example.quizlet.ui.study.LectureDetailViewModelFactory
import com.example.quizlet.ui.study.QuizScreen
import com.example.quizlet.ui.study.SpacedRepetitionScreen
import com.example.quizlet.ui.theme.QuizletTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuizletTheme {
                QuizletApp()
            }
        }
    }
}

// ----- Các tab -----
enum class BottomTab(val label: String, val icon: ImageVector) {
    Home("Home", Icons.Filled.Home),
    Search("Search", Icons.Filled.Search),
    Create("Create", Icons.Filled.Add),
    Library("Library", Icons.Filled.List),
    Profile("Profile", Icons.Filled.Person)
}

sealed class Screen {
    data class Tab(val tab: BottomTab) : Screen()
    data class Flashcard(val term: String, val definition: String) : Screen()
}

data class RecentQuiz(
    val title: String,
    val questionsCount: Int,
    val status: String,
    val term: String,
    val definition: String
)

private val sampleCategories = listOf("Math", "Chemistry", "Physics")
private val recentQuizzes = listOf(
    RecentQuiz("Biology", 12, "Completed", "Cell", "Basic unit of life"),
    RecentQuiz("Geography", 20, "Incomplete", "Continent", "Large landmass")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizletApp() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Tab(BottomTab.Home)) }
    var selectedTab by remember { mutableStateOf(BottomTab.Home) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (currentScreen is Screen.Flashcard) {
                CenterAlignedTopAppBar(
                    title = { Text("Flashcard", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    navigationIcon = {
                        IconButton(onClick = { currentScreen = Screen.Tab(selectedTab) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (currentScreen is Screen.Tab) {
                QuizletBottomBar(
                    selectedTab = selectedTab,
                    onTabSelected = { tab ->
                        selectedTab = tab
                        currentScreen = Screen.Tab(tab)
                    }
                )
            }
        }
    ) { innerPadding ->
        when (val screen = currentScreen) {
            is Screen.Tab -> when (screen.tab) {
                BottomTab.Home -> HomeScreen(
                    modifier = Modifier.padding(innerPadding),
                    onRecentClick = { recent ->
                        currentScreen = Screen.Flashcard(recent.term, recent.definition)
                    },
                    onCategoryClick = { category ->
                        currentScreen = Screen.Flashcard("Category: $category", "Sample definition")
                    },
                    onPlayNowClick = {
                        currentScreen = Screen.Flashcard("Sample Quiz", "Enjoy learning!")
                    }
                )
                BottomTab.Search -> PlaceholderTabScreen(
                    modifier = Modifier.padding(innerPadding),
                    title = "Search",
                    message = "Tìm kiếm học phần, thư mục, người dùng..."
                )
                BottomTab.Create -> PlaceholderTabScreen(
                    modifier = Modifier.padding(innerPadding),
                    title = "Create",
                    message = "Tạo học phần flashcard mới của bạn tại đây."
                )
                BottomTab.Library -> LibraryScreen(
                    modifier = Modifier.padding(innerPadding),
                    onStudySetClick = { quiz ->
                        currentScreen = Screen.Flashcard(quiz.term, quiz.definition)
                    }
                )
                BottomTab.Profile -> PlaceholderTabScreen(
                    modifier = Modifier.padding(innerPadding),
                    title = "Profile",
                    message = "Thông tin tài khoản và thành tích của bạn."
                )
            }
            is Screen.Flashcard -> FlashcardScreen(
                modifier = Modifier.padding(innerPadding),
                term = screen.term,
                definition = screen.definition
            )
        }
    }
}

@Composable
fun QuizletBottomBar(
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        BottomTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label, fontSize = 11.sp) },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
fun PlaceholderTabScreen(modifier: Modifier = Modifier, title: String, message: String) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

enum class LibraryFilter(val label: String, val icon: ImageVector) {
    StudySets("Học phần", Icons.Filled.List),
    Classes("Lớp học", Icons.Filled.Groups),
    Folders("Thư mục", Icons.Filled.Folder),
    PracticeTests("Bài kiểm tra thử", Icons.Filled.Quiz)
}

@Composable
fun LibraryScreen(
    modifier: Modifier = Modifier,
    onStudySetClick: (RecentQuiz) -> Unit
) {
    var selectedFilter by remember { mutableStateOf(LibraryFilter.StudySets) }
    var selectedLecture by remember { mutableStateOf<Pair<String, LectureData>?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var currentAction by remember { mutableStateOf<LectureAction?>(null) }

    val context = LocalContext.current
    val app = context.applicationContext as QuizletApplication
    val folderViewModel: FolderViewModel = viewModel(
        factory = FolderViewModelFactory(app.folderRepository)
    )

    fun handleAction(action: LectureAction) {
        showBottomSheet = false
        currentAction = action
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = "Thư viện của tôi",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LibraryFilter.entries.forEach { filter ->
                LibraryFilterChip(
                    label = filter.label,
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        when (selectedFilter) {
            LibraryFilter.StudySets -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(recentQuizzes) { quiz ->
                        RecentQuizItem(quiz = quiz, onClick = { onStudySetClick(quiz) })
                    }
                }
            }
            LibraryFilter.Folders -> {
                if (selectedLecture != null && currentAction != null) {
                    val (folderId, lecture) = selectedLecture!!
                    when (currentAction) {
                        LectureAction.EDIT -> {
                            val viewModel: LectureDetailViewModel = viewModel(
                                key = "lecture_detail_${folderId}_${lecture.id}",
                                factory = LectureDetailViewModelFactory(
                                    context,
                                    app.folderRepository,
                                    folderId,
                                    lecture.id
                                )
                            )
                            val items by viewModel.items.collectAsState()
                            val title by viewModel.lectureTitle.collectAsState()
                            LectureDetailScreen(
                                lectureTitle = title,
                                items = items,
                                onBack = {
                                    selectedLecture = null
                                    currentAction = null
                                },
                                onAddItem = { foreign, native -> viewModel.addItem(foreign, native) },
                                onEditItem = { index, foreign, native -> viewModel.editItem(index, foreign, native) },
                                onDeleteItem = { index -> viewModel.deleteItem(index) },
                                onImportJson = { json -> viewModel.importFromJson(json) }
                            )
                        }
                        LectureAction.FLASHCARD -> {
                            val content = app.folderRepository.getLectureContent(lecture.id)
                            val items = LectureContentHelper.parseItems(content)
                            FlashcardStudyScreen(
                                items = items,
                                onBack = {
                                    selectedLecture = null
                                    currentAction = null
                                }
                            )
                        }
                        LectureAction.SPACED_REPETITION -> {
                            val viewModel: LectureDetailViewModel = viewModel(
                                key = "lecture_detail_${folderId}_${lecture.id}",
                                factory = LectureDetailViewModelFactory(
                                    context,
                                    app.folderRepository,
                                    folderId,
                                    lecture.id
                                )
                            )
                            val allItems by viewModel.items.collectAsState()
                            val dueItems by viewModel.dueItems.collectAsState()
                            val cardProgressMap by viewModel.cardProgressMap.collectAsState()
                            SpacedRepetitionScreen(
                                dueItems = dueItems,
                                allItems = allItems,
                                cardProgressMap = cardProgressMap,
                                onReviewCard = { foreign, rating -> viewModel.reviewCard(foreign, rating) },
                                onBack = {
                                    selectedLecture = null
                                    currentAction = null
                                }
                            )
                        }
                        LectureAction.QUIZ -> {
                            val content = app.folderRepository.getLectureContent(lecture.id)
                            val items = LectureContentHelper.parseItems(content)
                            QuizScreen(
                                items = items,
                                onBack = {
                                    selectedLecture = null
                                    currentAction = null
                                }
                            )
                        }
                        else -> {
                            PlaceholderScreen(
                                message = "Chức năng ${currentAction?.name} đang phát triển",
                                onBack = {
                                    selectedLecture = null
                                    currentAction = null
                                }
                            )
                        }
                    }
                } else {
                    FolderLibraryScreen(
                        modifier = Modifier.fillMaxSize(),
                        viewModel = folderViewModel,
                        onLectureSelected = { folderId, lecture ->
                            selectedLecture = Pair(folderId, lecture)
                            showBottomSheet = true
                        }
                    )
                }
            }
            else -> {
                LibraryEmptyState(filter = selectedFilter)
            }
        }
    }

    if (showBottomSheet && selectedLecture != null) {
        val (folderId, lecture) = selectedLecture!!
        val path = folderViewModel.getLecturePath(folderId, lecture.id)
        LectureOptionsBottomSheet(
            lecture = lecture,
            path = path,
            onAction = { action -> handleAction(action) },
            onDismiss = {
                showBottomSheet = false
                selectedLecture = null
            }
        )
    }
}

@Composable
fun LibraryFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    val contentColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    val containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface

    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable { onClick() },
        shape = RoundedCornerShape(50),
        color = containerColor,
        border = BorderStroke(if (selected) 1.5.dp else 1.dp, borderColor)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            color = contentColor,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

@Composable
fun LibraryEmptyState(filter: LibraryFilter) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(72.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = filter.icon,
                        contentDescription = filter.label,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Chưa có ${filter.label.lowercase()}",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Nội dung của bạn sẽ hiện ở đây.",
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onRecentClick: (RecentQuiz) -> Unit,
    onCategoryClick: (String) -> Unit,
    onPlayNowClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Hi, Kenzy",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Ready to play",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clickable { /* leaderboard */ },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(50),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "200",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = "",
            onValueChange = {},
            placeholder = { Text("Search for a quiz") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Play and Win",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Start a quiz now and enjoy",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onPlayNowClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Get Started >")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Categories",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "See all",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { /* navigate */ }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            sampleCategories.forEach { category ->
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onCategoryClick(category) },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = category,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Recent",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))
        recentQuizzes.forEach { quiz ->
            RecentQuizItem(
                quiz = quiz,
                onClick = { onRecentClick(quiz) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun RecentQuizItem(quiz: RecentQuiz, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = quiz.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${quiz.questionsCount} questions",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (quiz.status == "Completed")
                        MaterialTheme.colorScheme.tertiaryContainer
                    else
                        MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = quiz.status,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (quiz.status == "Completed")
                        MaterialTheme.colorScheme.onTertiaryContainer
                    else
                        MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
fun FlashcardScreen(
    modifier: Modifier = Modifier,
    term: String,
    definition: String
) {
    var isFlipped by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier
                .size(width = 300.dp, height = 200.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isFlipped) "Definition" else "Term",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isFlipped) definition else term,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { isFlipped = !isFlipped },
            modifier = Modifier.size(width = 150.dp, height = 50.dp)
        ) {
            Text(if (isFlipped) "Show Term" else "Show Definition")
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Card 1 of 1",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun PlaceholderScreen(
    message: String,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = message, fontSize = 18.sp, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBack) {
            Text("Quay lại")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHome() {
    QuizletTheme {
        QuizletApp()
    }
}