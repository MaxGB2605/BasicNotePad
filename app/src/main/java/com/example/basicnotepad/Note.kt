package com.example.basicnotepad

import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

data class Note(
    val id: String = UUID.randomUUID().toString(),
    var title: String = "",
    var content: String = "",
    var lastModified: Long = System.currentTimeMillis(),
    var isChecklist: Boolean = false,
    var checklistItems: MutableList<ChecklistItem> = mutableListOf()
) : Serializable {
    
    fun getPreview(): String {
        if (isChecklist) {
            val total = checklistItems.size
            val checked = checklistItems.count { it.isChecked }
            return "$checked/$total items completed"
        }
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
        } else if (isChecklist && checklistItems.isNotEmpty()) {
            checklistItems.firstOrNull()?.text?.take(30) ?: "Untitled Checklist"
        } else if (content.isNotEmpty()) {
            content.lines().firstOrNull()?.take(30) ?: "Untitled Note"
        } else {
            if (isChecklist) "Untitled Checklist" else "Untitled Note"
        }
    }
}
