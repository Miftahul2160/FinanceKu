package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TransactionEntity
import com.example.ui.components.CategoryIcon
import com.example.ui.components.DeleteConfirmDialog
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedContainer
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.IncomeGreenContainer
import com.example.ui.viewmodel.FinanceViewModel
import com.example.util.CurrencyUtils
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
  viewModel: FinanceViewModel
) {
  val transactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
  val allCategories by viewModel.allCategories.collectAsStateWithLifecycle()
  val searchQuery by viewModel.historySearchQuery.collectAsStateWithLifecycle()
  val typeFilter by viewModel.historyTypeFilter.collectAsStateWithLifecycle()
  val categoryFilter by viewModel.historyCategoryFilter.collectAsStateWithLifecycle()
  val dateFilter by viewModel.historyDateFilter.collectAsStateWithLifecycle()

  var transactionToDelete by remember { mutableStateOf<TransactionEntity?>(null) }

  if (transactionToDelete != null) {
    DeleteConfirmDialog(
      transaction = transactionToDelete!!,
      onDismiss = { transactionToDelete = null },
      onConfirm = {
        viewModel.deleteTransaction(transactionToDelete!!.id)
      }
    )
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Histori Transaksi",
            fontWeight = FontWeight.Bold
          )
        }
      )
    },
    modifier = Modifier.fillMaxSize()
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .testTag("history_screen")
    ) {
      // 1. Search Bar
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { viewModel.historySearchQuery.value = it },
        placeholder = { Text("Cari catatan atau kategori...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { viewModel.historySearchQuery.value = "" }) {
              Icon(Icons.Default.Clear, contentDescription = "Hapus pencarian")
            }
          }
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 8.dp)
          .testTag("history_search_input")
      )

      // 2. Type Filter Chips: Semua, Pemasukan, Pengeluaran
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState())
          .padding(horizontal = 20.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        FilterChip(
          selected = typeFilter == "ALL",
          onClick = { viewModel.historyTypeFilter.value = "ALL" },
          label = { Text("Semua Tipe") },
          modifier = Modifier.testTag("filter_type_all")
        )
        FilterChip(
          selected = typeFilter == "INCOME",
          onClick = { viewModel.historyTypeFilter.value = "INCOME" },
          label = { Text("Pemasukan") },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = IncomeGreenContainer,
            selectedLabelColor = IncomeGreen
          ),
          modifier = Modifier.testTag("filter_type_income")
        )
        FilterChip(
          selected = typeFilter == "EXPENSE",
          onClick = { viewModel.historyTypeFilter.value = "EXPENSE" },
          label = { Text("Pengeluaran") },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = ExpenseRedContainer,
            selectedLabelColor = ExpenseRed
          ),
          modifier = Modifier.testTag("filter_type_expense")
        )

        // Date Filter
        FilterChip(
          selected = dateFilter == "ALL",
          onClick = { viewModel.historyDateFilter.value = "ALL" },
          label = { Text("Semua Waktu") }
        )
        FilterChip(
          selected = dateFilter == "THIS_MONTH",
          onClick = { viewModel.historyDateFilter.value = "THIS_MONTH" },
          label = { Text("Bulan Ini") }
        )
        FilterChip(
          selected = dateFilter == "TODAY",
          onClick = { viewModel.historyDateFilter.value = "TODAY" },
          label = { Text("Hari Ini") }
        )
      }

      // 3. Category Filter Chips (if needed)
      if (allCategories.isNotEmpty()) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 4.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          FilterChip(
            selected = categoryFilter == "ALL",
            onClick = { viewModel.historyCategoryFilter.value = "ALL" },
            label = { Text("Semua Kategori") }
          )

          allCategories.map { it.name }.distinct().forEach { catName ->
            FilterChip(
              selected = categoryFilter.equals(catName, ignoreCase = true),
              onClick = {
                viewModel.historyCategoryFilter.value =
                  if (categoryFilter.equals(catName, ignoreCase = true)) "ALL" else catName
              },
              label = { Text(catName) }
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // 4. Transaction List
      if (transactions.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.History,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
              modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
              text = "Tidak Ada Transaksi",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Coba ubah kata kunci atau filter yang Anda pilih.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          items(transactions, key = { it.id }) { tx ->
            HistoryTransactionItem(
              transaction = tx,
              onDelete = { transactionToDelete = tx }
            )
          }
        }
      }
    }
  }
}

@Composable
fun HistoryTransactionItem(
  transaction: TransactionEntity,
  onDelete: () -> Unit
) {
  val isIncome = transaction.type == "INCOME"

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("history_item_${transaction.id}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(CircleShape)
          .background(if (isIncome) IncomeGreenContainer else ExpenseRedContainer),
        contentAlignment = Alignment.Center
      ) {
        CategoryIcon(
          categoryName = transaction.categoryName,
          tint = if (isIncome) IncomeGreen else ExpenseRed,
          modifier = Modifier.size(22.dp)
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = transaction.description.ifBlank { transaction.categoryName },
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text(
            text = transaction.categoryName,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
          )
          Text(
            text = "•",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
          )
          Text(
            text = DateUtils.formatToIndonesian(transaction.transactionDate),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.width(8.dp))

      Column(horizontalAlignment = Alignment.End) {
        Text(
          text = "${if (isIncome) "+" else "-"} ${CurrencyUtils.formatRupiah(transaction.amount)}",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.ExtraBold,
          color = if (isIncome) IncomeGreen else ExpenseRed
        )

        IconButton(
          onClick = onDelete,
          modifier = Modifier
            .size(32.dp)
            .testTag("delete_tx_btn_${transaction.id}")
        ) {
          Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Hapus transaksi",
            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.75f),
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }
  }
}
