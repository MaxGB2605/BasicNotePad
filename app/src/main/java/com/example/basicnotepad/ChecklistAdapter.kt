package com.example.basicnotepad

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView

class ChecklistAdapter(
    private val items: MutableList<ChecklistItem>,
    private val onItemChanged: () -> Unit,
    private val onItemDeleted: (ChecklistItem) -> Unit
) : RecyclerView.Adapter<ChecklistAdapter.ChecklistViewHolder>() {

    class ChecklistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkbox: CheckBox = itemView.findViewById(R.id.checkboxItem)
        val editText: EditText = itemView.findViewById(R.id.editTextItem)
        val deleteButton: ImageButton = itemView.findViewById(R.id.buttonDeleteItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChecklistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_checklist, parent, false)
        return ChecklistViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChecklistViewHolder, position: Int) {
        val item = items[position]
        
        // Remove previous listeners to avoid triggering on bind
        holder.checkbox.setOnCheckedChangeListener(null)
        holder.editText.removeTextChangedListener(holder.editText.tag as? TextWatcher)
        
        // Set values
        holder.checkbox.isChecked = item.isChecked
        holder.editText.setText(item.text)
        
        // Checkbox listener
        holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            item.isChecked = isChecked
            onItemChanged()
        }
        
        // EditText listener
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                item.text = s.toString()
                onItemChanged()
            }
        }
        holder.editText.addTextChangedListener(textWatcher)
        holder.editText.tag = textWatcher
        
        // Delete button listener
        holder.deleteButton.setOnClickListener {
            onItemDeleted(item)
        }
    }

    override fun getItemCount(): Int = items.size

    fun addItem(item: ChecklistItem) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }

    fun removeItem(item: ChecklistItem) {
        val position = items.indexOf(item)
        if (position != -1) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}
