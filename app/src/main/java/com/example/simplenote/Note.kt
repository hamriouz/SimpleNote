package com.example.simplenote

data class Note(
    val id: Long,
    var title: String,
    var content: String,
    var lastEdited: Long = System.currentTimeMillis()
) 