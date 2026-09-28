package com.example.data.repository

import com.example.data.local.CategoryDao
import com.example.data.local.TransactionDao
import com.example.data.local.UserDao
import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.session.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext

data class BalanceSummary(
  val openingBalance: Long = 0L,
  val totalIncome: Long = 0L,
  val totalExpense: Long = 0L,
  val currentBalance: Long = 0L
)

@OptIn(ExperimentalCoroutinesApi::class)
class FinanceRepository(
  private val userDao: UserDao,
  private val transactionDao: TransactionDao,
  private val categoryDao: CategoryDao,
  private val sessionManager: SessionManager
) {

  fun getBalanceSummaryFlow(): Flow<BalanceSummary> {
    return sessionManager.currentUserId.flatMapLatest { userId ->
      if (userId == null) {
        flowOf(BalanceSummary())
      } else {
        val userFlow = userDao.getUserById(userId)
        val incomeFlow = transactionDao.getTotalIncomeFlow(userId)
        val expenseFlow = transactionDao.getTotalExpenseFlow(userId)

        combine(userFlow, incomeFlow, expenseFlow) { user, income, expense ->
          val opening = user?.openingBalance ?: 0L
          val current = opening + income - expense
          BalanceSummary(
            openingBalance = opening,
            totalIncome = income,
            totalExpense = expense,
            currentBalance = current
          )
        }
      }
    }
  }

  suspend fun getCurrentBalance(): Long = withContext(Dispatchers.IO) {
    val userId = sessionManager.currentUserId.value ?: return@withContext 0L
    val user = userDao.getUserByIdSync(userId) ?: return@withContext 0L
    val income = transactionDao.getTotalIncomeSync(userId)
    val expense = transactionDao.getTotalExpenseSync(userId)
    return@withContext user.openingBalance + income - expense
  }

  fun getRecentTransactions(limit: Int = 5): Flow<List<TransactionEntity>> {
    return sessionManager.currentUserId.flatMapLatest { userId ->
      if (userId == null) emptyFlow()
      else transactionDao.getRecentTransactions(userId, limit)
    }
  }

  fun getAllTransactions(): Flow<List<TransactionEntity>> {
    return sessionManager.currentUserId.flatMapLatest { userId ->
      if (userId == null) emptyFlow()
      else transactionDao.getTransactions(userId)
    }
  }

  fun getCategories(type: String): Flow<List<CategoryEntity>> {
    return sessionManager.currentUserId.flatMapLatest { userId ->
      val resolvedUserId = userId ?: -1L
      categoryDao.getCategoriesForUser(resolvedUserId, type)
    }
  }

  fun getAllCategories(): Flow<List<CategoryEntity>> {
    return sessionManager.currentUserId.flatMapLatest { userId ->
      val resolvedUserId = userId ?: -1L
      categoryDao.getAllCategoriesForUser(resolvedUserId)
    }
  }

  suspend fun addCategory(name: String, type: String, iconKey: String): Result<Long> = withContext(Dispatchers.IO) {
    val userId = sessionManager.currentUserId.value
      ?: return@withContext Result.failure(IllegalStateException("Anda harus login untuk mengakses halaman ini."))
    val trimmedName = name.trim()
    if (trimmedName.isEmpty()) {
      return@withContext Result.failure(IllegalArgumentException("Nama kategori wajib diisi."))
    }
    try {
      val category = CategoryEntity(
        userId = userId,
        name = trimmedName,
        type = type,
        iconKey = iconKey
      )
      val id = categoryDao.insertCategory(category)
      Result.success(id)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun addTransaction(
    type: TransactionType,
    amount: Long,
    categoryId: Long,
    categoryName: String,
    description: String,
    date: String
  ): Result<Long> = withContext(Dispatchers.IO) {
    val userId = sessionManager.currentUserId.value
      ?: return@withContext Result.failure(IllegalStateException("Anda harus login untuk mengakses halaman ini."))

    if (amount <= 0) {
      return@withContext Result.failure(IllegalArgumentException("Nominal harus lebih besar dari 0."))
    }

    if (categoryName.isBlank()) {
      return@withContext Result.failure(IllegalArgumentException("Kategori wajib dipilih."))
    }

    // Critical Business Rule:
    // Pengguna tidak boleh melakukan transaksi pengeluaran jika nominal pengeluaran lebih besar daripada saldo tersedia.
    if (type == TransactionType.EXPENSE) {
      val currentBalance = getCurrentBalance()
      if (amount > currentBalance) {
        return@withContext Result.failure(IllegalStateException("Saldo tidak mencukupi untuk melakukan transaksi ini."))
      }
    }

    val transaction = TransactionEntity(
      userId = userId,
      categoryId = categoryId,
      categoryName = categoryName.trim(),
      type = type.name,
      amount = amount,
      description = description.trim(),
      transactionDate = date
    )

    try {
      val id = transactionDao.insertTransaction(transaction)
      Result.success(id)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun deleteTransaction(txId: Long): Result<Unit> = withContext(Dispatchers.IO) {
    val userId = sessionManager.currentUserId.value
      ?: return@withContext Result.failure(IllegalStateException("Anda harus login untuk mengakses halaman ini."))

    val existing = transactionDao.getTransactionById(userId, txId)
    if (existing == null) {
      return@withContext Result.failure(IllegalStateException("Data tidak ditemukan."))
    }

    val rowsAffected = transactionDao.deleteTransaction(userId, txId)
    if (rowsAffected > 0) {
      Result.success(Unit)
    } else {
      Result.failure(IllegalStateException("Gagal menghapus transaksi."))
    }
  }
}
