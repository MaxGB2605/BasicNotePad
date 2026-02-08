package com.example.basicnotepad

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class ChecklistAdapter(
    private val items: MutableList<ChecklistItem>,
    private val onItemChanged: () -> Unit,
    private val onCheckStatusChanged: () -> Unit,
    private val onItemDeleted: (ChecklistItem) -> Unit,
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

        holder.checkbox.setOnCheckedChangeListener(null)
        holder.editText.removeTextChangedListener(holder.editText.tag as? TextWatcher)

        holder.checkbox.isChecked = item.isChecked
        holder.editText.setText(item.text)

        holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            if (item.isChecked != isChecked) {
                item.isChecked = isChecked
                onCheckStatusChanged()
            }
        }

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (item.text != s.toString()) {
                    item.text = s.toString()
                    onItemChanged()
                }
            }
        }
        holder.editText.addTextChangedListener(textWatcher)
        holder.editText.tag = textWatcher

        // Delete button listener
        holder.deleteButton.setOnClickListener {
            onItemDeleted(item)
        }
        if (item.shouldAutoFocus) {
            holder.editText.requestFocus()
            holder.editText.post {
                val imm =
                    holder.editText.context.getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                imm.showSoftInput(
                    holder.editText,
                    android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
                )
            }
            item.shouldAutoFocus = false
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

class ChecklistDiffCallback(
    private val oldList: List<ChecklistItem>,
    private val newList: List<ChecklistItem>,
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}

