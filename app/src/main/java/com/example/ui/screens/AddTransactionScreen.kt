package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TransactionType
import com.example.ui.components.AddCategoryDialog
import com.example.ui.components.CategoryIcon
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedContainer
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.IncomeGreenContainer
import com.example.ui.viewmodel.FinanceViewModel
import com.example.util.CurrencyUtils
import com.example.util.DateUtils
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTransactionScreen(
  viewModel: FinanceViewModel,
  initialIsExpense: Boolean = false,
  onNavigateBack: () -> Unit
) {
  val context = LocalContext.current
  val balanceSummary by viewModel.balanceSummary.collectAsStateWithLifecycle()
  val incomeCategories by viewModel.incomeCategories.collectAsStateWithLifecycle()
  val expenseCategories by viewModel.expenseCategories.collectAsStateWithLifecycle()

  var selectedTab by remember { mutableIntStateOf(if (initialIsExpense) 1 else 0) } // 0: Income, 1: Expense
  val isExpense = selectedTab == 1

  var amountInput by remember { mutableStateOf("") }
  var descriptionInput by remember { mutableStateOf("") }
  var selectedDate by remember { mutableStateOf(DateUtils.getTodayDbDate()) }
  var selectedCategoryId by remember { mutableLongStateOf(-1L) }
  var selectedCategoryName by remember { mutableStateOf("") }

  var clientError by remember { mutableStateOf<String?>(null) }
  var showAddCategoryDialog by remember { mutableStateOf(false) }

  val currentCategories = if (isExpense) expenseCategories else incomeCategories

  // Auto select first category when list changes or tab switches
  if (selectedCategoryId == -1L && currentCategories.isNotEmpty()) {
    selectedCategoryId = currentCategories.first().id
    selectedCategoryName = currentCategories.first().name
  }

  val parsedAmount = amountInput.toLongOrNull() ?: 0L
  val isExceedingBalance = isExpense && parsedAmount > balanceSummary.currentBalance

  if (showAddCategoryDialog) {
    AddCategoryDialog(
      initialType = if (isExpense) "EXPENSE" else "INCOME",
      onDismiss = { showAddCategoryDialog = false },
      onSave = { name, type ->
        viewModel.addCustomCategory(name, type) {
          selectedCategoryName = name
        }
      }
    )
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = if (isExpense) "Tambah Pengeluaran" else "Tambah Pemasukan",
            fontWeight = FontWeight.Bold
          )
        },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
          }
        }
      )
    },
    modifier = Modifier
      .fillMaxSize()
      .imePadding()
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 12.dp)
        .testTag("add_transaction_screen"),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Transaction Type Tab Selector
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
      ) {
        TabRow(
          selectedTabIndex = selectedTab,
          containerColor = MaterialTheme.colorScheme.surface
        ) {
          Tab(
            selected = selectedTab == 0,
            onClick = {
              selectedTab = 0
              selectedCategoryId = -1L
              clientError = null
            },
            text = {
              Text(
                text = "Pemasukan (+)",
                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                color = if (selectedTab == 0) IncomeGreen else MaterialTheme.colorScheme.onSurfaceVariant
              )
            },
            modifier = Modifier.testTag("tab_income")
          )
          Tab(
            selected = selectedTab == 1,
            onClick = {
              selectedTab = 1
              selectedCategoryId = -1L
              clientError = null
            },
            text = {
              Text(
                text = "Pengeluaran (-)",
                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                color = if (selectedTab == 1) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant
              )
            },
            modifier = Modifier.testTag("tab_expense")
          )
        }
      }

      // 2. Saldo Tersedia Live Indicator
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (isExceedingBalance) ExpenseRedContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Saldo Tersedia Saat Ini:",
            style = MaterialTheme.typography.bodyMedium,
            color = if (isExceedingBalance) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = CurrencyUtils.formatRupiah(balanceSummary.currentBalance),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isExceedingBalance) ExpenseRed else MaterialTheme.colorScheme.primary,
            modifier = Modifier.testTag("available_balance_display")
          )
        }
      }

      // 3. Live Error or Insufficient Balance Warning Banner (Section 7.6)
      AnimatedVisibility(visible = isExceedingBalance || clientError != null) {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Warning,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.error,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = if (isExceedingBalance) {
                "Saldo tidak mencukupi untuk melakukan transaksi ini. Saldo tersedia: ${CurrencyUtils.formatRupiah(balanceSummary.currentBalance)}"
              } else {
                clientError ?: ""
              },
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onErrorContainer,
              modifier = Modifier.testTag("insufficient_balance_warning")
            )
          }
        }
      }

      // 4. Amount Input Field
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text(
            text = "Nominal",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = amountInput,
            onValueChange = { input ->
              amountInput = input.filter { it.isDigit() }
              clientError = null
            },
            placeholder = { Text("0") },
            prefix = {
              Text(
                "Rp ",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (isExpense) ExpenseRed else IncomeGreen
              )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("transaction_amount_input")
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Quick Chips for fast amount entry
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            listOf(10_000L, 50_000L, 100_000L, 500_000L, 1_000_000L).forEach { chipAmount ->
              SuggestionChip(
                onClick = {
                  val current = amountInput.toLongOrNull() ?: 0L
                  amountInput = (current + chipAmount).toString()
                  clientError = null
                },
                label = { Text("+${CurrencyUtils.formatRupiah(chipAmount)}") }
              )
            }
          }
        }
      }

      // 5. Category Selection Section (Section 7.7)
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Kategori",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface
            )

            OutlinedButton(
              onClick = { showAddCategoryDialog = true },
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.testTag("add_custom_category_button")
            ) {
              Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Kategori Baru", style = MaterialTheme.typography.labelSmall)
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            currentCategories.forEach { category ->
              val isSelected = selectedCategoryId == category.id || selectedCategoryName == category.name
              FilterChip(
                selected = isSelected,
                onClick = {
                  selectedCategoryId = category.id
                  selectedCategoryName = category.name
                  clientError = null
                },
                leadingIcon = {
                  CategoryIcon(
                    categoryName = category.name,
                    iconKey = category.iconKey,
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                  )
                },
                label = { Text(category.name) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = if (isExpense) ExpenseRedContainer else IncomeGreenContainer,
                  selectedLabelColor = if (isExpense) ExpenseRed else IncomeGreen
                ),
                modifier = Modifier.testTag("category_chip_${category.name.lowercase()}")
              )
            }
          }
        }
      }

      // 6. Date & Description Fields
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          // Tanggal
          Text(
            text = "Tanggal Transaksi",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
          )
          Spacer(modifier = Modifier.height(6.dp))

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
              .clickable {
                val cal = Calendar.getInstance()
                try {
                  val parts = selectedDate.split("-")
                  if (parts.size == 3) {
                    cal.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
                  }
                } catch (_: Exception) {}

                DatePickerDialog(
                  context,
                  { _, year, month, dayOfMonth ->
                    val m = (month + 1).toString().padStart(2, '0')
                    val d = dayOfMonth.toString().padStart(2, '0')
                    selectedDate = "$year-$m-$d"
                  },
                  cal.get(Calendar.YEAR),
                  cal.get(Calendar.MONTH),
                  cal.get(Calendar.DAY_OF_MONTH)
                ).show()
              }
              .padding(horizontal = 16.dp, vertical = 14.dp)
              .testTag("transaction_date_picker")
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = DateUtils.formatToIndonesian(selectedDate),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
              )
              Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = "Pilih Tanggal",
                tint = MaterialTheme.colorScheme.primary
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Catatan / Deskripsi
          Text(
            text = "Catatan / Deskripsi",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
          )
          Spacer(modifier = Modifier.height(6.dp))
          OutlinedTextField(
            value = descriptionInput,
            onValueChange = { descriptionInput = it },
            placeholder = { Text("Contoh: Makan siang, Gaji bulanan, Bensin") },
            leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("transaction_description_input")
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // 7. Submit Button (Section 7.6 Critical Validation)
      Button(
        onClick = {
          clientError = null
          val amount = amountInput.toLongOrNull()
          if (amount == null || amount <= 0) {
            clientError = "Nominal harus lebih besar dari 0."
            return@Button
          }

          if (selectedCategoryName.isBlank()) {
            clientError = "Kategori wajib dipilih."
            return@Button
          }

          // Critical Rule: If expense and amount > balance, reject!
          if (isExpense && amount > balanceSummary.currentBalance) {
            clientError = "Saldo tidak mencukupi untuk melakukan transaksi ini."
            return@Button
          }

          val resolvedCat = currentCategories.find { it.id == selectedCategoryId || it.name == selectedCategoryName }
          val catId = resolvedCat?.id ?: 1L
          val catName = resolvedCat?.name ?: selectedCategoryName

          viewModel.addTransaction(
            type = if (isExpense) TransactionType.EXPENSE else TransactionType.INCOME,
            amount = amount,
            categoryId = catId,
            categoryName = catName,
            description = descriptionInput.ifBlank { catName },
            date = selectedDate,
            onSuccess = onNavigateBack,
            onError = { err ->
              clientError = err
            }
          )
        },
        colors = ButtonDefaults.buttonColors(
          containerColor = if (isExpense) ExpenseRed else IncomeGreen
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("save_transaction_button")
      ) {
        Text(
          text = if (isExpense) "Simpan Pengeluaran" else "Simpan Pemasukan",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
      }

      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}
