package com.example.data.repository

import com.example.data.local.UserDao
import com.example.data.model.UserEntity
import com.example.data.security.PasswordSecurity
import com.example.data.session.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.withContext

class AuthRepository(
  private val userDao: UserDao,
  private val sessionManager: SessionManager
) {
  val currentUserIdFlow: Flow<Long?> = sessionManager.currentUserId

  suspend fun register(
    name: String,
    email: String,
    password: String,
    confirmPassword: String
  ): Result<UserEntity> = withContext(Dispatchers.IO) {
    val trimmedName = name.trim()
    val trimmedEmail = email.trim().lowercase()

    if (trimmedName.isEmpty()) {
      return@withContext Result.failure(IllegalArgumentException("Nama wajib diisi."))
    }
    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
      return@withContext Result.failure(IllegalArgumentException("Format email tidak valid."))
    }
    if (password.length < 8) {
      return@withContext Result.failure(IllegalArgumentException("Password minimal 8 karakter."))
    }
    if (password != confirmPassword) {
      return@withContext Result.failure(IllegalArgumentException("Konfirmasi password tidak cocok."))
    }

    val existingUser = userDao.getUserByEmail(trimmedEmail)
    if (existingUser != null) {
      return@withContext Result.failure(IllegalStateException("Email sudah terdaftar."))
    }

    val salt = PasswordSecurity.generateSalt()
    val hash = PasswordSecurity.hashPassword(password, salt)

    val newUser = UserEntity(
      name = trimmedName,
      email = trimmedEmail,
      passwordHash = hash,
      salt = salt,
      openingBalance = 0L,
      openingBalanceNote = ""
    )

    try {
      val newId = userDao.insertUser(newUser)
      val createdUser = newUser.copy(id = newId)
      Result.success(createdUser)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun login(email: String, password: String): Result<UserEntity> = withContext(Dispatchers.IO) {
    val trimmedEmail = email.trim().lowercase()
    if (trimmedEmail.isEmpty() || password.isEmpty()) {
      return@withContext Result.failure(IllegalArgumentException("Email dan password wajib diisi."))
    }

    val user = userDao.getUserByEmail(trimmedEmail)
    if (user == null) {
      return@withContext Result.failure(IllegalArgumentException("Email atau password yang Anda masukkan salah."))
    }

    val isPasswordValid = PasswordSecurity.verifyPassword(password, user.salt, user.passwordHash)
    if (!isPasswordValid) {
      return@withContext Result.failure(IllegalArgumentException("Email atau password yang Anda masukkan salah."))
    }

    sessionManager.saveUserId(user.id)
    Result.success(user)
  }

  fun logout() {
    sessionManager.clearSession()
  }

  fun getCurrentUserFlow(): Flow<UserEntity?> {
    val userId = sessionManager.currentUserId.value ?: return emptyFlow()
    return userDao.getUserById(userId)
  }

  fun getUserById(userId: Long): Flow<UserEntity?> {
    return userDao.getUserById(userId)
  }

  suspend fun updateProfile(name: String): Result<Unit> = withContext(Dispatchers.IO) {
    val userId = sessionManager.currentUserId.value
      ?: return@withContext Result.failure(IllegalStateException("Anda harus login untuk mengakses halaman ini."))
    val trimmedName = name.trim()
    if (trimmedName.isEmpty()) {
      return@withContext Result.failure(IllegalArgumentException("Nama tidak boleh kosong."))
    }
    userDao.updateProfile(userId, trimmedName)
    Result.success(Unit)
  }

  suspend fun updatePassword(oldPassword: String, newPassword: String): Result<Unit> = withContext(Dispatchers.IO) {
    val userId = sessionManager.currentUserId.value
      ?: return@withContext Result.failure(IllegalStateException("Anda harus login untuk mengakses halaman ini."))
    if (newPassword.length < 8) {
      return@withContext Result.failure(IllegalArgumentException("Password baru minimal 8 karakter."))
    }

    val user = userDao.getUserByIdSync(userId)
      ?: return@withContext Result.failure(IllegalStateException("Data tidak ditemukan."))

    val isOldValid = PasswordSecurity.verifyPassword(oldPassword, user.salt, user.passwordHash)
    if (!isOldValid) {
      return@withContext Result.failure(IllegalArgumentException("Password lama tidak sesuai."))
    }

    val newSalt = PasswordSecurity.generateSalt()
    val newHash = PasswordSecurity.hashPassword(newPassword, newSalt)
    userDao.updatePassword(userId, newHash, newSalt)
    Result.success(Unit)
  }

  suspend fun updateOpeningBalance(balance: Long, note: String): Result<Unit> = withContext(Dispatchers.IO) {
    val userId = sessionManager.currentUserId.value
      ?: return@withContext Result.failure(IllegalStateException("Anda harus login untuk mengakses halaman ini."))
    if (balance < 0) {
      return@withContext Result.failure(IllegalArgumentException("Saldo awal tidak boleh negatif."))
    }
    userDao.updateOpeningBalance(userId, balance, note.trim())
    Result.success(Unit)
  }
}
