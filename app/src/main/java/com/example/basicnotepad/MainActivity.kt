package com.example.basicnotepad

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {
    
    private lateinit var noteEditText: EditText
    private lateinit var backButton: ImageButton
    private lateinit var saveButton: Button
    private lateinit var themeButton: Button
    private lateinit var clearButton: Button
    private lateinit var noteManager: NoteManager
    private lateinit var themeManager: ThemeManager
    private var currentNote: Note? = null
    private var hasUnsavedChanges = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        noteEditText = findViewById(R.id.noteEditText)
        backButton = findViewById(R.id.backButton)
        saveButton = findViewById(R.id.saveButton)
        themeButton = findViewById(R.id.themeButton)
        clearButton = findViewById(R.id.clearButton)
        noteManager = NoteManager(this)
        themeManager = ThemeManager(this)
        
        // Get note ID from intent or create new note
        val noteId = intent.getStringExtra("NOTE_ID")
        if (noteId != null) {
            currentNote = noteManager.getNoteById(noteId)
        }
        
        if (currentNote == null) {
            currentNote = Note()
        }
        
        // Load note content
        loadNote()
        
        // Set up button click listeners
        backButton.setOnClickListener {
            onBackPressed()
        }
        
        saveButton.setOnClickListener {
            saveNote()
            hasUnsavedChanges = false
            Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show()
        }
        
        themeButton.setOnClickListener {
            themeManager.showThemeDialog()
        }
        
        clearButton.setOnClickListener {
            clearNote()
        }
        
        // Add text change listener for auto-save
        noteEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                hasUnsavedChanges = true
            }
            
            override fun afterTextChanged(s: Editable?) {
                // Auto-save after 2 seconds of inactivity
                noteEditText.removeCallbacks(autoSaveRunnable)
                noteEditText.postDelayed(autoSaveRunnable, 2000)
            }
        })
    }
    
    private val autoSaveRunnable = Runnable {
        if (hasUnsavedChanges) {
            saveNote()
            hasUnsavedChanges = false
        }
    }
    
    private fun saveNote() {
        currentNote?.let { note ->
            note.content = noteEditText.text.toString()
            noteManager.saveNote(note)
        }
    }
    
    private fun loadNote() {
        currentNote?.let { note ->
            noteEditText.setText(note.content)
            noteEditText.setSelection(note.content.length)
        }
        hasUnsavedChanges = false
    }
    
    private fun clearNote() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Clear Note")
            .setMessage("Are you sure you want to clear all text?")
            .setPositiveButton("Clear") { _, _ ->
                noteEditText.text.clear()
                saveNote()
                Toast.makeText(this, "Note cleared", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    
    override fun onPause() {
        super.onPause()
        // Save when app goes to background
        if (hasUnsavedChanges) {
            saveNote()
        }
    }
}
