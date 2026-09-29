package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.TransactionType
import com.example.data.repository.AuthRepository
import com.example.data.repository.FinanceRepository
import com.example.data.security.PasswordSecurity
import com.example.data.session.SessionManager
import com.example.util.CurrencyUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  private lateinit var database: AppDatabase
  private lateinit var sessionManager: SessionManager
  private lateinit var authRepository: AuthRepository
  private lateinit var financeRepository: FinanceRepository
  private lateinit var context: Context

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    sessionManager = SessionManager(context)
    sessionManager.clearSession()

    authRepository = AuthRepository(database.userDao(), sessionManager)
    financeRepository = FinanceRepository(
      userDao = database.userDao(),
      transactionDao = database.transactionDao(),
      categoryDao = database.categoryDao(),
      sessionManager = sessionManager
    )
  }

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun testAppNameResource() {
    val appName = context.getString(R.string.app_name)
    assertEquals("FinanceKu", appName)
  }

  @Test
  fun testPasswordHashingAndVerification() {
    val password = "mypassword123"
    val salt = PasswordSecurity.generateSalt()
    val hash = PasswordSecurity.hashPassword(password, salt)

    assertTrue(PasswordSecurity.verifyPassword(password, salt, hash))
    assertFalse(PasswordSecurity.verifyPassword("wrongpass", salt, hash))
  }

  @Test
  fun testUserRegistrationAndLogin() = runBlocking {
    val regResult = authRepository.register(
      name = "Ahmad Dani",
      email = "ahmad@example.com",
      password = "password123",
      confirmPassword = "password123"
    )
    assertTrue("Registration should succeed", regResult.isSuccess)

    // Cannot register with same email
    val dupResult = authRepository.register(
      name = "Duplicate",
      email = "ahmad@example.com",
      password = "password123",
      confirmPassword = "password123"
    )
    assertTrue("Duplicate email should fail", dupResult.isFailure)

    // Login with wrong password should fail
    val failLogin = authRepository.login("ahmad@example.com", "wrongpass")
    assertTrue("Wrong password should fail", failLogin.isFailure)

    // Login with correct credentials
    val loginResult = authRepository.login("ahmad@example.com", "password123")
    assertTrue("Login should succeed", loginResult.isSuccess)
    assertEquals(loginResult.getOrNull()?.id, sessionManager.currentUserId.value)
  }

  @Test
  fun testOpeningBalanceAndCurrentBalanceCalculation() = runBlocking {
    // 1. Register & login User
    val user = authRepository.register(
      name = "Siti Rahma",
      email = "siti@example.com",
      password = "password123",
      confirmPassword = "password123"
    ).getOrThrow()
    authRepository.login("siti@example.com", "password123").getOrThrow()

    // 2. Set Opening Balance: Rp 1.000.000
    authRepository.updateOpeningBalance(1_000_000L, "Tabungan Awal")

    var balanceSummary = financeRepository.getBalanceSummaryFlow().first()
    assertEquals(1_000_000L, balanceSummary.openingBalance)
    assertEquals(1_000_000L, balanceSummary.currentBalance)

    // 3. Add Income: Rp 500.000
    financeRepository.addTransaction(
      type = TransactionType.INCOME,
      amount = 500_000L,
      categoryId = 1,
      categoryName = "Gaji",
      description = "Gaji Tambahan",
      date = "2026-09-28"
    ).getOrThrow()

    balanceSummary = financeRepository.getBalanceSummaryFlow().first()
    assertEquals(500_000L, balanceSummary.totalIncome)
    assertEquals(1_500_000L, balanceSummary.currentBalance)

    // 4. Add Expense: Rp 300.000
    financeRepository.addTransaction(
      type = TransactionType.EXPENSE,
      amount = 300_000L,
      categoryId = 2,
      categoryName = "Makanan",
      description = "Makan Siang",
      date = "2026-09-28"
    ).getOrThrow()

    balanceSummary = financeRepository.getBalanceSummaryFlow().first()
    assertEquals(300_000L, balanceSummary.totalExpense)
    // 1.000.000 + 500.000 - 300.000 = 1.200.000
    assertEquals(1_200_000L, balanceSummary.currentBalance)
  }

  @Test
  fun testExpenseExceedingBalanceIsRejected() = runBlocking {
    // Critical Business Rule (Section 7.6):
    // Saldo = Rp 100.000, Pengeluaran = Rp 150.000 -> Transaksi ditolak.
    // Saldo tetap Rp 100.000. Tampilkan pesan: "Saldo tidak mencukupi untuk melakukan transaksi ini."
    val user = authRepository.register(
      name = "Joko",
      email = "joko@example.com",
      password = "password123",
      confirmPassword = "password123"
    ).getOrThrow()
    authRepository.login("joko@example.com", "password123").getOrThrow()

    authRepository.updateOpeningBalance(100_000L, "Saldo Kas")

    val excessExpenseResult = financeRepository.addTransaction(
      type = TransactionType.EXPENSE,
      amount = 150_000L,
      categoryId = 2,
      categoryName = "Makanan",
      description = "Traktir Teman",
      date = "2026-09-28"
    )

    assertTrue("Expense exceeding available balance MUST fail", excessExpenseResult.isFailure)
    assertEquals(
      "Saldo tidak mencukupi untuk melakukan transaksi ini.",
      excessExpenseResult.exceptionOrNull()?.message
    )

    // Verify balance remains Rp 100.000
    val summary = financeRepository.getBalanceSummaryFlow().first()
    assertEquals(100_000L, summary.currentBalance)
  }

  @Test
  fun testDeleteTransactionRecalculatesBalance() = runBlocking {
    val user = authRepository.register(
      name = "Rina",
      email = "rina@example.com",
      password = "password123",
      confirmPassword = "password123"
    ).getOrThrow()
    authRepository.login("rina@example.com", "password123").getOrThrow()

    authRepository.updateOpeningBalance(1_000_000L, "")

    val txId = financeRepository.addTransaction(
      type = TransactionType.EXPENSE,
      amount = 200_000L,
      categoryId = 1,
      categoryName = "Belanja",
      description = "Baju",
      date = "2026-09-28"
    ).getOrThrow()

    var summary = financeRepository.getBalanceSummaryFlow().first()
    assertEquals(800_000L, summary.currentBalance)

    // Delete transaction
    val delResult = financeRepository.deleteTransaction(txId)
    assertTrue("Delete should succeed", delResult.isSuccess)

    // Balance should revert back to 1.000.000
    summary = financeRepository.getBalanceSummaryFlow().first()
    assertEquals(1_000_000L, summary.currentBalance)
    assertEquals(0L, summary.totalExpense)
  }

  @Test
  fun testMultiUserDataIsolation() = runBlocking {
    // User A
    val userA = authRepository.register("User A", "userA@example.com", "password123", "password123").getOrThrow()
    authRepository.login("userA@example.com", "password123").getOrThrow()
    authRepository.updateOpeningBalance(5_000_000L, "Akun A")
    val txAId = financeRepository.addTransaction(
      type = TransactionType.INCOME,
      amount = 2_000_000L,
      categoryId = 1,
      categoryName = "Gaji",
      description = "Gaji A",
      date = "2026-09-28"
    ).getOrThrow()

    val balanceA = financeRepository.getBalanceSummaryFlow().first().currentBalance
    assertEquals(7_000_000L, balanceA)

    // Logout User A, Login User B
    authRepository.logout()
    val userB = authRepository.register("User B", "userB@example.com", "password123", "password123").getOrThrow()
    authRepository.login("userB@example.com", "password123").getOrThrow()
    authRepository.updateOpeningBalance(500_000L, "Akun B")

    // User B should NOT see User A's transactions or balance
    val txsB = financeRepository.getAllTransactions().first()
    assertTrue("User B must have empty transactions initially", txsB.isEmpty())
    val balanceB = financeRepository.getBalanceSummaryFlow().first().currentBalance
    assertEquals(500_000L, balanceB)

    // User B CANNOT delete User A's transaction
    val illegalDelete = financeRepository.deleteTransaction(txAId)
    assertTrue("User B cannot delete User A's transaction", illegalDelete.isFailure)
  }

  @Test
  fun testCurrencyFormatting() {
    assertEquals("Rp 1.000.000", CurrencyUtils.formatRupiah(1_000_000L))
    assertEquals("Rp 5.250.000", CurrencyUtils.formatRupiah(5_250_000L))
    assertEquals("Rp 0", CurrencyUtils.formatRupiah(0L))
    assertEquals("- Rp 500.000", CurrencyUtils.formatRupiah(-500_000L))
  }

  @Test
  fun testThemeModePersistence() {
    sessionManager.setThemeMode("DARK")
    assertEquals("DARK", sessionManager.themeMode.value)

    sessionManager.setThemeMode("LIGHT")
    assertEquals("LIGHT", sessionManager.themeMode.value)

    sessionManager.setThemeMode("SYSTEM")
    assertEquals("SYSTEM", sessionManager.themeMode.value)
  }

  @Test
  fun testPdfAndExcelExportGeneration() {
    val recap = com.example.ui.viewmodel.MonthRecap(
      monthKey = "2026-09",
      monthName = "September 2026",
      totalIncome = 5_000_000L,
      totalExpense = 2_000_000L,
      netAmount = 3_000_000L,
      openingBalance = 1_000_000L,
      closingBalance = 4_000_000L,
      expenseByCategory = emptyList(),
      incomeByCategory = emptyList()
    )
    val transactions = listOf(
      com.example.data.model.TransactionEntity(
        id = 1L,
        userId = 1L,
        categoryId = 1L,
        categoryName = "Gaji",
        type = "INCOME",
        amount = 5_000_000L,
        description = "Gaji Pokok",
        transactionDate = "2026-09-01"
      )
    )

    // Test PDF generation
    val pdfFile = com.example.util.ReportExporter.exportToPdf(context, recap, transactions, "Tester")
    assertTrue(pdfFile.exists())
    assertTrue(pdfFile.length() > 0)

    // Test Excel / CSV generation
    val csvFile = com.example.util.ReportExporter.exportToExcelCsv(context, recap, transactions, "Tester")
    assertTrue(csvFile.exists())
    assertTrue(csvFile.length() > 0)
    val content = csvFile.readText()
    assertTrue(content.contains("FINANCEKU - LAPORAN REKAP KEUANGAN"))
    assertTrue(content.contains("5000000"))
  }
}
