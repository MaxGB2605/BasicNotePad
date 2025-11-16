package com.example.basicnotepad

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {
    
    private lateinit var noteTitleEditText: EditText
    private lateinit var noteEditText: EditText
    private lateinit var headerTitle: TextView
    private lateinit var backButton: ImageButton
    private lateinit var saveButton: Button
    private lateinit var themeButton: Button
    private lateinit var clearButton: Button
    private lateinit var noteManager: NoteManager
    private lateinit var themeManager: ThemeManager
    private var currentNote: Note? = null
    private var hasUnsavedChanges = false
    private lateinit var onBackPressedCallback: OnBackPressedCallback
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)
        
        noteTitleEditText = findViewById(R.id.noteTitleEditText)
        noteEditText = findViewById(R.id.noteEditText)
        headerTitle = findViewById(R.id.headerTitle)
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
            handleBackPressed()
        }
        
        saveButton.setOnClickListener {
            saveNote()
            hasUnsavedChanges = false
            Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show()
            finish()  // Return to previous activity
        }
        
        themeButton.setOnClickListener {
            themeManager.showThemeDialog()
        }
        
        clearButton.setOnClickListener {
            clearNote()
        }
        
        // Add text change listener for auto-save on content
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                hasUnsavedChanges = true
            }
            
            override fun afterTextChanged(s: Editable?) {
                // Update header title when note title changes
                if (noteTitleEditText.text.toString() != currentNote?.title) {
                    // Update header title, fallback to app name if empty
                    headerTitle.text = noteTitleEditText.text.toString().ifBlank { getString(R.string.app_name) }
                }
                // Auto-save after 2 seconds of inactivity
                noteEditText.removeCallbacks(autoSaveRunnable)
                noteEditText.postDelayed(autoSaveRunnable, 2000)
            }
        }
        
        noteTitleEditText.addTextChangedListener(textWatcher)
        noteEditText.addTextChangedListener(textWatcher)
        
        // Set up back press handler
        setupBackPressedHandler()
    }
    
    private fun setupBackPressedHandler() {
        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackPressed()
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }
    
    private fun handleBackPressed() {
        if (hasUnsavedChanges) {
            MaterialAlertDialogBuilder(this)
                .setTitle("Unsaved Changes")
                .setMessage("You have unsaved changes. Do you want to save before leaving?")
                .setPositiveButton("Save") { _, _ ->
                    saveNote()
                    hasUnsavedChanges = false
                    finish()
                }
                .setNegativeButton("Don't Save") { _, _ ->
                    finish()
                }
                .setNeutralButton("Cancel", null)
                .show()
        } else {
            finish()
        }
    }
    
    private val autoSaveRunnable = Runnable {
        if (hasUnsavedChanges) {
            saveNote()
            hasUnsavedChanges = false
        }
    }
    
    private fun saveNote() {
        currentNote?.let { note ->
            note.title = noteTitleEditText.text.toString()
            note.content = noteEditText.text.toString()
            noteManager.saveNote(note)
        }
    }
    
    private fun loadNote() {
        currentNote?.let { note ->
            noteTitleEditText.setText(note.title)
            noteEditText.setText(note.content)
            noteEditText.setSelection(note.content.length)
            // Update header title, fallback to app name if empty
            headerTitle.text = note.title.ifBlank { getString(R.string.app_name) }
        }
        hasUnsavedChanges = false
    }
    
    private fun clearNote() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Clear Note")
            .setMessage("Are you sure you want to clear this note?")
            .setPositiveButton("Clear") { _, _ ->
                noteTitleEditText.text.clear()
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
