package com.example.basicnotepad

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ThemeManager(private val context: Context) {
    
    private val PREFS_NAME = "ThemePrefs"
    private val THEME_KEY = "app_theme"
    
    companion object {
        const val THEME_LIGHT = 0
        const val THEME_DARK = 1
        const val THEME_SYSTEM = 2
    }
    
    fun getCurrentTheme(): Int {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getInt(THEME_KEY, THEME_SYSTEM)
    }
    
    fun setTheme(theme: Int) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putInt(THEME_KEY, theme).apply()
        applyTheme(theme)
    }
    
    fun applyTheme(theme: Int) {
        when (theme) {
            THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            THEME_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            THEME_SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
    
    fun toggleTheme() {
        val currentTheme = getCurrentTheme()
        val newTheme = when (currentTheme) {
            THEME_LIGHT -> THEME_DARK
            THEME_DARK -> THEME_LIGHT
            else -> THEME_DARK // Default to dark if system
        }
        setTheme(newTheme)
    }
    
    fun isDarkTheme(): Boolean {
        return getCurrentTheme() == THEME_DARK
    }
    
    fun showThemeDialog() {
        val themes = arrayOf(
            context.getString(R.string.theme_light),
            context.getString(R.string.theme_dark),
            context.getString(R.string.theme_system)
        )
        
        val currentTheme = getCurrentTheme()
        
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.theme_dialog_title)
            .setSingleChoiceItems(themes, currentTheme) { dialog, which ->
                setTheme(which)
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
