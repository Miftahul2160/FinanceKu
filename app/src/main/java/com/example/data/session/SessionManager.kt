package com.example.data.session

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionManager(context: Context) {
  private val prefs: SharedPreferences = context.getSharedPreferences("financeku_session", Context.MODE_PRIVATE)

  private val _currentUserId = MutableStateFlow<Long?>(loadUserId())
  val currentUserId: StateFlow<Long?> = _currentUserId.asStateFlow()

  private val _themeMode = MutableStateFlow(loadThemeMode())
  val themeMode: StateFlow<String> = _themeMode.asStateFlow()

  private fun loadUserId(): Long? {
    val id = prefs.getLong(KEY_USER_ID, -1L)
    return if (id != -1L) id else null
  }

  private fun loadThemeMode(): String {
    return prefs.getString(KEY_THEME_MODE, "SYSTEM") ?: "SYSTEM"
  }

  fun saveUserId(userId: Long) {
    prefs.edit().putLong(KEY_USER_ID, userId).apply()
    _currentUserId.value = userId
  }

  fun setThemeMode(mode: String) {
    prefs.edit().putString(KEY_THEME_MODE, mode).apply()
    _themeMode.value = mode
  }

  fun clearSession() {
    prefs.edit().remove(KEY_USER_ID).apply()
    _currentUserId.value = null
  }

  companion object {
    private const val KEY_USER_ID = "logged_in_user_id"
    private const val KEY_THEME_MODE = "app_theme_mode"
  }
}
