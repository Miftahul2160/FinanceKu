package com.example.data.security

import java.security.MessageDigest
import java.security.SecureRandom

object PasswordSecurity {
  private const val SALT_LENGTH = 16

  fun generateSalt(): String {
    val random = SecureRandom()
    val salt = ByteArray(SALT_LENGTH)
    random.nextBytes(salt)
    return bytesToHex(salt)
  }

  fun hashPassword(password: String, salt: String): String {
    val md = MessageDigest.getInstance("SHA-256")
    val input = (salt + password).toByteArray(Charsets.UTF_8)
    val hash = md.digest(input)
    return bytesToHex(hash)
  }

  fun verifyPassword(password: String, salt: String, storedHash: String): Boolean {
    val computedHash = hashPassword(password, salt)
    return computedHash.equals(storedHash, ignoreCase = true)
  }

  private fun bytesToHex(bytes: ByteArray): String {
    val hexChars = CharArray(bytes.size * 2)
    val hexArray = "0123456789ABCDEF".toCharArray()
    for (i in bytes.indices) {
      val v = bytes[i].toInt() and 0xFF
      hexChars[i * 2] = hexArray[v ushr 4]
      hexChars[i * 2 + 1] = hexArray[v and 0x0F]
    }
    return String(hexChars)
  }
}
