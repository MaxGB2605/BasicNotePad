package com.example.basicnotepad

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ChecklistActivity : AppCompatActivity() {
    
    private lateinit var editTextTitle: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var buttonAddItem: MaterialButton
    private lateinit var backButton: ImageButton
    private lateinit var saveButton: Button
    private lateinit var themeButton: Button
    private lateinit var clearButton: Button
    private lateinit var checklistAdapter: ChecklistAdapter
    private lateinit var noteManager: NoteManager
    private lateinit var themeManager: ThemeManager
    private var currentNote: Note? = null
    private var hasUnsavedChanges = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checklist)
        
        editTextTitle = findViewById(R.id.editTextTitle)
        recyclerView = findViewById(R.id.recyclerViewChecklist)
        buttonAddItem = findViewById(R.id.buttonAddItem)
        backButton = findViewById(R.id.backButton)
        saveButton = findViewById(R.id.saveButton)
        themeButton = findViewById(R.id.themeButton)
        clearButton = findViewById(R.id.clearButton)
        
        noteManager = NoteManager(this)
        themeManager = ThemeManager(this)
        
        // Get note ID from intent
        val noteId = intent.getStringExtra("NOTE_ID")
        if (noteId != null) {
            currentNote = noteManager.getNoteById(noteId)
        }
        
        if (currentNote == null) {
            currentNote = Note(isChecklist = true)
        }
        
        loadChecklist()
        setupRecyclerView()
        
        // Title change listener
        editTextTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                hasUnsavedChanges = true
            }
            override fun afterTextChanged(s: Editable?) {
                currentNote?.title = s.toString()
                autoSave()
            }
        })
        
        // Set up button click listeners
        backButton.setOnClickListener {
            onBackPressed()
        }
        
        saveButton.setOnClickListener {
            saveChecklist()
            hasUnsavedChanges = false
            Toast.makeText(this, "Checklist saved", Toast.LENGTH_SHORT).show()
        }
        
        themeButton.setOnClickListener {
            themeManager.showThemeDialog()
        }
        
        clearButton.setOnClickListener {
            clearChecklist()
        }
        
        // Add item button
        buttonAddItem.setOnClickListener {
            addNewItem()
        }
    }
    
    private fun setupRecyclerView() {
        checklistAdapter = ChecklistAdapter(
            currentNote?.checklistItems ?: mutableListOf(),
            onItemChanged = {
                hasUnsavedChanges = true
                autoSave()
            },
            onItemDeleted = { item ->
                deleteItem(item)
            }
        )
        
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChecklistActivity)
            adapter = checklistAdapter
        }
    }
    
    private fun loadChecklist() {
        currentNote?.let { note ->
            editTextTitle.setText(note.title)
        }
        hasUnsavedChanges = false
    }
    
    private fun addNewItem() {
        val newItem = ChecklistItem(shouldAutoFocus = true)
        checklistAdapter.addItem(newItem)
        hasUnsavedChanges = true
        autoSave()
    }
    
    private fun deleteItem(item: ChecklistItem) {
        if (currentNote?.checklistItems?.size ?: 0 <= 1) {
            Toast.makeText(this, "Checklist must have at least one item", Toast.LENGTH_SHORT).show()
            return
        }
        
        checklistAdapter.removeItem(item)
        hasUnsavedChanges = true
        saveChecklist()
    }
    
    private fun autoSave() {
        recyclerView.removeCallbacks(autoSaveRunnable)
        recyclerView.postDelayed(autoSaveRunnable, 2000)
    }
    
    private val autoSaveRunnable = Runnable {
        if (hasUnsavedChanges) {
            saveChecklist()
            hasUnsavedChanges = false
        }
    }
    
    private fun saveChecklist() {
        currentNote?.let { note ->
            noteManager.saveNote(note)
        }
    }
    
    private fun clearChecklist() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Clear Checklist")
            .setMessage("Are you sure you want to clear all items?")
            .setPositiveButton("Clear") { _, _ ->
                currentNote?.checklistItems?.clear()
                currentNote?.checklistItems?.add(ChecklistItem(shouldAutoFocus = true))
                checklistAdapter.notifyDataSetChanged()
                saveChecklist()
                Toast.makeText(this, "Checklist cleared", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    
    override fun onPause() {
        super.onPause()
        if (hasUnsavedChanges) {
            saveChecklist()
        }
    }
}
