package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.UserEntity
import com.example.data.security.PasswordSecurity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
  entities = [UserEntity::class, CategoryEntity::class, TransactionEntity::class],
  version = 1,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun userDao(): UserDao
  abstract fun categoryDao(): CategoryDao
  abstract fun transactionDao(): TransactionDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "financeku_database"
        )
          .addCallback(DatabaseCallback(scope))
          .build()
        INSTANCE = instance
        instance
      }
    }
  }

  private class DatabaseCallback(
    private val scope: CoroutineScope
  ) : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
      super.onCreate(db)
      INSTANCE?.let { database ->
        scope.launch(Dispatchers.IO) {
          populateInitialData(database)
        }
      }
    }

    private suspend fun populateInitialData(database: AppDatabase) {
      val categoryDao = database.categoryDao()
      val userDao = database.userDao()
      val transactionDao = database.transactionDao()

      // 1. Seed Default Categories (Global, userId = null)
      val defaultCategories = listOf(
        // Pemasukan
        CategoryEntity(name = "Gaji", type = "INCOME", iconKey = "work"),
        CategoryEntity(name = "Bonus", type = "INCOME", iconKey = "card_giftcard"),
        CategoryEntity(name = "Freelance", type = "INCOME", iconKey = "laptop"),
        CategoryEntity(name = "Bisnis", type = "INCOME", iconKey = "store"),
        CategoryEntity(name = "Investasi", type = "INCOME", iconKey = "trending_up"),
        CategoryEntity(name = "Lainnya", type = "INCOME", iconKey = "more_horiz"),
        // Pengeluaran
        CategoryEntity(name = "Makanan", type = "EXPENSE", iconKey = "restaurant"),
        CategoryEntity(name = "Transportasi", type = "EXPENSE", iconKey = "directions_car"),
        CategoryEntity(name = "Belanja", type = "EXPENSE", iconKey = "shopping_bag"),
        CategoryEntity(name = "Tagihan", type = "EXPENSE", iconKey = "receipt"),
        CategoryEntity(name = "Hiburan", type = "EXPENSE", iconKey = "movie"),
        CategoryEntity(name = "Pendidikan", type = "EXPENSE", iconKey = "school"),
        CategoryEntity(name = "Kesehatan", type = "EXPENSE", iconKey = "medical_services"),
        CategoryEntity(name = "Kebutuhan Rumah", type = "EXPENSE", iconKey = "home"),
        CategoryEntity(name = "Lainnya", type = "EXPENSE", iconKey = "more_horiz")
      )
      categoryDao.insertCategories(defaultCategories)

      // 2. Seed Demo Account 1 (PRD Section 20)
      val salt1 = PasswordSecurity.generateSalt()
      val hash1 = PasswordSecurity.hashPassword("password123", salt1)
      val demoUser = UserEntity(
        name = "Demo User",
        email = "demo@example.com",
        passwordHash = hash1,
        salt = salt1,
        openingBalance = 3_000_000L,
        openingBalanceNote = "Tabungan Awal"
      )
      val demoUserId = userDao.insertUser(demoUser)

      // Seed Demo Transactions for User 1
      transactionDao.insertTransaction(
        TransactionEntity(
          userId = demoUserId,
          categoryId = 1,
          categoryName = "Gaji",
          type = "INCOME",
          amount = 5_000_000L,
          description = "Gaji Pokok September",
          transactionDate = "2026-09-01"
        )
      )
      transactionDao.insertTransaction(
        TransactionEntity(
          userId = demoUserId,
          categoryId = 7,
          categoryName = "Makanan",
          type = "EXPENSE",
          amount = 500_000L,
          description = "Belanja Makan Mingguan",
          transactionDate = "2026-09-05"
        )
      )
      transactionDao.insertTransaction(
        TransactionEntity(
          userId = demoUserId,
          categoryId = 8,
          categoryName = "Transportasi",
          type = "EXPENSE",
          amount = 300_000L,
          description = "Bensin dan Tol",
          transactionDate = "2026-09-10"
        )
      )
      transactionDao.insertTransaction(
        TransactionEntity(
          userId = demoUserId,
          categoryId = 10,
          categoryName = "Tagihan",
          type = "EXPENSE",
          amount = 700_000L,
          description = "Listrik dan Wifi Bulanan",
          transactionDate = "2026-09-15"
        )
      )

      // 3. Seed User 2 for multi-user isolation verification
      val salt2 = PasswordSecurity.generateSalt()
      val hash2 = PasswordSecurity.hashPassword("password123", salt2)
      val budiUser = UserEntity(
        name = "Budi Santoso",
        email = "budi@example.com",
        passwordHash = hash2,
        salt = salt2,
        openingBalance = 1_000_000L,
        openingBalanceNote = "Kas Pribadi"
      )
      val budiUserId = userDao.insertUser(budiUser)

      transactionDao.insertTransaction(
        TransactionEntity(
          userId = budiUserId,
          categoryId = 3,
          categoryName = "Freelance",
          type = "INCOME",
          amount = 1_500_000L,
          description = "Proyek Desain Web",
          transactionDate = "2026-09-08"
        )
      )
      transactionDao.insertTransaction(
        TransactionEntity(
          userId = budiUserId,
          categoryId = 9,
          categoryName = "Belanja",
          type = "EXPENSE",
          amount = 400_000L,
          description = "Beli Perlengkapan Kerja",
          transactionDate = "2026-09-12"
        )
      )
    }
  }
}
