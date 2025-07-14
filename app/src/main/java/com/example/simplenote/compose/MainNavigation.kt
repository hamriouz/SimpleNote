package com.example.simplenote.compose

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun MainNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "notes_list"
    ) {
        composable("notes_list") {
            NotesListScreen(
                onNoteClick = { noteId ->
                    if (noteId != null) {
                        navController.navigate("note_editor/$noteId")
                    } else {
                        navController.navigate("note_editor/-1")
                    }
                },
                onAddNoteClick = {
                    navController.navigate("note_editor/-1")
                },
                onSettingsClick = {
                    navController.navigate("settings")
                }
            )
        }
        
        composable("note_editor/{noteId}") { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")?.toIntOrNull() ?: -1
            NoteEditorScreen(
                noteId = noteId,
                onBackClick = {
                    navController.navigateUp()
                }
            )
        }
        
        composable("settings") {
            SettingsScreen(
                onBackClick = {
                    navController.navigateUp()
                },
                onChangePasswordClick = {
                    navController.navigate("change_password")
                }
            )
        }
        
        composable("change_password") {
            ChangePasswordScreen(
                onBackClick = {
                    navController.navigateUp()
                }
            )
        }
    }
} 