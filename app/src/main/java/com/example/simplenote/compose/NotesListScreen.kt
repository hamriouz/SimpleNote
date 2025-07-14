package com.example.simplenote.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.simplenote.R
import com.example.simplenote.core.data.local.model.Note
import com.example.simplenote.core.data.local.AppDatabase
import com.example.simplenote.core.repository.NoteRepository
import com.example.simplenote.core.util.UserManager
import com.example.simplenote.ui.theme.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun NotesListScreen(
    onNoteClick: (Long?) -> Unit,
    onAddNoteClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf<List<Note>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var currentPage by remember { mutableIntStateOf(0) }
    var isEndPage by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val db = remember { AppDatabase.getDatabase(context) }
    val repo = remember { NoteRepository(db) }
    val username = remember { UserManager.getCurrentUsername(context) }

    val gridState = rememberLazyGridState()

    val pageSize = 8

    fun searchNotes(query: String) {
        scope.launch(Dispatchers.Default) {
            searchQuery = query
            val loadedNotes = repo.searchNotes(username = username, query = query.trim())
            notes = loadedNotes
        }
    }

    fun loadNotes(page: Int) {
        scope.launch(Dispatchers.Default) {
            delay(300L)
            val loadedNotes = repo.getAllNotes(
                username = username,
                limit = pageSize,
                offset = (page * pageSize)
            ).sortedWith(
                compareByDescending<Note> { it.isPinned }
                    .thenByDescending { it.lastEdited }
            )
            isEndPage = loadedNotes.isEmpty()
            notes += loadedNotes
            isLoading = false
        }


//        if (isEndPage && page > 0) return
//        scope.launch(Dispatchers.Default) {
//            delay(300)
//            val loadedNotes = if (query.isEmpty()) {
//                repo.getAllNotes(
//                    username = username,
//                    limit = pageSize,
//                    offset = (page * pageSize)
//                ).sortedWith(
//                    compareByDescending<Note> { it.isPinned }
//                        .thenByDescending { it.lastEdited }
//                )
//            } else {
//                repo.searchNotes(username = username, query = query)
//            }
//
//            isEndPage = loadedNotes.isEmpty()
//            notes = if (page == 0 || query.isNotEmpty()) {
//                loadedNotes
//            } else {
//                notes + loadedNotes
//            }
//            isLoading = false
//        }
    }

    LaunchedEffect("firstInitScreen") {
        loadNotes(currentPage)
        currentPage++
    }

    val isScrolledToEnd by remember {
        derivedStateOf {
            val lastVisibleIndex = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = gridState.layoutInfo.totalItemsCount
            lastVisibleIndex >= totalItems - 1 && totalItems > 0
        }
    }

    LaunchedEffect(isScrolledToEnd) {
        if (isScrolledToEnd) {
            loadNotes(currentPage)
            currentPage++
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = BackgroundWhite),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(R.string.search_image),
                        tint = TextTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchNotes(it) },
                        placeholder = { Text("Search...") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                }
            }

            Text(
                text = stringResource(R.string.notes),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 72.dp)
            ) {
                if (notes.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.empty_notes),
                            contentDescription = stringResource(R.string.empty_note),
                            modifier = Modifier.size(200.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.start_your_journey),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.every_big_step_start_with_small_step_nnotes_your_first_idea_and_start_n_your_journey),
                            fontSize = 14.sp,
                            color = TextTertiary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Image(
                            painter = painterResource(id = R.drawable.arrow),
                            contentDescription = stringResource(R.string.arrow),
                            modifier = Modifier.size(100.dp)
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(notes) { note ->
                            NoteCard(
                                note = note,
                                onClick = { onNoteClick(note.id) }
                            )
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .align(Alignment.BottomCenter),
            colors = CardDefaults.cardColors(containerColor = BackgroundWhite),
            shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = stringResource(R.string.home_navigation),
                        tint = PrimaryBlue,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.home),
                        color = PrimaryBlue,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onSettingsClick() },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.settings),
                        tint = TextTertiary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.settings),
                        color = TextTertiary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = onAddNoteClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-36).dp),
            containerColor = PrimaryBlue,
            contentColor = TextWhite,
            shape = RoundedCornerShape(50)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.add_note),
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

@Composable
fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = BackgroundNoteCard),
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = note.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = note.content,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp,
                    modifier = Modifier.weight(1f)
                )
            }

            if (note.isPinned) {
                Image(
                    painter = painterResource(id = R.drawable.pin96),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(24.dp)
                        .offset(x = 10.dp, y = (-10).dp)
                )
            }
        }
    }
} 