package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.model.UserEntity
import com.example.data.repository.AuthRepository
import com.example.data.repository.BalanceSummary
import com.example.data.repository.FinanceRepository
import com.example.util.DateUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CategorySummaryItem(
  val categoryName: String,
  val type: String,
  val totalAmount: Long,
  val percentage: Float
)

data class MonthRecap(
  val monthKey: String,
  val monthName: String,
  val totalIncome: Long,
  val totalExpense: Long,
  val netAmount: Long,
  val openingBalance: Long,
  val closingBalance: Long,
  val expenseByCategory: List<CategorySummaryItem>,
  val incomeByCategory: List<CategorySummaryItem>
)

class FinanceViewModel(
  private val authRepository: AuthRepository,
  private val financeRepository: FinanceRepository
) : ViewModel() {

  val currentUser: StateFlow<UserEntity?> = authRepository.getCurrentUserFlow()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  val balanceSummary: StateFlow<BalanceSummary> = financeRepository.getBalanceSummaryFlow()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BalanceSummary())

  val recentTransactions: StateFlow<List<TransactionEntity>> = financeRepository.getRecentTransactions(5)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allTransactions: StateFlow<List<TransactionEntity>> = financeRepository.getAllTransactions()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val incomeCategories: StateFlow<List<CategoryEntity>> = financeRepository.getCategories("INCOME")
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val expenseCategories: StateFlow<List<CategoryEntity>> = financeRepository.getCategories("EXPENSE")
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allCategories: StateFlow<List<CategoryEntity>> = financeRepository.getAllCategories()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // History Filter State
  val historySearchQuery = MutableStateFlow("")
  val historyTypeFilter = MutableStateFlow("ALL") // "ALL", "INCOME", "EXPENSE"
  val historyCategoryFilter = MutableStateFlow("ALL")
  val historyDateFilter = MutableStateFlow("ALL") // "ALL", "TODAY", "THIS_MONTH"

  // Filtered History Transactions
  val filteredTransactions: StateFlow<List<TransactionEntity>> = combine(
    allTransactions,
    historySearchQuery,
    historyTypeFilter,
    historyCategoryFilter,
    historyDateFilter
  ) { list, query, type, cat, dateFilter ->
    val today = DateUtils.getTodayDbDate()
    val thisMonthPrefix = today.take(7)

    list.filter { tx ->
      val matchesQuery = query.isBlank() ||
        tx.description.contains(query, ignoreCase = true) ||
        tx.categoryName.contains(query, ignoreCase = true)

      val matchesType = when (type) {
        "INCOME" -> tx.type == "INCOME"
        "EXPENSE" -> tx.type == "EXPENSE"
        else -> true
      }

      val matchesCat = (cat == "ALL" || tx.categoryName.equals(cat, ignoreCase = true))

      val matchesDate = when (dateFilter) {
        "TODAY" -> tx.transactionDate == today
        "THIS_MONTH" -> tx.transactionDate.startsWith(thisMonthPrefix)
        else -> true
      }

      matchesQuery && matchesType && matchesCat && matchesDate
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Recap Filter State
  val selectedRecapMonth = MutableStateFlow(DateUtils.getCurrentYearMonthKey()) // e.g. "2026-09"

  val monthlyRecap: StateFlow<MonthRecap> = combine(
    allTransactions,
    currentUser,
    selectedRecapMonth
  ) { transactions, user, monthKey ->
    val isAllTime = monthKey == "ALL"
    val filtered = if (isAllTime) {
      transactions
    } else {
      transactions.filter { it.transactionDate.startsWith(monthKey) }
    }

    val income = filtered.filter { it.type == "INCOME" }.sumOf { it.amount }
    val expense = filtered.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    val net = income - expense
    val opening = user?.openingBalance ?: 0L
    val closing = opening + (if (isAllTime) (transactions.filter { it.type == "INCOME" }.sumOf { it.amount } - transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }) else net)

    // Expense breakdown
    val expGroup = filtered.filter { it.type == "EXPENSE" }.groupBy { it.categoryName }
    val expItems = expGroup.map { (cat, txs) ->
      val total = txs.sumOf { it.amount }
      val pct = if (expense > 0) (total.toFloat() / expense.toFloat()) * 100f else 0f
      CategorySummaryItem(cat, "EXPENSE", total, pct)
    }.sortedByDescending { it.totalAmount }

    // Income breakdown
    val incGroup = filtered.filter { it.type == "INCOME" }.groupBy { it.categoryName }
    val incItems = incGroup.map { (cat, txs) ->
      val total = txs.sumOf { it.amount }
      val pct = if (income > 0) (total.toFloat() / income.toFloat()) * 100f else 0f
      CategorySummaryItem(cat, "INCOME", total, pct)
    }.sortedByDescending { it.totalAmount }

    val monthName = if (isAllTime) "Semua Waktu" else DateUtils.formatToMonthYear("$monthKey-01")

    MonthRecap(
      monthKey = monthKey,
      monthName = monthName,
      totalIncome = income,
      totalExpense = expense,
      netAmount = net,
      openingBalance = opening,
      closingBalance = closing,
      expenseByCategory = expItems,
      incomeByCategory = incItems
    )
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000),
    MonthRecap(
      monthKey = DateUtils.getCurrentYearMonthKey(),
      monthName = DateUtils.getCurrentMonthYearName(),
      totalIncome = 0L,
      totalExpense = 0L,
      netAmount = 0L,
      openingBalance = 0L,
      closingBalance = 0L,
      expenseByCategory = emptyList(),
      incomeByCategory = emptyList()
    )
  )

  // Event feedback (Snackbar / Error message)
  private val _snackbarEvent = MutableSharedFlow<String>()
  val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

  fun addTransaction(
    type: TransactionType,
    amount: Long,
    categoryId: Long,
    categoryName: String,
    description: String,
    date: String,
    onSuccess: () -> Unit = {},
    onError: (String) -> Unit = {}
  ) {
    viewModelScope.launch {
      val result = financeRepository.addTransaction(
        type = type,
        amount = amount,
        categoryId = categoryId,
        categoryName = categoryName,
        description = description,
        date = date
      )
      result.fold(
        onSuccess = {
          _snackbarEvent.emit("Transaksi ${type.label.lowercase()} berhasil disimpan.")
          onSuccess()
        },
        onFailure = { error ->
          val msg = error.localizedMessage ?: "Gagal menyimpan transaksi."
          onError(msg)
          _snackbarEvent.emit(msg)
        }
      )
    }
  }

  fun deleteTransaction(txId: Long, onDone: () -> Unit = {}) {
    viewModelScope.launch {
      val result = financeRepository.deleteTransaction(txId)
      result.fold(
        onSuccess = {
          _snackbarEvent.emit("Transaksi berhasil dihapus.")
          onDone()
        },
        onFailure = { error ->
          _snackbarEvent.emit(error.localizedMessage ?: "Gagal menghapus transaksi.")
        }
      )
    }
  }

  fun setOpeningBalance(balance: Long, note: String, onDone: () -> Unit = {}) {
    viewModelScope.launch {
      val result = authRepository.updateOpeningBalance(balance, note)
      result.fold(
        onSuccess = {
          _snackbarEvent.emit("Saldo awal berhasil diperbarui.")
          onDone()
        },
        onFailure = { error ->
          _snackbarEvent.emit(error.localizedMessage ?: "Gagal mengubah saldo awal.")
        }
      )
    }
  }

  fun updateProfile(name: String, onDone: () -> Unit = {}) {
    viewModelScope.launch {
      val result = authRepository.updateProfile(name)
      result.fold(
        onSuccess = {
          _snackbarEvent.emit("Nama profil berhasil diubah.")
          onDone()
        },
        onFailure = { error ->
          _snackbarEvent.emit(error.localizedMessage ?: "Gagal mengubah nama profil.")
        }
      )
    }
  }

  fun updatePassword(oldPass: String, newPass: String, onDone: () -> Unit = {}, onError: (String) -> Unit = {}) {
    viewModelScope.launch {
      val result = authRepository.updatePassword(oldPass, newPass)
      result.fold(
        onSuccess = {
          _snackbarEvent.emit("Password berhasil diperbarui.")
          onDone()
        },
        onFailure = { error ->
          val msg = error.localizedMessage ?: "Gagal mengubah password."
          onError(msg)
          _snackbarEvent.emit(msg)
        }
      )
    }
  }

  fun addCustomCategory(name: String, type: String, iconKey: String = "category", onDone: () -> Unit = {}) {
    viewModelScope.launch {
      val result = financeRepository.addCategory(name, type, iconKey)
      result.fold(
        onSuccess = {
          _snackbarEvent.emit("Kategori baru berhasil ditambahkan.")
          onDone()
        },
        onFailure = { error ->
          _snackbarEvent.emit(error.localizedMessage ?: "Gagal menambahkan kategori.")
        }
      )
    }
  }

  fun logout(onLoggedOut: () -> Unit = {}) {
    authRepository.logout()
    onLoggedOut()
  }

  companion object {
    fun provideFactory(
      authRepository: AuthRepository,
      financeRepository: FinanceRepository
    ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
      @Suppress("UNCHECKED_CAST")
      override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FinanceViewModel(authRepository, financeRepository) as T
      }
    }
  }
}
