package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "users",
  indices = [Index(value = ["email"], unique = true)]
)
data class UserEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val name: String,
  val email: String,
  @ColumnInfo(name = "password_hash")
  val passwordHash: String,
  val salt: String,
  @ColumnInfo(name = "opening_balance")
  val openingBalance: Long = 0L,
  @ColumnInfo(name = "opening_balance_note")
  val openingBalanceNote: String = "",
  @ColumnInfo(name = "created_at")
  val createdAt: Long = System.currentTimeMillis(),
  @ColumnInfo(name = "updated_at")
  val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "categories")
data class CategoryEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  @ColumnInfo(name = "user_id")
  val userId: Long? = null, // null for global default categories
  val name: String,
  val type: String, // "INCOME" or "EXPENSE"
  @ColumnInfo(name = "icon_key")
  val iconKey: String = "default",
  @ColumnInfo(name = "created_at")
  val createdAt: Long = System.currentTimeMillis(),
  @ColumnInfo(name = "updated_at")
  val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
  tableName = "transactions",
  indices = [
    Index(value = ["user_id"]),
    Index(value = ["transaction_date"]),
    Index(value = ["type"])
  ]
)
data class TransactionEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  @ColumnInfo(name = "user_id")
  val userId: Long,
  @ColumnInfo(name = "category_id")
  val categoryId: Long,
  @ColumnInfo(name = "category_name")
  val categoryName: String,
  val type: String, // "INCOME" or "EXPENSE"
  val amount: Long, // in Rupiah, always positive
  val description: String,
  @ColumnInfo(name = "transaction_date")
  val transactionDate: String, // YYYY-MM-DD
  @ColumnInfo(name = "created_at")
  val createdAt: Long = System.currentTimeMillis(),
  @ColumnInfo(name = "updated_at")
  val updatedAt: Long = System.currentTimeMillis()
)

enum class TransactionType(val label: String) {
  INCOME("Pemasukan"),
  EXPENSE("Pengeluaran")
}
