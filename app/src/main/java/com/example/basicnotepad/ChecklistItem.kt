package com.example.basicnotepad

import java.io.Serializable

data class ChecklistItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    var text: String = "",
    var isChecked: Boolean = false,
    @Transient var shouldAutoFocus: Boolean = false
) : Serializable
