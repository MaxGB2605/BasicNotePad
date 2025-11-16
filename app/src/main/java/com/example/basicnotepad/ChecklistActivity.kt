package com.example.basicnotepad

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ChecklistActivity : AppCompatActivity(), ThemeManager.ThemeChangeListener {
    
    private lateinit var editTextTitle: EditText
    private lateinit var headerTitle: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var buttonAddItem: MaterialButton
    private lateinit var backButton: ImageButton
    private lateinit var saveButton: Button
    private lateinit var themeButton: Button
    private lateinit var clearButton: Button
    private lateinit var emptyStateTextView: TextView
    private lateinit var checklistAdapter: ChecklistAdapter
    private lateinit var noteManager: NoteManager
    private lateinit var themeManager: ThemeManager
    private var currentNote: Note? = null
    private var hasUnsavedChanges = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        val themeManager = ThemeManager(this)
        setTheme(themeManager.getThemeResourceId())
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checklist)
        
        this.themeManager = themeManager
        themeManager.setThemeChangeListener(this)
        themeManager.applyTheme(themeManager.getCurrentTheme())
        
        editTextTitle = findViewById(R.id.editTextTitle)
        headerTitle = findViewById(R.id.headerTitle)
        recyclerView = findViewById(R.id.recyclerViewChecklist)
        buttonAddItem = findViewById(R.id.buttonAddItem)
        backButton = findViewById(R.id.backButton)
        saveButton = findViewById(R.id.saveButton)
        themeButton = findViewById(R.id.themeButton)
        clearButton = findViewById(R.id.clearButton)
        emptyStateTextView = findViewById(R.id.emptyStateTextView)
        
        noteManager = NoteManager(this)
        
        // Get note ID from intent
        val noteId = intent.getStringExtra("NOTE_ID")
        if (noteId != null) {
            currentNote = noteManager.getNoteById(noteId)
        }
        
        if (currentNote == null) {
            currentNote = Note(isChecklist = true).apply {
                checklistItems = mutableListOf()
            }
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
                // Update header title, fallback to "Checklist" if empty
                headerTitle.text = s.toString().ifBlank { "Checklist" }
                autoSave()
            }
        })
        
        // Set up button click listeners
        backButton.setOnClickListener {
            (onBackPressedDispatcher.onBackPressed())
        }
        
        saveButton.setOnClickListener {
            saveChecklist(shouldFinish = true)
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
            showAddItemDialog()
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
            // Update header title, fallback to "Checklist" if empty
            headerTitle.text = note.title.ifBlank { "Checklist" }
        }
        hasUnsavedChanges = false
        updateEmptyState()
    }
    
    private fun showAddItemDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_checklist_item, null)
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()
            
        val contentInput = dialogView.findViewById<TextInputEditText>(R.id.itemContentInput)
        val addButton = dialogView.findViewById<Button>(R.id.addButton)
        
        // Show keyboard
        contentInput.requestFocus()
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        
        addButton.setOnClickListener {
            val content = contentInput.text.toString().trim()
            if (content.isNotEmpty()) {
                addNewItem(content)
                dialog.dismiss()
            } else {
                contentInput.error = "Please enter some text"
            }
        }
        
        // Handle keyboard done/enter key
        contentInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addButton.performClick()
                true
            } else {
                false
            }
        }
        
        dialog.show()
    }
    
    private fun addNewItem(content: String = "") {
        currentNote?.let { _ ->
            val newItem = ChecklistItem(
                text = content,
                isChecked = false,
                shouldAutoFocus = content.isEmpty()
            )
            checklistAdapter.addItem(newItem)
            
            // Update empty state
            updateEmptyState()
            
            hasUnsavedChanges = true
            autoSave()
        }
    }
    
    private fun deleteItem(item: ChecklistItem) {
        currentNote?.let { _ ->
            checklistAdapter.removeItem(item)
            hasUnsavedChanges = true
            autoSave()
            updateEmptyState()
        }
    }
    
    private fun autoSave() {
        recyclerView.removeCallbacks(autoSaveRunnable)
        recyclerView.postDelayed(autoSaveRunnable, 2000)
    }
    
    private val autoSaveRunnable = Runnable {
        if (hasUnsavedChanges) {
            saveChecklist(shouldFinish = false)
            hasUnsavedChanges = false
        }
    }
    
    private fun saveChecklist(shouldFinish: Boolean = false) {
        currentNote?.let { note ->
            noteManager.saveNote(note)
            // Only finish if explicitly saved (not auto-save)
            if (shouldFinish) {
                finish()
            }
        }
    }
    
    private fun clearChecklist() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Clear Checklist")
            .setMessage("Are you sure you want to clear all items?")
            .setPositiveButton("Clear") { _, _ ->
                val itemCount = currentNote?.checklistItems?.size ?: 0
                if (itemCount > 0) {
                    currentNote?.checklistItems?.clear()
                    checklistAdapter.notifyItemRangeRemoved(0, itemCount)
                    updateEmptyState()
                    hasUnsavedChanges = true
                    autoSave()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun updateEmptyState() {
        if (currentNote?.checklistItems?.isEmpty() == true) {
            emptyStateTextView.visibility = View.VISIBLE
        } else {
            emptyStateTextView.visibility = View.GONE
        }
    }
    
    override fun onThemeChanged() {
        recreate()
    }
    
    override fun onPause() {
        super.onPause()
        if (hasUnsavedChanges) {
            saveChecklist()
        }
    }
}
