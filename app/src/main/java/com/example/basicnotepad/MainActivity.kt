package com.example.basicnotepad

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {
    
    private lateinit var noteEditText: EditText
    private lateinit var noteManager: NoteManager
    private lateinit var themeManager: ThemeManager
    private var currentNote: Note? = null
    private var hasUnsavedChanges = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Set up toolbar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        noteEditText = findViewById(R.id.noteEditText)
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
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_save -> {
                saveNote()
                hasUnsavedChanges = false
                Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_theme -> {
                themeManager.showThemeDialog()
                true
            }
            R.id.action_clear -> {
                clearNote()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    override fun onPause() {
        super.onPause()
        // Save when app goes to background
        if (hasUnsavedChanges) {
            saveNote()
        }
    }
}
