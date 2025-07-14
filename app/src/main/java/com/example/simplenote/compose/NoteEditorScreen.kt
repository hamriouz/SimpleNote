package com.example.simplenote.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.simplenote.R
import com.example.simplenote.core.data.local.AppDatabase
import com.example.simplenote.core.data.local.model.Note
import com.example.simplenote.core.repository.NoteRepository
import com.example.simplenote.core.util.UserManager
import com.example.simplenote.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NoteEditorScreen(
    noteId: Int,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var lastEdited by remember { mutableStateOf(Date()) }
    var currentNote by remember { mutableStateOf<Note?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val db = remember { AppDatabase.getDatabase(context) }
    val repo = remember { NoteRepository(db) }
    val username = remember { UserManager.getCurrentUsername(context) }
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }
    
    LaunchedEffect(noteId) {
        if (noteId != -1) {
            scope.launch(Dispatchers.IO) {
                val note = repo.getNoteById(noteId.toLong(), username)
                note?.let {
                    currentNote = it
                    title = it.title
                    content = it.content
                    lastEdited = Date(it.lastEdited)
                }
            }
        }
    }
    
    LaunchedEffect(title, content) {
        lastEdited = Date()
    }
    
    fun saveNote() {
        if (title.isNotBlank() || content.isNotBlank()) {
            scope.launch(Dispatchers.IO) {
                val createNote = currentNote == null
                if (!createNote) {
                    currentNote?.title = title
                    currentNote?.content = content
                    currentNote?.lastEdited = System.currentTimeMillis()
                    currentNote?.isSynced = false
                    repo.update(currentNote ?: return@launch)
                } else {
                    val note = Note(
                        title = title,
                        content = content,
                        lastEdited = System.currentTimeMillis(),
                        username = username
                    )
                    val generatedId = repo.insert(note)
                    val noteCopy = note.copy(id = generatedId, userId = generatedId)
                    repo.update(noteCopy)
                }
            }
        }
    }
    
    fun deleteNote() {
        currentNote?.let { note ->
            scope.launch(Dispatchers.IO) {
                repo.delete(note)
                scope.launch(Dispatchers.Main) {
                    onBackClick()
                }
            }
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundWhite)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(BackgroundWhite)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.clickable { 
                    saveNote()
                    onBackClick() 
                },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.purple_arrow),
                    contentDescription = stringResource(R.string.back),
                    tint = PrimaryBlue,
                    modifier = Modifier.size(10.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.back),
                    color = PrimaryBlue,
                    fontSize = 18.sp
                )
            }
        }
        
        HorizontalDivider(
            color = BackgroundSeparator,
            thickness = 1.dp
        )
        
        BasicTextField(
            value = title,
            onValueChange = { title = it },
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundWhite)
                .padding(horizontal = 24.dp)
                .padding(top = 32.dp, bottom = 15.dp),
            textStyle = TextStyle(
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = TextBlack
            ),
            decorationBox = { innerTextField ->
                if (title.isEmpty()) {
                    Text(
                        text = stringResource(R.string.title),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextBlack
                    )
                }
                innerTextField()
            }
        )
        
        BasicTextField(
            value = content,
            onValueChange = { content = it },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.Transparent)
                .padding(horizontal = 24.dp),
            textStyle = TextStyle(
                fontSize = 18.sp,
                color = TextDark
            ),
            decorationBox = { innerTextField ->
                if (content.isEmpty()) {
                    Text(
                        text = "Feel Free to Write Here...",
                        fontSize = 18.sp,
                        color = TextDark
                    )
                }
                innerTextField()
            }
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Last edited on ${dateFormat.format(lastEdited)}",
                color = TextPrimary,
                fontSize = 16.sp,
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFFF8F8F8))
                    .padding(16.dp)
            )
            
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(PrimaryBlue)
                    .clickable { 
                        if (currentNote != null) {
                            showDeleteDialog = true
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.trash),
                    contentDescription = "Delete Note",
                    tint = TextWhite,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Note") },
            text = { Text("Are you sure you want to delete this note?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        deleteNote()
                    }
                ) {
                    Text("Delete", color = AccentRed)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
} 