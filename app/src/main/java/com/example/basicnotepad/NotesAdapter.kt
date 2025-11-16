package com.example.basicnotepad

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class NotesAdapter(
    private var notes: MutableList<Note>,
    private val onNoteClick: (Note) -> Unit,
    private val onNoteLongClick: (Note) -> Boolean
) : RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.noteTitleTextView)
        val previewTextView: TextView = itemView.findViewById(R.id.notePreviewTextView)
        val dateTextView: TextView = itemView.findViewById(R.id.noteDateTextView)
    }

    class NoteDiffCallback(
        private val oldList: List<Note>,
        private val newList: List<Note>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldNote = oldList[oldItemPosition]
            val newNote = newList[newItemPosition]
            return oldNote.title == newNote.title && 
                   oldNote.content == newNote.content &&
                   oldNote.lastModified == newNote.lastModified
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        
        holder.titleTextView.text = note.getTitleOrDefault()
        holder.previewTextView.text = note.getPreview()
        holder.dateTextView.text = note.getFormattedDate()
        
        holder.itemView.setOnClickListener {
            onNoteClick(note)
        }
        
        holder.itemView.setOnLongClickListener {
            onNoteLongClick(note)
        }
    }

    override fun getItemCount(): Int = notes.size

    fun updateNotes(newNotes: List<Note>) {
        val diffCallback = NoteDiffCallback(notes, newNotes)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        
        notes.clear()
        notes.addAll(newNotes)
        diffResult.dispatchUpdatesTo(this)
    }
    
    fun removeNote(note: Note) {
        val position = notes.indexOf(note)
        if (position != -1) {
            notes.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}
