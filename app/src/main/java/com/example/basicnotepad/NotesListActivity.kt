package com.example.basicnotepad

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton

class NotesListActivity : AppCompatActivity() {
    
    private lateinit var notesRecyclerView: RecyclerView
    private lateinit var emptyStateTextView: TextView
    private lateinit var fabAddNote: FloatingActionButton
    private lateinit var notesAdapter: NotesAdapter
    private lateinit var noteManager: NoteManager
    private lateinit var themeManager: ThemeManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes_list)
        
        setSupportActionBar(findViewById(R.id.toolbar))
        
        noteManager = NoteManager(this)
        themeManager = ThemeManager(this)
        themeManager.applyTheme(themeManager.getCurrentTheme())
        
        notesRecyclerView = findViewById(R.id.notesRecyclerView)
        emptyStateTextView = findViewById(R.id.emptyStateTextView)
        fabAddNote = findViewById(R.id.fabAddNote)
        
        setupRecyclerView()
        
        fabAddNote.setOnClickListener {
            showNoteTypeDialog()
        }
    }
    
    override fun onResume() {
        super.onResume()
        loadNotes()
    }
    
    private fun setupRecyclerView() {
        notesAdapter = NotesAdapter(
            mutableListOf(),
            onNoteClick = { note ->
                openNote(note)
            },
            onNoteLongClick = { note ->
                showDeleteDialog(note)
                true
            }
        )
        
        notesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@NotesListActivity)
            adapter = notesAdapter
        }
    }
    
    private fun loadNotes() {
        val notes = noteManager.getAllNotes()
        
        if (notes.isEmpty()) {
            notesRecyclerView.visibility = View.GONE
            emptyStateTextView.visibility = View.VISIBLE
        } else {
            notesRecyclerView.visibility = View.VISIBLE
            emptyStateTextView.visibility = View.GONE
            notesAdapter.updateNotes(notes)
        }
    }
    
    private fun showNoteTypeDialog() {
        val options = arrayOf(
            getString(R.string.create_note),
            getString(R.string.create_checklist)
        )
        
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.choose_note_type)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> createNewNote()
                    1 -> createNewChecklist()
                }
            }
            .show()
    }
    
    private fun createNewNote() {
        val newNote = Note()
        noteManager.saveNote(newNote)
        openNote(newNote)
    }
    
    private fun createNewChecklist() {
        val newNote = Note(isChecklist = true)
        newNote.checklistItems.add(ChecklistItem(shouldAutoFocus = true))
        noteManager.saveNote(newNote)
        openNote(newNote)
    }
    
    private fun openNote(note: Note) {
        val intent = if (note.isChecklist) {
            Intent(this, ChecklistActivity::class.java)
        } else {
            Intent(this, MainActivity::class.java)
        }
        intent.putExtra("NOTE_ID", note.id)
        startActivity(intent)
    }
    
    private fun showDeleteDialog(note: Note) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Note")
            .setMessage("Are you sure you want to delete this note?")
            .setPositiveButton("Delete") { _, _ ->
                noteManager.deleteNote(note.id)
                notesAdapter.removeNote(note)
                loadNotes()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_notes_list, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_theme -> {
                themeManager.showThemeDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
