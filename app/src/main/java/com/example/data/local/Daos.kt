package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
  @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
  suspend fun getUserByEmail(email: String): UserEntity?

  @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
  fun getUserById(userId: Long): Flow<UserEntity?>

  @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
  suspend fun getUserByIdSync(userId: Long): UserEntity?

  @Insert(onConflict = OnConflictStrategy.ABORT)
  suspend fun insertUser(user: UserEntity): Long

  @Update
  suspend fun updateUser(user: UserEntity)

  @Query("UPDATE users SET opening_balance = :balance, opening_balance_note = :note, updated_at = :updatedAt WHERE id = :userId")
  suspend fun updateOpeningBalance(userId: Long, balance: Long, note: String, updatedAt: Long = System.currentTimeMillis())

  @Query("UPDATE users SET name = :name, updated_at = :updatedAt WHERE id = :userId")
  suspend fun updateProfile(userId: Long, name: String, updatedAt: Long = System.currentTimeMillis())

  @Query("UPDATE users SET password_hash = :hash, salt = :salt, updated_at = :updatedAt WHERE id = :userId")
  suspend fun updatePassword(userId: Long, hash: String, salt: String, updatedAt: Long = System.currentTimeMillis())

  @Query("SELECT COUNT(*) FROM users")
  suspend fun countUsers(): Int

  @Query("SELECT * FROM users ORDER BY name ASC")
  suspend fun getAllUsersSync(): List<UserEntity>
}

@Dao
interface CategoryDao {
  @Query("SELECT * FROM categories WHERE (user_id IS NULL OR user_id = :userId) AND type = :type ORDER BY id ASC")
  fun getCategoriesForUser(userId: Long, type: String): Flow<List<CategoryEntity>>

  @Query("SELECT * FROM categories WHERE (user_id IS NULL OR user_id = :userId) ORDER BY type ASC, id ASC")
  fun getAllCategoriesForUser(userId: Long): Flow<List<CategoryEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCategory(category: CategoryEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCategories(categories: List<CategoryEntity>)

  @Query("SELECT COUNT(*) FROM categories")
  suspend fun countCategories(): Int
}

@Dao
interface TransactionDao {
  @Query("SELECT * FROM transactions WHERE user_id = :userId ORDER BY transaction_date DESC, id DESC")
  fun getTransactions(userId: Long): Flow<List<TransactionEntity>>

  @Query("SELECT * FROM transactions WHERE user_id = :userId ORDER BY transaction_date DESC, id DESC LIMIT :limit")
  fun getRecentTransactions(userId: Long, limit: Int = 5): Flow<List<TransactionEntity>>

  @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE user_id = :userId AND type = 'INCOME'")
  fun getTotalIncomeFlow(userId: Long): Flow<Long>

  @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE user_id = :userId AND type = 'EXPENSE'")
  fun getTotalExpenseFlow(userId: Long): Flow<Long>

  @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE user_id = :userId AND type = 'INCOME'")
  suspend fun getTotalIncomeSync(userId: Long): Long

  @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE user_id = :userId AND type = 'EXPENSE'")
  suspend fun getTotalExpenseSync(userId: Long): Long

  @Query("SELECT * FROM transactions WHERE id = :txId AND user_id = :userId LIMIT 1")
  suspend fun getTransactionById(userId: Long, txId: Long): TransactionEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTransaction(transaction: TransactionEntity): Long

  @Query("DELETE FROM transactions WHERE id = :txId AND user_id = :userId")
  suspend fun deleteTransaction(userId: Long, txId: Long): Int
}
