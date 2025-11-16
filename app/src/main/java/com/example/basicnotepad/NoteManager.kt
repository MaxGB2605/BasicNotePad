package com.example.basicnotepad

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class NoteManager(private val context: Context) {
    
    private val prefsName = "NotepadPrefs"
    private val notesKey = "notes_list"
    private val gson = Gson()
    
    fun getAllNotes(): MutableList<Note> {
        val sharedPreferences = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        val notesJson = sharedPreferences.getString(notesKey, null)
        
        return if (notesJson != null) {
            val type = object : TypeToken<MutableList<Note>>() {}.type
            gson.fromJson(notesJson, type)
        } else {
            mutableListOf()
        }
    }
    
    fun saveNote(note: Note) {
        val notes = getAllNotes()
        val existingIndex = notes.indexOfFirst { it.id == note.id }
        
        note.lastModified = System.currentTimeMillis()
        
        if (existingIndex != -1) {
            notes[existingIndex] = note
        } else {
            notes.add(0, note)
        }
        
        saveAllNotes(notes)
    }
    
    fun deleteNote(noteId: String) {
        val notes = getAllNotes()
        notes.removeAll { it.id == noteId }
        saveAllNotes(notes)
    }
    
    fun getNoteById(noteId: String): Note? {
        return getAllNotes().find { it.id == noteId }
    }
    
    private fun saveAllNotes(notes: List<Note>) {
        val sharedPreferences = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        val notesJson = gson.toJson(notes)
        sharedPreferences.edit {
            putString(notesKey, notesJson)
        }
    }
}
