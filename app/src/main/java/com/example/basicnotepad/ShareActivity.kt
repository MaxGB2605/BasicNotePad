package com.example.basicnotepad

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class ShareActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Handle the incoming intent
        when (intent?.action) {
            Intent.ACTION_SEND -> {
                if ("text/plain" == intent.type) {
                    handleSendText(intent)
                }
            }
        }
        finish()
    }
    
    private fun handleSendText(intent: Intent) {
        intent.getStringExtra(Intent.EXTRA_TEXT)?.let { sharedText ->
            // Create a new note with the shared text
            val newNote = Note(title = sharedText.take(50), content = sharedText)
            val noteManager = NoteManager(this)
            noteManager.saveNote(newNote)
            
            // Open the note in MainActivity
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("NOTE_ID", newNote.id)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            startActivity(intent)
        }
    }
}
