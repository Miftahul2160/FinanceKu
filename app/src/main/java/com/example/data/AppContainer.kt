package com.example.data

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.repository.AuthRepository
import com.example.data.repository.FinanceRepository
import com.example.data.session.SessionManager

class AppContainer(context: Context) {
  val database: AppDatabase by lazy {
    AppDatabase.getDatabase(context)
  }

  val sessionManager: SessionManager by lazy {
    SessionManager(context)
  }

  val authRepository: AuthRepository by lazy {
    AuthRepository(
      userDao = database.userDao(),
      sessionManager = sessionManager
    )
  }

  val financeRepository: FinanceRepository by lazy {
    FinanceRepository(
      userDao = database.userDao(),
      transactionDao = database.transactionDao(),
      categoryDao = database.categoryDao(),
      sessionManager = sessionManager
    )
  }
}
