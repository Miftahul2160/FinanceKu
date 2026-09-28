package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.CategoryIcon
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedContainer
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.IncomeGreenContainer
import com.example.ui.viewmodel.CategorySummaryItem
import com.example.ui.viewmodel.FinanceViewModel
import com.example.util.CurrencyUtils
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecapScreen(
  viewModel: FinanceViewModel
) {
  val recap by viewModel.monthlyRecap.collectAsStateWithLifecycle()
  val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
  val selectedMonthKey by viewModel.selectedRecapMonth.collectAsStateWithLifecycle()

  var isDropdownExpanded by remember { mutableStateOf(false) }
  var categoryTab by remember { mutableIntStateOf(0) } // 0: Pengeluaran, 1: Pemasukan

  // Available months list derived from transactions + current month
  val availableMonths = remember(allTransactions) {
    val months = allTransactions.map { DateUtils.getYearMonthKey(it.transactionDate) }.toMutableSet()
    months.add(DateUtils.getCurrentYearMonthKey())
    months.toList().sortedDescending()
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Rekap Keuangan",
            fontWeight = FontWeight.Bold
          )
        }
      )
    },
    modifier = Modifier.fillMaxSize()
  ) { innerPadding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .testTag("recap_screen"),
      contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Period Selector (Month/Year or Semua Waktu)
      item {
        ExposedDropdownMenuBox(
          expanded = isDropdownExpanded,
          onExpandedChange = { isDropdownExpanded = !isDropdownExpanded },
          modifier = Modifier.fillMaxWidth()
        ) {
          OutlinedTextField(
            value = if (selectedMonthKey == "ALL") "Semua Waktu" else DateUtils.formatToMonthYear("$selectedMonthKey-01"),
            onValueChange = {},
            readOnly = true,
            label = { Text("Pilih Periode") },
            leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
            modifier = Modifier
              .menuAnchor()
              .fillMaxWidth()
              .testTag("recap_period_selector")
          )

          ExposedDropdownMenu(
            expanded = isDropdownExpanded,
            onDismissRequest = { isDropdownExpanded = false }
          ) {
            DropdownMenuItem(
              text = { Text("Semua Waktu", fontWeight = FontWeight.Bold) },
              onClick = {
                viewModel.selectedRecapMonth.value = "ALL"
                isDropdownExpanded = false
              }
            )
            availableMonths.forEach { monthKey ->
              DropdownMenuItem(
                text = { Text(DateUtils.formatToMonthYear("$monthKey-01")) },
                onClick = {
                  viewModel.selectedRecapMonth.value = monthKey
                  isDropdownExpanded = false
                }
              )
            }
          }
        }
      }

      // 2. Main Summary Card (Section 7.10)
      item {
        ElevatedCard(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("recap_summary_card")
        ) {
          Column(modifier = Modifier.padding(20.dp)) {
            Text(
              text = "Ringkasan Finansial (${recap.monthName})",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            RecapRowItem(
              label = "Saldo Awal",
              value = CurrencyUtils.formatRupiah(recap.openingBalance),
              color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            RecapRowItem(
              label = "Total Pemasukan",
              value = "+ ${CurrencyUtils.formatRupiah(recap.totalIncome)}",
              color = IncomeGreen,
              icon = Icons.Default.TrendingUp
            )

            Spacer(modifier = Modifier.height(10.dp))

            RecapRowItem(
              label = "Total Pengeluaran",
              value = "- ${CurrencyUtils.formatRupiah(recap.totalExpense)}",
              color = ExpenseRed,
              icon = Icons.Default.TrendingDown
            )

            Spacer(modifier = Modifier.height(14.dp))
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant)
            )
            Spacer(modifier = Modifier.height(14.dp))

            RecapRowItem(
              label = "Selisih (Net Cash Flow)",
              value = "${if (recap.netAmount >= 0) "+" else ""} ${CurrencyUtils.formatRupiah(recap.netAmount)}",
              color = if (recap.netAmount >= 0) IncomeGreen else ExpenseRed,
              isBold = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            RecapRowItem(
              label = "Saldo Akhir",
              value = CurrencyUtils.formatRupiah(recap.closingBalance),
              color = MaterialTheme.colorScheme.primary,
              isBold = true
            )
          }
        }
      }

      // 3. Category Breakdown Tabs (Section 7.10)
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier.fillMaxWidth()
        ) {
          TabRow(selectedTabIndex = categoryTab) {
            Tab(
              selected = categoryTab == 0,
              onClick = { categoryTab = 0 },
              text = {
                Text(
                  text = "Pengeluaran (${recap.expenseByCategory.size})",
                  fontWeight = if (categoryTab == 0) FontWeight.Bold else FontWeight.Normal,
                  color = if (categoryTab == 0) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant
                )
              },
              modifier = Modifier.testTag("tab_recap_expense")
            )
            Tab(
              selected = categoryTab == 1,
              onClick = { categoryTab = 1 },
              text = {
                Text(
                  text = "Pemasukan (${recap.incomeByCategory.size})",
                  fontWeight = if (categoryTab == 1) FontWeight.Bold else FontWeight.Normal,
                  color = if (categoryTab == 1) IncomeGreen else MaterialTheme.colorScheme.onSurfaceVariant
                )
              },
              modifier = Modifier.testTag("tab_recap_income")
            )
          }
        }
      }

      // 4. Category Breakdown List
      val currentList = if (categoryTab == 0) recap.expenseByCategory else recap.incomeByCategory
      val isExpenseTab = categoryTab == 0

      if (currentList.isEmpty()) {
        item {
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
          ) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "Belum ada ${if (isExpenseTab) "pengeluaran" else "pemasukan"} pada periode ini.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      } else {
        items(currentList, key = { it.categoryName }) { item ->
          CategoryProgressItem(item = item, isExpense = isExpenseTab)
        }
      }
    }
  }
}

@Composable
private fun RecapRowItem(
  label: String,
  value: String,
  color: Color,
  isBold: Boolean = false,
  icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      if (icon != null) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = color,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
      }
      Text(
        text = label,
        style = if (isBold) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
        fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
        color = MaterialTheme.colorScheme.onSurface
      )
    }
    Text(
      text = value,
      style = if (isBold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
      fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.SemiBold,
      color = color
    )
  }
}

@Composable
private fun CategoryProgressItem(
  item: CategorySummaryItem,
  isExpense: Boolean
) {
  val accentColor = if (isExpense) ExpenseRed else IncomeGreen
  val containerColor = if (isExpense) ExpenseRedContainer else IncomeGreenContainer

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("category_progress_${item.categoryName.lowercase()}")
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(containerColor),
          contentAlignment = Alignment.Center
        ) {
          CategoryIcon(
            categoryName = item.categoryName,
            tint = accentColor,
            modifier = Modifier.size(18.dp)
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = item.categoryName,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "${String.format("%.1f", item.percentage)}% dari total",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Text(
          text = CurrencyUtils.formatRupiah(item.totalAmount),
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.ExtraBold,
          color = accentColor
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      LinearProgressIndicator(
        progress = { (item.percentage / 100f).coerceIn(0f, 1f) },
        color = accentColor,
        trackColor = containerColor,
        modifier = Modifier
          .fillMaxWidth()
          .height(6.dp)
          .clip(RoundedCornerShape(3.dp))
      )
    }
  }
}
