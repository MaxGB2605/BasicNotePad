package com.example.basicnotepad

import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

data class Note(
    val id: String = UUID.randomUUID().toString(),
    var title: String = "",
    var content: String = "",
    var lastModified: Long = System.currentTimeMillis()
) : Serializable {
    
    fun getPreview(): String {
        return if (content.length > 100) {
            content.substring(0, 100) + "..."
        } else {
            content
        }
    }
    
    fun getFormattedDate(): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(lastModified))
    }
    
    fun getTitleOrDefault(): String {
        return if (title.isNotEmpty()) {
            title
        } else if (content.isNotEmpty()) {
            content.lines().firstOrNull()?.take(30) ?: "Untitled Note"
        } else {
            "Untitled Note"
        }
    }
}
